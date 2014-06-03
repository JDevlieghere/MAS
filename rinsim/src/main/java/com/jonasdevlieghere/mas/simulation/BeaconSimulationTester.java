package com.jonasdevlieghere.mas.simulation;

import com.google.common.collect.ImmutableList;
import com.jonasdevlieghere.mas.config.RuntimeConfiguration;
import com.jonasdevlieghere.mas.config.SimulationConfiguration;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06ObjectiveFunction;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06Parser;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06Scenario;
import com.jonasdevlieghere.mas.strategy.delivery.NearestDeliveryStrategy;
import com.jonasdevlieghere.mas.strategy.pickup.NearestPickupStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rinde.sim.pdptw.experiment.Experiment;
import rinde.sim.pdptw.gendreau06.Gendreau06ObjectiveFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeaconSimulationTester {

    final static Logger logger = LoggerFactory.getLogger(BeaconSimulationTester.class);


    public static final List<String> GENDREAU = Arrays.asList(
            "req_rapide_1_240_24",
            "req_rapide_1_240_33",
            "req_rapide_1_450_24",
            "req_rapide_2_240_24",
            "req_rapide_2_240_33",
            "req_rapide_2_450_24",
            "req_rapide_3_240_24",
            "req_rapide_3_240_33",
            "req_rapide_3_450_24",
            "req_rapide_4_240_24",
            "req_rapide_4_240_33",
            "req_rapide_4_450_24",
            "req_rapide_5_240_24",
            "req_rapide_5_240_33",
            "req_rapide_5_450_24"
    );

    public static final List<RuntimeConfiguration> CONFIGURATIONS = Arrays.asList(
            new RuntimeConfiguration(0.5,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, true, true),
            new RuntimeConfiguration(1,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, true, true),
            new RuntimeConfiguration(1.5,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, true, true),
            new RuntimeConfiguration(2,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, true, true)
    );

    public static void main(String[] args){
        ArrayList<RuntimeConfiguration> runtimeConfigurations = new ArrayList<RuntimeConfiguration>(CONFIGURATIONS);
        ArrayList<String> datasets = new ArrayList<String>(GENDREAU);
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
                try{
                    logger.info(runtimeConfiguration.toString());
                    logger.info(runExperiment(runtimeConfiguration, dataset).toString());
                }catch (Exception e){
                    logger.error(e.toString());
                }
            }
        }
    }

    ImmutableList<Experiment.SimulationResult> runExperiment(RuntimeConfiguration runtimeConfiguration, String dataset){
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
                .perform().results;
    }

}
