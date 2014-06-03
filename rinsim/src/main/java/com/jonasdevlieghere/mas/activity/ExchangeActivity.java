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
import rinde.sim.core.model.pdp.TripleDJPDPModel;
import rinde.sim.core.model.road.RoadModel;

import java.util.ArrayList;
import java.util.List;

public class ExchangeActivity extends Activity{

    private ExchangeStatus status;
    private MessageStore messageStore;
    private Point meetingPoint;
    private ArrayList<Point> myDropList;
    private ArrayList<Point> myPickupList;
    DeliveryTruck otherTruck;

    public ExchangeActivity(ActivityUser user, MessageStore messageStore){
        super(user);
        this.messageStore = messageStore;
        setExchangeStatus(ExchangeStatus.INITIAL);
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time) {
        //Reset activity status
        setActivityStatus(ActivityStatus.NORMAL);
        DeliveryTruck truck = (DeliveryTruck) getUser();
        //Master of the exchange
        switch (truck.getStatus()){
            case ACTIVE:
                assert(status == ExchangeStatus.INITIAL);
                initiateExchange(truck, bm);
                break;
            case MASTER:
                switch (status){
                    case PENDING:
                        pending();
                        break;
                    case PLANNING:
                        planExchange(pm, truck);
                        break;
                    case MEETING:
                        if(rm.getObjectsAt(truck,DeliveryTruck.class).contains(otherTruck))
                            setExchangeStatus(ExchangeStatus.EXCHANGING);
                        meet(rm, time, truck);
                        setActivityStatus(ActivityStatus.END_TICK);
                        break;
                    case EXCHANGING:
                        System.out.println("EXCHANGING");
                        //EXCHANGING
                        exchange(pm,truck,time);
                        status = ExchangeStatus.RESETTING;
                        setActivityStatus(ActivityStatus.END_TICK);
                        break;
                    case RESETTING:
                        if(!bm.getDetectableTrucks(truck).contains(otherTruck)){
                            reset(truck);
                        }
                        break;
                    default:
                        System.out.println("This case is strange: " + status);
                        break;
                }
                break;
            case SLAVE:
                switch (status){
                    case INITIAL:
                        List<ExchangeRequestMessage> messages1 = messageStore.retrieve(ExchangeRequestMessage.class);
                        if(!messages1.isEmpty()){
                            //At all times there should only be one message of this type.
                            ExchangeRequestMessage request = messages1.get(0);
                            System.out.println("Request recieved");
                            //TODO: Always send reply for gracefull reset.
                            if(truck.getNbOfParcels() > 0){
                                otherTruck = (DeliveryTruck) request.getSender();
                                System.out.println("Replyed");
                                truck.send(otherTruck, new ExchangeReplyMessage(truck, pm.getContents(truck)));
                                setExchangeStatus(ExchangeStatus.PENDING);
                            }
                            setActivityStatus(ActivityStatus.END_TICK);
                        }
                        break;
                    case PENDING:
                        pending();
                        break;
                    case PLANNING:
                        List<ExchangeAssignmentMessage> messages2 = messageStore.retrieve(ExchangeAssignmentMessage.class);
                        //At all times there should only be one message of this type.
                        ExchangeAssignmentMessage assignment = messages2.get(0);
                        myDropList = assignment.getDropList();
                        myPickupList = assignment.getPickupList();
                        meetingPoint = assignment.getMeetingPoint();
                        setExchangeStatus(ExchangeStatus.MEETING);
                        setActivityStatus(ActivityStatus.END_TICK);
                    case MEETING:
                        if(rm.getObjectsAt(truck,DeliveryTruck.class).contains(otherTruck))
                            setExchangeStatus(ExchangeStatus.EXCHANGING);
                        meet(rm,time,truck);
                        setActivityStatus(ActivityStatus.END_TICK);
                        break;
                    case EXCHANGING:
                        //See whether this isn't too fast.
                        setExchangeStatus(ExchangeStatus.INITIAL);
                        setActivityStatus(ActivityStatus.END_TICK);
                        break;
                }
                break;
            case INACTIVE:
                break;
        }
    }

    private void exchange(PDPModel pm, DeliveryTruck truck, TimeLapse time) {
        for(Parcel parcel :pm.getContents(truck)){
            if(myDropList.contains(parcel.getDestination())){
                ((TripleDJPDPModel) pm).transship(truck,otherTruck,parcel,time);
                myDropList.remove(parcel.getDestination());
            }
        }
        for(Parcel parcel :pm.getContents(otherTruck)){
            if(myPickupList.contains(parcel.getDestination())){
                ((TripleDJPDPModel) pm).transship(otherTruck,truck,parcel,time);
                myPickupList.remove(parcel.getDestination());
            }
        }
    }

    private void reset(DeliveryTruck truck) {
        otherTruck.setStatus(BeaconStatus.ACTIVE);
        otherTruck = null;
        truck.setStatus(BeaconStatus.ACTIVE);
        setExchangeStatus(ExchangeStatus.INITIAL);
        meetingPoint = null;
        myDropList = new ArrayList<Point>();
        myPickupList = new ArrayList<Point>();
    }

    private void planExchange(PDPModel pm, DeliveryTruck truck) {
        List<ExchangeReplyMessage> messages = messageStore.retrieve(ExchangeReplyMessage.class);
        //At all times there should only be one message of this type.
        if(!messages.isEmpty()){
            ExchangeReplyMessage reply = messages.get(0);
            assert(otherTruck.equals(reply.getSender()));
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
                   myPickupList.add(dest);
                }
            }

            for(Parcel parcel: otherParcels){
                Point dest = parcel.getDestination();
                if(myPoints.contains(dest)){
                    myDropList.add(dest);
                }
            }

            this.myDropList = myDropList;
            this.myPickupList = myPickupList;

            double newX =  (truck.getPosition().x + otherTruck.getPosition().x)/2;
            double newY =  (truck.getPosition().y + otherTruck.getPosition().y)/2;
            meetingPoint = new Point(newX, newY);
            truck.send(otherTruck, new ExchangeAssignmentMessage(truck, otherDropList,otherPickupList, meetingPoint));
            setExchangeStatus(ExchangeStatus.MEETING);
            setActivityStatus(ActivityStatus.END_TICK);
        } else {
            truck.setStatus(BeaconStatus.ACTIVE);
            setExchangeStatus(ExchangeStatus.INITIAL);
            setActivityStatus(ActivityStatus.END_TICK);
        }
    }

    private void initiateExchange(DeliveryTruck truck, BeaconModel bm) {
        List<DeliveryTruck> trucks = bm.getDetectableTrucks(truck);
        //TODO: might be more usefull to put it in getDetectableTrucks()
        trucks.remove(truck);
        if(trucks.isEmpty())
            return;
        if(trucks.get(0).ping()){
            otherTruck = trucks.get(0);
            truck.send(otherTruck, new ExchangeRequestMessage(truck));
            truck.setStatus(BeaconStatus.MASTER);
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
