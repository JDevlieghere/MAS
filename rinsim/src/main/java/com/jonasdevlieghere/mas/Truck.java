package com.jonasdevlieghere.mas;

import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.DefaultVehicle;
import rinde.sim.pdptw.common.VehicleDTO;

import javax.annotation.Nullable;

public class Truck extends DefaultVehicle {

    public Truck(VehicleDTO pDto) {
        super(pDto);
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = roadModel.get();
        final PDPModel pm = pdpModel.get();
        final Parcel delivery = getDelivery(time, 5);
    }

    @Nullable
    public Parcel getDelivery(TimeLapse time, int distance) {
        Parcel target = null;
        double closest = distance;
        final PDPModel pm = pdpModel.get();
        for (final Parcel p : pm.getContents(this)) {
            final double dist = Point.distance(roadModel.get().getPosition(this), p.getDestination());
            if (dist < closest && pm.getTimeWindowPolicy().canDeliver(p.getDeliveryTimeWindow(), time.getTime(), p.getPickupDuration())) {
                closest = dist;
                target = p;
            }
        }
        return target;
    }
}
