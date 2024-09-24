package com.alba.tracer;

public class Shape {
    public Vector3 origin;       // Origin of the object
    public Vector3 scale;        // Scale for each axis (for ellipsoids, spheres, cubes, and cuboids)
    public Vector3 rotation;     // Rotation for the object (represented as Euler angles)
    public ObjectProperties properties; // Material properties like color, emission, etc.
    public String objectType;    // Type of object ("sphere", "cube", etc.)
    private Sphere sphere;
    private Cube cube;

    // Constructor to initialize object with type, origin, scale, rotation, and properties
    public Shape(String objectType, Vector3 origin, Vector3 scale, Vector3 rotation, ObjectProperties properties) {
        this.objectType = objectType;
        this.origin = origin;
        this.scale = scale;
        this.rotation = rotation;
        this.properties = properties;
        this.sphere = new Sphere(origin, scale, rotation);
        this.cube = new Cube(origin, scale, rotation);
    }

    // Constructor with default properties
    public Shape(String objectType, Vector3 origin, Vector3 scale, Vector3 rotation) {
        this(objectType, origin, scale, rotation, new ObjectProperties());
    }

    // Intersection method - Checks the type of object and applies appropriate intersection logic
    public double intersect(Ray ray) {
        if (objectType.equalsIgnoreCase("sphere")) {
            if (isUniformScale()) {
                return sphere.intersectSphere(ray);
            } else {
                return sphere.intersectEllipsoid(ray);
            }
        } else if (objectType.equalsIgnoreCase("cube")) {
            return cube.intersectCube(ray);  // Handle both cubes and cuboids
        }

        return -1.0; // No intersection if the object type isn't handled
    }

    // Calculate normal based on the object type
    public Vector3 getNormal(Vector3 point) {
        if (objectType.equalsIgnoreCase("sphere")) {
            if (isUniformScale()) {
                return sphere.getSphereNormal(point);
            } else {
                return sphere.getEllipsoidNormal(point);
            }
        } else if (objectType.equalsIgnoreCase("cube")) {
            return cube.getCubeNormal(point);
        }

        return null; // No normal if the object type isn't handled
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
