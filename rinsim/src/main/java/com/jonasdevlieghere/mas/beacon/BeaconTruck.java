package com.jonasdevlieghere.mas.beacon;

import com.jonasdevlieghere.mas.action.*;
import com.jonasdevlieghere.mas.activity.*;
import com.jonasdevlieghere.mas.common.TickStatus;
import com.jonasdevlieghere.mas.communication.MessageStore;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.DefaultVehicle;
import rinde.sim.pdptw.common.VehicleDTO;

import java.util.HashSet;
import java.util.Set;

public class BeaconTruck extends DefaultVehicle implements Beacon, CommunicationUser, ActionUser, ActivityUser {

    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(BeaconTruck.class);

    /**
     * Constants
     */
    private static final double RADIUS = 0.7;
    private static final double RELIABILITY = 1;

    /**
     * Models
     */
    private BeaconModel bm;
    private CommunicationAPI ca;

    /**
     * Random Generator
     */
    private final RandomGenerator rand;
    private static int count = 0;

    /**
     * Parcels ready for pickup by this DeliveryTruck
     */
    private Set<BeaconParcel> pickupQueue;

    /**
     * Message Storage
     */
    private MessageStore messageStore;

    /**
     * Activities
     */
    private AuctionActivity auctionActivity;
    private AssignmentActivity assignmentActivity;
    private TransportActivity transportActivity;
    private FetchActivity fetchActivity;
    private ExchangeActivity exchangeActivity;

    /**
     * Point for Exploration Activity
     */
    private Point explorationDestination;

    /**
     * Beacon Status
     */
    private BeaconStatus status;

    public BeaconTruck(VehicleDTO pDto) {
        super(pDto);
        this.messageStore = new MessageStore();
        this.pickupQueue = new HashSet<BeaconParcel>();
        this.rand = new MersenneTwister(123*count++);
        this.auctionActivity = new AuctionActivity(this, messageStore);
        this.assignmentActivity = new AssignmentActivity(this, messageStore);
        this.transportActivity = new TransportActivity(this);
        this.fetchActivity = new FetchActivity(this);
        this.exchangeActivity = new ExchangeActivity(this,messageStore);
        this.setStatus(BeaconStatus.ACTIVE);
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = roadModel.get();
        final PDPModel pm = pdpModel.get();

        if(endsTick(assignmentActivity, rm, pm, bm, time))
            return;

        if(endsTick(exchangeActivity, rm, pm, bm, time))
            return;

        if(endsTick(new PickupAction(rm, pm ,this), time))
            return;

        if(endsTick(new DeliverAction(rm, pm ,this), time))
            return;

        if(endsTick(fetchActivity, rm, pm, bm, time))
            return;

        if(endsTick(transportActivity, rm, pm, bm, time))
            return;

        if(endsTick(new DiscoverAction(rm, pm, bm, auctionActivity, this), time))
            return;

        if(endsTick(auctionActivity, rm, pm, bm, time))
            return;

//        logger.info(this.toString());

//        if(endsTick(new ReturnAction(rm, pm, bm, dto, this), time))
//            return;

//        if(endsTick(new SmartExploreAction(rm, pm, this, this.rand), time))
//            return;
    }


    public void queuePickup(BeaconParcel parcel){
        this.pickupQueue.add(parcel);
    }

    public Set<BeaconParcel> getPickupQueue(){
        return new HashSet<BeaconParcel>(this.pickupQueue);
    }

    public void unqueuePickup(BeaconParcel parcel){
        this.pickupQueue.remove(parcel);
    }

    @Override
    public double getRadius() {
        return RADIUS;
    }

    @Override
    public double getReliability() {
        return RELIABILITY;
    }

    @Override
    public void receive(Message message) {
        messageStore.store(message);
    }

    @Override
    public void setCommunicationAPI(CommunicationAPI api) {
        this.ca = api;
    }

    @Override
    public Point getPosition() {
        return roadModel.get().getPosition(this);
    }

    @Override
    public BeaconStatus getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(BeaconStatus status) {
        this.status = status;
    }

    @Override
    public boolean ping(){
        if(getStatus() == BeaconStatus.ACTIVE){
            setStatus(BeaconStatus.SLAVE);
            return true;
        }
        return false;
    }

    @Override
    public void setModel(BeaconModel model) {
        this.bm = model;
    }

    public void send(CommunicationUser recipient, Message message){
        ca.send(recipient, message);
    }

    public void broadcast(Message message){
         ca.broadcast(message);
    }

    @Override
    public String toString() {
        try{
            return "DeliveryTruck("+this.hashCode()+"){Pickups: " +this.getPickupQueue().size()+ ", Parcels: "+ getNbOfParcels() +"}";
        }catch (IllegalStateException e){
            return "DeliveryTruck("+this.hashCode()+")";
        }
    }

    public int getNbOfParcels() {
        return getPDPModel().getContents(this).size();
    }

    @Override
    public boolean endsTick(Action action, TimeLapse time) {
        action.execute(time);
        if(action.getStatus() == TickStatus.END_TICK)
            return true;
        return false;
    }

    @Override
    public boolean endsTick(Activity activity, RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time) {
        activity.execute(rm, pm, bm, time);
        if(activity.getStatus() == TickStatus.END_TICK)
            return true;
        return false;
    }

    public Point getExplorationDestination(){
        return explorationDestination;
    }

    public void setExplorationDestination(Point destination) {
             explorationDestination = destination;
    }
}