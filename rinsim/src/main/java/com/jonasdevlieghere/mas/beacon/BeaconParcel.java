package com.jonasdevlieghere.mas.beacon;

import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.graph.Point;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.ParcelDTO;

public class BeaconParcel extends DefaultParcel implements Beacon {

    private static final double RADIUS = 0.5;

    private Point pos;
    private boolean hasAuctioneer = false;

    public BeaconParcel(ParcelDTO pDto) {
        super(pDto);
        this.pos = pDto.pickupLocation;
    }

    @Override
    public void setModel(BeaconModel model) {}

    @Override
    public double getRadius() {
        return RADIUS;
    }

    @Override
    public Point getPosition() {
        return this.pos;
    }

    public boolean ping(){
        if(!hasAuctioneer()){
            setHasAuctioneer(true);
            return true;
        }
        return false;
    }

    private void setHasAuctioneer(boolean value){
        this.hasAuctioneer = value;
    }

    public boolean hasAuctioneer(){
        return this.hasAuctioneer;
    }
}