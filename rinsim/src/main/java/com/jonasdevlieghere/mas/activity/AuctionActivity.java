package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.ActionUser;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconStatus;
import com.jonasdevlieghere.mas.communication.*;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: dieter
 * Date: 5/29/14
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuctionActivity extends Activity{

    private MessageStore messageStore;
    private ActionUser truck;
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
        List<Message> messages = messageStore.retrieve(ParticipationReply.class);
        System.out.println(auctionableParcels.size());
        for(Map.Entry<BeaconParcel,AuctionStatus> bpEntry: auctionableParcels.entrySet()){
            switch (bpEntry.getValue()){
                case UNAUCTIONED:
                    System.out.println("UNAUC " + bpEntry.getKey());
                    truck.broadcast(new ParticipationRequest((ActionUser)this.getUser(), bpEntry.getKey()));
                    auctionableParcels.put(bpEntry.getKey(),AuctionStatus.PENDING);
                    break;
                case PENDING:
                    auctionableParcels.put(bpEntry.getKey(), AuctionStatus.AUCTIONING);
                    break;
                case AUCTIONING:
                    toRemove.add(bpEntry.getKey());
                    System.out.println("PENDING for "+ bpEntry.getKey().toString()+":" + messages.size());
                    ActionUser bestTruck = truck;
                    double bestDistance = Point.distance(((ActionUser)this.getUser()).getPosition(), bpEntry.getKey().getDestination());
                    System.out.println("Initial best:" + bestDistance);
                    for(Message msg : messages){
                        try {
                            ParticipationReply reply = (ParticipationReply) msg;
                            if (reply.getRequest().getAuctionableParcel().equals(bpEntry.getKey())){
                                System.out.println("Reply recieved with dist " + reply.getDistance());
                                if(reply.getDistance() < bestDistance){
                                    bestDistance = reply.getDistance();
                                    bestTruck = (ActionUser) reply.getSender();
                                }
                            }
                        } catch (ClassCastException e){
                            // NOP
                        }
                    }
                    if(bestTruck.equals(this.getUser())){
                        truck.queuePickup(bpEntry.getKey());
                    } else {
                        truck.send(bestTruck, new Assignment((ActionUser)this.getUser(), bpEntry.getKey()));
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
        List<Message> messages = messageStore.retrieve(ParticipationRequest.class);
        for(Message msg : messages){
            try {
                ParticipationRequest request = (ParticipationRequest) msg;
                if(discoveredParcels.contains(request.getAuctionableParcel())){
                    System.out.println("Biddin from " + ((ActionUser)this.getUser()).getPosition().toString() + " for " + request.getAuctionableParcel());
                    CommunicationUser sender = request.getSender();
                    double distance = Point.distance(((ActionUser)this.getUser()).getPosition(), request.getAuctionableParcel().getDestination());
                    System.out.println("Biddin from " + ((ActionUser)this.getUser()).getPosition().toString());
                    ((ActionUser)this.getUser()).send(sender, new ParticipationReply(((ActionUser) this.getUser()), request, distance));
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
}
