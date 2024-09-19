package com.alba.tracer;

public class Matrix3 {
    private double[][] matrix; // 3x3 matrix

    // Constructor to initialize an identity matrix
    public Matrix3() {
        matrix = new double[3][3];
        for (int i = 0; i < 3; i++) {
            matrix[i][i] = 1.0;
        }
    }

    // Constructor to initialize with a custom 2D array
    public Matrix3(double[][] matrix) {
        this.matrix = matrix;
    }

    // Factory method to create a rotation matrix around the X-axis
    public static Matrix3 rotationX(double angle) {
        double[][] mat = new double[3][3];
        mat[0][0] = 1;
        mat[1][1] = Math.cos(angle);
        mat[1][2] = -Math.sin(angle);
        mat[2][1] = Math.sin(angle);
        mat[2][2] = Math.cos(angle);
        return new Matrix3(mat);
    }

    // Factory method to create a rotation matrix around the Y-axis
    public static Matrix3 rotationY(double angle) {
        double[][] mat = new double[3][3];
        mat[0][0] = Math.cos(angle);
        mat[0][2] = Math.sin(angle);
        mat[1][1] = 1;
        mat[2][0] = -Math.sin(angle);
        mat[2][2] = Math.cos(angle);
        return new Matrix3(mat);
    }

    // Factory method to create a rotation matrix around the Z-axis
    public static Matrix3 rotationZ(double angle) {
        double[][] mat = new double[3][3];
        mat[0][0] = Math.cos(angle);
        mat[0][1] = -Math.sin(angle);
        mat[1][0] = Math.sin(angle);
        mat[1][1] = Math.cos(angle);
        mat[2][2] = 1;
        return new Matrix3(mat);
    }

    // Matrix multiplication (this * other matrix)
    public Matrix3 multiply(Matrix3 other) {
        double[][] result = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = 0;
                for (int k = 0; k < 3; k++) {
                    result[i][j] += this.matrix[i][k] * other.matrix[k][j];
                }
            }
        }
        return new Matrix3(result);
    }

    // Matrix multiplication with a vector (this * vector)
    public Vector3 multiply(Vector3 vec) {
        double x = matrix[0][0] * vec.x + matrix[0][1] * vec.y + matrix[0][2] * vec.z;
        double y = matrix[1][0] * vec.x + matrix[1][1] * vec.y + matrix[1][2] * vec.z;
        double z = matrix[2][0] * vec.x + matrix[2][1] * vec.y + matrix[2][2] * vec.z;
        return new Vector3(x, y, z);
    }

    // Invert the matrix (only for rotation matrices)
    public Matrix3 inverse() {
        // For rotation matrices, the inverse is the transpose
        double[][] inv = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                inv[i][j] = matrix[j][i];
            }
        }
        return new Matrix3(inv);
    }

    // Transpose the matrix
    public Matrix3 transpose() {
        return inverse();  // For rotation matrices, transpose is same as inverse
    }

    // Pretty print for debugging
    public void print() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
}
