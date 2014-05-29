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
public class ParticipationReplyMessage extends Message {

    private ParticipationRequestMessage request;
    private double distance;

    public ParticipationReplyMessage(CommunicationUser sender, ParticipationRequestMessage request, double distance){
        super(sender);
        this.request = request;
        this.distance = distance;
    }

    public ParticipationRequestMessage getRequest(){
        return request;
    }

    public double getDistance(){
        return distance;
    }

}