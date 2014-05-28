package com.jonasdevlieghere.mas.beacon;

import com.jonasdevlieghere.mas.action.*;
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

    private static final double MIN_RELIABILITY = .10;
    private static final double MAX_RELIABILITY = .80;

    private BeaconModel bm;
    private CommunicationAPI ca;

    private double reliability;
    private final Mailbox mailbox;
    private final ReentrantLock lock;
    private Set<DeliveryTruck> communicatedWith;
    private Map<BeaconParcel,AuctionStatus> auctionableParcels;
    private Set<BeaconParcel> discoveredParcels;



    public DeliveryTruck(VehicleDTO pDto) {
        super(pDto);
        this.reliability =  MIN_RELIABILITY + ((new MersenneTwister(123)).nextDouble() * (MAX_RELIABILITY - MIN_RELIABILITY));
        this.mailbox = new Mailbox();
        this.lock = new ReentrantLock();
        discoveredParcels = new HashSet<BeaconParcel>();
        auctionableParcels = new HashMap<BeaconParcel,AuctionStatus>();
        this.communicatedWith = new HashSet<DeliveryTruck>();
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

        if(isSuccess(new DiscoverAction(rm, pm, bm, this), time))
            return;

        moveToNearestDelivery(time);
    }

    private void bid() {
    }

    private void auctioneer() {
        for(Map.Entry<BeaconParcel,AuctionStatus> bpEntry: auctionableParcels.entrySet()){
            switch (bpEntry.getValue()){
                case UNAUCTIONED:
                    ca.broadcast(new ParticipationRequest(this, bpEntry.getKey()));
                    break;
                case PENDING:
                    Queue<Message> messages = mailbox.getMessages();
                    while (!messages.isEmpty()){
                        Message message = messages.poll();
                        DeliveryTruck bestTruck = this;
                        double bestDistance = Point.distance(this.getPosition(), bpEntry.getKey().getDestination());
                        try {
                            ParticipationReply reply = (ParticipationReply) message;
                            if (reply.getRequest().equals(bpEntry.getKey())){

                            }
                        } catch (ClassCastException e){
                              // NOOP
                        }
                    }
                    break;
            }
        }
    }

    private boolean isSuccess(Action action, TimeLapse time){
        action.execute(time);
        if(action.getStatus() == ActionStatus.SUCCESS)
            return true;
        return false;
    }

    private BeaconParcel getNearestDelivery() {
        final PDPModel pm = pdpModel.get();

        double minDistance = Double.POSITIVE_INFINITY;
        BeaconParcel bestParcel = null;
        for (final Parcel parcel : pm.getContents(this)) {
            double distance = Point.distance(this.getPosition(), parcel.getDestination());
            if (distance < minDistance){
                minDistance = distance;
                bestParcel = (BeaconParcel)parcel;
            }
        }
        return bestParcel;
    }

    private void moveToNearestDelivery(TimeLapse time){
        final RoadModel rm = roadModel.get();

        BeaconParcel parcel = getNearestDelivery();
        if(parcel != null){
            rm.moveTo(this, parcel.getDestination(), time);
        }
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
        return this.reliability;
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

    @Override
    public String toString() {
        return "DeliveryTruck ("+getPDPModel().getContentsSize(this)+"/"+this.getCapacity()+")";
    }

    public Map<BeaconParcel,AuctionStatus> getAuctionableParcels(){
        return new HashMap<BeaconParcel,AuctionStatus>(auctionableParcels);
    }

    public Set<DeliveryTruck> getCommunicatedWith() {
        lock.lock();
        final Set<DeliveryTruck> result = new HashSet<DeliveryTruck>(communicatedWith);
        lock.unlock();
        return result;
    }

    public void addDiscoveredParcel(BeaconParcel parcel) {
        discoveredParcels.add(parcel);
    }

    public void addAuctionableParcel(BeaconParcel parcel) {
        auctionableParcels.put(parcel,AuctionStatus.UNAUCTIONED);
    }
}