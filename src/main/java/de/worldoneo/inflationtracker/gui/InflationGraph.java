package de.worldoneo.inflationtracker.gui;

import de.worldoneo.inflationtracker.Config;
import de.worldoneo.inflationtracker.InflationTracker;
import de.worldoneo.inflationtracker.calculator.InflationCalculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.sql.SQLException;
import java.util.List;

public class InflationGraph extends JPanel implements ActionListener {
    private static final long CACHE_TIME = 20000;
    private long millis = 0;
    private List<InflationCalculator.Point> pointList;
    private final InflationCalculator inflationCalculator;
    private final Config config;
    private double pMin = +Double.MAX_VALUE;
    private double pMax = -Double.MAX_VALUE;

    public InflationGraph(InflationCalculator inflationCalculator, Config config) {
        this.inflationCalculator = inflationCalculator;
        this.config = config;

        Timer timer = new Timer(3000, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (millis - CACHE_TIME < 0) {
            try {
                pointList = inflationCalculator.getInflationPoints(config.basket);
                millis = System.currentTimeMillis();
                for (InflationCalculator.Point point : pointList) {
                    pMin = Math.min(pMin, point.value);
                    pMax = Math.max(pMax, point.value);
                }
            } catch (SQLException sqlException) {
                InflationTracker.logger.error("Failed to calculate inflation for GUI", sqlException);
                return;
            }
        }
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int height = getHeight();
        double v = pMax - pMin;

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, getWidth(), height);

        GeneralPath path = new GeneralPath();
        GeneralPath average = new GeneralPath();
        GeneralPath middle = new GeneralPath();


        double startY = height - ((1 - pMin) / v * height);
        double xScale = getWidth() / (pointList.get(pointList.size() - 1).time - (double) pointList.get(0).time);

        path.moveTo(0, startY);
        average.moveTo(0, startY);
        middle.moveTo(0, startY);

        int incr = 0;
        double totalValue = 0;
        for (InflationCalculator.Point point : pointList) {
            incr++;
            totalValue += point.value;
            double x = (point.time - pointList.get(0).time) * xScale;
            double y = height - ((point.value - pMin) / v * height);
            double avgY = height - (((totalValue / incr) - pMin) / v * height);
            average.lineTo(x, avgY);
            path.lineTo(x, y);
        }

        middle.lineTo(getWidth(), startY);
        g2.setColor(Color.CYAN);
        g2.draw(path);
        g2.setColor(Color.GREEN);
        g2.draw(average);
        g2.setColor(Color.red);
        g2.draw(middle);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public List<InflationCalculator.Point> getPointList() {
        return pointList;
    }
}
