package com.jonasdevlieghere.mas.communication;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

public class ParticipationRequestMessage extends Message{

    private BeaconParcel parcel;

    public ParticipationRequestMessage(CommunicationUser sender, BeaconParcel auctionableParcel){
        super(sender);
        setAuctionableParcel(auctionableParcel);
    }

    void setAuctionableParcel(BeaconParcel parcel){
        this.parcel = parcel;
    }

    public BeaconParcel getAuctionableParcel(){
        return parcel;
    }
}
