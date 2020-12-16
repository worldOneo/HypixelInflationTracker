package de.worldoneo.inflationtracker.api;

import de.worldoneo.inflationtracker.calculator.InflationCalculator;

import java.util.List;

public class APIResponse {
    public final List<InflationCalculator.Point> data;
    public final String[] products;

    public APIResponse(List<InflationCalculator.Point> data, String[] products) {
        this.data = data;
        this.products = products;
    }
}
