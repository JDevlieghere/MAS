package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import org.apache.commons.math3.random.RandomGenerator;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;


public class ExploreAction extends Action {

    private final RandomGenerator rand;

    public ExploreAction(RoadModel rm, PDPModel pm, DeliveryTruck truck, RandomGenerator rand) {
        super(rm, pm, null, truck);
        this.rand = rand;
    }

    @Override
    public void execute(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        final Point destination = rm.getRandomPosition(this.rand);
        DeliveryTruck truck = (DeliveryTruck)getUser();
        rm.moveTo(truck, destination, time);
        setStatus(ActionStatus.SUCCESS);
    }
}
