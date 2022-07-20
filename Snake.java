// rewrite twoPointRender, based on toRectangle, refactor common codes, like find upperleft corner, width, height

package com.greedysnakeproject;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Snake {
    private double headX;
    private double headY;
    private double tailX;
    private double tailY;


    private double idealSpeed;
    private double velHeadX;
    private double velHeadY;
    private double velTailX;
    private double velTailY;

    private short thickness;

    private boolean isLengthening = false;

    private boolean isAccelerating = true;

    private Queue<Double> queueOfVelTailX = new LinkedList<>();
    private Queue<Double> queueOfVelTailY = new LinkedList<>();

    public double getIdealTotalLength() {
        return idealTotalLength;
    }

    public void setIdealTotalLength(double idealTotalLength) {
        this.idealTotalLength = idealTotalLength;
    }

    private double idealTotalLength;
    private double guidingLength;

    private double oldTailToButtDistance = Double.MAX_VALUE;
    public ArrayList<Point> turningPoints = new ArrayList<>();


    public Snake(double headX, double headY, short thickness, double initLength, byte initDirection, double initSpeed) {
        this.headX = headX;
        this.headY = headY;
        this.thickness = thickness;
        this.idealTotalLength = initLength;
        this.idealSpeed = initSpeed;

        var xFactor = getXFactor(initDirection);
        var yFactor = getYFactor(initDirection);

        this.tailX = headX + xFactor * initLength;
        this.tailY = headY + yFactor * initLength;

        this.velHeadX = - xFactor * initSpeed;
        this.velHeadY = - yFactor * initSpeed;

        this.velTailX = - xFactor * initSpeed;
        this.velTailY = - yFactor * initSpeed;
    }

    public ArrayList<Rectangle> toRectangles() {
        var headToTailPoints = getHeadToTailPoints();

        int numOfRect = headToTailPoints.size() - 1;
        ArrayList<Rectangle> rectangles = new ArrayList<>();
//        Rectangle[] rectangles = new Rectangle[numOfRect];
        for (short i = 0; i < numOfRect; i++) {
            var origin = new Point(0, 0);
            var pointA = headToTailPoints.get(i);
            var pointB = headToTailPoints.get(i+1);
            var upperLeftPoint = (Point.twoPointDistance(origin, pointA)
                                < Point.twoPointDistance(origin, pointB)) ? pointA : pointB;
            double width = Math.abs(pointA.getX() - pointB.getX()) + thickness;
            double height = Math.abs(pointA.getY() - pointB.getY()) + thickness;

            rectangles.add(new Rectangle(upperLeftPoint.getX(), upperLeftPoint.getY(), width, height));
//            rectangles[i] = new Rectangle(upperLeftPoint.getX(), upperLeftPoint.getY(), width, height);
        }

        return rectangles;
    }

    public Rectangle headToRectangle() {
        return new Rectangle(headX, headY, thickness, thickness);
    }

    public void tick() {
        updateHead();

        updateLengthening();

        updateTail();

        if (isAccelerating)
            isAccelerating = false;
    }

    private void updateHead() {
        if (isAccelerating) {
            var velHead = getUpdatedXYVel(velHeadX, velHeadY);
            velHeadX = velHead[0];
            velHeadY = velHead[1];
        }

        headX += velHeadX;
        headY += velHeadY;
    }

    private double[] getUpdatedXYVel(double velX, double velY) {
        if (velX != 0 && velY == 0) {
            if (velX > 0)
                velX = idealSpeed;
            else
                velX = - idealSpeed;
        }
        else if (velX == 0 && velY != 0) {
            if (velY > 0)
                velY = idealSpeed;
            else
                velY = - idealSpeed;
        }

        double[] vel = {velX, velY};
        return vel;
    }

    private void updateLengthening() {
        if (calculateTotalLength() >= idealTotalLength)
            isLengthening = false;

        if (isLengthening)
            guidingLength += idealSpeed;
        else
            guidingLength = idealTotalLength;
    }

    private void updateTail() {
        if (!isLengthening) {
            tailX += velTailX;
            tailY += velTailY;
        }
        if (isAccelerating) {
            var velTail = getUpdatedXYVel(velTailX, velTailY);
            velTailX = velTail[0];
            velTailY = velTail[1];
        }

        if (turningPoints.isEmpty())
            return;


        var tail = new Point(tailX, tailY);

        var buttIndex = turningPoints.size() - 1;
        Point butt = turningPoints.get(buttIndex);

        var currentTailToButtDistance = Point.twoPointDistance(tail, butt);
        if (currentTailToButtDistance > oldTailToButtDistance)  // that means the tail already pass the butt
        {
            // update butt
            Point newButt = (turningPoints.size() == 1) ? new Point(headX, headY) : turningPoints.get(buttIndex - 1);
            turningPoints.remove(buttIndex);  // must be placed before calculation of tailToButtDistance


            // update tail
            var buttToNewButtDirection = Point.twoPointsDirection(butt, newButt);
            byte xFactor = getXFactor(buttToNewButtDirection);
            byte yFactor = getYFactor(buttToNewButtDirection);

            double idealTailToButtDistance = guidingLength - calculateHeadToButtLength();
            tailX = newButt.getX() + xFactor * idealTailToButtDistance;
            tailY = newButt.getY() + yFactor * idealTailToButtDistance;


            // update tail velocity
            var velTail = getUpdatedXYVel(queueOfVelTailX.remove(), queueOfVelTailY.remove());
            velTailX = velTail[0];
            velTailY = velTail[1];

            // reset parameter
            oldTailToButtDistance = Double.MAX_VALUE;
        }
        else
            oldTailToButtDistance = currentTailToButtDistance;

        // if (headToButtLength >= targetLength)
        //      remove butt
        //
    }

    private ArrayList<Point> getHeadToTailPoints() {
        ArrayList<Point> headToTailPoints = new ArrayList<>();

        headToTailPoints.add(new Point(headX, headY));
        for (short i = 0; i < turningPoints.size(); i++)
            headToTailPoints.add(turningPoints.get(i));
        headToTailPoints.add(new Point(tailX, tailY));

        return headToTailPoints;
    }
    public void render(Graphics g) {
        g.setColor(Color.white);

        ArrayList<Point> headToTailPoints = getHeadToTailPoints();

        for (int i = 0; i < headToTailPoints.size() - 1; i++) {
            var pointA = headToTailPoints.get(i);
            var pointB = headToTailPoints.get(i+1);

            twoPointRender(g, pointA, pointB);
        }
    }

    public void addNextVelTailX(double nextVelTailX) {
        this.queueOfVelTailX.add(nextVelTailX);
    }

    public void addNextVelTailY(double nextVelTailY) {
        this.queueOfVelTailY.add(nextVelTailY);
    }


    private byte getXFactor(byte direction) {
        if (direction == 0) // right
            return (byte) -1;
        else if (direction == 1) // left
            return (byte) 1;
        else // up or down
            return (byte) 0;
    }

    private byte getYFactor(byte direction) {
        if (direction == 2) // down
            return (byte) -1;
        else if (direction == 3)
            return (byte) 1; // up
        else
            return (byte) 0;
    }

    private void twoPointRender(Graphics g, Point pointA, Point pointB) {
        g.setColor(Color.white);
        var xDisplacement = pointB.getX() - pointA.getX();
        var yDisplacement = pointB.getY() - pointA.getY();

        if (xDisplacement != 0 && yDisplacement != 0) // pointA and pointB has oblique displacement
            System.out.println("oblique displacement");
//            throw new RuntimeException("oblique displacement");

        if (xDisplacement > 0) { // pointA is on the left of pointB
            g.fillRect( (int) pointA.getX(),
                    (int) pointA.getY(),
                    (int) (Math.abs(xDisplacement) + thickness),
                    thickness);
        }
        else if (xDisplacement < 0) { // pointA is on the right of pointB
            g.fillRect( (int) pointB.getX(),
                    (int) pointB.getY(),
                    (int) (Math.abs(xDisplacement) + thickness),
                    thickness);
        }
        else if (yDisplacement > 0) { // pointA is above pointB
            g.fillRect( (int) pointA.getX(),
                    (int) pointA.getY(),
                    thickness,
                    (int) (Math.abs(yDisplacement) + thickness));
        }
        else if (yDisplacement < 0) { // pointA is below pointB
            g.fillRect( (int) pointB.getX(),
                    (int) pointB.getY(),
                    thickness,
                    (int) (Math.abs(yDisplacement) + thickness));
        }
//        else {
//            System.out.println(xDisplacement);
//            System.out.println(yDisplacement);
//            throw new RuntimeException("error 2"); // pointA = pointB
//        }
    }

    public double calculateTotalLength() {
        var headToTailPoints = getHeadToTailPoints();

        double totalLength = 0.0;
        for (short i = 0; i < headToTailPoints.size() - 1; i++) {
            totalLength += Point.twoPointDistance(
                    headToTailPoints.get(i),
                    headToTailPoints.get(i + 1)
            );
        }

        return totalLength;
    }

    private double calculateHeadToButtLength() {
        var headToTailPoints = getHeadToTailPoints();

        double headToButtLength = 0.0;
        for (short i = 0; i < headToTailPoints.size() - 2; i++) {
            headToButtLength += Point.twoPointDistance(
                    headToTailPoints.get(i),
                    headToTailPoints.get(i + 1)
            );
        }

        return headToButtLength;
    }

    public double getHeadX() {
        return headX;
    }

    public void setHeadX(double headX) {
        this.headX = headX;
    }

    public double getHeadY() {
        return headY;
    }

    public void setHeadY(double headY) {
        this.headY = headY;
    }

    public double getTailX() {
        return tailX;
    }

    public void setTailX(double tailX) {
        this.tailX = tailX;
    }

    public double getTailY() {
        return tailY;
    }

    public void setTailY(double tailY) {
        this.tailY = tailY;
    }

    public double getVelHeadX() {
        return velHeadX;
    }

    public void setVelHeadX(double velHeadX) {
        this.velHeadX = velHeadX;
    }

    public double getVelHeadY() {
        return velHeadY;
    }

    public void setVelHeadY(double velHeadY) {
        this.velHeadY = velHeadY;
    }

    public double getVelTailX() {
        return velTailX;
    }

    public void setVelTailX(double velTailX) {
        this.velTailX = velTailX;
    }

    public double getVelTailY() {
        return velTailY;
    }

    public void setVelTailY(double velTailY) {
        this.velTailY = velTailY;
    }

    public short getThickness() {
        return thickness;
    }

    public boolean isLengthening() {
        return isLengthening;
    }

    public void setLengthening(boolean lengthening) {
        isLengthening = lengthening;
    }

    public boolean isAccelerating() {
        return isAccelerating;
    }

    public void setAccelerating(boolean accelerating) {
        isAccelerating = accelerating;
    }

    public double getIdealSpeed() {
        return idealSpeed;
    }

    public void setIdealSpeed(double idealSpeed) {
        this.idealSpeed = idealSpeed;
    }
}
