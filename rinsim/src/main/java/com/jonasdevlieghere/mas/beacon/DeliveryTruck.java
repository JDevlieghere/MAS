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
    private static final double RELIABILITY = 1;

    private BeaconModel bm;
    private CommunicationAPI ca;

    private final Mailbox mailbox;
    private final ReentrantLock lock;

    /**
     * Parcels for Delivery
     */

    private Set<DeliveryTruck> communicatedWith;
    private Map<BeaconParcel,AuctionStatus> auctionableParcels;
    private Set<BeaconParcel> discoveredParcels;

    public DeliveryTruck(VehicleDTO pDto) {
        super(pDto);
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

        if(isSuccess(new TransportAction(rm, pm, this), time))
            return;

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
}