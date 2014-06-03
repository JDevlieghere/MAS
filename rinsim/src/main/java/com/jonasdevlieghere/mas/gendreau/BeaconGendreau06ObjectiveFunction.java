package com.jonasdevlieghere.mas.gendreau;

import rinde.sim.pdptw.common.StatisticsDTO;
import rinde.sim.pdptw.gendreau06.Gendreau06ObjectiveFunction;

public class BeaconGendreau06ObjectiveFunction extends Gendreau06ObjectiveFunction {

    @Override
    public boolean isValidResult(StatisticsDTO stats) {
        return stats.totalParcels == stats.acceptedParcels
                && stats.totalParcels == stats.totalPickups
                && stats.totalParcels == stats.totalDeliveries && stats.simFinish;
    }

}
