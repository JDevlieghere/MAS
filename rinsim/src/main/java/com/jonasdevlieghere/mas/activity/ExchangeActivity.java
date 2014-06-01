package com.jonasdevlieghere.mas.activity;

import com.google.common.collect.ImmutableSet;
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

/**
 * Created with IntelliJ IDEA.
 * User: dieter
 * Date: 6/1/14
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExchangeActivity extends Activity{

    private static double exchangeRadius = 0.5;
    private ExchangeStatus status;
    private MessageStore messageStore;

    public ExchangeActivity(ActivityUser user, MessageStore messageStore){
        super(user);
        this.messageStore = messageStore;
        this.status = ExchangeStatus.INITIAL;
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, TimeLapse time) {
        DeliveryTruck truck = (DeliveryTruck) getUser();
        DeliveryTruck otherTruck;
        if(!truck.isPinged()){
            switch (status){
                case INITIAL:
                    otherTruck = getNearestTruck(rm, truck);
                    if(otherTruck != null){
                        //Ensure you are the first and only one to initiate exchange
                        if(otherTruck.ping()){
                            truck.send(otherTruck,new ExchangeRequestMessage(truck));
                        }
                    }
                    status = ExchangeStatus.PENDING;
                    break;
                case PENDING:
                    status = ExchangeStatus.MEETING;
                    setStatus(ActivityStatus.END_TICK);
                    break;
                case MEETING:
                    List<ExchangeReplyMessage> messages = messageStore.retrieve(ExchangeReplyMessage.class);
                    //At all times there should only be one message of this type.
                    ExchangeReplyMessage reply = messages.get(0);
                    otherTruck = (DeliveryTruck) reply.getSender();
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

                    truck.send(otherTruck, new ExchangeAssignmentMessage(truck, otherDropList,otherPickupList));

                    status = ExchangeStatus.EXCHANGING;
                    break;
                case EXCHANGING:

                    break;

            }

        } else {
            List<ExchangeRequestMessage> messages = messageStore.retrieve(ExchangeRequestMessage.class);
            //At all times there should only be one message of this type.
            ExchangeRequestMessage request = messages.get(0);
            truck.send(request.getSender(), new ExchangeReplyMessage(truck, pm.getContents(truck)));
        }
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
