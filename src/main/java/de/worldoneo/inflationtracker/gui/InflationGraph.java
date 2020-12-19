package de.worldoneo.inflationtracker.gui;

import de.worldoneo.inflationtracker.Config;
import de.worldoneo.inflationtracker.InflationTracker;
import de.worldoneo.inflationtracker.calculator.InflationCalculator;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.GeneralPath;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InflationGraph extends JPanel implements ActionListener {
    private static final long CACHE_TIME = 60000;
    @Setter
    @Getter
    private Config config;

    private long millis = 0;
    private List<InflationCalculator.Point> pointList;
    private final InflationCalculator inflationCalculator;
    @Getter
    private int zoomAmount = 0;
    @Getter
    private int rlPosition = 0;
    @Getter
    private Point pressedPoint;
    @Getter
    private boolean mouseDown = false;

    public InflationGraph(InflationCalculator inflationCalculator, Config config) {
        this.inflationCalculator = inflationCalculator;
        this.config = config;

        enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
        enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);

        Timer timer = new Timer(10000, this);
        timer.start();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(this::fetchData, 0, 1, TimeUnit.MINUTES);
    }

    public void fetchData() {
        if (System.currentTimeMillis() - millis >= CACHE_TIME) {
            try {
                pointList = inflationCalculator.getInflationPoints(config.basket);
                millis = System.currentTimeMillis();
                repaint();
            } catch (SQLException sqlException) {
                InflationTracker.logger.error("Failed to calculate inflation for GUI", sqlException);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (pointList == null) {
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.LIGHT_GRAY);
            drawCenteredString(g, "Loading...",
                    new Rectangle(getWidth(), getHeight()),
                    new Font("Georgia", Font.PLAIN, getHeight() / 10));
            return;
        }

        List<InflationCalculator.Point> listToRender = pointList.stream().skip(zoomAmount + rlPosition).collect(Collectors.toList());
        listToRender.subList(listToRender.size() - (zoomAmount - rlPosition), listToRender.size()).clear();

        double pMin = +Double.MAX_VALUE;
        double pMax = -Double.MAX_VALUE;
        for (InflationCalculator.Point point : listToRender) {
            pMin = Math.min(pMin, point.getValue());
            pMax = Math.max(pMax, point.getValue());
        }

        int height = getHeight() - 10;
        double v = pMax - pMin;

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());

        GeneralPath path = new GeneralPath();
        GeneralPath average = new GeneralPath();
        GeneralPath middle = new GeneralPath();


        double startY = height - ((listToRender.get(0).getValue() - pMin) / v * height);
        int size = listToRender.size();
        double xScale = (getWidth() - 12) / (listToRender.get(size - 1).getTime() - (double) listToRender.get(0).getTime());

        path.moveTo(0, startY);
        average.moveTo(0, startY);
        middle.moveTo(0, startY);

        int incr = 0;
        double totalValue = 0;
        List<Coordinate> normalCoordinates = new ArrayList<>(size);
        List<Coordinate> avgCoordinates = new ArrayList<>(size);
        for (InflationCalculator.Point point : listToRender) {
            incr++;
            totalValue += point.getValue();
            double x = (point.getTime() - listToRender.get(0).getTime()) * xScale;
            double y = height - ((point.getValue() - pMin) / v * height) + 10;
            double avgY = height - (((totalValue / incr) - pMin) / v * height) + 10;

            g2.setColor(Color.GRAY);
            g2.drawLine((int) x, 0, (int) x, getHeight());

            normalCoordinates.add(new Coordinate(x, y, point));
            avgCoordinates.add(new Coordinate(x, avgY, new InflationCalculator.Point(0, (totalValue / incr))));
        }

        g2.setFont(new Font("Liberation Mono", Font.BOLD, 12));
        for (Coordinate coordinate : normalCoordinates) {
            g2.setColor(Color.LIGHT_GRAY);
            g2.rotate(Math.toRadians(90), coordinate.getX() - 12, 0);
            g2.drawString(coordinate.getPoint().getTime() + "", (int) coordinate.getX() - 12, 0);
            g2.rotate(Math.toRadians(-90), coordinate.getX() - 12, 0);
            g2.setColor(Color.WHITE);
            g2.drawString(String.format("%.2f", coordinate.getPoint().getValue()), (int) coordinate.getX() - 12, (int) coordinate.getY());
            path.lineTo(coordinate.getX(), coordinate.getY());
        }

        for (Coordinate coordinate : avgCoordinates) {
            g2.drawString(String.format("%.2f", coordinate.getPoint().getValue()), (int) coordinate.getX() - 12, (int) coordinate.getY());
            average.lineTo(coordinate.getX(), coordinate.getY());
        }

        middle.lineTo(getWidth(), startY);
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.RED);
        g2.draw(middle);
        g2.setColor(Color.CYAN);
        g2.draw(path);
        g2.setColor(Color.GREEN);
        g2.draw(average);
    }

    public void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setFont(font);
        g.drawString(text, x, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    @Override
    protected void processMouseWheelEvent(final MouseWheelEvent e) {
        zoomAmount -= e.getWheelRotation();
        zoomAmount = Math.max(0, zoomAmount);
        zoomAmount = Math.min(pointList.size() / 2 - 1, zoomAmount);
        if (!isSafeZoomAndRL(zoomAmount, rlPosition)) {
            if (rlPosition > 0) rlPosition -= 1;
            else rlPosition += 1;
        }
        repaint();
    }

    @Override
    protected void processMouseEvent(final MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
            mouseDown = true;
            pressedPoint = e.getPoint();
        } else {
            mouseDown = false;
        }

    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        if (getPressedPoint() == null) return;
        if (!isMouseDown()) return;
        if (e.getPoint().getX() - getPressedPoint().getX() > 10) {
            if (isSafeZoomAndRL(zoomAmount, rlPosition - 1)) rlPosition -= 1;
            pressedPoint = e.getPoint();
        } else if (e.getPoint().getX() - getPressedPoint().getX() < -10) {
            if (isSafeZoomAndRL(zoomAmount, rlPosition + 1)) rlPosition += 1;
            pressedPoint = e.getPoint();
        }
        repaint();
    }

    public boolean isSafeZoomAndRL(int zA, int rlPos) {
        int lTS = pointList.size() - (zA + rlPos);
        return lTS - (zA - rlPos) <= lTS && lTS >= 0 && (zA + rlPos) >= 0;
    }

    public List<InflationCalculator.Point> getPointList() {
        return pointList;
    }

    @Data
    public static class Coordinate {
        private final double x;
        private final double y;
        private final InflationCalculator.Point point;
    }
}
