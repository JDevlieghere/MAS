package com.jonasdevlieghere.mas.activity;

import com.google.common.collect.ImmutableSet;
import com.jonasdevlieghere.mas.beacon.BeaconStatus;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.cluster.Cluster;
import com.jonasdevlieghere.mas.cluster.KMeans;
import com.jonasdevlieghere.mas.communication.*;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExchangeActivity extends Activity{

    private ExchangeStatus status;
    private MessageStore messageStore;
    private Point meetingPoint;
    private ArrayList<Point> dropList;
    private ArrayList<Point> pickupList;
    DeliveryTruck otherTruck;
    private BeaconModel bm;

    public ExchangeActivity(ActivityUser user, BeaconModel bm, MessageStore messageStore){
        super(user);
        this.bm = bm;
        this.messageStore = messageStore;
        this.status = ExchangeStatus.INITIAL;
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, TimeLapse time) {
        //Reset activity status
        setActivityStatus(ActivityStatus.NORMAL);
        DeliveryTruck truck = (DeliveryTruck) getUser();
        //Master of the exchange
        if(truck.getStatus() == BeaconStatus.ACTIVE){
            switch (status){
                case INITIAL:
                    initiateExchange(truck);
                    break;
                case PENDING:
                    pending();
                    break;
                case PLANNING:
                    planExchange(pm, truck);
                    break;
                case MEETING:
                    if(rm.getObjectsAt(truck,DeliveryTruck.class).contains(otherTruck))
                        setExchangeStatus(ExchangeStatus.DROPPING);
                    meet(rm, time, truck);
                    setActivityStatus(ActivityStatus.END_TICK);
                    break;
                case DROPPING:
                    drop(pm, time, truck);
                    setActivityStatus(ActivityStatus.END_TICK);
                    break;
                case PICKUP:
                    pickUp(rm, pm, time, truck);
                    status = ExchangeStatus.INITIAL;
                    setActivityStatus(ActivityStatus.END_TICK);
                    break;
            }
        } else {
            switch (status){
                case INITIAL:
                    List<ExchangeRequestMessage> messages1 = messageStore.retrieve(ExchangeRequestMessage.class);
                    //At all times there should only be one message of this type.
                    ExchangeRequestMessage request = messages1.get(0);
                    if(truck.getNbOfParcels() > 0){
                        otherTruck = (DeliveryTruck) request.getSender();
                        truck.send(otherTruck, new ExchangeReplyMessage(truck, pm.getContents(truck)));
                        setExchangeStatus(ExchangeStatus.PENDING);
                    }
                    setActivityStatus(ActivityStatus.END_TICK);
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
                    setExchangeStatus(ExchangeStatus.MEETING);
                    setActivityStatus(ActivityStatus.END_TICK);
                case MEETING:
                    if(rm.getObjectsAt(truck,DeliveryTruck.class).contains(otherTruck))
                        setExchangeStatus(ExchangeStatus.EXCHANGING);
                    meet(rm,time,truck);
                    break;
                case DROPPING:
                    drop(pm, time, truck);
                    setActivityStatus(ActivityStatus.END_TICK);
                    break;
                case PICKUP:
                    pickUp(rm, pm, time, truck);
                    reset();
                    break;
            }
        }
    }

    private void reset() {
        meetingPoint = null;
        otherTruck = null;
        dropList = new ArrayList<Point>();
        pickupList = new ArrayList<Point>();
        status=ExchangeStatus.INITIAL;
        setActivityStatus(ActivityStatus.END_TICK);
    }

    private void drop(PDPModel pm, TimeLapse time, DeliveryTruck truck) {
        if(!dropList.isEmpty()){
            if(pm.getVehicleState(truck) == PDPModel.VehicleState.IDLE ){
                Point p = dropList.remove(0);
                for(Parcel parcel :pm.getContents(truck)){
                   if(parcel.getDestination().equals(p)){
                       pm.drop(truck, parcel, time);
                   }
                }
            }
        } else {
            setExchangeStatus(ExchangeStatus.PICKUP);
        }
    }

    private void pickUp(RoadModel rm, PDPModel pm, TimeLapse time, DeliveryTruck truck) {
        if(!pickupList.isEmpty()){
            if(pm.getVehicleState(truck) == PDPModel.VehicleState.IDLE ){
                Point pickupPoint =null;
                Parcel toPickup =null;
                Set<Parcel> availableParcels = rm.getObjectsAt(truck,Parcel.class);
                for(Point p:pickupList){
                    for(Parcel parcel : availableParcels){
                        if(parcel.getDestination().equals(p)){
                            pickupPoint = p;
                            toPickup = parcel;
                        }
                    }
                }
                if(toPickup !=null){
                    pickupList.remove(pickupPoint);
                    pm.pickup(truck, toPickup, time);
                }
            }
        } else {
            setExchangeStatus(ExchangeStatus.PICKUP);
        }
    }

    private void planExchange(PDPModel pm, DeliveryTruck truck) {
        List<ExchangeReplyMessage> messages = messageStore.retrieve(ExchangeReplyMessage.class);
        //At all times there should only be one message of this type.
        if(!messages.isEmpty()){
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
            setExchangeStatus(ExchangeStatus.MEETING);
            setActivityStatus(ActivityStatus.END_TICK);
        } else {
            setExchangeStatus(ExchangeStatus.INITIAL);
            setActivityStatus(ActivityStatus.END_TICK);
        }
    }

    private void initiateExchange(DeliveryTruck truck) {
        List<DeliveryTruck> trucks = bm.getDetectableTrucks(truck);
        if(trucks.isEmpty())
            return;
        DeliveryTruck otherTruck = trucks.get(0);
        if(otherTruck.ping()){
            truck.send(otherTruck, new ExchangeRequestMessage(truck));
            setExchangeStatus(ExchangeStatus.PENDING);
            setActivityStatus(ActivityStatus.END_TICK);
        }
    }

    private void meet(RoadModel rm, TimeLapse time, DeliveryTruck truck) {
        rm.moveTo(truck, meetingPoint, time);
    }

    private void pending() {
        setExchangeStatus(ExchangeStatus.PLANNING);
        setActivityStatus(ActivityStatus.END_TICK);
    }

    private void setExchangeStatus(ExchangeStatus status){
        this.status = status;
    }

}
