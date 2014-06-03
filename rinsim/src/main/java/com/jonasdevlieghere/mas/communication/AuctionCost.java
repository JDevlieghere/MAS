package com.jonasdevlieghere.mas.communication;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import rinde.sim.core.graph.Point;

public class AuctionCost implements Comparable<AuctionCost> {
    private BeaconTruck truck;
    private BeaconParcel parcel;

    public AuctionCost(BeaconTruck truck, BeaconParcel parcel){
        this.truck = truck;
        this.parcel = parcel;
    }

    @Override
    public int compareTo(AuctionCost o) {
        if(this.equals(o) || this.getValue() ==  o.getValue())
            return 0;
        if(this.getValue() < o.getValue())
            return -1;
        return 1;
    }

    public double getValue(){
        double result =  Point.distance(truck.getPosition(), parcel.getDestination()) ;
        result += truck.getPickupQueue().size();
        result += 0.7*truck.getNbOfParcels();
        return result;
    }

    @Override
    public String toString() {
        return "Cost: " + getValue();
    }
}
