package com.jonasdevlieghere.mas.beacon;

import com.jonasdevlieghere.mas.action.*;
import com.jonasdevlieghere.mas.activity.*;
import com.jonasdevlieghere.mas.communication.*;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.DefaultVehicle;
import rinde.sim.pdptw.common.VehicleDTO;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class DeliveryTruck extends DefaultVehicle implements Beacon, CommunicationUser, ActionUser, ActivityUser {

    /**
     * Constants
     */
    private static final double RADIUS = 0.7;
    private static final double RELIABILITY = 1;

    /**
     * Models
     */
    private BeaconModel bm;
    private CommunicationAPI ca;

    /**
     * Utilities
     */
    private final ReentrantLock lock;
    private final RandomGenerator rand;

    /**
     * Parcels ready for pickup by this DeliveryTruck
     */
    private Set<BeaconParcel> pickupQueue;

    private MessageStore messageStore;
    private Set<BeaconParcel> discoveredParcels;

    private AuctionActivity auction;
    private Activity processAssignments;

    public DeliveryTruck(VehicleDTO pDto) {
        super(pDto);
        this.lock = new ReentrantLock();
        this.discoveredParcels = new HashSet<BeaconParcel>();
        this.messageStore = new MessageStore();
        this.pickupQueue = new HashSet<BeaconParcel>();
        this.rand = new MersenneTwister(123);
        this.auction = new AuctionActivity(this, messageStore);
        this.processAssignments = new AssignmentsActivity(this, messageStore);
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = roadModel.get();
        final PDPModel pm = pdpModel.get();

        auction.execute();
        if(endsTick(auction, time))
            return;

        processAssignments.execute();
        if(endsTick(processAssignments, time))
            return;

        if(endsTick(new PickupAction(rm, pm, this), time))
            return;

        if(endsTick(new DeliverAction(rm, pm ,this), time))
            return;

        if(endsTick(new FetchAction(rm, pm ,this), time))
            return;

        if(endsTick(new TransportAction(rm, pm, this), time))
            return;

        if(endsTick(new DiscoverAction(rm, pm, bm, this), time))
            return;

//        if(isSuccess(new ExploreAction(rm, pm, this, this.rand), time))
//            return;
    }


    public void queuePickup(BeaconParcel parcel){
        this.pickupQueue.add(parcel);
    }

    public Set<BeaconParcel> getPickupQueue(){
        return new HashSet<BeaconParcel>(this.pickupQueue);
    }

    public void unqueuePickup(BeaconParcel parcel){
        this.pickupQueue.remove(parcel);
    }

    @Override
    public double getRadius() {
        return RADIUS;
    }

    @Override
    public double getReliability() {
        return RELIABILITY;
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
    public BeaconStatus getStatus() {
        return null;
    }

    @Override
    public void setStatus(BeaconStatus status) {

    }

    @Override
    public void setModel(BeaconModel model) {
        this.bm = model;
    }

    public void addDiscoveredParcel(BeaconParcel parcel) {
        auction.addDiscoveredParcel(parcel);
    }

    public void addAuctionableParcel(BeaconParcel parcel) {
        auction.addAuctionableParcel(parcel);
    }

    public void send(CommunicationUser recipient, Message message){
        ca.send(recipient, message);
    }

    public void broadcast(Message message){
         ca.broadcast(message);
    }

    @Override
    public boolean endsTick(Action action, TimeLapse time) {
        action.execute(time);
        if(action.getStatus() == ActionStatus.SUCCESS)
            return true;
        return false;
    }

    @Override
    public boolean endsTick(Activity activity, TimeLapse time) {
        activity.execute();
        if(activity.getStatus() == ActivityStatus.END_TICK)
            return true;
        return false;
    }
}