package de.worldoneo.inflationtracker.gui;

import de.worldoneo.inflationtracker.InflationTracker;
import de.worldoneo.inflationtracker.util.Exporter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ExportListener implements ActionListener {
    private JFrame jFrame;
    private InflationGraph inflationGraph;
    public static final String[] IMAGE_TYPES = new String[]{"png", "jpg", "bmp"};

    public ExportListener(JFrame jFrame, InflationGraph inflationGraph) {
        this.jFrame = jFrame;
        this.inflationGraph = inflationGraph;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        FileDialog fileDialog = new FileDialog(jFrame, "Export inflation data", FileDialog.SAVE);
        fileDialog.setVisible(true);

        String result = fileDialog.getFile();
        if (result == null) return;
        try {
            File file = new File(fileDialog.getDirectory() + result);
            String absolutePath = file.getAbsolutePath();
            if (result.endsWith(".json")) {
                InflationTracker.logger.info("Exporting inflation data to " + absolutePath + " as JSON");
                Exporter.exportJSON(file, inflationGraph.getPointList(), true);
                return;
            } else if (result.endsWith(".csv")) {
                InflationTracker.logger.info("Exporting inflation data to " + absolutePath + " as CSV");
                Exporter.exportCSV(file, inflationGraph.getPointList());
                return;
            }
            for (String imageType : IMAGE_TYPES) {
                if (absolutePath.endsWith("." + imageType)) {
                    InflationTracker.logger.info("Exporting inflation data to " + absolutePath + " as " + imageType.toUpperCase());
                    BufferedImage bufferedImage = new BufferedImage(inflationGraph.getWidth(), inflationGraph.getHeight(), BufferedImage.TYPE_INT_RGB);
                    inflationGraph.update(bufferedImage.getGraphics());
                    ImageIO.write(bufferedImage, imageType, file);
                    return;
                }
            }
            JOptionPane.showMessageDialog(jFrame, "The file couldn't be exported as the given format!\nAvailable formats are: CSV,JSON,JPG,BMP,PNG");
        } catch (IOException exception) {
            InflationTracker.logger.error("Couldn't export file", exception);
        }
    }
}
