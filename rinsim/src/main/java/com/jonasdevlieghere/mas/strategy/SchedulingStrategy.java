package com.jonasdevlieghere.mas.strategy;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.common.Scheduler;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public interface SchedulingStrategy {

    /**
     * Get the next BeaconParcel based on this SchedulingStrategy
     *
     * @param   rm
     *          The RoadModel
     * @param   pm
     *          The PDPModel
     * @param   time
     *          The current TimeLapse
     * @return  The next BeaconParcel based on this SchedulingStrategy
     */
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time);

    /**
     * Set the scheduler associated with this SchedulingStrategy
     *
     * @param   scheduler
     *          The scheduler using this SchedulingStrategy
     */
    public void setScheduler(Scheduler scheduler);

}
