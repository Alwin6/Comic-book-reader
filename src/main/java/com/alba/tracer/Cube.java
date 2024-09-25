package com.alba.tracer;

public class Cube {
    private Vector3 origin;
    private Vector3 scale;
    private Vector3 rotation;

    public Cube(Vector3 origin, Vector3 scale, Vector3 rotation) {
        this.origin = origin;
        this.scale = scale;
        this.rotation = rotation;
    }

    public double intersectCube(Ray ray) {
        // Handle zero scale dimensions (i.e., planes)
        Vector3 scaledOrigin = ray.origin.subtract(origin).divide(scale.withNonZero());
        Vector3 scaledDirection = ray.direction.divide(scale.withNonZero());

        Ray scaledRay = new Ray(scaledOrigin, scaledDirection);

        Vector3 invDir = new Vector3(
                (scaledDirection.x != 0) ? 1.0 / scaledDirection.x : Double.POSITIVE_INFINITY,
                (scaledDirection.y != 0) ? 1.0 / scaledDirection.y : Double.POSITIVE_INFINITY,
                (scaledDirection.z != 0) ? 1.0 / scaledDirection.z : Double.POSITIVE_INFINITY
        );

        // Define unit bounds (as if it's a unit cube)
        Vector3 boundsMin = new Vector3(-0.5, -0.5, -0.5);
        Vector3 boundsMax = new Vector3(0.5, 0.5, 0.5);

        double tMin = (boundsMin.x - scaledRay.origin.x) * invDir.x;
        double tMax = (boundsMax.x - scaledRay.origin.x) * invDir.x;

        if (tMin > tMax) {
            double temp = tMin;
            tMin = tMax;
            tMax = temp;
        }

        double tyMin = (boundsMin.y - scaledRay.origin.y) * invDir.y;
        double tyMax = (boundsMax.y - scaledRay.origin.y) * invDir.y;

        if (tyMin > tyMax) {
            double temp = tyMin;
            tyMin = tyMax;
            tyMax = temp;
        }

        if ((tMin > tyMax) || (tyMin > tMax)) {
            return -1.0; // No intersection
        }

        if (tyMin > tMin) {
            tMin = tyMin;
        }

        if (tyMax < tMax) {
            tMax = tyMax;
        }

        double tzMin = (boundsMin.z - scaledRay.origin.z) * invDir.z;
        double tzMax = (boundsMax.z - scaledRay.origin.z) * invDir.z;

        if (tzMin > tzMax) {
            double temp = tzMin;
            tzMin = tzMax;
            tzMax = temp;
        }

        if ((tMin > tzMax) || (tzMin > tMax)) {
            return -1.0; // No intersection
        }

        if (tzMin > tMin) {
            tMin = tzMin;
        }

        if (tzMax < tMax) {
            tMax = tzMax;
        }

        return tMin; // Return the closest intersection
    }

    public Vector3 getCubeNormal(Vector3 point) {
        Vector3 localPoint = point.subtract(origin).divide(scale.withNonZero()); // Avoid division by zero

        // Handle degenerate case: If any scale component is zero, it's a plane
        if (scale.x == 0) {
            return new Vector3(1, 0, 0); // X-plane
        } else if (scale.y == 0) {
            return new Vector3(0, 1, 0); // Y-plane
        } else if (scale.z == 0) {
            return new Vector3(0, 0, 1); // Z-plane
        }

        // Normal calculation for regular cuboid
        double absX = Math.abs(localPoint.x);
        double absY = Math.abs(localPoint.y);
        double absZ = Math.abs(localPoint.z);

        double maxComponent = Math.max(absX, Math.max(absY, absZ));

        if (maxComponent == absX) {
            return new Vector3(Math.signum(localPoint.x), 0, 0).divide(scale).normalize();
        } else if (maxComponent == absY) {
            return new Vector3(0, Math.signum(localPoint.y), 0).divide(scale).normalize();
        } else {
            return new Vector3(0, 0, Math.signum(localPoint.z)).divide(scale).normalize();
        }
    }



    public Vector2 cubeToUV(Vector3 point, Vector3 center) {
        // Transform point to unit cube space by subtracting origin and dividing by scale
        Vector3 p = point.subtract(center).divide(scale.withNonZero());

        double absX = Math.abs(p.x);
        double absY = Math.abs(p.y);
        double absZ = Math.abs(p.z);

        double u = 0, v = 0;

        // Handle degenerate cases (planes)
        if (scale.x == 0) {
            // X-plane
            u = (p.z + 1) / 2.0;
            v = (p.y + 1) / 2.0;
        } else if (scale.y == 0) {
            // Y-plane
            u = (p.x + 1) / 2.0;
            v = (p.z + 1) / 2.0;
        } else if (scale.z == 0) {
            // Z-plane
            u = (p.x + 1) / 2.0;
            v = (p.y + 1) / 2.0;
        } else {
            // Determine the face of the cuboid based on the dominant axis
            if (absX >= absY && absX >= absZ) {
                // X face
                if (p.x > 0) {
                    // Right face (positive X)
                    u = (p.z + 1) / 2.0;
                    v = (p.y + 1) / 2.0;
                } else {
                    // Left face (negative X)
                    u = (-p.z + 1) / 2.0;
                    v = (p.y + 1) / 2.0;
                }
            } else if (absY >= absX && absY >= absZ) {
                // Y face
                if (p.y > 0) {
                    u = (p.x + 1) / 2.0;
                    v = (-p.z + 1) / 2.0;
                } else {
                    u = (p.x + 1) / 2.0;
                    v = (p.z + 1) / 2.0;
                }
            } else {
                // Z face
                if (p.z > 0) {
                    u = (p.x + 1) / 2.0;
                    v = (p.y + 1) / 2.0;
                } else {
                    u = (-p.x + 1) / 2.0;
                    v = (p.y + 1) / 2.0;
                }
            }
        }

        return new Vector2(u, v);
    }


}