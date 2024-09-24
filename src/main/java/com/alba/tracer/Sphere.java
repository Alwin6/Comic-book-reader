package com.alba.tracer;

public class Sphere {
    private Vector3 origin;
    private Vector3 scale;
    private Vector3 rotation;

    public Sphere(Vector3 origin, Vector3 scale, Vector3 rotation) {
        this.origin = origin;
        this.scale = scale;
        this.rotation = rotation;
    }

    // Ray-sphere intersection logic (for uniform spheres)
    public double intersectSphere(Ray ray) {
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
    public double intersectEllipsoid(Ray ray) {
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

    // Calculate the normal for uniform spheres
    public Vector3 getSphereNormal(Vector3 point) {
        return point.subtract(origin).normalize();  // Use 'origin' instead of 'center'
    }

    // Calculate the normal for ellipsoids
    public Vector3 getEllipsoidNormal(Vector3 point) {
        Vector3 localPoint = point.subtract(origin).divide(scale);
        return localPoint.normalize();  // Normal in object space
    }

    // Helper function to convert a point on a sphere to UV coordinates
    public Vector2 sphereToUV(Vector3 point, Vector3 center) {
        Vector3 p = point.subtract(center).normalize();
        double u = 0.5 + (Math.atan2(p.z, p.x) / (2 * Math.PI));
        double v = 0.5 - (Math.asin(p.y) / Math.PI);
        return new Vector2(u, v);
    }

}
