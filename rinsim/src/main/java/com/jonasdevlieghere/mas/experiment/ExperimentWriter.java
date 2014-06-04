package com.jonasdevlieghere.mas.experiment;

import rinde.sim.pdptw.common.ObjectiveFunction;
import rinde.sim.pdptw.common.StatisticsDTO;
import rinde.sim.pdptw.experiment.Experiment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonas on 3/06/2014.
 */
public class ExperimentWriter {

    private static final String DELIMITER = " & ";

    private static final int LINE_ACCEPTED_PARCELS = 0;
    private static final int LINE_COMPUTATION_TIME = 1;
    private static final int LINE_PICKUP_TARDINESS = 2;
    private static final int LINE_DELIVR_TARDINESS = 3;
    private static final int LINE_OVER_TIME = 4;
    private static final int LINE_TOTAL_DELIVERIES = 5;
    private static final int LINE_TOTAL_DISTANCE = 6;
    private static final int LINE_TOTAL_PICKUPS = 7;
    private static final int LINE_SIMULATIN_TIME = 8;
    private static final int LINE_COST_FUNCTION = 9;
    private static final int LINE_MESSAGES = 10;

    private ArrayList<String> lines;

    public ExperimentWriter(){
        this.lines = new ArrayList<String>();
        lines.add("Accepted Parcels");
        lines.add("Computation Time");
        lines.add("Pickup Tardiness");
        lines.add("Delivery Tardiness");
        lines.add("Overtime");
        lines.add("Total Deliveries");
        lines.add("Total Distance");
        lines.add("Total Pickups");
        lines.add("Simulation Time");
        lines.add("Cost Function");
        lines.add("Total Messages");
    }

    private void addToLine(int line, Object o){
        lines.set(line, lines.get(line).concat(DELIMITER).concat(String.valueOf(o)));
    }

    public void addAll(List<Experiment.SimulationResult> simulationResults, ObjectiveFunction function, int messages){
        for(Experiment.SimulationResult simulationResult: simulationResults){
            add(simulationResult, function, messages);
        }
    }

    public void add(Experiment.SimulationResult simulationResult, ObjectiveFunction function, int messages) {
        StatisticsDTO statistics = simulationResult.stats;
        addToLine(LINE_ACCEPTED_PARCELS, statistics.acceptedParcels);
        addToLine(LINE_COMPUTATION_TIME, statistics.computationTime);
        addToLine(LINE_PICKUP_TARDINESS, statistics.pickupTardiness);
        addToLine(LINE_DELIVR_TARDINESS, statistics.deliveryTardiness);
        addToLine(LINE_OVER_TIME, statistics.overTime);
        addToLine(LINE_TOTAL_DELIVERIES, statistics.totalDeliveries);
        addToLine(LINE_TOTAL_DISTANCE, statistics.totalDistance);
        addToLine(LINE_TOTAL_PICKUPS, statistics.totalPickups);
        addToLine(LINE_SIMULATIN_TIME, statistics.simulationTime);
        addToLine(LINE_COST_FUNCTION, function.computeCost(statistics));
        addToLine(LINE_MESSAGES, messages);

    }

    public void add(String str){
        addToLine(LINE_ACCEPTED_PARCELS, str);
        addToLine(LINE_COMPUTATION_TIME, str);
        addToLine(LINE_PICKUP_TARDINESS, str);
        addToLine(LINE_DELIVR_TARDINESS, str);
        addToLine(LINE_OVER_TIME, str);
        addToLine(LINE_TOTAL_DELIVERIES, str);
        addToLine(LINE_TOTAL_DISTANCE, str);
        addToLine(LINE_TOTAL_PICKUPS, str);
        addToLine(LINE_SIMULATIN_TIME, str);
        addToLine(LINE_COST_FUNCTION, str);
        addToLine(LINE_MESSAGES, str);
    }

    public void writeTo(File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        for(String line: lines){
            fileWriter.write(line + '\n');
        }
        fileWriter.close();
    }

}
