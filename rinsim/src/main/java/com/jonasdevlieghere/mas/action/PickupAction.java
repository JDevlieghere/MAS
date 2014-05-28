package com.jonasdevlieghere.mas.action;


import com.google.common.base.Predicate;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.RoadModels;
import rinde.sim.core.model.road.RoadUser;
import rinde.sim.pdptw.common.DefaultParcel;

import java.util.Set;

public class PickupAction extends Action {

    public PickupAction(RoadModel rm, PDPModel pm, DeliveryTruck truck) {
        super(rm, pm, null, truck);
    }

    @Override
    public void execute(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();
        final DefaultParcel nearest = getNearestParcel();

        if (nearest != null && rm.equalPosition(nearest, getTruck())
                && pm.getTimeWindowPolicy().canPickup(nearest.getPickupTimeWindow(),
                time.getTime(), nearest.getPickupDuration()) && getTruck().getPickupQueue().contains(nearest)) {
            final double newSize = getPDPModel().getContentsSize(getTruck())
                    + nearest.getMagnitude();
            if (newSize <= getTruck().getCapacity()) {
                pm.pickup(getTruck(), nearest, time);
                getTruck().unqueuePickup((BeaconParcel) nearest);
                setStatus(ActionStatus.SUCCESS);
            }else{
                setStatus(ActionStatus.FAILURE);
            }
        }else{
            setStatus(ActionStatus.FAILURE);
        }
    }

    private DefaultParcel getNearestParcel(){
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();

        return (DefaultParcel) RoadModels.findClosestObject(
                rm.getPosition(getTruck()), rm, new Predicate<RoadUser>() {
                    @Override
                    public boolean apply(RoadUser input) {
                        return input instanceof DefaultParcel
                                && pm.getParcelState(((DefaultParcel) input)) == PDPModel.ParcelState.AVAILABLE;
                    }
                }
        );
    }

    @Override
    public String toString(){
        return "PickupAction [" + this.getStatus() + "]";
    }

}
