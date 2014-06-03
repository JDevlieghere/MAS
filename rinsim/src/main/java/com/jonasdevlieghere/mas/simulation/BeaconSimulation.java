package com.jonasdevlieghere.mas.simulation;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rinde.sim.core.Simulator;
import rinde.sim.pdptw.common.DefaultDepot;
import rinde.sim.pdptw.common.RouteRenderer;
import rinde.sim.pdptw.experiment.Experiment;
import rinde.sim.pdptw.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.pdptw.gendreau06.TripleDJGendreau06Parser;
import rinde.sim.pdptw.gendreau06.TripleDJGendreau06Scenario;
import rinde.sim.scenario.ScenarioController;
import rinde.sim.ui.View;
import rinde.sim.ui.renderers.PDPModelRenderer;
import rinde.sim.ui.renderers.PlaneRoadModelRenderer;
import rinde.sim.ui.renderers.RoadUserRenderer;
import rinde.sim.ui.renderers.UiSchema;

public class BeaconSimulation {

    final static Logger logger = LoggerFactory.getLogger(BeaconSimulation.class);

    private BeaconSimulation() {}

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("t", false, "Testing");
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if(cmd.hasOption("t")){
                run(true);
            }else{
                run(false);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void run(final boolean testing) {
        logger.debug("Running with testing mode set to {}", testing);
        final ScenarioController.UICreator uic = new ScenarioController.UICreator() {
            @Override
            public void createUI(Simulator sim) {
                final UiSchema schema = new UiSchema(false);
                schema.add(DeliveryTruck.class, "/graphics/perspective/deliverytruck.png");
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
                if (testing) {
                    viewBuilder.enableAutoClose().enableAutoPlay().setSpeedUp(64);
                }
                viewBuilder.show();
            }
        };

        final TripleDJGendreau06Scenario scenario = TripleDJGendreau06Parser
                .parser().addFile(BeaconSimulation.class
                                .getResourceAsStream("/data/gendreau06/req_rapide_1_240_24"),
                        "req_rapide_1_240_24")
                .allowDiversion()
                .parse().get(0);

        final Gendreau06ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();
        Experiment
                .build(objFunc)
                .withRandomSeed(123)
                .addConfiguration(new BeaconConfiguration())
                .addScenario(scenario)
                .showGui(uic)
                .repeat(1)
                .perform();
    }
}