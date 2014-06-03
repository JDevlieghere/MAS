package com.jonasdevlieghere.mas.communication;

import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.Message;
public class ExchangeAssignmentMessage extends Message {
    private Point meetingPoint;

    public ExchangeAssignmentMessage(BeaconTruck truck, Point meetingPoint) {
        super(truck);
        setMeetingPoint(meetingPoint);
    }

    public Point getMeetingPoint() {
        return meetingPoint;
    }

    public void setMeetingPoint(Point meetingPoint) {
        this.meetingPoint = meetingPoint;
    }
}
