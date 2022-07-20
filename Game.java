// FUTURE UPDATES
// ADD queue of size 2 to store fast typing keys temporarily

package com.greedysnakeproject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

public class Game extends Canvas implements Runnable{
    final double TICKS_PER_SECOND = 60.0;
    final double NS_PER_TICK = 1_000_000_000/TICKS_PER_SECOND;

    private static final int WIDTH = 320;
    private static final int HEIGHT = WIDTH ;
    private static final int SCALE = 2;
    public final String TITLE = "Snake";

    // Snake Parameters
    final double INIT_SPEED = 50 * SCALE; // pixels travel per second by the snake (initial)
    final double ACCEL_FACTOR = INIT_SPEED / 4;
    final double INIT_LENGTH = (double) (100*SCALE); // initial pixel length of snake
    final double LENGTHENING_FACTOR = INIT_LENGTH / 2;
    final short THICKNESS = (short) (7*SCALE); // pixel thickness of snake

    private boolean isRunning = false;
    private Thread thread;

    private Snake snake;
    private Item food;
    private ArrayList<Rectangle> obstacles = new ArrayList<>();
//    private Queue<Integer> queueOfKeyEvents = new LinkedList<>();


    public static void main (String args[]) {

        var game = new Game();
        game.init();
        game.start();
    }

    @Override
    public void run() {
//        initSnake();
//        addKeyListener(new KeyInput(this));
//        this.requestFocus();
//        setFocusable(true);
        long lastTime = System.nanoTime();
        double delta = 0;
        int updates = 0;
        int frames = 0;
        long timer = System.currentTimeMillis();

        while (isRunning) {
            // game loop
            delta = (System.nanoTime() - lastTime) / NS_PER_TICK;

            if (delta >= 1) { // it's time for another tick
                render();
                if (!isGameOver()){
                    tick();
                }
                lastTime = System.nanoTime();
                updates++;
                delta--;
            }
//            if (!isGameOver())
//                render();
            frames++;

            if (System.currentTimeMillis() - timer >= 1000) {
//                System.out.println(updates + " Ticks, Fps: " + frames);
//                System.out.println(obstacles.get(0).x+obstacles.get(0).width);
                timer = System.currentTimeMillis();
                updates = 0;
                frames = 0;
            }
        }

        stop();
    }

    public void init() {
        initDimension();

        initFrame();

        initSnake();

        initFood();

        initObstacles();

        addKeyListener(new KeyInput(this));

        requestFocus();

    }

