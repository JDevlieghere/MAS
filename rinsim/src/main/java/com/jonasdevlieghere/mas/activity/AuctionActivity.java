package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.TickStatus;
import com.jonasdevlieghere.mas.communication.*;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

import java.util.*;

public class AuctionActivity extends Activity{

    private MessageStore messageStore;
    private Map<BeaconParcel,AuctionStatus> auctionableParcels;
    private Set<BeaconParcel> discoveredParcels;

    public AuctionActivity(ActivityUser user, MessageStore messageStore){
        super(user);
        this.messageStore = messageStore;
        this.discoveredParcels = new HashSet<BeaconParcel>();
        this.auctionableParcels = new HashMap<BeaconParcel,AuctionStatus>();
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time) {
        List<AssignmentMessage> messages = messageStore.retrieve(AssignmentMessage.class);
        BeaconTruck truck = (BeaconTruck)getUser();
        for(AssignmentMessage assignment: messages){
            truck.queuePickup((BeaconParcel) assignment.getParcel());
        }
        setActivityStatus(TickStatus.NORMAL);
        if(!auctionableParcels.isEmpty()){
            auction();
        }
        if(!discoveredParcels.isEmpty()){
            bid();
        }
    }

    public void auction(){
        Set<BeaconParcel> toRemove = new HashSet<BeaconParcel>();
        BeaconTruck truck = (BeaconTruck)this.getUser();
        for(Map.Entry<BeaconParcel,AuctionStatus> bpEntry: auctionableParcels.entrySet()){
            switch (bpEntry.getValue()){
                case UNAUCTIONED:
                    //System.out.println("UNAUC at " + truck.toString() + ", parcel = " + bpEntry.getKey());
                    truck.broadcast(new ParticipationRequestMessage(truck, bpEntry.getKey()));
                    auctionableParcels.put(bpEntry.getKey(),AuctionStatus.PENDING);
                    setActivityStatus(TickStatus.END_TICK);
                    break;
                case PENDING:
                    // Waiting for replies to come back.
                    auctionableParcels.put(bpEntry.getKey(), AuctionStatus.AUCTIONING);
                    setActivityStatus(TickStatus.END_TICK);
                    break;
                case AUCTIONING:
                    //System.out.println("Bidders for all auctions (except me) = " + messageStore.getSize(ParticipationReplyMessage.class));
                    List<ParticipationReplyMessage> messages = messageStore.retrieve(ParticipationReplyMessage.class);
                    toRemove.add(bpEntry.getKey());
                    BeaconTruck bestTruck = truck;
                    AuctionCost lowestAuctionCost = new AuctionCost(truck,bpEntry.getKey());
                    //System.out.println("MY "+ truck + " BID:"+ lowestAuctionCost);
                    for(Message msg : messages){
                        try {
                            ParticipationReplyMessage reply = (ParticipationReplyMessage) msg;
                            if (reply.getRequest().getAuctionableParcel().equals(bpEntry.getKey())){
                                //System.out.println("OTHER "+ reply.getSender()+" BID:"+ reply.getAuctionCost() + " FOR:" + reply.getRequest().getAuctionableParcel());
                                if(lowestAuctionCost.compareTo(reply.getAuctionCost()) > 0){
                                    lowestAuctionCost = reply.getAuctionCost();
                                    bestTruck = (BeaconTruck) reply.getSender();
                                }
                            }
                        } catch (ClassCastException e){
                            // NOP
                        }
                    }
                    if(bestTruck.equals(this.getUser())){
                        truck.queuePickup(bpEntry.getKey());
                    } else {
                        truck.send(bestTruck, new AssignmentMessage(truck, bpEntry.getKey()));
                    }
                    setActivityStatus(TickStatus.NORMAL);
                    break;
            }
        }
        for(BeaconParcel bp : toRemove){
            auctionableParcels.remove(bp);
        }
        return;
    }

    private void bid() {
        //System.out.println("Bidding " + messages.size());
        List<ParticipationRequestMessage> messages = messageStore.retrieve(ParticipationRequestMessage.class);
        BeaconTruck truck = (BeaconTruck)this.getUser();
        for(ParticipationRequestMessage request : messages){
            if(hasDiscovered(request.getAuctionableParcel())){
                CommunicationUser sender = request.getSender();
                //System.out.println("Bidding on " + request.getAuctionableParcel().toString());
                double distance = Point.distance(truck.getPosition(), request.getAuctionableParcel().getDestination()) + truck.getPickupQueue().size();
                //System.out.println("Biddin from " + truck.getPosition().toString());
                truck.send(sender, new ParticipationReplyMessage(truck, request,new AuctionCost(truck, request.getAuctionableParcel())));
                discoveredParcels.remove(request.getAuctionableParcel());
            }
        }
        if(messages.size() > 0 )
            setActivityStatus(TickStatus.END_TICK);
        return;
    }

    private Map<BeaconParcel,AuctionStatus> getAuctionableParcels(){
        return new HashMap<BeaconParcel,AuctionStatus>(auctionableParcels);
    }

    public void addAuctionableParcel(BeaconParcel parcel) {
        auctionableParcels.put(parcel, AuctionStatus.UNAUCTIONED);
    }

    public void addDiscoveredParcel(BeaconParcel parcel) {
        discoveredParcels.add(parcel);
    }

    public Set<BeaconParcel> getDiscoveredParcels() {
        return new HashSet<BeaconParcel>(discoveredParcels);
    }

    public boolean hasDiscovered(Parcel bp) {
        return discoveredParcels.contains(bp);
    }

}
