package com.jonasdevlieghere.mas.strategy.delivery;

import com.google.common.collect.ImmutableSet;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.Cluster;
import com.jonasdevlieghere.mas.common.KMeans;
import com.jonasdevlieghere.mas.common.Scheduler;
import com.jonasdevlieghere.mas.strategy.SchedulingStrategy;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class ClusterDeliveryStrategy implements SchedulingStrategy {

    private static final int K = 2;

    private Scheduler scheduler;
    private final SchedulingStrategy fallback;

    public ClusterDeliveryStrategy(){
        this.fallback = new NearestDeliveryStrategy();
    }

    @Override
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time) {
        try {
            BeaconTruck truck = scheduler.getUser();
            ImmutableSet<Parcel> parcels = pm.getContents(truck);

            ArrayList<Point> points = new ArrayList<Point>();
            for(Parcel parcel : parcels){
                points.add(parcel.getDestination());
            }
            KMeans kMeans = new KMeans(points, K);
            ArrayList<Cluster> clusters = kMeans.getClusters();
            TreeMap<Double, Cluster> clusterTreeMap = new TreeMap<Double, Cluster>();
            for(Cluster cluster: clusters){
                double distance = Point.distance(truck.getPosition(), cluster.getCenter());
                clusterTreeMap.put(distance, cluster);
            }
            System.out.println(clusterTreeMap);
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
        }catch (IllegalArgumentException exc){
            return fallback.next(rm, pm, time);
        }catch (NoSuchElementException exc){
            return fallback.next(rm, pm, time);
        }

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
        this.fallback.setScheduler(scheduler);
    }
}
