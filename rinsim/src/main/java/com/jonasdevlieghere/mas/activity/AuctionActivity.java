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
            bid();
            setStatus(ActivityStatus.END_TICK);
        }
    }

    public void auction(){
        Set<BeaconParcel> toRemove = new HashSet<BeaconParcel>();
        List<Message> messages = messageStore.retrieve(ParticipationReplyMessage.class);
        DeliveryTruck truck = (DeliveryTruck)this.getUser();
        System.out.println(auctionableParcels.size());
        for(Map.Entry<BeaconParcel,AuctionStatus> bpEntry: auctionableParcels.entrySet()){
            switch (bpEntry.getValue()){
                case UNAUCTIONED:
                    System.out.println("UNAUC " + bpEntry.getKey());
                    truck.broadcast(new ParticipationRequestMessage(truck, bpEntry.getKey()));
                    auctionableParcels.put(bpEntry.getKey(),AuctionStatus.PENDING);
                    break;
                case PENDING:
                    auctionableParcels.put(bpEntry.getKey(), AuctionStatus.AUCTIONING);
                    break;
                case AUCTIONING:
                    toRemove.add(bpEntry.getKey());
                    System.out.println("PENDING for "+ bpEntry.getKey().toString()+":" + messages.size());
                    DeliveryTruck bestTruck = truck;
                    double bestDistance = Point.distance(truck.getPosition(), bpEntry.getKey().getDestination());
                    System.out.println("Initial best:" + bestDistance);
                    for(Message msg : messages){
                        try {
                            ParticipationReplyMessage reply = (ParticipationReplyMessage) msg;
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
                    if(bestTruck.equals(this.getUser())){
                        truck.queuePickup(bpEntry.getKey());
                    } else {
                        truck.send(bestTruck, new AssignmentMessage(truck, bpEntry.getKey()));
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

    private void bid() {
        List<Message> messages = messageStore.retrieve(ParticipationRequestMessage.class);
        DeliveryTruck truck = (DeliveryTruck)this.getUser();

        for(Message msg : messages){
            try {
                ParticipationRequestMessage request = (ParticipationRequestMessage) msg;
                if(discoveredParcels.contains(request.getAuctionableParcel())){
                    System.out.println("Biddin from " + truck.getPosition().toString() + " for " + request.getAuctionableParcel());
                    CommunicationUser sender = request.getSender();
                    double distance = Point.distance(truck.getPosition(), request.getAuctionableParcel().getDestination());
                    System.out.println("Biddin from " + truck.getPosition().toString());
                    truck.send(sender, new ParticipationReplyMessage(truck, request, distance));
                    discoveredParcels.remove(request.getAuctionableParcel());
                }
            } catch (ClassCastException e){
                // NOP
            }
        }
        return;
    }

    private Map<BeaconParcel,AuctionStatus> getAuctionableParcels(){
        return new HashMap<BeaconParcel,AuctionStatus>(auctionableParcels);
    }

    @Override
    public ActivityStatus getStatus() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addAuctionableParcel(BeaconParcel parcel) {
        auctionableParcels.put(parcel, AuctionStatus.UNAUCTIONED);
    }

    public void addDiscoveredParcel(BeaconParcel parcel) {
        discoveredParcels.add(parcel);
    }
}