    public synchronized void start() {
        if (isRunning)
            return;

        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        if (!isRunning)
            return;

        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.exit(1);
    }

//    public void keyTyped(KeyEvent e) {
//        char key = e.getKeyChar();
//
//        if (!isTurnSnakeAllowed()) return;
//
//        turnSnake(key);
//    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        respondToKey(key);
    }

    private boolean isGameOver() {
        if (isCollision() || hitObstacles())
            return true;

        return false;
    }

    private void tick() {
        snake.tick();

        tick_Food();
    }

    private void tick_Food() {
        if (food.getCoolDown() == 0) { // if food is not in cool down (cool down time == 0)
            if (!food.isAvailable) { // if food is not available (isAvailable == false)
                // redefine food occupancy and hitBox
                double foodOccupancyDiameter = 15 * SCALE;
                var newFoodOccupancy = genFoodOccupancy(foodOccupancyDiameter);

                if (newFoodOccupancy != null) {
                    double foodDisplayDiameter = foodOccupancyDiameter - 5 * SCALE;
                    var newFoodHitBox = genFoodHitBox(newFoodOccupancy, foodDisplayDiameter);

                    food.setOccupancy(newFoodOccupancy);
                    food.setHitBox(newFoodHitBox);
                    food.isAvailable = true;
                }
            }
            else if (snake.headToRectangle().overlaps(food.getHitBox())) { // else if snake head collide food hitBox
                // disable food and add cool down time
                food.isAvailable = false;
                food.setCoolDown((short) (TICKS_PER_SECOND * 0.5));

                // trigger accelerate and lengthening
                snake.setIdealSpeed(snake.getIdealSpeed() + ACCEL_FACTOR / TICKS_PER_SECOND);
                snake.setAccelerating(true);

                snake.setIdealTotalLength(snake.getIdealTotalLength() + LENGTHENING_FACTOR);
                snake.setLengthening(true);
            }
        }
        else // else (food is in cool down)
            food.setCoolDown((short)(food.getCoolDown()-1));// reduce cool down time
    }

    private Rectangle genFoodOccupancy(double foodOccupancyDiameter) {
        var occupancies = getOccupancies();

        for (int i = 0; i < 1000; i++) { // only try 1000 times
            // generate random rectangle within gameCage
            var gameCage = getGameCage();
//            double boundaryBuffer = 10;
//            double randX = gameCage.x + boundaryBuffer + Math.random() * (gameCage.width - boundaryBuffer - foodOccupancyDiameter);
//            double randY = gameCage.y + boundaryBuffer + Math.random() * (gameCage.height - boundaryBuffer - foodOccupancyDiameter);
            double randX = gameCage.x + Math.random() * (gameCage.width - foodOccupancyDiameter);
            double randY = gameCage.y + Math.random() * (gameCage.height - foodOccupancyDiameter);
            Rectangle randOccupancy = new Rectangle(randX, randY, foodOccupancyDiameter, foodOccupancyDiameter);

            // if the random occupancy overlap other occupancies, go back and generate new random occupancy
            boolean anyOverlap = false;
            for (int j = 0; j < occupancies.size(); j++) {
                if (randOccupancy.overlaps(occupancies.get(j))) {
                    anyOverlap = true;
                    break;
                }
            }

            if (!anyOverlap) // if no overlap then just return the occupancy
                return randOccupancy;
        }
        return null; // if after 1000 tries still not successful, just give up and return null
    }

    private Rectangle genFoodHitBox(Rectangle foodOccupancy, double foodDisplayDiameter) {
        double hitBoxX = foodOccupancy.x + (foodOccupancy.width - foodDisplayDiameter) / 2;
        double hitBoxY = foodOccupancy.y + (foodOccupancy.height - foodDisplayDiameter) / 2;
        return new Rectangle(hitBoxX, hitBoxY, foodDisplayDiameter, foodDisplayDiameter);
    }

    private ArrayList<Rectangle> getOccupancies() {
        var occupancies = snake.toRectangles();

        occupancies.addAll(obstacles);

        if (food.isAvailable)
            occupancies.add(food.getOccupancy());

        // later can add wall occupancies

        return occupancies;
    }

    private Rectangle getGameCage() {
        return new Rectangle(0, 0, WIDTH * SCALE, HEIGHT * SCALE);
    }

    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(2);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.black);
        g.fillRect(0, 0, WIDTH * SCALE, HEIGHT * SCALE);

        if (!isGameOver()) {
//            g.setColor(Color.white);
//            g.fillOval(300, 300, 100, 50);
            snake.render(g);
            render_Food(g);
            render_Obstacles(g);
        }
        else {
            return;
//            g.setColor(Color.white);
//            g.fillRect(WIDTH * SCALE / 4, HEIGHT * SCALE / 4, WIDTH * SCALE / 2, HEIGHT * SCALE / 2);
        }

