package com.jonasdevlieghere.mas.communication;

import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

public class ExchangeReplyMessage extends Message{

    public ExchangeReplyMessage(CommunicationUser sender ) {
        super(sender);
    }

}
