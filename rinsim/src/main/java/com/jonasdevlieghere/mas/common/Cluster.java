package com.jonasdevlieghere.mas.common;

import rinde.sim.core.graph.Point;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class Cluster {

    private ArrayList<Point> points;

    Cluster(){
        this(new ArrayList<Point>());
    }

    Cluster(ArrayList<Point> points){
        this.points = points;
    }

    public void add(Point point){
        this.points.add(point);
    }

    public ArrayList<Point> getPoints(){
        return this.points;
    }

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

        if (!getPoints().equals(cluster.getPoints())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return points.hashCode();
    }
}
