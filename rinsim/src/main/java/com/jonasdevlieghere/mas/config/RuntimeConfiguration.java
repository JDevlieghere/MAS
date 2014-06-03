package com.jonasdevlieghere.mas.config;

import com.jonasdevlieghere.mas.strategy.SchedulingStrategy;

public class RuntimeConfiguration {

    private double beaconRadius;
    private double communicationRadius;
    private double communicationReliability;

    private boolean doExplore;
    private boolean doExchange;

    private SchedulingStrategy pickupStrategy;
    private SchedulingStrategy deliveryStrategy;

    public RuntimeConfiguration(double beaconRadius, double communicationRadius, double communicationReliability,
                                boolean doExplore, boolean doExchange, SchedulingStrategy pickupStrategy, SchedulingStrategy deliveryStrategy)
    {
        setBeaconRadius(beaconRadius);
        setCommunicationRadius(communicationRadius);
        setCommunicationReliability(communicationReliability);
        setDoExplore(doExplore);
        setDoExchange(doExchange);
        setPickupStrategy(pickupStrategy);
        setDeliveryStrategy(deliveryStrategy);
    }

    public double getBeaconRadius() {
        return beaconRadius;
    }

    public void setBeaconRadius(double beaconRadius) {
        this.beaconRadius = beaconRadius;
    }

    public double getCommunicationRadius() {
        return communicationRadius;
    }

    public void setCommunicationRadius(double communicationRadius) {
        this.communicationRadius = communicationRadius;
    }

    public double getCommunicationReliability() {
        return communicationReliability;
    }

    public void setCommunicationReliability(double communicationReliability) {
        this.communicationReliability = communicationReliability;
    }

    public boolean isDoExplore() {
        return doExplore;
    }

    public void setDoExplore(boolean doExplore) {
        this.doExplore = doExplore;
    }

    public boolean isDoExchange() {
        return doExchange;
    }

    public void setDoExchange(boolean doExchange) {
        this.doExchange = doExchange;
    }

    public SchedulingStrategy getPickupStrategy() {
        return pickupStrategy;
    }

    public void setPickupStrategy(SchedulingStrategy pickupStrategy) {
        this.pickupStrategy = pickupStrategy;
    }

    public SchedulingStrategy getDeliveryStrategy() {
        return deliveryStrategy;
    }

    public void setDeliveryStrategy(SchedulingStrategy deliveryStrategy) {
        this.deliveryStrategy = deliveryStrategy;
    }

}
