package com.jonasdevlieghere.mas.beacon;

import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.ParcelDTO;

public class BeaconParcel extends DefaultParcel implements Beacon {

    private static final double DEFAULT_RADIUS = 0.7;
    private double radius;

    private Point pos;
    private BeaconStatus status;

    public BeaconParcel(ParcelDTO pDto) {
        super(pDto);
        this.pos = pDto.pickupLocation;
        setBeaconStatus(BeaconStatus.ACTIVE);
        this.radius = DEFAULT_RADIUS;
    }

    public BeaconParcel(ParcelDTO pDto, double radius) {
        this(pDto);
        setRadius(radius);
    }

    @Override
    public void setModel(BeaconModel model) {}

    @Override
    public double getBeaconRadius() {
        return radius;
    }

    @Override
    public Point getPosition() {
        return this.pos;
    }

    @Override
    public BeaconStatus getBeaconStatus() {
        return this.status;
    }

    @Override
    public boolean ping(){
        if(getBeaconStatus() == BeaconStatus.ACTIVE){
            setBeaconStatus(BeaconStatus.SLAVE);
            return true;
        }
        return false;
    }

    public void setBeaconStatus(BeaconStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Beacon Parcel " +
                pos;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public boolean canBeDelivered(Vehicle v, long time) {
        long destinationTime = getDestinationTime((BeaconTruck) v, time);
        return getPDPModel().getTimeWindowPolicy().canDeliver(this.getDeliveryTimeWindow(), time, destinationTime);
    }

    @Override
    public boolean canBePickedUp(Vehicle v, long time) {
        long originTime = getOriginTime((BeaconTruck) v, time);
        return getPDPModel().getTimeWindowPolicy().canPickup(this.getPickupTimeWindow(), time, originTime);
    }

    public long getOriginTime(BeaconTruck truck, long currentTime){
        double distance = Point.distance(truck.getPosition(), this.getPosition());
        double speed = truck.getSpeed();
        return currentTime + (long)(distance/speed) + this.getPickupDuration();
    }

    public long getDestinationTime(BeaconTruck truck, long currentTime){
        double distance = Point.distance(truck.getPosition(), this.getDestination());
        double speed = truck.getSpeed();
        return currentTime + (long)(distance/speed) + this.getPickupDuration();
    }


}