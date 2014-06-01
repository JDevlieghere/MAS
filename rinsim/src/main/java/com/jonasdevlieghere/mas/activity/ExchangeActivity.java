package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.communication.ExchangeReplyMessage;
import com.jonasdevlieghere.mas.communication.ExchangeRequestMessage;
import com.jonasdevlieghere.mas.communication.ExchangeStatus;
import com.jonasdevlieghere.mas.communication.MessageStore;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: dieter
 * Date: 6/1/14
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExchangeActivity extends Activity{

    private static double exchangeRadius = 0.5;
    private ExchangeStatus status;
    private MessageStore messageStore;

    public ExchangeActivity(ActivityUser user, MessageStore messageStore){
        super(user);
        this.messageStore = messageStore;
        this.status = ExchangeStatus.INITIAL;
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, TimeLapse time) {
        DeliveryTruck truck = (DeliveryTruck) getUser();
        if(!truck.isPinged()){
            switch (status){
                case INITIAL:
                    DeliveryTruck otherTruck = getNearestTruck(rm, truck);
                    if(otherTruck != null){
                        //Ensure you are the first and only one to initiate exchange
                        if(otherTruck.ping()){
                            truck.send(otherTruck,new ExchangeRequestMessage(truck));
                        }
                    }
                    break;
                case PENDING:
                    status = ExchangeStatus.MEETING;
                    setStatus(ActivityStatus.END_TICK);
                    break;
                case MEETING:
                    messageStore.retrieve(ExchangeReplyMessage.class);
                    break;
                case EXCHANGING:

                    break;

            }

        } else {
            List<ExchangeRequestMessage> messages = messageStore.retrieve(ExchangeRequestMessage.class);
            ExchangeRequestMessage request = messages.get(0);

        }
    }

    private DeliveryTruck getNearestTruck(RoadModel rm, DeliveryTruck truck){
        Set<DeliveryTruck> allTrucks = rm.getObjectsOfType(DeliveryTruck.class);
        DeliveryTruck bestTruck = null;
        double distance;
        double bestDistance = exchangeRadius;
        for(DeliveryTruck otherTruck : allTrucks){
            distance = Point.distance(otherTruck.getPosition(), truck.getPosition());
            if(distance < bestDistance){
               bestTruck = otherTruck;
               bestDistance = distance;
            }
        }
        return bestTruck;
    }
}
