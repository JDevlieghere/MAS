package com.jonasdevlieghere.mas.cluster;

import rinde.sim.core.graph.Point;

import java.util.ArrayList;

/**
 * Created by jonas on 01/06/14.
 */
public class KMeansTest {

    public static void main(String[] args){
        ArrayList<Point> points = new ArrayList<Point>();
        points.add(new Point(1,1));
        points.add(new Point(1,1));
        KMeans km = new KMeans(points, 2);
        for(Cluster c: km.getClusters()){
            System.out.println(c);
        }
    }
}
