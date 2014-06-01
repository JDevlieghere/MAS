package com.jonasdevlieghere.mas.schedule;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public interface SchedulingStrategy {

    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time);

    public void setScheduler(Scheduler scheduler);

}
