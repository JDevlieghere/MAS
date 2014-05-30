package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconStatus;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.communication.*;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

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
    public void execute() {
        if(!auctionableParcels.isEmpty()){
            auction();
        }
        if(!discoveredParcels.isEmpty()){
            //System.out.println("start biddin on " + discoveredParcels.size());
            bid();
        }
    }

    public void auction(){
        Set<BeaconParcel> toRemove = new HashSet<BeaconParcel>();
        DeliveryTruck truck = (DeliveryTruck)this.getUser();
        for(Map.Entry<BeaconParcel,AuctionStatus> bpEntry: auctionableParcels.entrySet()){
            switch (bpEntry.getValue()){
                case UNAUCTIONED:
                    System.out.println("UNAUC " + bpEntry.getKey());
                    truck.broadcast(new ParticipationRequestMessage(truck, bpEntry.getKey()));
                    auctionableParcels.put(bpEntry.getKey(),AuctionStatus.PENDING);
                    setStatus(ActivityStatus.END_TICK);
                    break;
                case PENDING:
                    auctionableParcels.put(bpEntry.getKey(), AuctionStatus.AUCTIONING);
                    setStatus(ActivityStatus.END_TICK);
                    break;
                case AUCTIONING:
                    System.out.println("Bidders for all auctions (except me) = " + messageStore.getSize(ParticipationReplyMessage.class));
                    List<Message> messages = messageStore.retrieve(ParticipationReplyMessage.class);
                    toRemove.add(bpEntry.getKey());
                    DeliveryTruck bestTruck = truck;
                    double bestDistance = Point.distance(truck.getPosition(), bpEntry.getKey().getDestination());
                    System.out.println("MY"+ this.toString()+" BID:"+ bestDistance);
                    for(Message msg : messages){
                        try {
                            ParticipationReplyMessage reply = (ParticipationReplyMessage) msg;
                            System.out.println(reply.getRequest().getAuctionableParcel().toString());
                            if (reply.getRequest().getAuctionableParcel().equals(bpEntry.getKey())){
                                System.out.println("OTHER"+ reply.getSender().toString()+" BID:"+ bestDistance);
                                if(reply.getDistance() < bestDistance){
                                    bestDistance = reply.getDistance();
                                    bestTruck = (DeliveryTruck) reply.getSender();
                                }
                            }
                        } catch (ClassCastException e){
                            // NOP
                        }
                    }
                    if(bestTruck.equals(this.getUser())){
                        System.out.println("I WON:" + this.toString());
                        truck.queuePickup(bpEntry.getKey());
                    } else {
                        System.out.println("OTHER:" + bestTruck.toString());
                        truck.send(bestTruck, new AssignmentMessage(truck, bpEntry.getKey()));
                    }
                    bpEntry.getKey().setStatus(BeaconStatus.INACTIVE);
                    setStatus(ActivityStatus.NORMAL);
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
        List<Message> messages = messageStore.retrieve(ParticipationRequestMessage.class);
        DeliveryTruck truck = (DeliveryTruck)this.getUser();
        for(Message msg : messages){
            try {
                ParticipationRequestMessage request = (ParticipationRequestMessage) msg;
                if(discoveredParcels.contains(request.getAuctionableParcel())){
                    CommunicationUser sender = request.getSender();
                    System.out.println("Bidding on " + request.getAuctionableParcel().toString());
                    double distance = Point.distance(truck.getPosition(), request.getAuctionableParcel().getDestination());
                    System.out.println("Biddin from " + truck.getPosition().toString());
                    truck.send(sender, new ParticipationReplyMessage(truck, request, distance));
                    discoveredParcels.remove(request.getAuctionableParcel());
                }
            } catch (ClassCastException e){
                // NOP
            }
        }
        if(messages.size() > 0 )
            setStatus(ActivityStatus.END_TICK);
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
}
