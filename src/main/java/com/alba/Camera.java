package com.alba;

public class Camera {
    private Vector3 origin;            // Camera position
    private Vector3 lookAt;            // Where the camera is looking
    private Vector3 up;                // Up direction for the camera
    private Vector3 lowerLeftCorner;   // Lower-left corner of the viewport
    private Vector3 horizontal;        // Horizontal vector for the viewport
    private Vector3 vertical;          // Vertical vector for the viewport

    private double viewportHeight;     // Viewport height
    private double viewportWidth;      // Viewport width
    private double focalLength;        // Focal length of the camera

    private Quaternion orientation;    // Camera orientation using quaternion
    private static final Vector3 worldUp = new Vector3(0, 1, 0);  // Consistent world up direction

    public Camera(double aspectRatio, double fov) {
        this.origin = new Vector3(0, 0, 2);    // Initial camera position
        this.lookAt = new Vector3(0, 0, 0);    // Look at the origin by default
        this.up = worldUp;                     // Define up direction (Y-axis up)
        this.focalLength = 1.0;
        this.orientation = new Quaternion(0, 0, 0, 1);  // Identity quaternion

        // Calculate the viewport height and width based on the aspect ratio and FOV
        this.viewportHeight = 2.0 * Math.tan(Math.toRadians(fov) / 2);
        this.viewportWidth = aspectRatio * viewportHeight;

        // Set up the horizontal and vertical vectors for the camera
        updateCamera();
    }

    public Camera(double aspectRatio, double fov, Vector3 origin, Vector3 lookAt, Quaternion orientation) {
        this.origin = origin;    // Initial camera position
        this.lookAt = lookAt;    // Look at the origin by default
        this.up = worldUp;                     // Define up direction (Y-axis up)
        this.focalLength = 1.0;
        this.orientation = orientation;  // Identity quaternion

        // Calculate the viewport height and width based on the aspect ratio and FOV
        this.viewportHeight = 2.0 * Math.tan(Math.toRadians(fov) / 2);
        this.viewportWidth = aspectRatio * viewportHeight;

        // Set up the horizontal and vertical vectors for the camera
        updateCamera();
    }

    private void updateCamera() {
        // Calculate the camera's coordinate system using quaternion orientation
        Vector3 forward = orientation.rotate(new Vector3(0, 0, -1));  // Camera forward direction
        Vector3 right = orientation.rotate(new Vector3(1, 0, 0));     // Camera right direction
        up = orientation.rotate(worldUp);                             // Camera up direction

        lookAt = origin.add(forward);  // Update lookAt position

        horizontal = right.multiply(viewportWidth);
        vertical = up.multiply(viewportHeight);

        // Calculate the lower-left corner of the viewport
        lowerLeftCorner = origin.subtract(horizontal.multiply(0.5))
                                .subtract(vertical.multiply(0.5))
                                .add(forward.multiply(focalLength));
    }

    public Vector3 getOrigin() {
        return this.origin;
    }
    public Vector3 getLookAt() {
        return this.lookAt;
    }
    public Quaternion getOrientation() {
        return this.orientation;
    }

    public Ray getRay(double u, double v) {
        // Calculate the direction of the ray and normalize it
        Vector3 direction = lowerLeftCorner.add(horizontal.multiply(u))
                                            .add(vertical.multiply(v))
                                            .subtract(origin);
        return new Ray(origin, direction.normalize());
    }

    public void rotateY(double angle) {
        // Convert angle to radians and create a quaternion for rotation around the Y axis
        double rad = Math.toRadians(angle);
        Quaternion q = Quaternion.fromAxisAngle(worldUp, rad);

        // Update orientation and camera vectors
        orientation = q.multiply(orientation).normalize();
        updateCamera();
    }

    public void rotateX(double angle) {
        // Convert angle to radians and create a quaternion for rotation around the camera's right axis
        double rad = Math.toRadians(angle);
        Vector3 right = orientation.rotate(new Vector3(1, 0, 0));  // Camera right direction
        Quaternion q = Quaternion.fromAxisAngle(right, rad);

        // Update orientation and camera vectors
        orientation = q.multiply(orientation).normalize();
        updateCamera();
    }

    public void moveForward(double distance) {
        // Move the camera forward in the direction it is looking
        Vector3 forward = orientation.rotate(new Vector3(0, 0, -1)).normalize();
        origin = origin.add(forward.multiply(distance));
        lookAt = lookAt.add(forward.multiply(distance));
        updateCamera();
    }

    public void moveBackward(double distance) {
        // Move the camera backward opposite to the direction it is looking
        Vector3 forward = orientation.rotate(new Vector3(0, 0, -1)).normalize();
        origin = origin.subtract(forward.multiply(distance));
        lookAt = lookAt.subtract(forward.multiply(distance));
        updateCamera();
    }

    public void moveRight(double distance) {
        // Move the camera to the right
        Vector3 right = orientation.rotate(new Vector3(1, 0, 0)).normalize();
        origin = origin.add(right.multiply(distance));
        lookAt = lookAt.add(right.multiply(distance));
        updateCamera();
    }

    public void moveLeft(double distance) {
        // Move the camera to the left
        Vector3 right = orientation.rotate(new Vector3(1, 0, 0)).normalize();
        origin = origin.subtract(right.multiply(distance));
        lookAt = lookAt.subtract(right.multiply(distance));
        updateCamera();
    }

    public void moveUp(double distance) {
        // Move the camera up
        origin = origin.add(up.multiply(distance));
        lookAt = lookAt.add(up.multiply(distance));
        updateCamera();
    }

    public void moveDown(double distance) {
        // Move the camera down
        origin = origin.subtract(up.multiply(distance));
        lookAt = lookAt.subtract(up.multiply(distance));
        updateCamera();
    }

    // Getter for the camera's up direction
    public Vector3 getUp() {
        return up;  // The 'up' vector is updated in updateCamera()
    }

    // Getter for the camera's right direction
    public Vector3 getRight() {
        return orientation.rotate(new Vector3(1, 0, 0)).normalize();  // Calculated using the camera's orientation
    }

    // Getter for the camera's forward direction
    public Vector3 getForward() {
        return orientation.rotate(new Vector3(0, 0, -1)).normalize();  // Calculated using the camera's orientation
    }
}
