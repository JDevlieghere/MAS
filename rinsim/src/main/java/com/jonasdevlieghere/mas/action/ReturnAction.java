package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.VehicleDTO;

public class ReturnAction extends Action {

    private VehicleDTO dto;

    public ReturnAction(RoadModel rm, PDPModel pm, BeaconModel bm, VehicleDTO dto, ActionUser user) {
        super(rm, pm, bm, user);
        this.dto = dto;
    }

    @Override
    public void execute(TimeLapse time) {
        RoadModel rm = getRoadModel();
        if (rm.getObjectsOfType(Parcel.class).isEmpty()) {
            rm.moveTo((DeliveryTruck)getUser(), dto.startPosition, time);
            setStatus(ActionStatus.SUCCESS);
        }
    }
}
