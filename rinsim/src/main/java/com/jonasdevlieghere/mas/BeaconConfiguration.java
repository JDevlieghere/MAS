package com.jonasdevlieghere.mas;

import rinde.sim.pdptw.common.AddVehicleEvent;
import rinde.sim.pdptw.common.DynamicPDPTWProblem;
import rinde.sim.pdptw.experiment.DefaultMASConfiguration;


public class BeaconConfiguration extends DefaultMASConfiguration {

    @Override
    public DynamicPDPTWProblem.Creator<AddVehicleEvent> getVehicleCreator() {
        return null;
    }
}
