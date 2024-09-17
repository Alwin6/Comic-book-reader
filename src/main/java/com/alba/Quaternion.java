package com.alba;

public class Quaternion {
    public double x, y, z, w;

    public Quaternion(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public static Quaternion fromAxisAngle(Vector3 axis, double angle) {
        double halfAngle = angle / 2.0;
        double s = Math.sin(halfAngle);
        return new Quaternion(axis.x * s, axis.y * s, axis.z * s, Math.cos(halfAngle));
    }

    public Quaternion multiply(Quaternion q) {
        return new Quaternion(
            w * q.x + x * q.w + y * q.z - z * q.y,
            w * q.y - x * q.z + y * q.w + z * q.x,
            w * q.z + x * q.y - y * q.x + z * q.w,
            w * q.w - x * q.x - y * q.y - z * q.z
        );
    }

    public Quaternion normalize() {
        double length = Math.sqrt(x * x + y * y + z * z + w * w);
        return new Quaternion(x / length, y / length, z / length, w / length);
    }

    public Vector3 rotate(Vector3 v) {
        Quaternion qVec = new Quaternion(v.x, v.y, v.z, 0);
        Quaternion result = this.multiply(qVec).multiply(conjugate());
        return new Vector3(result.x, result.y, result.z);
    }

    public Quaternion conjugate() {
        return new Quaternion(-x, -y, -z, w);
    }
}
