package com.greedysnakeproject;

public class Item {

    private Rectangle occupancy;
    private Rectangle hitBox;

    private short coolDown = 0;
    public boolean isAvailable = false;


    public Rectangle getOccupancy() {
        return occupancy;
    }

    public Rectangle getHitBox() {
        return hitBox;
    }

    public void setOccupancy(Rectangle occupancy) {
        this.occupancy = occupancy;
    }

    public void setHitBox(Rectangle hitBox) {
        this.hitBox = hitBox;
    }

    public short getCoolDown() {
        return coolDown;
    }

    public void setCoolDown(short coolDown) {
        this.coolDown = coolDown;
    }
}
