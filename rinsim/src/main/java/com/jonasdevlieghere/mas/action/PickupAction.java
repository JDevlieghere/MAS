package com.jonasdevlieghere.mas.action;


import com.google.common.base.Predicate;
import com.jonasdevlieghere.mas.common.TickStatus;
import com.jonasdevlieghere.mas.beacon.*;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.RoadModels;
import rinde.sim.core.model.road.RoadUser;
import rinde.sim.pdptw.common.DefaultParcel;

public class PickupAction extends Action {

    public PickupAction(RoadModel rm, PDPModel pm, BeaconTruck truck) {
        super(rm, pm, null, truck);
    }

    @Override
    public void execute(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();
        final DefaultParcel nearest = getNearestParcel();
        BeaconTruck truck = (BeaconTruck)getUser();

        if (nearest != null && rm.equalPosition(nearest, truck)
                && pm.getTimeWindowPolicy().canPickup(nearest.getPickupTimeWindow(),
                time.getTime(), nearest.getPickupDuration()) && truck.getPickupQueue().contains(nearest)) {
            final double newSize = getPDPModel().getContentsSize(truck)
                    + nearest.getMagnitude();
            if (newSize <= truck.getCapacity()) {
                pm.pickup(truck, nearest, time);
                BeaconParcel beaconParcel = (BeaconParcel) nearest;
                beaconParcel.setBeaconStatus(BeaconStatus.INACTIVE);
                truck.unqueuePickup(beaconParcel);
                setStatus(TickStatus.END_TICK);
            }
        }
    }

    private DefaultParcel getNearestParcel(){
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();
        BeaconTruck truck = (BeaconTruck)getUser();

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
