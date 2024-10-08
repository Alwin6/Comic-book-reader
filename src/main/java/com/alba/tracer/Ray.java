package com.alba.tracer;

public class Ray {
    public Vector3 origin, direction;

    public Ray(Vector3 origin, Vector3 direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vector3 pointAt(double t) {
        return origin.add(direction.multiply(t));
    }
}
