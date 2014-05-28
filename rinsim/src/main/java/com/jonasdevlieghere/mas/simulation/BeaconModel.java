package com.jonasdevlieghere.mas.simulation;


import com.google.common.collect.ImmutableList;
import com.jonasdevlieghere.mas.beacon.Beacon;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.ModelReceiver;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.util.SupplierRng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BeaconModel implements Model<Beacon>, ModelReceiver {

    private final List<Beacon> beacons;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private PDPModel pdpModel;
    private RoadModel roadModel;

    public BeaconModel(){
        this.beacons = new CopyOnWriteArrayList<Beacon>();
    }

    private List<DeliveryTruck> getTruckBeacons() {
        final List<DeliveryTruck> trucks = new ArrayList<DeliveryTruck>();
        for (final Beacon beacon : beacons) {
            if (beacon instanceof DeliveryTruck) {
                trucks.add((DeliveryTruck) beacon);
            }
        }
        return trucks;
    }

    private List<BeaconParcel> getParcelBeacons() {
        final List<BeaconParcel> parcels = new ArrayList<BeaconParcel>();
        for (final Beacon beacon : beacons) {
            if (beacon instanceof BeaconParcel) {
                parcels.add((BeaconParcel) beacon);
            }
        }
        return parcels;
    }

    public List<BeaconParcel> getDetectableParcels(DeliveryTruck truck) {
        final List<BeaconParcel> parcels = new ArrayList<BeaconParcel>();
        for (final BeaconParcel parcel : getParcelBeacons()) {
            if(Point.distance(truck.getPosition(), parcel.getPosition()) <= truck.getRadius() + parcel.getRadius()
                    && pdpModel.getParcelState(((DefaultParcel) parcel)) == PDPModel.ParcelState.AVAILABLE)
                parcels.add((BeaconParcel) parcel);
        }
        return parcels;
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
        roadModel = modelProvider.getModel(RoadModel.class);
        final ImmutableList<Point> bounds = roadModel.getBounds();
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
