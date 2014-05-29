package com.jonasdevlieghere.mas.action;


import com.google.common.base.Predicate;
import com.jonasdevlieghere.mas.beacon.*;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.RoadModels;
import rinde.sim.core.model.road.RoadUser;
import rinde.sim.pdptw.common.DefaultParcel;

public class PickupAction extends Action {

    public PickupAction(RoadModel rm, PDPModel pm, DeliveryTruck truck) {
        super(rm, pm, null, truck);
    }

    @Override
    public void execute(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();
        final DefaultParcel nearest = getNearestParcel();
        DeliveryTruck truck = (DeliveryTruck)getUser();

        if (nearest != null && rm.equalPosition(nearest, truck)
                && pm.getTimeWindowPolicy().canPickup(nearest.getPickupTimeWindow(),
                time.getTime(), nearest.getPickupDuration()) && truck.getPickupQueue().contains(nearest)) {
            final double newSize = getPDPModel().getContentsSize(truck)
                    + nearest.getMagnitude();
            if (newSize <= truck.getCapacity()) {
                pm.pickup(truck, nearest, time);
                truck.unqueuePickup((BeaconParcel) nearest);
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
        DeliveryTruck truck = (DeliveryTruck)getUser();

        return (DefaultParcel) RoadModels.findClosestObject(
                rm.getPosition(truck), rm, new Predicate<RoadUser>() {
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
