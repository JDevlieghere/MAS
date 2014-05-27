package com.jonasdevlieghere.mas;

import rinde.sim.pdptw.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.pdptw.gendreau06.Gendreau06Parser;
import rinde.sim.pdptw.gendreau06.Gendreau06Scenario;

public class BeaconSimulation {

    final Gendreau06Scenario scenario = Gendreau06Parser
            .parser().addFile(BeaconSimulation.class.getResourceAsStream("/data/gendreau06/req_rapide_1_240_24"), "req_rapide_1_240_24")
            .allowDiversion()
            .parse().get(0);
    final Gendreau06ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();

    public static void main(String[] args) {

    }


}