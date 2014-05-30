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
    private Cost cost;

    public ParticipationReplyMessage(CommunicationUser sender, ParticipationRequestMessage request, Cost cost){
        super(sender);
        this.request = request;
        this.cost = cost;
    }

    public ParticipationRequestMessage getRequest(){
        return request;
    }

    public Cost getCost() {
        return cost;
    }
}