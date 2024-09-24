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

    // Ray-cube intersection logic (for both cubes and cuboids)
    public double intersectCube(Ray ray) {
        Vector3 invDir = new Vector3(1.0 / ray.direction.x, 1.0 / ray.direction.y, 1.0 / ray.direction.z);
        Vector3 boundsMin = origin.subtract(scale.multiply(0.5)); // Calculate the cuboid min bound
        Vector3 boundsMax = origin.add(scale.multiply(0.5));      // Calculate the cuboid max bound

        double tMin = (boundsMin.x - ray.origin.x) * invDir.x;
        double tMax = (boundsMax.x - ray.origin.x) * invDir.x;

        if (tMin > tMax) {
            double temp = tMin;
            tMin = tMax;
            tMax = temp;
        }

        double tyMin = (boundsMin.y - ray.origin.y) * invDir.y;
        double tyMax = (boundsMax.y - ray.origin.y) * invDir.y;

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

        double tzMin = (boundsMin.z - ray.origin.z) * invDir.z;
        double tzMax = (boundsMax.z - ray.origin.z) * invDir.z;

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

    // Calculate the normal for cubes/cuboids
    public Vector3 getCubeNormal(Vector3 point) {
        Vector3 localPoint = point.subtract(origin);

        // Check which face of the cube is closest
        double absX = Math.abs(localPoint.x);
        double absY = Math.abs(localPoint.y);
        double absZ = Math.abs(localPoint.z);

        double maxComponent = Math.max(absX, Math.max(absY, absZ));

        if (maxComponent == absX) {
            return new Vector3(Math.signum(localPoint.x), 0, 0);
        } else if (maxComponent == absY) {
            return new Vector3(0, Math.signum(localPoint.y), 0);
        } else {
            return new Vector3(0, 0, Math.signum(localPoint.z));
        }
    }

    // Helper function to convert a point on a cube to UV coordinates
    public Vector2 cubeToUV(Vector3 point, Vector3 center) {
        Vector3 p = point.subtract(center);

        double absX = Math.abs(p.x);
        double absY = Math.abs(p.y);
        double absZ = Math.abs(p.z);

        double u = 0, v = 0;

        // Determine the face of the cube (X, Y, or Z is dominant)
        if (absX >= absY && absX >= absZ) {
            // X face
            if (p.x > 0) {
                // Right face (positive X)
                u = (p.z / absX + 1) / 2.0;
                v = (p.y / absX + 1) / 2.0;
            } else {
                // Left face (negative X)
                u = (-p.z / absX + 1) / 2.0;
                v = (p.y / absX + 1) / 2.0;
            }
        } else if (absY >= absX && absY >= absZ) {
            // Y face
            if (p.y > 0) {
                // Top face (positive Y)
                u = (p.x / absY + 1) / 2.0;
                v = (-p.z / absY + 1) / 2.0;
            } else {
                // Bottom face (negative Y)
                u = (p.x / absY + 1) / 2.0;
                v = (p.z / absY + 1) / 2.0;
            }
        } else {
            // Z face
            if (p.z > 0) {
                // Front face (positive Z)
                u = (p.x / absZ + 1) / 2.0;
                v = (p.y / absZ + 1) / 2.0;
            } else {
                // Back face (negative Z)
                u = (-p.x / absZ + 1) / 2.0;
                v = (p.y / absZ + 1) / 2.0;
            }
        }

        return new Vector2(u, v);
    }
}
