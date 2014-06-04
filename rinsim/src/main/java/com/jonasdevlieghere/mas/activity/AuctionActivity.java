package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.communication.*;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

import java.util.*;

public class AuctionActivity extends Activity{

    private final MessageStore messageStore;
    private final Map<BeaconParcel,AuctionStatus> auctionableParcels;
    private final Set<BeaconParcel> discoveredParcels;

    public AuctionActivity(ActivityUser user, MessageStore messageStore){
        super(user);
        this.messageStore = messageStore;
        this.discoveredParcels = new HashSet<BeaconParcel>();
        this.auctionableParcels = new HashMap<BeaconParcel,AuctionStatus>();
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time) {
        setActivityStatus(ActivityStatus.NORMAL);
        List<AssignmentMessage> messages = messageStore.retrieve(AssignmentMessage.class);
        BeaconTruck truck = (BeaconTruck)getUser();
        for(AssignmentMessage assignment: messages){
            truck.queuePickup((BeaconParcel) assignment.getParcel());
        }
        setActivityStatus(ActivityStatus.NORMAL);
        if(!auctionableParcels.isEmpty()){
            auction();
        }
        if(!discoveredParcels.isEmpty()){
            bid();
        }
    }

    void auction(){
        Set<BeaconParcel> toRemove = new HashSet<BeaconParcel>();
        BeaconTruck truck = (BeaconTruck)this.getUser();
        for(Map.Entry<BeaconParcel,AuctionStatus> bpEntry: auctionableParcels.entrySet()){
            switch (bpEntry.getValue()){
                case UNAUCTIONED:
                    //System.out.println("UNAUC at " + truck.toString() + ", parcel = " + bpEntry.getKey());
                    truck.broadcast(new ParticipationRequestMessage(truck, bpEntry.getKey()));
                    auctionableParcels.put(bpEntry.getKey(),AuctionStatus.PENDING);
                    setActivityStatus(ActivityStatus.END_TICK);
                    break;
                case PENDING:
                    // Waiting for replies to come back.
                    auctionableParcels.put(bpEntry.getKey(), AuctionStatus.AUCTIONING);
                    setActivityStatus(ActivityStatus.END_TICK);
                    break;
                case AUCTIONING:
                    //System.out.println("Bidders for all auctions (except me) = " + messageStore.getSize(ParticipationReplyMessage.class));
                    List<ParticipationReplyMessage> messages = messageStore.retrieve(ParticipationReplyMessage.class);
                    toRemove.add(bpEntry.getKey());
                    BeaconTruck bestTruck = truck;
                    AuctionCost lowestAuctionCost = new AuctionCost(truck);
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
                    break;
            }
        }
        for(BeaconParcel bp : toRemove){
            auctionableParcels.remove(bp);
        }
    }

    private void bid() {
        //System.out.println("Bidding " + messages.size());
        List<ParticipationRequestMessage> messages = messageStore.retrieve(ParticipationRequestMessage.class);
        BeaconTruck truck = (BeaconTruck)this.getUser();
        for(ParticipationRequestMessage request : messages){
            if(hasDiscovered(request.getAuctionableParcel())){
                CommunicationUser sender = request.getSender();
                //System.out.println("Bidding on " + request.getAuctionableParcel().toString());
                //System.out.println("Biddin from " + truck.getPosition().toString());
                truck.send(sender, new ParticipationReplyMessage(truck, request,new AuctionCost(truck)));
                discoveredParcels.remove(request.getAuctionableParcel());
            }
        }
        if(messages.size() > 0 )
            setActivityStatus(ActivityStatus.END_TICK);
    }

    public void addAuctionableParcel(BeaconParcel parcel) {
        auctionableParcels.put(parcel, AuctionStatus.UNAUCTIONED);
    }

    public void addDiscoveredParcel(BeaconParcel parcel) {
        discoveredParcels.add(parcel);
    }


    public boolean hasDiscovered(Parcel bp) {
        return discoveredParcels.contains(bp);
    }

}
