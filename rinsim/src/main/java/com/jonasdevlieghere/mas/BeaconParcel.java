package com.jonasdevlieghere.mas;

import rinde.sim.core.graph.Point;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.ParcelDTO;

public class BeaconParcel extends DefaultParcel implements Beacon {

    private Point pos;
    private boolean auctioneerDesignated = false;

    public BeaconParcel(ParcelDTO pDto) {
        super(pDto);
        this.pos = pDto.pickupLocation;
    }

    @Override
    public void setModel(BeaconModel model) {}

    @Override
    public double getRadius() {
        return 1;
    }

    @Override
    public Point getPosition() {
        return this.pos;
    }

    public boolean ping(){
        if(!auctioneerDesignated){
            auctioneerDesignated = true;
            return true;
        }
        return false;
    }
}