package com.jonasdevlieghere.mas.communication;

import rinde.sim.core.model.communication.Message;

import java.util.*;

public class MessageStore {
    private List<Message> messages;

    public MessageStore(){
        messages = new ArrayList<Message>();
    }

    public void store(Message message){
        this.messages.add(message);
    }

    public List<Message> retrieve(Class clazz){
        List<Message> result = new ArrayList<Message>();
        for(Message msg : messages){
            if(clazz.isInstance(msg)){
                result.add(msg);
            }
        }
        messages.removeAll(result);
        return result;
    }
}
