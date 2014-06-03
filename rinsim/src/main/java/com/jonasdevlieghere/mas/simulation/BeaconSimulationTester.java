package com.jonasdevlieghere.mas.simulation;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.config.RuntimeConfiguration;
import com.jonasdevlieghere.mas.config.SimulationConfiguration;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06ObjectiveFunction;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06Parser;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06Scenario;
import com.jonasdevlieghere.mas.strategy.delivery.NearestDeliveryStrategy;
import com.jonasdevlieghere.mas.strategy.pickup.NearestPickupStrategy;
import rinde.sim.pdptw.experiment.Experiment;
import rinde.sim.pdptw.gendreau06.Gendreau06ObjectiveFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonas on 03/06/14.
 */
public class BeaconSimulationTester {

    public static void main(String[] args){
        ArrayList<RuntimeConfiguration> runtimeConfigurations = new ArrayList<RuntimeConfiguration>();
        ArrayList<String> datasets = new ArrayList<String>();
        datasets.add("req_rapide_1_240_24");
        runtimeConfigurations.add(new RuntimeConfiguration(1,10,1,true, true, new NearestPickupStrategy(), new NearestDeliveryStrategy()));
        BeaconSimulationTester tester = new BeaconSimulationTester(runtimeConfigurations, datasets);
        tester.run();
    }

    private List<RuntimeConfiguration> runtimeConfigurations;
    private List<String> datasets;

    public BeaconSimulationTester(List<RuntimeConfiguration> runtimeConfigurations, List<String> datasets){
        this.runtimeConfigurations = runtimeConfigurations;
        this.datasets = datasets;
    }

    public void run(){
        for(RuntimeConfiguration runtimeConfiguration: runtimeConfigurations){
            for(String dataset: datasets){
                System.out.println(runtimeConfiguration);
                System.out.println(runExperiment(runtimeConfiguration, dataset));
            }
        }
    }

    Experiment.ExperimentResults runExperiment(RuntimeConfiguration runtimeConfiguration, String dataset){

        final BeaconGendreau06Scenario scenario = BeaconGendreau06Parser
                .parser().addFile(BeaconSimulation.class
                                .getResourceAsStream("/data/gendreau06/" + dataset),
                        dataset)
                .allowDiversion()
                .parse().get(0);

        final Gendreau06ObjectiveFunction objFunc = new BeaconGendreau06ObjectiveFunction();
        return Experiment
                .build(objFunc)
                .withRandomSeed(123)
                .addConfiguration(new SimulationConfiguration(runtimeConfiguration))
                .addScenario(scenario)
                .repeat(1)
                .perform();
    }

}
