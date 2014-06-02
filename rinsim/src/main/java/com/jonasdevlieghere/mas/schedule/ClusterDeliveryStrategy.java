package com.jonasdevlieghere.mas.schedule;

import com.google.common.collect.ImmutableSet;
import com.jonasdevlieghere.mas.beacon.Beacon;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.cluster.Cluster;
import com.jonasdevlieghere.mas.cluster.KMeans;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.util.Tuple;

import javax.annotation.concurrent.Immutable;
import java.util.*;

public class ClusterDeliveryStrategy implements SchedulingStrategy {

    public static final int K = 2;
    private ArrayList<Cluster> clusters;

    private Scheduler scheduler;

    @Override
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time) {
        DeliveryTruck truck = scheduler.getUser();
        ImmutableSet<Parcel> parcels = pm.getContents(truck);

        ArrayList<Point> points = new ArrayList<Point>();
        for(Parcel parcel : parcels){
            points.add(parcel.getDestination());
        }
        KMeans kMeans = new KMeans(points, K);
        this.clusters = kMeans.getClusters();
        TreeMap<Double, Cluster> clusterTreeMap = new TreeMap<Double, Cluster>();
        for(Cluster cluster: this.clusters){
            double distance = Point.distance(truck.getPosition(), cluster.getCenter());
            clusterTreeMap.put(distance, cluster);
        }
        for(Map.Entry<Double, Cluster> clusterEntry: clusterTreeMap.entrySet()){
            double bestDistance = Double.POSITIVE_INFINITY;
            Parcel bestParcel = null;
            points = clusterEntry.getValue().getPoints();
            for(Point p: points){
                double distance = Point.distance(truck.getPosition(), p);
                if(distance < bestDistance){
                    try {
                        BeaconParcel parcel = getParcel(p, parcels);
                        if(pm.getTimeWindowPolicy().canDeliver(parcel.getDeliveryTimeWindow(),
                                time.getTime(), parcel.getPickupDuration()) ) {
                            bestDistance = distance;
                            bestParcel = parcel;
                        }
                    }catch (NoSuchElementException exc){
                        // NOP
                    }
                }
            }
            if(bestParcel != null)
                return (BeaconParcel)bestParcel;
        }
        return null;
    }

    private BeaconParcel getParcel(Point position, ImmutableSet<Parcel> parcels){
        for(Parcel parcel: parcels){
            if(parcel.getDestination().equals(position))
                return (BeaconParcel)parcel;
        }
        throw new NoSuchElementException();
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}