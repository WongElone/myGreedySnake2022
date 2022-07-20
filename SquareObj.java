package com.greedysnakeproject;

public class SquareObj {

    private double x;
    private double y;
    private short pixelSize;

    public SquareObj(double x, double y, short pixelSize) {
        this.x = x;
        this.y = y;
        this.pixelSize = pixelSize;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
