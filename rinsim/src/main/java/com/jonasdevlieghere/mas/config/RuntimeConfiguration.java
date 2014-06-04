package com.jonasdevlieghere.mas.config;

import com.jonasdevlieghere.mas.strategy.SchedulingStrategy;

public class RuntimeConfiguration {

    private String title;

    private double beaconRadius;
    private double communicationRadius;
    private double communicationReliability;

    private boolean doExplore;
    private boolean doExchange;

    private Class<SchedulingStrategy> pickupStrategy;
    private Class<SchedulingStrategy> deliveryStrategy;

    public <T extends SchedulingStrategy, Y extends SchedulingStrategy> RuntimeConfiguration(String title, double beaconRadius, double communicationRadius,  double communicationReliability,
                                Class<T> pickupStrategy, Class<Y> deliveryStrategy,
                                boolean doExchange, boolean doExplore)
    {
        setTitle(title);
        setBeaconRadius(beaconRadius);
        setCommunicationRadius(communicationRadius);
        setCommunicationReliability(communicationReliability);
        setDoExchange(doExchange);
        setDoExplore(doExplore);
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
        try {
            return pickupStrategy.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T extends SchedulingStrategy> void setPickupStrategy(Class<T> pickupStrategy) {
        this.pickupStrategy = (Class< SchedulingStrategy>)pickupStrategy;
    }

    public SchedulingStrategy getDeliveryStrategy() {
        try {
            return deliveryStrategy.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T extends SchedulingStrategy> void setDeliveryStrategy(Class<T> deliveryStrategy) {
        this.deliveryStrategy = (Class< SchedulingStrategy>)deliveryStrategy;
    }

    @Override
    public String toString() {
        return "RuntimeConfiguration("+getTitle()+"){" +
                "beaconRadius=" + beaconRadius +
                "\n, communicationRadius=" + communicationRadius +
                "\n, communicationReliability=" + communicationReliability +
                "\n, doExplore=" + doExplore +
                "\n, doExchange=" + doExchange +
                "\n, pickupStrategy=" + pickupStrategy +
                "\n, deliveryStrategy=" + deliveryStrategy +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
