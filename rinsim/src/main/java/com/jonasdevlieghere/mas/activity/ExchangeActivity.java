package com.jonasdevlieghere.mas.activity;

import com.google.common.collect.ImmutableSet;
import com.jonasdevlieghere.mas.beacon.BeaconStatus;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.Cluster;
import com.jonasdevlieghere.mas.common.KMeans;
import com.jonasdevlieghere.mas.common.TickStatus;
import com.jonasdevlieghere.mas.communication.*;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.TripleDJPDPModel;
import rinde.sim.core.model.road.RoadModel;

import java.util.ArrayList;
import java.util.List;

public class ExchangeActivity extends Activity{

    final Logger logger = LoggerFactory.getLogger(ExchangeActivity.class);
    private ExchangeStatus status;
    private MessageStore messageStore;
    private Point meetingPoint;
    private ArrayList<Point> myDropList;
    private ArrayList<Point> myPickupList;
    BeaconTruck otherTruck;

    public ExchangeActivity(ActivityUser user, MessageStore messageStore){
        super(user);
        this.messageStore = messageStore;
        setExchangeStatus(ExchangeStatus.INITIATE);
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time) {
        //Reset activity status
        setActivityStatus(TickStatus.NORMAL);
        BeaconTruck truck = (BeaconTruck) getUser();
        switch (truck.getStatus()){
            case ACTIVE:
                assert(status == ExchangeStatus.INITIATE);
                masterInitiate(truck, bm);
                break;
            case MASTER:
                switch (status){
                    case PENDING:
                        pending();
                        break;
                    case PLANNING:
                        masterPlanning(pm, truck);
                        break;
                    case MEETING:
                        meeting(rm, time, truck);
                        break;
                    case EXCHANGING:
                        //EXCHANGING
                        masterExchanging(pm, truck, time);
                        break;
                    case RESETTING:
                        if(!bm.getDetectableTrucks(truck).contains(otherTruck)){
                            reset(truck);
                        }
                        break;
                    default:
                        logger.warn("This shouldn't happen at the master:" + status);
                        assert(false);
                        break;
                }
                break;
            case SLAVE:
                switch (status){
                    case INITIATE:
                        slaveInitiate(pm, truck);
                        break;
                    case PENDING:
                        pending();
                        break;
                    case PLANNING:
                        slavePlanning();
                        break;
                    case MEETING:
                        meeting(rm, time, truck);
                        break;
                    case EXCHANGING:
                        slaveExchanging();
                        break;
                    default:
                        logger.warn("This shouldn't happen at the slave:" + status);
                        assert(false);
                }
                break;
            case INACTIVE:
                break;
        }
    }

    private void slaveInitiate(PDPModel pm, BeaconTruck truck) {
        List<ExchangeRequestMessage> messages1 = messageStore.retrieve(ExchangeRequestMessage.class);
        if(!messages1.isEmpty()){
            //At all times there should only be one message of this type.
            ExchangeRequestMessage request = messages1.get(0);
            otherTruck = (BeaconTruck) request.getSender();
            truck.send(otherTruck, new ExchangeReplyMessage(truck, pm.getContents(truck)));
            setExchangeStatus(ExchangeStatus.PENDING);
            setActivityStatus(TickStatus.END_TICK);
        }
    }

    private void slaveExchanging() {
        setExchangeStatus(ExchangeStatus.INITIATE);
        setActivityStatus(TickStatus.END_TICK);
    }

    private void slavePlanning() {
        List<ExchangeAssignmentMessage> messages2 = messageStore.retrieve(ExchangeAssignmentMessage.class);
        //At all times there should only be one message of this type.
        ExchangeAssignmentMessage assignment = messages2.get(0);
        meetingPoint = assignment.getMeetingPoint();
        //No useful masterExchanging possible
        if(meetingPoint == null){
            setExchangeStatus(ExchangeStatus.INITIATE);
        } else {
            setExchangeStatus(ExchangeStatus.MEETING);
        }
        setActivityStatus(TickStatus.END_TICK);
    }

