package com.jonasdevlieghere.mas.action;


import rinde.sim.core.TimeLapse;

public interface ActionUser {

    public boolean endsTick(Action action, TimeLapse time);

}
