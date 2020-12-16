package de.worldoneo.inflationtracker.gui;

import de.worldoneo.inflationtracker.InflationTracker;
import de.worldoneo.inflationtracker.util.Exporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class ExportListener implements ActionListener {
    private JFrame jFrame;
    private InflationGraph inflationGraph;

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
            if (result.endsWith(".json")) {
                InflationTracker.logger.info("Exporting inflation data to " + file.getAbsolutePath() + " as JSON");
                Exporter.exportJSON(file, inflationGraph.getPointList(), true);
            } else if (result.endsWith(".csv")) {
                InflationTracker.logger.info("Exporting inflation data to " + file.getAbsolutePath() + " as CSV");
                Exporter.exportCSV(file, inflationGraph.getPointList());
            }
        } catch (IOException exception) {
            InflationTracker.logger.error("Couldn't export file", exception);
        }
    }
}
