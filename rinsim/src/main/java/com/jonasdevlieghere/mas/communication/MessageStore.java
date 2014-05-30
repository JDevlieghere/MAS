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
        System.out.println(this.getSize());
    }

    public List<Message> retrieve(Class clazz){
        List<Message> result = getMessages(clazz);
        messages.removeAll(result);
        return result;
    }

    private List<Message> getMessages(Class clazz) {
        List<Message> result = new ArrayList<Message>();
        for(Message msg : messages){
            if(clazz.isInstance(msg)){
                result.add(msg);
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
}
