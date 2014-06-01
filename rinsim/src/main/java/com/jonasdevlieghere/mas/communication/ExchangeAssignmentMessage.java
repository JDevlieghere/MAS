package com.jonasdevlieghere.mas.communication;

import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.Message;

import java.util.ArrayList;
public class ExchangeAssignmentMessage extends Message {
    private ArrayList<Point> dropList;
    private ArrayList<Point> pickupList;
    private Point meetingPoint;

    public ExchangeAssignmentMessage(DeliveryTruck truck, ArrayList<Point> otherDropList, ArrayList<Point> otherPickupList, Point meetingPoint) {
        super(truck);
        setDropList(otherDropList);
        setPickupList(otherPickupList);
        setMeetingPoint(meetingPoint);
    }

    public ArrayList<Point> getDropList() {
        return dropList;
    }

    public void setDropList(ArrayList<Point> dropList) {
        this.dropList = dropList;
    }

    public ArrayList<Point> getPickupList() {
        return pickupList;
    }

    public void setPickupList(ArrayList<Point> pickupList) {
        this.pickupList = pickupList;
    }

    public Point getMeetingPoint() {
        return meetingPoint;
    }

    public void setMeetingPoint(Point meetingPoint) {
        this.meetingPoint = meetingPoint;
    }
}
