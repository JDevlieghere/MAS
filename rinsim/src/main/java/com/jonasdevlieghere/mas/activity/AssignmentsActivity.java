package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.communication.AssignmentMessage;
import com.jonasdevlieghere.mas.communication.MessageStore;
import rinde.sim.core.model.communication.Message;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dieter
 * Date: 5/29/14
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class AssignmentsActivity extends Activity {

    private MessageStore messageStore;

    public AssignmentsActivity(ActivityUser user, MessageStore messageStore){
        super(user);
        this.messageStore = messageStore;
    }

    @Override
    public void execute() {
        List<Message> messages = messageStore.retrieve(AssignmentMessage.class);
        DeliveryTruck truck = (DeliveryTruck)getUser();
        for(Message msg : messages){
            try {
                AssignmentMessage assignmentMessage = (AssignmentMessage) msg;
                truck.queuePickup((BeaconParcel) assignmentMessage.getParcel());
            } catch (ClassCastException e){
                // NOP
            }
        }
    }
}
