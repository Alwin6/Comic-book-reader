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

    // Default constructor for point light (with radius = 0)
    public Light(Vector3 position, Vector3 color, double intensity) {
        this(position, color, intensity, 0.0);  // Set default radius to 0 for point light behavior
    }
}
