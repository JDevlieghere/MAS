package com.jonasdevlieghere.mas.communication;

import com.google.common.collect.ImmutableSet;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.Parcel;

public class ExchangeReplyMessage extends Message{
    private ImmutableSet<Parcel> parcels;

    public ExchangeReplyMessage(CommunicationUser sender ) {
        super(sender);
    }

    public ExchangeReplyMessage(CommunicationUser sender, ImmutableSet<Parcel> contents) {
        super(sender);
        parcels = contents;
    }

    public ImmutableSet<Parcel> getParcels(){
       return parcels;
    }
}
