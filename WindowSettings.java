package com.greedysnakeproject;

import java.awt.*;
import java.awt.image.BufferedImage;

public class WindowSettings extends Canvas{
    private static final int WIDTH = 320;
    private static final int HEIGHT = WIDTH /12 * 9;
    private static final int SCALE = 2;
    public final String TITLE = "Snake";

    public BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

    public void init() {
        var dimension = new Dimension(WIDTH * SCALE, HEIGHT * SCALE);
        this.setPreferredSize(dimension);
        this.setMaximumSize(dimension);
        this.setMinimumSize(dimension);
    }
}
