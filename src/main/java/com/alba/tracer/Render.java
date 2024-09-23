package com.alba.tracer;

public class Render {
    private HDRLoader hdrLoader;

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

    public Vector3 computeLighting(Vector3 point, Vector3 normal, Vector3 viewDir, Shape hitShape, Light[] lights, Shape[] shapes, Vector3 ambientLight, Vector3 backgroundColor, Ray ray, int sampleCount) {
        Vector3 baseColor = hitShape.properties.color;

        // Check if the sphere has a texture and sample it if so
        if (hitShape.properties.texture != null && hitShape.properties.texture.hasTexture()) {
            Vector2 uv = sphereToUV(point, hitShape.origin);
            baseColor = hitShape.properties.texture.sample(uv.x, uv.y);
        }

        // Start with ambient lighting
        Vector3 color = baseColor.multiply(ambientLight);

        // Iterate over all the lights in the scene
        for (Light light : lights) {
            Vector3 totalLightContribution = new Vector3(0, 0, 0); // Total light contribution for this light

            // Handle lights with radius > 0 (area lights) for soft shadows
            if (light.radius > 0) {
                for (int i = 0; i < sampleCount; i++) {
                    for (int j = 0; j < sampleCount; j++) {
                        // Calculate sample position on the light's area (structured grid sampling)
                        double u = (double) i / (sampleCount - 1);  // u in [0, 1]
                        double v = (double) j / (sampleCount - 1);  // v in [0, 1]
                        Vector3 offset = new Vector3(
                                (u - 0.5) * 2 * light.radius,  // Offset x-coordinate
                                0,                              // Keep y the same
                                (v - 0.5) * 2 * light.radius    // Offset z-coordinate
                        );
                        Vector3 samplePosition = light.position.add(offset);  // Light sample position

                        // Calculate light contribution for this sample position
                        Vector3 lightDir = samplePosition.subtract(point).normalize();
                        totalLightContribution = totalLightContribution.add(
                                calculateLightContribution(point, normal, viewDir, baseColor, light, lightDir, shapes, hitShape)
                        );
                    }
                }

                // Average the total light contribution for all samples
                totalLightContribution = totalLightContribution.multiply(1.0 / (sampleCount * sampleCount));
            } else {
                // Handle point lights (radius == 0) as before
                Vector3 lightDir = light.position.subtract(point).normalize();
                totalLightContribution = calculateLightContribution(point, normal, viewDir, baseColor, light, lightDir, shapes, hitShape);
            }

            // Add the final contribution from this light to the total color
            color = color.add(totalLightContribution);
        }

        // Handle emissive objects
        for (Shape shape : shapes) {
            if (shape.isEmissive() && shape != hitShape) {
                Vector3 lightDir = shape.origin.subtract(point).normalize();
                double nDotL = normal.dot(lightDir);
                if (nDotL <= 0) continue;

                // Shadow check for emissive spheres
                boolean inShadow = false;
                Ray shadowRay = new Ray(point.add(normal.multiply(1e-5)), lightDir);
                for (Shape otherShape : shapes) {
                    if (otherShape != hitShape && otherShape != shape) {
                        double shadowT = otherShape.intersect(shadowRay);
                        if (shadowT > 0 && shadowT < shape.origin.subtract(point).length()) {
                            inShadow = true;
                            break;
                        }
                    }
                }
                if (inShadow) continue;

                // Emissive color and intensity
                Vector3 emissiveColor = shape.properties.color;
                if (shape.properties.texture != null && shape.properties.texture.hasTexture()) {
                    Vector2 uv = sphereToUV(point, shape.origin);
                    emissiveColor = shape.properties.texture.sample(uv.x, uv.y);
                }

                double distanceSquared = shape.origin.subtract(point).lengthSquared();
                double intensity = shape.properties.emission / distanceSquared;
                Vector3 contribution = emissiveColor.multiply(intensity).multiply(nDotL);

                color = color.add(contribution);
            }
        }

        // Reflection handling (unchanged)
        if (hitShape.properties.reflectiveness > 0) {
            Vector3 reflectDir = ray.direction.subtract(normal.multiply(2 * ray.direction.dot(normal))).normalize();
            Ray reflectRay = new Ray(point.add(normal.multiply(1e-5)), reflectDir);
            Vector3 reflectColor = traceRay(reflectRay, shapes, lights, ambientLight, backgroundColor, sampleCount, -1);
            color = color.multiply(1 - hitShape.properties.reflectiveness).add(reflectColor.multiply(hitShape.properties.reflectiveness));
        }

        // Refraction handling (unchanged)
        if (hitShape.properties.transparency > 0) {
            Vector3 refractDir = refract(ray.direction, normal, 1.0, 1.5);
            Ray refractRay = new Ray(point.add(normal.multiply(-1e-5)), refractDir);
            Vector3 refractColor = traceRay(refractRay, shapes, lights, ambientLight, backgroundColor, sampleCount, -1);
            color = color.multiply(1 - hitShape.properties.transparency).add(refractColor.multiply(hitShape.properties.transparency));
        }

        return color.clamp(0, 1);
    }

