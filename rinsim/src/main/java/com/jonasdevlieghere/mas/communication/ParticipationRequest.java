package com.jonasdevlieghere.mas.communication;

import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.Parcel;

/**
 * Created with IntelliJ IDEA.
 * User: dieter
 * Date: 5/28/14
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class ParticipationRequest extends Message{
    private Parcel parcel;

    public ParticipationRequest(CommunicationUser sender){
        super(sender);
    }

    public void setAuctionableParcel(Parcel parcel){
        this.parcel = parcel;
    }

    public Parcel getAuctionableParcel(){
        return parcel;
    }
}
