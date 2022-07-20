package com.greedysnakeproject;

public class Point{
    public static double twoPointDistance(Point pointA, Point pointB) {
        return Math.sqrt(Math.pow(pointA.getX() - pointB.getX(), 2)
                + Math.pow(pointA.getY() - pointB.getY(), 2));
    }

    public static byte twoPointsDirection(Point start, Point end) {
        var startX = start.getX();
        var startY = start.getY();
        var endX = end.getX();
        var endY = end.getY();

        if (endX - startX != 0 && endY - startY != 0)
            throw new RuntimeException("oblique displacement error");

        if (endX - startX > 0) // pointing right
            return 0;
        else if (endX - startX < 0) // pointing left
            return 1;
        else if (endY - startY > 0) // pointing down
            return 2;
        else if (endY - startY < 0) // pointing up
            return 3;
        else
            throw new RuntimeException("zero displacement between two points");
    }

    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
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
