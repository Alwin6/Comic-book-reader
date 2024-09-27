package com.alba.tracer;

public class Light {
    public Vector3 position;
    public Vector3 color;
    public double intensity;
    public double radius;  // New property to represent the radius of the light source

    public Light(Vector3 position, Vector3 color, double intensity, double radius) {
        this.position = position;
        this.color = color;
        this.intensity = intensity;
        this.radius = radius;  // Initialize the radius in the constructor
    }

    public double Intersect(Ray ray) {
        Vector3 oc = ray.origin.subtract(position);
        double a = ray.direction.dot(ray.direction);
        double b = 2.0 * oc.dot(ray.direction);
        double rad = Math.max(radius, .1);
        double c = oc.dot(oc) - (rad * rad);  // Treat 'scale.x' as the radius of the sphere
        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return -1.0; // No intersection
        } else {
            return (-b - Math.sqrt(discriminant)) / (2.0 * a);
        }
    }
}
