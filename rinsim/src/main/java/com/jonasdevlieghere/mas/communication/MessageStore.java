package com.jonasdevlieghere.mas.communication;

import rinde.sim.core.model.communication.Message;

import java.util.*;

public class MessageStore {
    private List<Message> messages;
    private static int totalNbOfMessages = 0;

    public MessageStore(){
        messages = new ArrayList<Message>();
    }

    public void store(Message message){
        this.messages.add(message);
        totalNbOfMessages++;
    }

    public <Y extends Message> List<Y> retrieve(Class<Y> clazz){
        List<Y> result = getMessages(clazz);
        messages.removeAll(result);
        return result;
    }

    private <Y extends Message> List<Y> getMessages(Class<Y> clazz) {
        List<Y> result = new ArrayList<Y>();
        for(Message msg : messages){
            if(clazz.isInstance(msg)){
                result.add((Y) msg);
            }
        }
        return result;
    }

    public int getSize(Class clazz){
        return getMessages(clazz).size();
    }

    public int getSize(){
        return messages.size();
    }


    public static int retrieveNbOfMessages(){
        int result = totalNbOfMessages;
        this.totalNbOfMessages = 0;
        return result;
    }
}
