package com.jonasdevlieghere.mas.communication;

import com.jonasdevlieghere.mas.beacon.BeaconTruck;

public class AuctionCost implements Comparable<AuctionCost> {
    private final BeaconTruck truck;

    public AuctionCost(BeaconTruck truck){
        this.truck = truck;
    }

    @Override
    public int compareTo(AuctionCost o) {
        if(this.equals(o) || this.getValue() ==  o.getValue())
            return 0;
        if(this.getValue() < o.getValue())
            return -1;
        return 1;
    }

    double getValue(){
        double result = truck.getPickupQueue().size();
        result += 0.7*truck.getNbOfParcels();
        return result;
    }

    @Override
    public String toString() {
        return "Cost: " + getValue();
    }
}
