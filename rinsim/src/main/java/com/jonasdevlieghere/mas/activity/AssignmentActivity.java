package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.communication.AssignmentMessage;
import com.jonasdevlieghere.mas.communication.MessageStore;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dieter
 * Date: 5/29/14
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class AssignmentActivity extends Activity {

    private MessageStore messageStore;

    public AssignmentActivity(ActivityUser user, MessageStore messageStore){
        super(user);
        this.messageStore = messageStore;
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, TimeLapse time) {
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
