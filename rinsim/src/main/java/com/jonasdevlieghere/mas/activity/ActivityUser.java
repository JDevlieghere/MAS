package com.jonasdevlieghere.mas.activity;

import rinde.sim.core.TimeLapse;

public interface ActivityUser {

    public boolean endsTick(Activity activity, TimeLapse time);

}
