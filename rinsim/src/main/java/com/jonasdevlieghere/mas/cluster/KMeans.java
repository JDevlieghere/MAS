package com.jonasdevlieghere.mas.cluster;


import rinde.sim.core.graph.Point;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class KMeans {


    private ArrayList<Point> points;
    private ArrayList<Point> centroids;
    private ArrayList<Cluster> clusters;
    private int k;

    public KMeans(ArrayList<Point> points, int k) {
        setNbClusters(k);
        setPoints(points);
    }

    private void setNbClusters(int k){
        if(k <= 1)
            throw new IllegalArgumentException("The amount of clusters should be greater than 1.");
        this.k = k;
    }

    public int getNbClusters(){
        return this.k;
    }


    private void setPoints(ArrayList<Point> points) {
        if(points.size() <= getNbClusters())
            throw new IllegalArgumentException("The amount of points should be greater than or equal to the amount of clusters.");
        this.points = points;
    }

    /**
     * Guess an initial position for the centroids of the k clusters.
     */
    private void initCentroids(){
        this.centroids = new ArrayList<Point>();
        Cluster initial = new Cluster(this.points);
        for(int i = 0; i < getNbClusters(); i++){
            Point c = initial.getCenter();
            double x = c.x*Math.random();
            double y = c.y*Math.random();
            centroids.add(new Point(x,y));
        }
    }

    /**
     * Create k empty clusters
     */
    private void initClusters() {
        this.clusters = new ArrayList<Cluster>();
        for (int i = 0; i < getNbClusters(); i++) {
            clusters.add(new Cluster());
        }
    }

    public ArrayList<Cluster> getClusters(){
        initCentroids();
        boolean improved = true;
        while(improved){
            /**
             * Assign each point to the cluster with the cluster with the nearest centroid.
             */
            initClusters();
            for(Point p : points){
                double bestDistance = Double.POSITIVE_INFINITY;
                int cluster = -1;
                for(int i = 0; i < getNbClusters(); i++){
                    double distance = Point.distance(p, centroids.get(i));
                    if(distance < bestDistance){
                        bestDistance = distance;
                        cluster = i;
                    }
                }
                clusters.get(cluster).add(p);
            }
            /**
             * Update the centroid of each cluster according to the points currently assigned.
             * If all elements are assigned to a single cluster, start over with a new approximation.
             */
            ArrayList<Point> newCentroids = new ArrayList<Point>();
            try{
                for(int i = 0; i < getNbClusters(); i++){
                    newCentroids.add(clusters.get(i).getCenter());
                }
                improved = !centroids.equals(newCentroids);
                this.centroids = newCentroids;
            }catch (NoSuchElementException e){
                initCentroids();
            }
        }
        return this.clusters;
    }
}