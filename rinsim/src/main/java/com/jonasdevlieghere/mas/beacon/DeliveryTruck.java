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
    private Map<BeaconParcel,AuctionStatus> auctionableParcels;
    private Set<BeaconParcel> discoveredParcels;

    private Activity auctioneering;
    private Activity bidding;
    private Activity processAssignments;



    public DeliveryTruck(VehicleDTO pDto) {
        super(pDto);
        this.lock = new ReentrantLock();
        this.discoveredParcels = new HashSet<BeaconParcel>();
        this.auctionableParcels = new HashMap<BeaconParcel,AuctionStatus>();
        this.messageStore = new MessageStore();
        this.pickupQueue = new HashSet<BeaconParcel>();
        this.rand = new MersenneTwister(123);
        auctioneering = new Auctioneering();
        bidding = new Bidding();
        processAssignments = new ProcessAssignments();
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = roadModel.get();
        final PDPModel pm = pdpModel.get();

        auctioneering.execute();
        if(endsTick(auctioneering, time))
            return;

        bidding.execute();
        if(endsTick(bidding, time))
            return;

        processAssignments.execute();
        if(endsTick(processAssignments, time))
            return;

        if(!auctionableParcels.isEmpty()){
            auctioneer();
        }
        
        if(!discoveredParcels.isEmpty()){
            bid();
            return;
        }

        processAssignments();

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

    private void processAssignments() {
        List<Message> messages = messageStore.retrieve(Assignment.class);
        for(Message msg : messages){
            try {
                Assignment assignment = (Assignment) msg;
                queuePickup((BeaconParcel) assignment.getParcel());
            } catch (ClassCastException e){
                // NOP
            }
        }
        return;
    }

    private void bid() {
        List<Message> messages = messageStore.retrieve(ParticipationRequest.class);
        for(Message msg : messages){
            try {
                ParticipationRequest request = (ParticipationRequest) msg;
                if(discoveredParcels.contains(request.getAuctionableParcel())){
                    System.out.println("Biddin from " + this.getPosition().toString() + " for " + request.getAuctionableParcel());
                    CommunicationUser sender = request.getSender();
                    double distance = Point.distance(this.getPosition(), request.getAuctionableParcel().getDestination());
                    System.out.println("Biddin from " + this.getPosition().toString());
                    send(sender,new ParticipationReply(this,request,distance));
                    discoveredParcels.remove(request.getAuctionableParcel());
                }
            } catch (ClassCastException e){
                // NOP
            }
        }
        return;
    }

    private void auctioneer() {
        Set<BeaconParcel> toRemove = new HashSet<BeaconParcel>();
        List<Message> messages = messageStore.retrieve(ParticipationReply.class);
        System.out.println(auctionableParcels.size());
        for(Map.Entry<BeaconParcel,AuctionStatus> bpEntry: auctionableParcels.entrySet()){
            switch (bpEntry.getValue()){
                case UNAUCTIONED:
                    System.out.println("UNAUC " + bpEntry.getKey());
                    broadcast(new ParticipationRequest(this, bpEntry.getKey()));
                    auctionableParcels.put(bpEntry.getKey(),AuctionStatus.PENDING);
                    break;
                case PENDING:
                    auctionableParcels.put(bpEntry.getKey(), AuctionStatus.AUCTIONING);
                    break;
                case AUCTIONING:
                    toRemove.add(bpEntry.getKey());
                    System.out.println("PENDING for "+ bpEntry.getKey().toString()+":" + messages.size());
                    DeliveryTruck bestTruck = this;
                    double bestDistance = Point.distance(this.getPosition(), bpEntry.getKey().getDestination());
                    System.out.println("Initial best:" + bestDistance);
                    for(Message msg : messages){
                        try {
                            ParticipationReply reply = (ParticipationReply) msg;
                            if (reply.getRequest().getAuctionableParcel().equals(bpEntry.getKey())){
                                System.out.println("Reply recieved with dist " + reply.getDistance());
                                if(reply.getDistance() < bestDistance){
                                    bestDistance = reply.getDistance();
                                    bestTruck = (DeliveryTruck) reply.getSender();
                                }
                            }
                        } catch (ClassCastException e){
                            // NOP
                        }
                    }
                    if(bestTruck == this){
                        queuePickup(bpEntry.getKey());
                    } else {
                        send(bestTruck, new Assignment(this, bpEntry.getKey()));
                    }
                    discoveredParcels.remove(bpEntry.getKey());
                    bpEntry.getKey().setStatus(BeaconStatus.INACTIVE);
                    break;
            }
        }
        for(BeaconParcel bp : toRemove){
            auctionableParcels.remove(bp);
        }
        return;
    }

    private void queuePickup(BeaconParcel parcel){
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

    public Map<BeaconParcel,AuctionStatus> getAuctionableParcels(){
        return new HashMap<BeaconParcel,AuctionStatus>(auctionableParcels);
    }

    public void addDiscoveredParcel(BeaconParcel parcel) {
        discoveredParcels.add(parcel);
    }

    public void addAuctionableParcel(BeaconParcel parcel) {
        auctionableParcels.put(parcel,AuctionStatus.UNAUCTIONED);
    }

    private void send(CommunicationUser recipient, Message message){
        ca.send(recipient, message);
    }

    private void broadcast(Message message){
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