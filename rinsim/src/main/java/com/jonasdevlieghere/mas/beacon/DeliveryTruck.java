package com.jonasdevlieghere.mas.beacon;

import com.jonasdevlieghere.mas.action.*;
import com.jonasdevlieghere.mas.activity.*;
import com.jonasdevlieghere.mas.communication.MessageStore;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationAPI;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.DefaultVehicle;
import rinde.sim.pdptw.common.VehicleDTO;

import java.util.HashSet;
import java.util.Set;

public class DeliveryTruck extends DefaultVehicle implements Beacon, CommunicationUser, ActionUser, ActivityUser {

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

    private Point explorationDestination;
    private Parcel cheatParcel;

    public DeliveryTruck(VehicleDTO pDto) {
        super(pDto);
        this.messageStore = new MessageStore();
        this.pickupQueue = new HashSet<BeaconParcel>();
        this.rand = new MersenneTwister(123*count++);
        this.auctionActivity = new AuctionActivity(this, messageStore);
        this.assignmentActivity = new AssignmentActivity(this, messageStore);
        this.transportActivity = new TransportActivity(this);
        this.fetchActivity = new FetchActivity(this);
        this.exchangeActivity = new ExchangeActivity(this,bm,messageStore);
        this.setStatus(BeaconStatus.ACTIVE);
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = roadModel.get();
        final PDPModel pm = pdpModel.get();

        if(endsTick(assignmentActivity, rm, pm, time))
            return;

        if(endsTick(exchangeActivity, rm, pm, time))
            return;

        if(endsTick(new PickupAction(rm, pm ,this), time))
            return;

        if(endsTick(new DeliverAction(rm, pm ,this), time))
            return;

        if(endsTick(fetchActivity, rm, pm, time))
            return;

        if(endsTick(transportActivity, rm, pm, time))
            return;

        if(endsTick(new DiscoverAction(rm, pm, bm, this), time))
            return;

        if(endsTick(auctionActivity, rm, pm, time))
            return;

        if(endsTick(new SmartExploreAction(rm, pm, this, this.rand), time))
            return;
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
        return null;
    }

    @Override
    public void setStatus(BeaconStatus status) {

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

    public void addDiscoveredParcel(BeaconParcel parcel) {
        auctionActivity.addDiscoveredParcel(parcel);
    }

    public void addAuctionableParcel(BeaconParcel parcel) {
        auctionActivity.addAuctionableParcel(parcel);
    }

    public void send(CommunicationUser recipient, Message message){
        ca.send(recipient, message);
    }

    public void broadcast(Message message){
         ca.broadcast(message);
    }

    @Override
    public String toString() {
        return "DeliveryTruck("+this.hashCode()+"){Pickups: " +this.getPickupQueue().size()+ ", Parcels: "+ getNbOfParcels() +"}";
    }

    public int getNbOfParcels() {
        return getPDPModel().getContents(this).size();
    }

    @Override
    public boolean endsTick(Action action, TimeLapse time) {
        action.execute(time);
        if(action.getStatus() == ActionStatus.SUCCESS)
            return true;
        return false;
    }

    @Override
    public boolean endsTick(Activity activity, RoadModel rm, PDPModel pm, TimeLapse time) {
        activity.execute(rm, pm, time);
        if(activity.getStatus() == ActivityStatus.END_TICK)
            return true;
        return false;
    }

    public boolean hasDiscovered(BeaconParcel bp) {
        return auctionActivity.hasDiscovered(bp);
    }

    public Point getExplorationDestination(){
        return explorationDestination;
    }

    public void setExplorationDestination(Point destination) {
             explorationDestination = destination;
    }
}