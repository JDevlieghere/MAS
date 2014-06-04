package com.jonasdevlieghere.mas.beacon;

import com.jonasdevlieghere.mas.activity.*;
import com.jonasdevlieghere.mas.communication.MessageStore;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import com.jonasdevlieghere.mas.strategy.SchedulingStrategy;
import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.DefaultVehicle;
import rinde.sim.pdptw.common.VehicleDTO;

import java.util.ArrayList;
import java.util.List;

public class BeaconTruck extends DefaultVehicle implements Beacon, CommunicationUser, ActivityUser {

    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(BeaconTruck.class);


    private final double beaconRadius;
    private final double commReliability;
    private final double commRadius;
    private final boolean doExchange;

    /**
     * Models
     */
    private BeaconModel bm;
    private CommunicationAPI ca;

    /**
     * Parcels ready for pickup by this DeliveryTruck
     */
    private final List<BeaconParcel> pickupQueue;

    /**
     * Message Storage
     */
    private final MessageStore messageStore;

    /**
     * Activities
     */
    private final AuctionActivity auctionActivity;
    private final TransportActivity transportActivity;
    private final FetchActivity fetchActivity;
    private final ExchangeActivity exchangeActivity;
    private final PickupActivity pickupActivity;
    private final DeliverActivity deliverActivity;
    private final DiscoverActivity discoverActivity;
    private final ExploreActivity exploreActivity;

    /**
     * Point for Exploration Activity
     */
    private Point explorationDestination;

    /**
     * Beacon Status
     */
    private BeaconStatus status;

    public BeaconTruck(VehicleDTO pDto, int seed,
                       double beaconRadius, double commRadius, double commReliability,
                       SchedulingStrategy pickupStrategy, SchedulingStrategy deliveryStrategy,
                       boolean doExchange) {
        super(pDto);

        this.beaconRadius = beaconRadius;
        this.commReliability = commReliability;
        this.commRadius = commRadius;
        this.doExchange = doExchange;

        this.messageStore = new MessageStore();
        this.pickupQueue = new ArrayList<BeaconParcel>();
        this.auctionActivity = new AuctionActivity(this, messageStore);
        this.fetchActivity = new FetchActivity(this, pickupStrategy);
        this.transportActivity = new TransportActivity(this, deliveryStrategy);
        this.exchangeActivity = new ExchangeActivity(this,messageStore);
        this.pickupActivity = new PickupActivity(this);
        this.deliverActivity = new DeliverActivity(this);
        this.discoverActivity = new DiscoverActivity(auctionActivity, this);
        this.exploreActivity = new ExploreActivity(this, new MersenneTwister(seed));

        this.setBeaconStatus(BeaconStatus.ACTIVE);
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = roadModel.get();
        final PDPModel pm = pdpModel.get();

        if(endsTick(pickupActivity, rm, pm, bm, time))
            return;

        if(endsTick(deliverActivity, rm, pm, bm, time))
            return;

        if(doExchange && endsTick(exchangeActivity, rm, pm, bm, time))
            return;

        if(endsTick(fetchActivity, rm, pm, bm, time))
            return;

        if(endsTick(transportActivity, rm, pm, bm, time))
            return;

        if(endsTick(auctionActivity, rm, pm, bm, time))
            return;

        if(endsTick(discoverActivity, rm, pm, bm, time))
            return;

        if(endsTick(exploreActivity, rm, pm, bm, time)) {
            // DONE
        }
    }


    /**
     * Add the given BeaconParcel to this Truck's pickup queue
     *
     * @param   parcel
     *          The parcel to be added
     */
    public void queuePickup(BeaconParcel parcel){
        this.pickupQueue.add(parcel);
    }

    /**
     * Get the pickup queue of this Truck
     *
     * @return  The pickup queue
     */
    public List<BeaconParcel> getPickupQueue(){
        return new ArrayList<BeaconParcel>(this.pickupQueue);
    }

    /**
     * Remove the given BeaconParcel to this Truck's pickup queue
     *
     * @param   parcel
     *          The parcel to be removed
     */
    public void dequeuePickup(BeaconParcel parcel){
        this.pickupQueue.remove(parcel);
    }

    @Override
    public double getBeaconRadius() {
        return this.beaconRadius;
    }

    @Override
    public double getReliability() {
        return commReliability;
    }

    @Override
    public void receive(Message message) {
        messageStore.store(message);
    }

    @Override
    public void setCommunicationAPI(CommunicationAPI api) {
        this.ca = api;
    }

    @Override
    public Point getPosition() {
        return roadModel.get().getPosition(this);
    }

    @Override
    public double getRadius() {
        return commRadius;
    }

    @Override
    public BeaconStatus getBeaconStatus() {
        return this.status;
    }

    @Override
    public void setBeaconStatus(BeaconStatus status) {
        this.status = status;
    }

    @Override
    public boolean ping(){
        if(getBeaconStatus() == BeaconStatus.ACTIVE){
            setBeaconStatus(BeaconStatus.SLAVE);
            return true;
        }
        return false;
    }

    @Override
    public void setModel(BeaconModel model) {
        this.bm = model;
    }

    public void send(CommunicationUser recipient, Message message){
        ca.send(recipient, message);
    }

    public void broadcast(Message message){
         ca.broadcast(message);
    }

    @Override
    public String toString() {
        try{
            return "DeliveryTruck("+this.hashCode()+"){Pickups: " +this.getPickupQueue().size()+ ", Parcels: "+ getNbOfParcels() +"}";
        }catch (IllegalStateException e){
            return "DeliveryTruck("+this.hashCode()+")";
        }
    }

    public int getNbOfParcels() {
        return getPDPModel().getContents(this).size();
    }

    @Override
    public boolean endsTick(Activity activity, RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time) {
        activity.execute(rm, pm, bm, time);
        return activity.getStatus() == ActivityStatus.END_TICK;
    }

    /**
     * Returns this Truck's exploration destination
     *
     * @return  The Point that is the exploration destination.
     */
    public Point getExplorationDestination(){
        return explorationDestination;
    }

    /**
     * Set this Truck's exploration destination
     * @param   destination
     *          The Point that is the exploration destination.
     */
    public void setExplorationDestination(Point destination) {
             explorationDestination = destination;
    }

}