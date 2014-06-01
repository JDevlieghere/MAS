package com.jonasdevlieghere.mas.activity;

import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public interface ActivityUser {

    public boolean endsTick(Activity activity, RoadModel rm, PDPModel pm, TimeLapse time);

}