    private void masterExchanging(PDPModel pm, BeaconTruck truck, TimeLapse time) {
        logger.debug("BEFORE: " + truck);
        logger.debug("other BEFORE: " + otherTruck);
        ImmutableSet<Parcel> myContents = pm.getContents(truck);
        ImmutableSet<Parcel> otherContents = pm.getContents(otherTruck);
        for(Parcel parcel :myContents){
            if(myDropList.contains(parcel.getDestination())){
                logger.debug("Transshipping to" + parcel);
                ((TripleDJPDPModel) pm).transship(truck,otherTruck,parcel,time);
                myDropList.remove(parcel.getDestination());
            }
        }
        for(Parcel parcel : otherContents){
            if(myPickupList.contains(parcel.getDestination())){
                logger.debug("Transshipping from" + parcel);
                ((TripleDJPDPModel) pm).transship(otherTruck,truck,parcel,time);
                myPickupList.remove(parcel.getDestination());
            }
        }
        logger.debug("AFTER: " + truck);
        logger.debug("other AFTER: " + otherTruck);
        setExchangeStatus(ExchangeStatus.RESETTING);
        setActivityStatus(TickStatus.END_TICK);
    }

    private void reset(BeaconTruck truck) {
        otherTruck.setStatus(BeaconStatus.ACTIVE);
        truck.setStatus(BeaconStatus.ACTIVE);
        setExchangeStatus(ExchangeStatus.INITIATE);
        otherTruck = null;
        meetingPoint = null;
        myDropList = new ArrayList<Point>();
        myPickupList = new ArrayList<Point>();
    }

    private void masterPlanning(PDPModel pm, BeaconTruck truck) {
        List<ExchangeReplyMessage> messages = messageStore.retrieve(ExchangeReplyMessage.class);
        //At all times there should only be one message of this type.
        //Also guaranteed reply.
        ExchangeReplyMessage reply = messages.get(0);
        assert(otherTruck.equals(reply.getSender()));
        ArrayList<Point> pointList = new ArrayList<Point>();
        int mySize = truck.getNbOfParcels();
        int otherSize = reply.getParcels().size();
        int sum = mySize + otherSize;
        if(sum >2 || (sum == 2 && (mySize == 0 || otherSize == 0))){
            ImmutableSet<Parcel> myParcels = pm.getContents(truck);
            ImmutableSet<Parcel> otherParcels = reply.getParcels();
            for(Parcel p: myParcels){
                pointList.add(p.getDestination());
            }

            for(Parcel p:otherParcels){
                pointList.add(p.getDestination());
            }
            KMeans km = new KMeans(pointList ,2, 123);
            ArrayList<Cluster> clusters = km.getClusters();
            ArrayList<Point> myPoints = clusters.get(0).getPoints();
            ArrayList<Point> otherPoints = clusters.get(1).getPoints();

            myPickupList = new ArrayList<Point>();
            myDropList = new ArrayList<Point>();
            Point dest;

            for(Parcel parcel: myParcels){
                dest = parcel.getDestination();
                if(otherPoints.contains(dest)){
                   myDropList.add(dest);
                }
            }

            for(Parcel parcel: otherParcels){
                dest = parcel.getDestination();
                if(myPoints.contains(dest)){
                    myPickupList.add(dest);
                }
            }

            double newX =  (truck.getPosition().x + otherTruck.getPosition().x)/2;
            double newY =  (truck.getPosition().y + otherTruck.getPosition().y)/2;
            meetingPoint = new Point(newX, newY);
            truck.send(otherTruck, new ExchangeAssignmentMessage(truck, meetingPoint));
            setExchangeStatus(ExchangeStatus.MEETING);
            setActivityStatus(TickStatus.END_TICK);
        } else {
            //let other truck know to end the masterExchanging
            truck.send(otherTruck, new ExchangeAssignmentMessage(truck, null));
            setExchangeStatus(ExchangeStatus.RESETTING);
            setActivityStatus(TickStatus.END_TICK);
        }
    }

    private void masterInitiate(BeaconTruck truck, BeaconModel bm) {
        List<BeaconTruck> trucks = bm.getDetectableTrucks(truck);
        if(trucks.isEmpty())
            return;
        if(trucks.get(0).ping()){
            otherTruck = trucks.get(0);
            truck.send(otherTruck, new ExchangeRequestMessage(truck));
            truck.setStatus(BeaconStatus.MASTER);
            setExchangeStatus(ExchangeStatus.PENDING);
            setActivityStatus(TickStatus.END_TICK);
        }
    }

    private void meeting(RoadModel rm, TimeLapse time, BeaconTruck truck) {
        if(rm.getObjectsAt(truck,BeaconTruck.class).contains(otherTruck))
            setExchangeStatus(ExchangeStatus.EXCHANGING);
        rm.moveTo(truck, meetingPoint, time);
        setActivityStatus(TickStatus.END_TICK);
    }

    private void pending() {
        setExchangeStatus(ExchangeStatus.PLANNING);
        setActivityStatus(TickStatus.END_TICK);
    }

    private void setExchangeStatus(ExchangeStatus status){
        this.status = status;
    }

}
