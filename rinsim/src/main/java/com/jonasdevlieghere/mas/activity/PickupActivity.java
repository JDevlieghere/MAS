package com.jonasdevlieghere.mas.activity;


import com.google.common.base.Predicate;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconStatus;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.TickStatus;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.RoadModels;
import rinde.sim.core.model.road.RoadUser;
import rinde.sim.pdptw.common.DefaultParcel;

public class PickupActivity extends Activity {

    public PickupActivity(BeaconTruck truck) {
        super(truck);
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time) {
        setActivityStatus(TickStatus.NORMAL);
        final DefaultParcel nearest = getNearestParcel(rm, pm);
        BeaconTruck truck = (BeaconTruck)getUser();

        if (nearest != null && rm.equalPosition(nearest, truck)
                && pm.getTimeWindowPolicy().canPickup(nearest.getPickupTimeWindow(),
                time.getTime(), nearest.getPickupDuration()) && truck.getPickupQueue().contains(nearest)) {
            final double newSize = pm.getContentsSize(truck)
                    + nearest.getMagnitude();
            if (newSize <= truck.getCapacity()) {
                pm.pickup(truck, nearest, time);
                BeaconParcel beaconParcel = (BeaconParcel) nearest;
                beaconParcel.setBeaconStatus(BeaconStatus.INACTIVE);
                truck.unqueuePickup(beaconParcel);
                setActivityStatus(TickStatus.END_TICK);
            }
        }
    }

    private DefaultParcel getNearestParcel(RoadModel rm, final PDPModel pm){
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
        return "PickupActivity [" + this.getStatus() + "]";
    }

}
