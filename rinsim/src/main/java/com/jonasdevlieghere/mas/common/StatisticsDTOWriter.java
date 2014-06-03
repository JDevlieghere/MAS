package com.jonasdevlieghere.mas.common;

import rinde.sim.pdptw.common.StatisticsDTO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class StatisticsDTOWriter {

    private ArrayList<String> statisticsLines;

    StatisticsDTOWriter(File file){

    }

    private void appendResults(StatisticsDTO stats) throws IOException {
        if(statisticsLines.size() ==0){
            statisticsLines.add("Accepted Parcels");
            statisticsLines.add("Computation Time");
            statisticsLines.add("Pickup Tardiness");
            statisticsLines.add("Delivery Tardiness");
            statisticsLines.add("Overtime");
            statisticsLines.add("Total Deliveries");
            statisticsLines.add("Total Distance");
            statisticsLines.add("Total Pickups");
            statisticsLines.add("Simulation Time");
        }
        statisticsLines.set(0,statisticsLines.get(0)  + " & " + stats.acceptedParcels);
        statisticsLines.set(1,statisticsLines.get(1)  + " & " + stats.computationTime);
        statisticsLines.set(2,statisticsLines.get(2)  + " & " + stats.pickupTardiness);
        statisticsLines.set(3,statisticsLines.get(3)  + " & " + stats.deliveryTardiness);
        statisticsLines.set(4,statisticsLines.get(4)  + " & " + stats.overTime);
        statisticsLines.set(5,statisticsLines.get(5)  + " & " + stats.totalDeliveries);
        statisticsLines.set(6,statisticsLines.get(6)  + " & " + stats.totalDistance);
        statisticsLines.set(7,statisticsLines.get(7)  + " & " + stats.totalPickups);
        statisticsLines.set(8,statisticsLines.get(8)  + " & " + stats.simulationTime);
    }

    public void finalizeFile(File file, String caption, String label) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        for(int i=0; i<statisticsLines.size(); i++){
            statisticsLines.set(i, statisticsLines.get(i) + "\\\\");
        }
        for(String str : statisticsLines){
            fileWriter.write(str);
        }
        fileWriter.close();
    }
}