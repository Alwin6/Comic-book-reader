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
}
