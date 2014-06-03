package com.jonasdevlieghere.mas.simulation;

import rinde.sim.pdptw.common.DynamicPDPTWProblem;

public class BeaconStopCondition extends DynamicPDPTWProblem.StopCondition {
    @Override
    public boolean isSatisfiedBy(DynamicPDPTWProblem.SimulationInfo context) {
        return  context.stats.movedVehicles > 0
                && context.stats.totalParcels == context.stats.totalDeliveries
                && context.stats.acceptedParcels > 0;
    }
}
