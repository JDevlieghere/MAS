package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.ActionUser;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.communication.Assignment;
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
    private ActionUser truck;

    public AssignmentsActivity(ActivityUser user, MessageStore messageStore, ActionUser truck){
        super(user);
        this.messageStore = messageStore;
    }

    @Override
    public void execute() {
        List<Message> messages = messageStore.retrieve(Assignment.class);
        for(Message msg : messages){
            try {
                Assignment assignment = (Assignment) msg;
                truck.queuePickup((BeaconParcel) assignment.getParcel());
            } catch (ClassCastException e){
                // NOP
            }
        }
        return;
    }

}
