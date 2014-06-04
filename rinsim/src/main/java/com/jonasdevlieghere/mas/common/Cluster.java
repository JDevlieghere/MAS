package com.jonasdevlieghere.mas.common;

import rinde.sim.core.graph.Point;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class Cluster {

    private final ArrayList<Point> points;

    /**
     * Create a new Cluster of Points
     */
    Cluster(){
        this(new ArrayList<Point>());
    }

    /**
     * Create a new Cluster of Points
     */
    Cluster(ArrayList<Point> points){
        this.points = points;
    }

    /**
     * Add the given point to this Cluster
     *
     * @param   point
     *          The Point to be added to this Cluster
     */
    public void add(Point point){
        this.points.add(point);
    }

    /**
     * Return the Points in this Cluster
     *
     * @return The Points in this Cluster
     */
    public ArrayList<Point> getPoints(){
        return this.points;
    }

    /**
     * Returns the Centroid of this Cluster
     *
     * @return The Centroid of this Cluster
     */
    public Point getCenter(){
        if(points.isEmpty())
            throw new NoSuchElementException("The empty cluster has no centroid.");
        double x = 0;
        double y = 0;
        for(Point p: points){
            x += p.x;
            y += p.y;
        }
        return new Point(x/points.size(),y/points.size());
    }

    @Override
    public String toString() {
        return "Cluster" +
                 points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cluster cluster = (Cluster) o;

        return getPoints().equals(cluster.getPoints());

    }

    @Override
    public int hashCode() {
        return points.hashCode();
    }
}
