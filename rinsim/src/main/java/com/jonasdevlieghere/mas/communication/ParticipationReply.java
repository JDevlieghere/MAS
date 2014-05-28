package com.jonasdevlieghere.mas.communication;

import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

/**
 * Created with IntelliJ IDEA.
 * User: dieter
 * Date: 5/28/14
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class ParticipationReply extends Message {
    private ParticipationRequest request;
    private double distance;

    public ParticipationReply(CommunicationUser sender, ParticipationRequest request, double distance){
        super(sender);
        this.request = request;
        this.distance = distance;
    }

    public ParticipationRequest getRequest(){
        return request;
    }

    public double getDistance(){
        return distance;
    }
}
