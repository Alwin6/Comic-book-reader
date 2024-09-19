package com.alba.tracer;

public class Shape {
    public Vector3 origin;       // Origin of the object
    public Vector3 scale;        // Scale for each axis (for ellipsoids, spheres, cubes, and cuboids)
    public Vector3 rotation;     // Rotation for the object (represented as Euler angles)
    public ObjectProperties properties; // Material properties like color, emission, etc.
    public String objectType;    // Type of object ("sphere", "cube", etc.)

    // Constructor to initialize object with type, origin, scale, rotation, and properties
    public Shape(String objectType, Vector3 origin, Vector3 scale, Vector3 rotation, ObjectProperties properties) {
        this.objectType = objectType;
        this.origin = origin;
        this.scale = scale;
        this.rotation = rotation;
        this.properties = properties;
    }

    // Constructor with default properties
    public Shape(String objectType, Vector3 origin, Vector3 scale, Vector3 rotation) {
        this(objectType, origin, scale, rotation, new ObjectProperties());
    }

    // Intersection method - Checks the type of object and applies appropriate intersection logic
    public double intersect(Ray ray) {
        if (objectType.equalsIgnoreCase("sphere")) {
            if (isUniformScale()) {
                return intersectSphere(ray);
            } else {
                return intersectEllipsoid(ray);
            }
        } else if (objectType.equalsIgnoreCase("cube")) {
            return intersectCube(ray);  // Handle both cubes and cuboids
        }

        return -1.0; // No intersection if the object type isn't handled
    }

    // Ray-sphere intersection logic (for uniform spheres)
    private double intersectSphere(Ray ray) {
        Vector3 oc = ray.origin.subtract(origin);  // Use 'origin' instead of 'center'
        double a = ray.direction.dot(ray.direction);
        double b = 2.0 * oc.dot(ray.direction);
        double c = oc.dot(oc) - (scale.x * scale.x);  // Treat 'scale.x' as the radius of the sphere
        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return -1.0; // No intersection
        } else {
            return (-b - Math.sqrt(discriminant)) / (2.0 * a);
        }
    }

    // Ray-ellipsoid intersection logic
    private double intersectEllipsoid(Ray ray) {
        // Transform ray to object space
        Vector3 invScale = new Vector3(1 / scale.x, 1 / scale.y, 1 / scale.z);
        Vector3 transformedOrigin = ray.origin.subtract(origin).multiply(invScale);
        Vector3 transformedDirection = ray.direction.multiply(invScale);

        // Solve quadratic equation for sphere intersection
        Vector3 oc = transformedOrigin;
        double a = transformedDirection.dot(transformedDirection);
        double b = 2.0 * oc.dot(transformedDirection);
        double c = oc.dot(oc) - 1; // Ellipsoid with unit radius after scaling
        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return -1.0; // No intersection
        } else {
            return (-b - Math.sqrt(discriminant)) / (2.0 * a);
        }
    }

    // Ray-cube intersection logic (for both cubes and cuboids)
    private double intersectCube(Ray ray) {
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

    // Calculate normal based on the object type
    public Vector3 getNormal(Vector3 point) {
        if (objectType.equalsIgnoreCase("sphere")) {
            if (isUniformScale()) {
                return getSphereNormal(point);
            } else {
                return getEllipsoidNormal(point);
            }
        } else if (objectType.equalsIgnoreCase("cube")) {
            return getCubeNormal(point);
        }

        return null; // No normal if the object type isn't handled
    }

    // Calculate the normal for uniform spheres
    private Vector3 getSphereNormal(Vector3 point) {
        return point.subtract(origin).normalize();  // Use 'origin' instead of 'center'
    }

    // Calculate the normal for ellipsoids
    private Vector3 getEllipsoidNormal(Vector3 point) {
        Vector3 localPoint = point.subtract(origin).divide(scale);
        return localPoint.normalize();  // Normal in object space
    }

    // Calculate the normal for cubes/cuboids
    private Vector3 getCubeNormal(Vector3 point) {
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

    // Check if the object has uniform scaling (i.e., a sphere)
    public boolean isUniformScale() {
        return scale.x == scale.y && scale.y == scale.z;
    }

    // Determine if the object is emissive
    public boolean isEmissive() {
        return properties.emission > 0;
    }

    public double getEmission() {
        return properties.emission;
    }
}
