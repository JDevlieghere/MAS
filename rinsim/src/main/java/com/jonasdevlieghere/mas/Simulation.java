package com.jonasdevlieghere.mas;

import rinde.sim.core.Simulator;
import rinde.sim.pdptw.common.DefaultDepot;
import rinde.sim.pdptw.common.RouteRenderer;
import rinde.sim.pdptw.experiment.Experiment;
import rinde.sim.pdptw.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.pdptw.gendreau06.Gendreau06Parser;
import rinde.sim.pdptw.gendreau06.Gendreau06Scenario;
import rinde.sim.scenario.ScenarioController.UICreator;

public class Simulation {

    final Gendreau06Scenario scenario = Gendreau06Parser
            .parser().addFile(Simulation.class.getResourceAsStream("/data/gendreau06/req_rapide_1_240_24"), "req_rapide_1_240_24")
            .allowDiversion()
            .parse().get(0);
    final Gendreau06ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();

    public static void main(String[] args) {

    }


}