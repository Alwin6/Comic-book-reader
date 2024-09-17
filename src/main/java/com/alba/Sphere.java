

public class Sphere {
    public Vector3 center;
    public double radius;
    public ObjectProperties properties; // New property to store ObjectProperties

    // Constructors to initialize sphere properties
    public Sphere(Vector3 center, double radius, ObjectProperties properties) {
        this.center = center;
        this.radius = radius;
        this.properties = properties;
    }

    // Constructors to initialize sphere without properties
    public Sphere(Vector3 center, double radius) {
        this.center = center;
        this.properties = new ObjectProperties();
    }

    // Method to check ray-sphere intersection
    public double intersect(Ray ray) {
        Vector3 oc = ray.origin.subtract(center);
        double a = ray.direction.dot(ray.direction);
        double b = 2.0 * oc.dot(ray.direction);
        double c = oc.dot(oc) - radius * radius;
        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return -1.0; // No intersection
        } else {
            return (-b - Math.sqrt(discriminant)) / (2.0 * a);
        }
    }

    public Vector3 getNormal(Vector3 point) {
        return point.subtract(center).normalize();
    }

    public boolean isEmissive() {
        return properties.emission > 0;
    }


    public double emissionIntensity() {
        return 1.0;  // Adjust this value based on how strong you want the emissive sphere to be
    }

    public double getEmission() {
        return properties.emission;
    }



}