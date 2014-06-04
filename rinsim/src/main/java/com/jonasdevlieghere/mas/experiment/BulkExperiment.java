package com.jonasdevlieghere.mas.experiment;

import com.jonasdevlieghere.mas.config.RuntimeConfiguration;
import com.jonasdevlieghere.mas.config.SimulationConfiguration;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06ObjectiveFunction;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06Parser;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06Scenario;
import com.jonasdevlieghere.mas.simulation.BeaconSimulation;
import com.jonasdevlieghere.mas.strategy.delivery.NearestDeliveryStrategy;
import com.jonasdevlieghere.mas.strategy.pickup.NearestPickupStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rinde.sim.pdptw.experiment.Experiment;
import rinde.sim.pdptw.gendreau06.Gendreau06ObjectiveFunction;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BulkExperiment {

    final static Logger logger = LoggerFactory.getLogger(BulkExperiment.class);


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
            new RuntimeConfiguration("Rad_0_5",0.5,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, true, true),
            new RuntimeConfiguration("Rad_1",1,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, true, true),
            new RuntimeConfiguration("Rad_1_5",1.5,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, true, true),
            new RuntimeConfiguration("Rad_2",2,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, true, true)
    );

    public static void main(String[] args){
        ArrayList<RuntimeConfiguration> runtimeConfigurations = new ArrayList<RuntimeConfiguration>(CONFIGURATIONS);
        ArrayList<String> datasets = new ArrayList<String>(GENDREAU);
        BulkExperiment tester = new BulkExperiment(runtimeConfigurations, datasets);
        tester.run();
    }

    private List<RuntimeConfiguration> runtimeConfigurations;
    private List<String> datasets;

    public BulkExperiment(List<RuntimeConfiguration> runtimeConfigurations, List<String> datasets){
        this.runtimeConfigurations = runtimeConfigurations;
        this.datasets = datasets;
    }

    public void run(){
        int i = 0;
        for(RuntimeConfiguration runtimeConfiguration: runtimeConfigurations){
            ExperimentWriter experimentWriter = new ExperimentWriter();
            for(String dataset: datasets){
                try {
                    Experiment.ExperimentResults r = runExperiment(runtimeConfiguration, dataset);
                    experimentWriter.addAll(r.results, r.objectiveFunction);
                }catch (RuntimeException e){
                    experimentWriter.add("/");
                }
            }
            File file = new File("output/"+(runtimeConfiguration.getTitle())+".dat");
            try {
                experimentWriter.writeTo(file);
                logger.info("Output file {} created.", file.getName());
            } catch (IOException e) {
                e.printStackTrace();
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
