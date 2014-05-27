package com.jonasdevlieghere.mas;


import com.google.common.collect.ImmutableList;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.ModelReceiver;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.util.SupplierRng;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BeaconModel implements Model<Beacon>, ModelReceiver {

    private final List<Beacon> beacons;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private PDPModel pdpModel;

    public BeaconModel(){
        this.beacons = new CopyOnWriteArrayList<Beacon>();
    }

    @Override
    public boolean register(Beacon beacon) {
        beacons.add(beacon);
        beacon.setModel(this);
        return true;
    }

    @Override
    public boolean unregister(Beacon beacon) {
        beacons.remove(beacon);
        return false;
    }

    @Override
    public Class<Beacon> getSupportedType() {
        return Beacon.class;
    }

    @Override
    public void registerModelProvider(ModelProvider modelProvider) {
        pdpModel = modelProvider.getModel(PDPModel.class);
        final ImmutableList<Point> bounds = modelProvider.getModel(RoadModel.class).getBounds();
        minX = bounds.get(0).x;
        maxX = bounds.get(1).x;
        minY = bounds.get(0).y;
        maxY = bounds.get(1).y;
    }

    public static SupplierRng<BeaconModel> supplier() {
        return new SupplierRng.DefaultSupplierRng<BeaconModel>() {
            @Override
            public BeaconModel get(long seed) {
                return new BeaconModel();
            }
        };
    }
}
