package com.jonasdevlieghere.mas.beacon;

import com.jonasdevlieghere.mas.action.*;
import com.jonasdevlieghere.mas.communication.Assignment;
import com.jonasdevlieghere.mas.communication.AuctionStatus;
import com.jonasdevlieghere.mas.communication.ParticipationReply;
import com.jonasdevlieghere.mas.communication.ParticipationRequest;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import org.apache.commons.math3.random.MersenneTwister;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Mailbox;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.DefaultVehicle;
import rinde.sim.pdptw.common.VehicleDTO;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class DeliveryTruck extends DefaultVehicle implements Beacon, CommunicationUser {

    private static final double RADIUS = 0.5;
    private static final double RELIABILITY = 1;

    private BeaconModel bm;
    private CommunicationAPI ca;

    private final Mailbox mailbox;
    private final ReentrantLock lock;

    /**
     * Parcels ready for pickup by this DeliveryTruck
     */
    private Set<BeaconParcel> pickupQueue;

    private Set<DeliveryTruck> communicatedWith;
    private Map<BeaconParcel,AuctionStatus> auctionableParcels;
    private Set<BeaconParcel> discoveredParcels;

    public DeliveryTruck(VehicleDTO pDto) {
        super(pDto);
        this.mailbox = new Mailbox();
        this.lock = new ReentrantLock();
        this.discoveredParcels = new HashSet<BeaconParcel>();
        this.auctionableParcels = new HashMap<BeaconParcel,AuctionStatus>();
        this.communicatedWith = new HashSet<DeliveryTruck>();
        this.pickupQueue = new HashSet<BeaconParcel>();
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = roadModel.get();
        final PDPModel pm = pdpModel.get();



        if(!auctionableParcels.isEmpty()){
            auctioneer();
            return;
        }
        
        if(!discoveredParcels.isEmpty()){
            bid();
            return;
        }

        if(isSuccess(new PickupAction(rm, pm, this), time))
            return;

        if(isSuccess(new DeliverAction(rm, pm ,this), time))
            return;

        if(isSuccess(new FetchAction(rm, pm ,this), time))
            return;

        if(isSuccess(new DiscoverAction(rm, pm, bm, this), time))
            return;

        if(isSuccess(new TransportAction(rm, pm, this), time))
            return;

    }

    private void bid() {
        Queue<Message> messages = mailbox.getMessages();
        while (!messages.isEmpty()){
            Message message = messages.poll();
            try {
                ParticipationRequest request = (ParticipationRequest) message;
                CommunicationUser sender = request.getSender();
                double distance = Point.distance(this.getPosition(), request.getAuctionableParcel().getDestination());
                send(request.getSender(), new ParticipationReply(this, request, distance));
            } catch (ClassCastException e){
                // NOOP
            }
        }
        return;
    }

    private void auctioneer() {
        for(Map.Entry<BeaconParcel,AuctionStatus> bpEntry: auctionableParcels.entrySet()){
            switch (bpEntry.getValue()){
                case UNAUCTIONED:
                    broadcast(new ParticipationRequest(this, bpEntry.getKey()));
                    auctionableParcels.put(bpEntry.getKey(),AuctionStatus.PENDING);
                    break;
                case PENDING:
                    Queue<Message> messages = mailbox.getMessages();
                    DeliveryTruck bestTruck = this;
                    while (!messages.isEmpty()){
                        Message message = messages.poll();
                        double bestDistance = Point.distance(this.getPosition(), bpEntry.getKey().getDestination());
                        try {
                            ParticipationReply reply = (ParticipationReply) message;
                            if (reply.getRequest().equals(bpEntry.getKey())){
                                 if(reply.getDistance() < bestDistance){
                                     bestDistance = reply.getDistance();
                                     bestTruck = (DeliveryTruck) reply.getSender();
                                 }
                            }
                        } catch (ClassCastException e){
                              // NOOP
                        }
                    }
                    if(bestTruck == this){
                        //TODO
                    }
                    send(bestTruck, new Assignment(this, bpEntry.getKey()));
                    break;
            }
        }
        return;
    }

    private boolean isSuccess(Action action, TimeLapse time){
        action.execute(time);
        if(action.getStatus() == ActionStatus.SUCCESS)
            return true;
        return false;
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
    public void setModel(BeaconModel model) {
        this.bm = model;
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
        mailbox.receive(message);
    }

    @Override
    public void setCommunicationAPI(CommunicationAPI api) {
        this.ca = api;
    }

    @Override
    public Point getPosition() {
        return roadModel.get().getPosition(this);
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
}