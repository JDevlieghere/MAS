package com.jonasdevlieghere.mas.config;

import com.jonasdevlieghere.mas.strategy.SchedulingStrategy;

public class RuntimeConfiguration {

    private String title;

    private double beaconRadius;
    private double communicationRadius;
    private double communicationReliability;

    private boolean doExchange;

    private Class<SchedulingStrategy> pickupStrategy;
    private Class<SchedulingStrategy> deliveryStrategy;

    /**
     * Create a new RuntimeConfiguration
     *  @param   title
     *          The title of the configuration
     * @param   beaconRadius
     *          The beacon radius of the Beacons
     * @param   communicationReliability
     *          The communication reliability of the BeaconTruck
     * @param   pickupStrategy
     *          The SchedulingStrategy used for pick ups
     * @param   deliveryStrategy
     *          The SchedulingStrategy used for deliveries
     * @param   doExchange
     *          Whether BeaconTruck should perform Exchanges
     */
    public <T extends SchedulingStrategy, Y extends SchedulingStrategy> RuntimeConfiguration(String title, double beaconRadius, double communicationReliability,
                                                                                             Class<T> pickupStrategy, Class<Y> deliveryStrategy,
                                                                                             boolean doExchange)
    {
        setTitle(title);
        setBeaconRadius(beaconRadius);
        setCommunicationRadius((double) 10);
        setCommunicationReliability(communicationReliability);
        setDoExchange(doExchange);
        setPickupStrategy(pickupStrategy);
        setDeliveryStrategy(deliveryStrategy);
    }

    public double getBeaconRadius() {
        return beaconRadius;
    }

    void setBeaconRadius(double beaconRadius) {
        this.beaconRadius = beaconRadius;
    }

    public double getCommunicationRadius() {
        return communicationRadius;
    }

    void setCommunicationRadius(double communicationRadius) {
        this.communicationRadius = communicationRadius;
    }

    public double getCommunicationReliability() {
        return communicationReliability;
    }

    void setCommunicationReliability(double communicationReliability) {
        this.communicationReliability = communicationReliability;
    }

    public boolean isDoExchange() {
        return doExchange;
    }

    void setDoExchange(boolean doExchange) {
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

    <T extends SchedulingStrategy> void setPickupStrategy(Class<T> pickupStrategy) {
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

    <T extends SchedulingStrategy> void setDeliveryStrategy(Class<T> deliveryStrategy) {
        this.deliveryStrategy = (Class< SchedulingStrategy>)deliveryStrategy;
    }

    @Override
    public String toString() {
        return "RuntimeConfiguration("+getTitle()+"){" +
                "beaconRadius=" + beaconRadius +
                "\n, communicationRadius=" + communicationRadius +
                "\n, communicationReliability=" + communicationReliability +
                "\n, doExchange=" + doExchange +
                "\n, pickupStrategy=" + pickupStrategy +
                "\n, deliveryStrategy=" + deliveryStrategy +
                '}';
    }

    public String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }
}
