package com.jonasdevlieghere.mas;

import rinde.sim.core.graph.Point;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.ParcelDTO;

public class BeaconParcel extends DefaultParcel implements Beacon {

    private Point pos;

    public BeaconParcel(ParcelDTO pDto) {
        super(pDto);
        this.pos = pDto.pickupLocation;
    }

    @Override
    public void setModel(BeaconModel model) {}

    @Override
    public Point getPosition() {
        return this.pos;
    }
}
