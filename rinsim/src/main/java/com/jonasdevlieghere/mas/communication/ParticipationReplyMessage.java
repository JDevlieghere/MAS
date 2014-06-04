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

    private final ParticipationRequestMessage request;
    private final AuctionCost auctionCost;

    public ParticipationReplyMessage(CommunicationUser sender, ParticipationRequestMessage request, AuctionCost auctionCost){
        super(sender);
        this.request = request;
        this.auctionCost = auctionCost;
    }

    public ParticipationRequestMessage getRequest(){
        return request;
    }

    public AuctionCost getAuctionCost() {
        return auctionCost;
    }
}