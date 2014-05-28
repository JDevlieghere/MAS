package com.jonasdevlieghere.mas.communication;

import rinde.sim.core.model.communication.Message;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: dieter
 * Date: 5/28/14
 * Time: 3:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class MessageStore {
    private Set<Message> messages;

    public MessageStore(){
        messages = new HashSet<Message>();
    }

    public void addMessages(Queue<Message> messages){
        this.messages.addAll(messages);
    }

    public Set<Message> popAllOfType(Class clazz){
        HashSet<Message> result = new HashSet<Message>();
        for(Message msg : messages){
            if(clazz.isInstance(msg)){
                result.add(msg);
            }
        }
        messages.removeAll(result);
        return result;
    }
}
