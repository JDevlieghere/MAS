package com.jonasdevlieghere.mas.simulation;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.config.SimulationConfiguration;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06ObjectiveFunction;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06Parser;
import com.jonasdevlieghere.mas.gendreau.BeaconGendreau06Scenario;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rinde.sim.core.Simulator;
import rinde.sim.pdptw.common.DefaultDepot;
import rinde.sim.pdptw.common.RouteRenderer;
import rinde.sim.pdptw.experiment.Experiment;
import rinde.sim.pdptw.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.scenario.ScenarioController;
import rinde.sim.ui.View;
import rinde.sim.ui.renderers.PDPModelRenderer;
import rinde.sim.ui.renderers.PlaneRoadModelRenderer;
import rinde.sim.ui.renderers.RoadUserRenderer;
import rinde.sim.ui.renderers.UiSchema;

public class BeaconSimulation {

    final static String DEFAULT_DATASET = "req_rapide_1_240_24";
    final static Logger logger = LoggerFactory.getLogger(BeaconSimulation.class);

    private BeaconSimulation() {}

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("s", true, "Testing");
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            int speedUp;
            String dataSet;
            if(cmd.hasOption("s")){
                speedUp = Integer.parseInt(cmd.getOptionValue("s"));
            }else{
                speedUp = 0;
            }
            if(cmd.hasOption("d")){
                dataSet = cmd.getOptionValue("d");
            }else{
                dataSet = DEFAULT_DATASET;
            }
            run(speedUp, dataSet);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void run(final int speedUp, String dataset) {
        final ScenarioController.UICreator uic = new ScenarioController.UICreator() {
            @Override
            public void createUI(Simulator sim) {
                final UiSchema schema = new UiSchema(false);
                schema.add(BeaconTruck.class, "/graphics/perspective/deliverytruck.png");
                schema.add(DefaultDepot.class, "/graphics/flat/warehouse-32.png");
                schema.add(BeaconParcel.class, "/graphics/perspective/deliverypackage2.png");

                final View.Builder viewBuilder = View.create(sim)
                        .with(
                                new PlaneRoadModelRenderer(),
                                new RoadUserRenderer(schema, false),
                                new RouteRenderer(),
                                new BeaconRenderer(),
                                new PDPModelRenderer(false)
                        );
                if(speedUp > 0) {
                    logger.debug("Running with speedup set to {}.", speedUp);
                    viewBuilder.enableAutoPlay().setSpeedUp(speedUp);
                }
                viewBuilder.show();
            }
        };


        final BeaconGendreau06Scenario scenario = BeaconGendreau06Parser
                .parser().addFile(BeaconSimulation.class
                                .getResourceAsStream("/data/gendreau06/" + dataset),
                        dataset)
                .allowDiversion()
                .parse().get(0);

        final Gendreau06ObjectiveFunction objFunc = new BeaconGendreau06ObjectiveFunction();
        Experiment.ExperimentResults results = Experiment
                .build(objFunc)
                .withRandomSeed(123)
                .addConfiguration(new SimulationConfiguration())
                .addScenario(scenario)
                .showGui(uic)
                .repeat(1)
                .perform();
        System.out.println("----- RESULTS -----");
        System.out.println(results.results);
    }
}