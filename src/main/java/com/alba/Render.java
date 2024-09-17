public class Render {
    private HDRLoader hdrLoader;
    private float[][][] hdrData; // HDR image data
    private boolean useHDR; // Flag to use HDR environment

    public Render(HDRLoader hdrLoader) {
        this.hdrLoader = hdrLoader;  // Initialize with HDRLoader instance
    }

    public Vector3 sampleHDR(Vector3 direction) {
        // Convert the 3D direction to spherical coordinates
        double theta = Math.acos(direction.y);
        double phi = Math.atan2(direction.z, direction.x);
        if (phi < 0) phi += 2 * Math.PI;

        // Normalize to [0, 1] range
        double u = phi / (2 * Math.PI);
        double v = theta / Math.PI;

        // Sample HDR image data
        return hdrLoader.sample(u, v);
    }

    public Vector3 computeLighting(Vector3 point, Vector3 normal, Vector3 viewDir, Sphere hitSphere, Light[] lights, Sphere[] spheres, Vector3 ambientLight, Vector3 backgroundColor, Ray ray) {
        Vector3 baseColor = hitSphere.properties.color;  // Default to the base color

        // Check if the sphere has a texture and sample it if so
        if (hitSphere.properties.texture != null && hitSphere.properties.texture.hasTexture()) {
            Vector2 uv = sphereToUV(point, hitSphere.center);  // Convert hit point to UV coordinates
            baseColor = hitSphere.properties.texture.sample(uv.x, uv.y);  // Sample texture color
        }

        // Start with ambient lighting
        Vector3 color = baseColor.multiply(ambientLight);

        // Iterate over all the lights in the scene and compute their effect
        for (Light light : lights) {
            Vector3 lightDir = light.position.subtract(point).normalize();
            double nDotL = normal.dot(lightDir);
            if (nDotL <= 0) continue;  // Skip if the surface is facing away from the light

            // Shadow check
            boolean inShadow = false;
            Ray shadowRay = new Ray(point.add(normal.multiply(1e-5)), lightDir);
            for (Sphere sphere : spheres) {
                if (sphere != hitSphere && sphere.intersect(shadowRay) > 0) {
                    inShadow = true;
                    break;
                }
            }
            if (inShadow) continue;

            // Diffuse component
            double diffuseIntensity = nDotL * light.intensity;
            Vector3 diffuse = baseColor.multiply(light.color).multiply(diffuseIntensity);

            // Specular component
            Vector3 reflectDir = lightDir.reflect(normal).normalize();
            double smoothness = hitSphere.properties.smoothness;
            double specularIntensity = Math.pow(Math.max(0.0, viewDir.dot(reflectDir)), 1 / (1 - smoothness)) * light.intensity;
            Vector3 specular = new Vector3(1.0, 1.0, 1.0).multiply(light.color).multiply(specularIntensity);

            // Blend diffuse and specular based on metallicness
            Vector3 finalDiffuse = diffuse.multiply(1 - hitSphere.properties.metallicness);
            Vector3 finalSpecular = specular.multiply(hitSphere.properties.metallicness);

            // Add diffuse and specular to the color
            color = color.add(finalDiffuse).add(finalSpecular);
        }

        // Handle emissive spheres acting as light sources
        for (Sphere sphere : spheres) {
            if (sphere.isEmissive() && sphere != hitSphere) {
                Vector3 lightDir = sphere.center.subtract(point).normalize();
                double nDotL = normal.dot(lightDir);
                if (nDotL <= 0) continue;

                // Shadow check
                boolean inShadow = false;
                Ray shadowRay = new Ray(point.add(normal.multiply(1e-5)), lightDir);
                for (Sphere otherSphere : spheres) {
                    if (otherSphere != hitSphere && otherSphere != sphere) {
                        double shadowT = otherSphere.intersect(shadowRay);
                        if (shadowT > 0 && shadowT < sphere.center.subtract(point).length()) {
                            inShadow = true;
                            break;
                        }
                    }
                }
                if (inShadow) continue;

                // Emissive color and intensity from texture or base color
                Vector3 emissiveColor = sphere.properties.color;  // Default to base color
                if (sphere.properties.texture != null && sphere.properties.texture.hasTexture()) {
                    Vector2 uv = sphereToUV(point, sphere.center);  // Convert hit point to UV coordinates
                    emissiveColor = sphere.properties.texture.sample(uv.x, uv.y);  // Sample texture color
                }

                // Apply the emission intensity and add to final color
                double distanceSquared = sphere.center.subtract(point).lengthSquared();
                double intensity = sphere.properties.emission / distanceSquared;
                Vector3 contribution = emissiveColor.multiply(intensity).multiply(nDotL);

                color = color.add(contribution);  // Add emissive contribution
            }
        }

        // Reflection handling
        if (hitSphere.properties.reflectiveness > 0) {
            Vector3 reflectDir = ray.direction.subtract(normal.multiply(2 * ray.direction.dot(normal))).normalize();
            Ray reflectRay = new Ray(point.add(normal.multiply(1e-5)), reflectDir);
            Vector3 reflectColor = traceRay(reflectRay, spheres, lights, ambientLight, backgroundColor);
            color = color.multiply(1 - hitSphere.properties.reflectiveness).add(reflectColor.multiply(hitSphere.properties.reflectiveness));
        }

        // Refraction (transparency) handling
        if (hitSphere.properties.transparency > 0) {
            Vector3 refractDir = refract(ray.direction, normal, 1.0, 1.5);  // Assume index of refraction for air to glass (1.0 to 1.5)
            Ray refractRay = new Ray(point.add(normal.multiply(-1e-5)), refractDir);  // Offset the point slightly to avoid self-intersection
            Vector3 refractColor = traceRay(refractRay, spheres, lights, ambientLight, backgroundColor);
            color = color.multiply(1 - hitSphere.properties.transparency).add(refractColor.multiply(hitSphere.properties.transparency));
        }

        return color.clamp(0, 1); // Clamp to [0,1] range for color values
    }


    // Helper function for Snell's Law to compute refraction
    private Vector3 refract(Vector3 incident, Vector3 normal, double n1, double n2) {
        double ratio = n1 / n2;
        double cosI = -normal.dot(incident);
        double sinT2 = ratio * ratio * (1.0 - cosI * cosI);

        // Total internal reflection check
        if (sinT2 > 1.0) {
            return incident.reflect(normal);  // Handle total internal reflection by returning reflection direction
        }

        double cosT = Math.sqrt(1.0 - sinT2);
        return incident.multiply(ratio).add(normal.multiply(ratio * cosI - cosT));
    }

    // Helper function to convert a point on a sphere to UV coordinates
    private Vector2 sphereToUV(Vector3 point, Vector3 center) {
        Vector3 p = point.subtract(center).normalize();
        double u = 0.5 + (Math.atan2(p.z, p.x) / (2 * Math.PI));
        double v = 0.5 - (Math.asin(p.y) / Math.PI);
        return new Vector2(u, v);
    }

    public Vector3 traceRay(Ray ray, Sphere[] spheres, Light[] light, Vector3 ambientLight, Vector3 backgroundColor) {
        double closestT = Double.MAX_VALUE;
        Sphere closestSphere = null;

        // Find the closest sphere that the ray intersects
        for (Sphere sphere : spheres) {
            double t = sphere.intersect(ray);
            if (t > 0 && t < closestT) {
                closestT = t;
                closestSphere = sphere;
            }
        }

        if (closestSphere != null) {
            Vector3 hitPoint = ray.pointAt(closestT);
            Vector3 normal = closestSphere.getNormal(hitPoint);
            Vector3 viewDir = ray.direction.multiply(-1).normalize(); // Direction from hit point to camera

            // If the closest sphere is emissive, return its emission color
            if (closestSphere.isEmissive()) {
                double emissionIntensity = closestSphere.properties.emission;

                // Check if the sphere has a texture and use the texture for emission if present
                Vector3 emissionColor = closestSphere.properties.color;  // Default to base color

                if (closestSphere.properties.texture != null && closestSphere.properties.texture.hasTexture()) {
                    Vector2 uv = sphereToUV(hitPoint, closestSphere.center);  // Convert hit point to UV coordinates
                    emissionColor = closestSphere.properties.texture.sample(uv.x, uv.y);  // Sample texture color
                }

                // Return the texture or base color multiplied by emission intensity
                return emissionColor.multiply(emissionIntensity);
            }

            // Compute lighting for non-emissive objects
            return computeLighting(hitPoint, normal, viewDir, closestSphere, light, spheres, ambientLight, backgroundColor, ray);
        }

        // Sample the HDR environment for rays that miss
        if (hdrLoader != null) {
            return sampleHDR(ray.direction).clamp(0, 1);
        } else {
            return backgroundColor;
        }
    }

    public Vector3 traceRayBasic(Ray ray, Sphere[] spheres, Light[] light, Vector3 ambientLight, Vector3 backgroundColor) {
        double closestT = Double.MAX_VALUE;
        Sphere closestSphere = null;

        for (Sphere sphere : spheres) {
            double t = sphere.intersect(ray);
            if (t > 0 && t < closestT) {
                closestT = t;
                closestSphere = sphere;
            }
        }

        if (closestSphere != null) {
            return closestSphere.properties.color;  // Return the sphere's color from ObjectProperties
        }

        return backgroundColor; // Background color (sky blue)
    }
}