    // Helper function to compute the contribution of a single light sample
    private Vector3 calculateLightContribution(Vector3 point, Vector3 normal, Vector3 viewDir, Vector3 baseColor, Light light, Vector3 lightDir, Shape[] shapes, Shape hitShape) {
        double nDotL = normal.dot(lightDir);
        if (nDotL <= 0) return new Vector3(0, 0, 0);  // No contribution if the light is behind the surface

        // Shadow check
        boolean inShadow = false;
        Ray shadowRay = new Ray(point.add(normal.multiply(1e-5)), lightDir);
        for (Shape shape : shapes) {
            if (shape != hitShape && shape.intersect(shadowRay) > 0) {
                inShadow = true;
                break;
            }
        }
        if (inShadow) return new Vector3(0, 0, 0);  // No contribution if in shadow

        // Diffuse component
        double diffuseIntensity = nDotL * light.intensity;
        Vector3 diffuse = baseColor.multiply(light.color).multiply(diffuseIntensity);

        // Specular component
        Vector3 reflectDir = lightDir.reflect(normal).normalize();
        double smoothness = hitShape.properties.smoothness;
        double specularIntensity = Math.pow(Math.max(0.0, viewDir.dot(reflectDir)), 1 / (1 - smoothness)) * light.intensity;
        Vector3 specular = new Vector3(1.0, 1.0, 1.0).multiply(light.color).multiply(specularIntensity);

        // Blend diffuse and specular based on metallicness
        Vector3 finalDiffuse = diffuse.multiply(1 - hitShape.properties.metallicness);
        Vector3 finalSpecular = specular.multiply(hitShape.properties.metallicness);

        return finalDiffuse.add(finalSpecular);  // Return the combined diffuse and specular contribution
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

    public Vector3 traceRay(Ray ray, Shape[] shapes, Light[] light, Vector3 ambientLight, Vector3 backgroundColor, int sampleCount, int selectedObject) {
        double closestT = Double.MAX_VALUE;
        Shape closestShape = null;
        int i =0;
        int clo = -1;
        // Find the closest sphere that the ray intersects
        for (Shape shape : shapes) {
            double t = shape.intersect(ray);
            if (t > 0 && t < closestT) {
                closestT = t;
                closestShape = shape;
                clo = i;
            }
            i++;
        }

        if (closestShape != null) {
            Vector3 hitPoint = ray.pointAt(closestT);
            Vector3 normal = closestShape.getNormal(hitPoint);
            Vector3 viewDir = ray.direction.multiply(-1).normalize(); // Direction from hit point to camera

            // If the closest sphere is emissive, return its emission color
            if (closestShape.isEmissive()) {
                double emissionIntensity = closestShape.properties.emission;

                // Check if the sphere has a texture and use the texture for emission if present
                Vector3 emissionColor = closestShape.properties.color;  // Default to base color

                if (closestShape.properties.texture != null && closestShape.properties.texture.hasTexture()) {
                    Vector2 uv = sphereToUV(hitPoint, closestShape.origin);  // Convert hit point to UV coordinates
                    emissionColor = closestShape.properties.texture.sample(uv.x, uv.y);  // Sample texture color
                }

                // Return the texture or base color multiplied by emission intensity
                if (clo == selectedObject) {
                    Vector3 originalColor = emissionColor.multiply(emissionIntensity).clamp(0, 1);
                    Vector3 invertedColor = new Vector3(1 - originalColor.x, 1 - originalColor.y, 1 - originalColor.z);
                    return invertedColor.multiply(1.5).clamp(0, 1);
                } else {
                    return emissionColor.multiply(emissionIntensity).clamp(0, 1);
                }
            }

            // Compute lighting for non-emissive objects
            if (clo == selectedObject) {
                Vector3 originalColor = computeLighting(hitPoint, normal, viewDir, closestShape, light, shapes, ambientLight, backgroundColor, ray, sampleCount);
                Vector3 invertedColor = new Vector3(1 - originalColor.x, 1 - originalColor.y, 1 - originalColor.z);
                return invertedColor.multiply(1.5).clamp(0, 1);
            } else {
            return computeLighting(hitPoint, normal, viewDir, closestShape, light, shapes, ambientLight, backgroundColor, ray, sampleCount);
            }
        }

        // Sample the HDR environment for rays that miss
        if (hdrLoader != null) {
            return sampleHDR(ray.direction).clamp(0, 1);
        } else {
            return backgroundColor;
        }
    }

    public Vector3 traceRayBasic(Ray ray, Shape[] shapes, Vector3 backgroundColor, int selectedObject) {
        double closestT = Double.MAX_VALUE;
        Shape closestShape = null;
        int i = 0;
        int clo = -1;
        for (Shape shape : shapes) {
            double t = shape.intersect(ray);
            if (t > 0 && t < closestT) {
                closestT = t;
                closestShape = shape;
                clo = i;
            }
            i++;
        }

        if (closestShape != null) {


            if (clo == selectedObject) {
                Vector3 originalColor = closestShape.properties.color;
                Vector3 invertedColor = new Vector3(1 - originalColor.x, 1 - originalColor.y, 1 - originalColor.z);
                return invertedColor.multiply(1.5).clamp(0, 1);
            } else {
                // Return the base color of the non-selected object
                return closestShape.properties.color;
            }
        }


        return backgroundColor; // Background color (sky blue)
    }

    public int traceFind(Ray ray, Shape[] shapes) {
        double closestT = Double.MAX_VALUE;
        int closestSphere = -1;
        int i = 0;
        for (Shape shape : shapes) {
            double t = shape.intersect(ray);
            if (t > 0 && t < closestT) {
                closestT = t;
                closestSphere = i;
            }
            i++;
        }

        System.out.println(shapes.length);
        return closestSphere;
    }
}
