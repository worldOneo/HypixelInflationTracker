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
import java.awt.geom.GeneralPath;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InflationGraph extends JPanel implements ActionListener {
    private static final long CACHE_TIME = 60000;
    @Setter
    @Getter
    private Config config;

    private long millis = 0;
    private List<InflationCalculator.Point> pointList;
    private final InflationCalculator inflationCalculator;
    private double pMin = +Double.MAX_VALUE;
    private double pMax = -Double.MAX_VALUE;

    public InflationGraph(InflationCalculator inflationCalculator, Config config) {
        this.inflationCalculator = inflationCalculator;
        this.config = config;

        Timer timer = new Timer(10000, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (System.currentTimeMillis() - millis >= CACHE_TIME) {
            try {
                pointList = inflationCalculator.getInflationPoints(config.basket);
                millis = System.currentTimeMillis();
                for (InflationCalculator.Point point : pointList) {
                    pMin = Math.min(pMin, point.getValue());
                    pMax = Math.max(pMax, point.getValue());
                }
            } catch (SQLException sqlException) {
                InflationTracker.logger.error("Failed to calculate inflation for GUI", sqlException);
                return;
            }
        }
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int height = getHeight() - 10;
        double v = pMax - pMin;

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());

        GeneralPath path = new GeneralPath();
        GeneralPath average = new GeneralPath();
        GeneralPath middle = new GeneralPath();


        double startY = height - ((1 - pMin) / v * height);
        int size = pointList.size();
        double xScale = (getWidth() - 12) / (pointList.get(size - 1).getTime() - (double) pointList.get(0).getTime());

        path.moveTo(0, startY);
        average.moveTo(0, startY);
        middle.moveTo(0, startY);

        int incr = 0;
        double totalValue = 0;
        List<Coordinate> normalCoordinates = new ArrayList<>(size);
        List<Coordinate> avgCoordinates = new ArrayList<>(size);
        for (InflationCalculator.Point point : pointList) {
            incr++;
            totalValue += point.getValue();
            double x = (point.getTime() - pointList.get(0).getTime()) * xScale;
            double y = height - ((point.getValue() - pMin) / v * height) + 10;
            double avgY = height - (((totalValue / incr) - pMin) / v * height) + 10;

            g2.setColor(Color.GRAY);
            g2.drawLine((int) x, 0, (int) x, getHeight());

            normalCoordinates.add(new Coordinate(x, y, point));
            avgCoordinates.add(new Coordinate(x, avgY, new InflationCalculator.Point(0, (totalValue / incr))));
        }

        for (Coordinate coordinate : normalCoordinates) {
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

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
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
