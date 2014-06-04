package com.jonasdevlieghere.mas.experiment;

import com.jonasdevlieghere.mas.communication.MessageStore;
import com.jonasdevlieghere.mas.config.RuntimeConfiguration;
import com.jonasdevlieghere.mas.config.SimulationConfiguration;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06ObjectiveFunction;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06Parser;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06Scenario;
import com.jonasdevlieghere.mas.simulation.BeaconSimulation;
import com.jonasdevlieghere.mas.strategy.delivery.EarliestDeadlineStrategy;
import com.jonasdevlieghere.mas.strategy.delivery.NearestDeliveryStrategy;
import com.jonasdevlieghere.mas.strategy.delivery.NearestOnTimeDeliveryStrategy;
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

    public static final List<RuntimeConfiguration> RADIUS_CONFIGURATIONS = Arrays.asList(
            new RuntimeConfiguration("Radius010",0.1,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false),
            new RuntimeConfiguration("Radius025",0.25,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false),
            new RuntimeConfiguration("Radius050",0.5,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false),
            new RuntimeConfiguration("Radius075",0.75,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false),
            new RuntimeConfiguration("Radius100",1,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false),
            new RuntimeConfiguration("Radius125",1.25,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false),
            new RuntimeConfiguration("Radius150",1.50,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false),
            new RuntimeConfiguration("Radius175",1.75,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false),
            new RuntimeConfiguration("Radius200",2,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false),
            new RuntimeConfiguration("Radius225",2.25,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false),
            new RuntimeConfiguration("Radius250",2.50,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false),
            new RuntimeConfiguration("Radius275",2.75,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false),
            new RuntimeConfiguration("Radius300",3.00,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false)
    );

    public static final List<RuntimeConfiguration> EXCHANGE_CONFIGURATIONS = Arrays.asList(
            new RuntimeConfiguration("Exchange",1,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, true),
            new RuntimeConfiguration("NoExchange",1,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, false)
    );

    public static final List<RuntimeConfiguration> STRATEGY_CONFIGURATIONS = Arrays.asList(
            new RuntimeConfiguration("NearestDeliveryStrategy",1,10,1, NearestPickupStrategy.class, NearestDeliveryStrategy.class, true),
            new RuntimeConfiguration("EarliestDeadlineStrategy",1,10,1, NearestPickupStrategy.class, EarliestDeadlineStrategy.class, true),
            new RuntimeConfiguration("NearestOnTimeDeliveryStrategy",1,10,1, NearestPickupStrategy.class, NearestOnTimeDeliveryStrategy.class, true)
    );


    public static void main(String[] args){
        ArrayList<RuntimeConfiguration> runtimeConfigurations = new ArrayList<RuntimeConfiguration>();
        ArrayList<String> datasets = new ArrayList<String>(GENDREAU);

//        runtimeConfigurations.addAll(RADIUS_CONFIGURATIONS);
        runtimeConfigurations.addAll(STRATEGY_CONFIGURATIONS);
//        runtimeConfigurations.addAll(EXCHANGE_CONFIGURATIONS);

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
        long startTime = System.currentTimeMillis();
        for(RuntimeConfiguration runtimeConfiguration: runtimeConfigurations){
            ExperimentWriter experimentWriter = new ExperimentWriter();
            logger.info("Testing configuration {} \n {}.", runtimeConfiguration.getTitle(), runtimeConfiguration.toString());
            for(String dataset: datasets){
                try {
                    Experiment.ExperimentResults r = runExperiment(runtimeConfiguration, dataset);
                    experimentWriter.addAll(r.results, r.objectiveFunction, MessageStore.retreiveNbOfMessages());
                }catch (RuntimeException e){
                    experimentWriter.add("/");
                }
            }
            File file = new File("output/"+(runtimeConfiguration.getTitle())+".csv");
            try {
                file.getParentFile().mkdirs();
                experimentWriter.writeTo(file);
                logger.info("Output file {} created.", file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        logger.info("Total run time of experiments: {}", totalTime);
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
