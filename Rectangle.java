package com.greedysnakeproject;


public class Rectangle {

    public double x;
    public double y;
    public double width;
    public double height;

    public boolean overlaps(Rectangle other) {
        double selfX1 = x;  // X1 is left edge
        double selfX2 = x + width;  // X2 is right edge
        double selfY1 = y;  // Y1 is top edge
        double selfY2 = y + height;  // Y2 is bottom edge

        double otherX1 = other.x;
        double otherX2 = other.x + other.width;
        double otherY1 = other.y;
        double otherY2 = other.y + other.height;

        if (selfX1 < otherX2 && selfX2 > otherX1 &&
            selfY1 < otherY2 && selfY2 > otherY1)
            return true;

        return false;
    }

    public Rectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

//    public Point[] getVertices(Rectangle rect) {
//        Point[] vertices = new Point[4];
//        vertices[0] = new Point(rect.getX(), rect.getY());
//        vertices[1] = new Point(rect.getX() + rect.width, rect.getY());
//        vertices[2] = new Point(rect.getX() + rect.width, rect.getY() + rect.height);
//        vertices[3] = new Point(rect.getX(), rect.getY() + rect.height);
//
//        return vertices;
//    }
//
//    public boolean contains(Point p) {
//        double pX = p.getX();
//        double pY = p.getY();
//
//        if (pX >= x && pX <= x + width &&
//            pY >= y && pY <= y + height)
//            return true;
//
//        return false;
//    }

}