////////////////////////////////////////////////////
        g.dispose();
        bs.show();
    }

    private void render_Obstacles(Graphics g) {
        g.setColor(Color.lightGray);

        for (int i = 0; i < obstacles.size(); i++) {
            var obstacle = obstacles.get(i);
            g.fillRect((int)obstacle.x, (int)obstacle.y, (int)obstacle.width, (int)obstacle.height);
        }
    }

    private void render_Food(Graphics g) {
        if (!food.isAvailable)
            return;

        g.setColor(Color.white);
        var foodHitBox = food.getHitBox();
        g.fillOval((int)foodHitBox.x, (int)foodHitBox.y, (int)foodHitBox.width, (int)foodHitBox.height);
    }

    private boolean isTurnSnakeAllowed() {
        Point head = new Point(snake.getHeadX(), snake.getHeadY());

        Point neck = (snake.turningPoints.isEmpty()) ? new Point(snake.getTailX(), snake.getTailY()) : snake.turningPoints.get(0);

        return Point.twoPointDistance(head, neck) > snake.getThickness() + 3 * SCALE;
    }

    private void respondToKey(int key) {
        switch (key) {
            case KeyEvent.VK_RIGHT:
                if (!isTurnSnakeAllowed()) break;
                if (snake.getVelHeadX() == 0) {
                    snake.turningPoints.add(0, new Point(snake.getHeadX(), snake.getHeadY()));

                    double newVelX = Math.abs(snake.getVelHeadY());
                    double newVelY = 0;

                    snake.setVelHeadX(newVelX);
                    snake.setVelHeadY(newVelY);

                    snake.addNextVelTailX(newVelX);
                    snake.addNextVelTailY(newVelY);
                }
                break;
            case KeyEvent.VK_LEFT:
                if (!isTurnSnakeAllowed()) break;
                if (snake.getVelHeadX() == 0) {
                    snake.turningPoints.add(0, new Point(snake.getHeadX(), snake.getHeadY()));

                    double newVelX = - Math.abs(snake.getVelHeadY());
                    double newVelY = 0;

                    snake.setVelHeadX(newVelX);
                    snake.setVelHeadY(newVelY);

                    snake.addNextVelTailX(newVelX);
                    snake.addNextVelTailY(newVelY);
                }
                break;
            case KeyEvent.VK_DOWN:
                if (!isTurnSnakeAllowed()) break;
                if (snake.getVelHeadY() == 0) {
                    snake.turningPoints.add(0, new Point(snake.getHeadX(), snake.getHeadY()));

                    double newVelX = 0;
                    double newVelY = Math.abs(snake.getVelHeadX());

                    snake.setVelHeadX(newVelX);
                    snake.setVelHeadY(newVelY);

                    snake.addNextVelTailX(newVelX);
                    snake.addNextVelTailY(newVelY);
                }
                break;
            case KeyEvent.VK_UP:
                if (!isTurnSnakeAllowed()) break;
                if (snake.getVelHeadY() == 0) {
                    snake.turningPoints.add(0, new Point(snake.getHeadX(), snake.getHeadY()));

                    double newVelX = 0;
                    double newVelY = - Math.abs(snake.getVelHeadX());

                    snake.setVelHeadX(newVelX);
                    snake.setVelHeadY(newVelY);

                    snake.addNextVelTailX(newVelX);
                    snake.addNextVelTailY(newVelY);
                }
                break;
//            case 'b': // lengthen and accelerate
//                if (!snake.isLengthening() && !snake.isAccelerating()) {
//                    snake.setIdealSpeed(snake.getIdealSpeed() * 1.2);
//                    snake.setAccelerating(true);
//
//                    snake.setIdealTotalLength(snake.getIdealTotalLength() + LENGTHENING_FACTOR);
//                    snake.setLengthening(true);
//                }
//                break;
        }
    }

    private boolean isCollision() {
        var snakeRectangles = snake.toRectangles();
        if (snakeRectangles.size() >= 4) {
            for (int i = 3; i < snakeRectangles.size(); i++) {
                if (snakeRectangles.get(0).overlaps(snakeRectangles.get(i)))
                    return true;
            }
        }

        return false;
    }

    private boolean hitObstacles() {
        var headRect = snake.headToRectangle();
        for (int i = 0; i < obstacles.size(); i++) {
            if (headRect.overlaps(obstacles.get(i)))
                return true;
        }

        return false;
    }

    private void initDimension() {
        var dimension = new Dimension(WIDTH * SCALE, HEIGHT * SCALE);
        this.setPreferredSize(dimension);
        this.setMaximumSize(dimension);
        this.setMinimumSize(dimension);
    }
    private void initFrame() {
        var frame = new JFrame(TITLE);
        frame.add(this);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initSnake() {
        double pixelTravelPerTick = INIT_SPEED / TICKS_PER_SECOND;
        snake = new Snake((double)WIDTH / 2 * SCALE,
                (double)HEIGHT / 2 * SCALE,
                THICKNESS,
                INIT_LENGTH,
                (byte) 0,
                pixelTravelPerTick);
    }

    private void initFood() {
        food = new Item();
    }

    private void initObstacles() {
        var gameCage = getGameCage();
        double boundaryThickness = 5;
        obstacles.add(new Rectangle(gameCage.x, gameCage.y, boundaryThickness, gameCage.height));
        obstacles.add(new Rectangle(gameCage.x, gameCage.y, gameCage.width, boundaryThickness));
        obstacles.add(new Rectangle(gameCage.x + gameCage.width - boundaryThickness,
                                    gameCage.y, boundaryThickness, gameCage.height));
        obstacles.add(new Rectangle(gameCage.x, gameCage.y + gameCage.height - boundaryThickness,
                                    gameCage.width, boundaryThickness));
    }
}
