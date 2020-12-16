package de.worldoneo.inflationtracker.gui;

import de.worldoneo.inflationtracker.Config;
import de.worldoneo.inflationtracker.calculator.InflationCalculator;

import javax.swing.*;
import java.awt.*;

public class TrackerGUI extends JFrame {
    public TrackerGUI(InflationCalculator inflationCalculator, Config config) {
        super("Hypixel Inflation Tracker - GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);



        InflationGraph inflationGraph = new InflationGraph(inflationCalculator, config);
        inflationGraph.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.weighty = 1.0;   //request any extra vertical space
        gbc.weightx = 1.0;   //request any extra vertical space
        JButton jButton = new JButton();
        jButton.setText("Export");
        jButton.setMargin(new Insets(0, 0, 0, 0));
        jButton.addActionListener(new ExportListener(this, inflationGraph));
        inflationGraph.add(jButton, gbc);

        getContentPane().add(inflationGraph);
        inflationGraph.repaint();
        setVisible(true);
    }
}
