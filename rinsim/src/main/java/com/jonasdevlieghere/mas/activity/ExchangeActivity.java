package com.jonasdevlieghere.mas.activity;

import com.google.common.collect.ImmutableSet;
import com.jonasdevlieghere.mas.beacon.BeaconStatus;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.cluster.Cluster;
import com.jonasdevlieghere.mas.cluster.KMeans;
import com.jonasdevlieghere.mas.communication.*;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExchangeActivity extends Activity{

    private static double exchangeRadius = 0.5;
    private ExchangeStatus status;
    private MessageStore messageStore;
    private Point meetingPoint;
    private ArrayList<Point> dropList;
    private ArrayList<Point> pickupList;
    DeliveryTruck otherTruck;

    public ExchangeActivity(ActivityUser user, MessageStore messageStore){
        super(user);
        this.messageStore = messageStore;
        this.status = ExchangeStatus.INITIAL;
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, TimeLapse time) {
        //Reset activity status
        setStatus(ActivityStatus.NORMAL);
        DeliveryTruck truck = (DeliveryTruck) getUser();
        //Master of the exchange
        if(truck.getStatus() == BeaconStatus.ACTIVE){
            switch (status){
                case INITIAL:
                    initiateExchange(rm, truck);
                    status = ExchangeStatus.PENDING;
                    setStatus(ActivityStatus.END_TICK);
                    break;
                case PENDING:
                    pending();
                    break;
                case PLANNING:
                    planExchange(pm, truck);
                    status = ExchangeStatus.MEETING;
                    setStatus(ActivityStatus.END_TICK);
                    break;
                case MEETING:
                    if(rm.getObjectsAt(truck,DeliveryTruck.class).contains(otherTruck))
                        status=ExchangeStatus.EXCHANGING;
                    meet(rm, time, truck);
                    setStatus(ActivityStatus.END_TICK);
                    break;
                case EXCHANGING:

            }
        } else {
            switch (status){
                case INITIAL:
                    List<ExchangeRequestMessage> messages1 = messageStore.retrieve(ExchangeRequestMessage.class);
                    //At all times there should only be one message of this type.
                    ExchangeRequestMessage request = messages1.get(0);
                    otherTruck = (DeliveryTruck) request.getSender();
                    truck.send(otherTruck, new ExchangeReplyMessage(truck, pm.getContents(truck)));
                    status = ExchangeStatus.PENDING;
                    break;
                case PENDING:
                    pending();
                    break;
                case PLANNING:
                    List<ExchangeAssignmentMessage> messages2 = messageStore.retrieve(ExchangeAssignmentMessage.class);
                    //At all times there should only be one message of this type.
                    ExchangeAssignmentMessage assignment = messages2.get(0);
                    dropList = assignment.getDropList();
                    pickupList = assignment.getPickupList();
                    meetingPoint = assignment.getMeetingPoint();
                    status = ExchangeStatus.MEETING;
                    setStatus(ActivityStatus.END_TICK);
                case MEETING:
                    if(rm.getObjectsAt(truck,DeliveryTruck.class).contains(otherTruck))
                        status=ExchangeStatus.EXCHANGING;
                    meet(rm,time,truck);
                    break;
                case EXCHANGING:
                    break;
            }
        }
    }

    private void drop() {
        //TODO: Check whether PDPModel.drop() transfers a parcel.
    }

    private void planExchange(PDPModel pm, DeliveryTruck truck) {
        List<ExchangeReplyMessage> messages = messageStore.retrieve(ExchangeReplyMessage.class);
        //At all times there should only be one message of this type.
        ExchangeReplyMessage reply = messages.get(0);
        DeliveryTruck otherTruck= (DeliveryTruck) reply.getSender();
        ArrayList<Point> pointList = new ArrayList<Point>();
        ImmutableSet<Parcel> myParcels = pm.getContents(truck);
        ImmutableSet<Parcel> otherParcels = reply.getParcels();
        for(Parcel p: myParcels){
            pointList.add(p.getDestination());
        }

        for(Parcel p:otherParcels){
            pointList.add(p.getDestination());
        }
        KMeans km = new KMeans(pointList ,2);
        ArrayList<Cluster> clusters = km.getClusters();
        ArrayList<Point> myPoints = clusters.get(0).getPoints();
        ArrayList<Point> otherPoints = clusters.get(1).getPoints();

        ArrayList<Point> myPickupList = new ArrayList<Point>();
        ArrayList<Point> myDropList = new ArrayList<Point>();

        ArrayList<Point> otherPickupList = new ArrayList<Point>();
        ArrayList<Point> otherDropList = new ArrayList<Point>();

        for(Parcel parcel: myParcels){
            Point dest = parcel.getDestination();
            if(otherPoints.contains(dest)){
               otherDropList.add(dest);
               myPickupList.add(dest);
            }
        }

        for(Parcel parcel: otherParcels){
            Point dest = parcel.getDestination();
            if(myPoints.contains(dest)){
                myDropList.add(dest);
                otherPickupList.add(dest);
            }
        }

        dropList = myDropList;
        pickupList = myPickupList;

        double newX =  (truck.getPosition().x + otherTruck.getPosition().x)/2;
        double newY =  (truck.getPosition().y + otherTruck.getPosition().y)/2;
        meetingPoint = new Point(newX, newY);
        truck.send(otherTruck, new ExchangeAssignmentMessage(truck, otherDropList,otherPickupList, meetingPoint));

    }

    private void initiateExchange(RoadModel rm, DeliveryTruck truck) {
        otherTruck = getNearestTruck(rm, truck);
        if(otherTruck != null){
            //Ensure you are the first and only one to initiate exchange
            if(otherTruck.ping()){
                truck.send(otherTruck,new ExchangeRequestMessage(truck));
            }
        }
    }

    private void meet(RoadModel rm, TimeLapse time, DeliveryTruck truck) {
        rm.moveTo(truck, meetingPoint, time);
    }

    private void pending() {
        status = ExchangeStatus.PLANNING;
        setStatus(ActivityStatus.END_TICK);
    }

    private DeliveryTruck getNearestTruck(RoadModel rm, DeliveryTruck truck){
        Set<DeliveryTruck> allTrucks = rm.getObjectsOfType(DeliveryTruck.class);
        DeliveryTruck bestTruck = null;
        double distance;
        double bestDistance = exchangeRadius;
        for(DeliveryTruck otherTruck : allTrucks){
            distance = Point.distance(otherTruck.getPosition(), truck.getPosition());
            if(distance < bestDistance){
               bestTruck = otherTruck;
               bestDistance = distance;
            }
        }
        return bestTruck;
    }
}
