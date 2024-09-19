package com.alba.tracer;

public class Vector3 {
    public double x, y, z;

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 add(Vector3 v) {
        return new Vector3(this.x + v.x, this.y + v.y, this.z + v.z);
    }

    public Vector3 subtract(Vector3 v) {
        return new Vector3(this.x - v.x, this.y - v.y, this.z - v.z);
    }

    public Vector3 multiply(double scalar) {
        return new Vector3(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Vector3 multiply(Vector3 v) {
        return new Vector3(this.x * v.x, this.y * v.y, this.z * v.z);
    }

    public Vector3 divide(double scalar) {
        return new Vector3(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    public Vector3 divide(Vector3 v) {
        return new Vector3(this.x / v.x, this.y / v.y, this.z / v.z);
    }

    public Vector3 negate() {
        return new Vector3(this.x * -1, this.y * -1, this.z * -1);
    }

    public double dot(Vector3 v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public Vector3 cross(Vector3 v) {
        // Calculate the cross product
        double crossX = this.y * v.z - this.z * v.y;
        double crossY = this.z * v.x - this.x * v.z;
        double crossZ = this.x * v.y - this.y * v.x;
        return new Vector3(crossX, crossY, crossZ);
    }

    public Vector3 normalize() {
        double length = Math.sqrt(x * x + y * y + z * z);
        return new Vector3(x / length, y / length, z / length);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double lengthSquared() {
        double temp = Math.sqrt(x * x + y * y + z * z);
        return temp * temp;
    }

    public Vector3 reflect(Vector3 normal) {
        return this.subtract(normal.multiply(2 * this.dot(normal)));
    }

    public Vector3 clamp(double min, double max) {
        return new Vector3(
                Math.min(max, Math.max(min, this.x)),
                Math.min(max, Math.max(min, this.y)),
                Math.min(max, Math.max(min, this.z))
        );
    }

    // Apply rotation by Euler angles (in radians) around x, y, and z axes
    public Vector3 rotate(double angleX, double angleY, double angleZ) {
        // Rotation around X-axis
        double cosX = Math.cos(angleX);
        double sinX = Math.sin(angleX);
        double[] rx = {
                1, 0, 0,
                0, cosX, -sinX,
                0, sinX, cosX
        };

        // Rotation around Y-axis
        double cosY = Math.cos(angleY);
        double sinY = Math.sin(angleY);
        double[] ry = {
                cosY, 0, sinY,
                0, 1, 0,
                -sinY, 0, cosY
        };

        // Rotation around Z-axis
        double cosZ = Math.cos(angleZ);
        double sinZ = Math.sin(angleZ);
        double[] rz = {
                cosZ, -sinZ, 0,
                sinZ, cosZ, 0,
                0, 0, 1
        };

        // Apply Z rotation
        double[] temp = applyMatrix(rz, new double[]{x, y, z});
        // Apply Y rotation
        temp = applyMatrix(ry, temp);
        // Apply X rotation
        temp = applyMatrix(rx, temp);

        return new Vector3(temp[0], temp[1], temp[2]);
    }

    // Helper method to apply a rotation matrix to a vector
    private double[] applyMatrix(double[] matrix, double[] vector) {
        double[] result = new double[3];
        result[0] = matrix[0] * vector[0] + matrix[1] * vector[1] + matrix[2] * vector[2];
        result[1] = matrix[3] * vector[0] + matrix[4] * vector[1] + matrix[5] * vector[2];
        result[2] = matrix[6] * vector[0] + matrix[7] * vector[1] + matrix[8] * vector[2];
        return result;
    }

}

