package com.jonasdevlieghere.mas.communication;

import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;

public class ExchangeRequestMessage extends Message {
    public ExchangeRequestMessage(CommunicationUser sender) {
        super(sender);
    }
}
