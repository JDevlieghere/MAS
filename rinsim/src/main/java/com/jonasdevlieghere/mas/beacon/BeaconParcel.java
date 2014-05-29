package com.jonasdevlieghere.mas.beacon;

import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.graph.Point;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.ParcelDTO;

public class BeaconParcel extends DefaultParcel implements Beacon {

    private static final double RADIUS = 10;

    private Point pos;
    private BeaconStatus status;

    public BeaconParcel(ParcelDTO pDto) {
        super(pDto);
        this.pos = pDto.pickupLocation;
        setStatus(BeaconStatus.ACTIVE);
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

    @Override
    public BeaconStatus getStatus() {
        return this.status;
    }

    public boolean ping(){
        if(getStatus() == BeaconStatus.ACTIVE){
            setStatus(BeaconStatus.IN_AUCTION);
            return true;
        }
        return false;
    }

    public void setStatus(BeaconStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Beacon Parcel " +
                pos;
    }
}