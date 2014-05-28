package com.jonasdevlieghere.mas.simulation;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import rinde.sim.core.Simulator;
import rinde.sim.core.model.Model;
import rinde.sim.pdptw.common.AddParcelEvent;
import rinde.sim.pdptw.common.AddVehicleEvent;
import rinde.sim.pdptw.common.DynamicPDPTWProblem;
import rinde.sim.pdptw.experiment.DefaultMASConfiguration;
import rinde.sim.util.SupplierRng;


public class BeaconConfiguration extends DefaultMASConfiguration {


    @Override
    public ImmutableList<? extends SupplierRng<? extends Model<?>>> getModels() {
        return ImmutableList.of(BeaconModel.supplier());
    }

    @Override
    public DynamicPDPTWProblem.Creator<AddVehicleEvent> getVehicleCreator() {
        return new DynamicPDPTWProblem.Creator<AddVehicleEvent>() {
            @Override
            public boolean create(Simulator sim, AddVehicleEvent event) {
                return sim.register(new DeliveryTruck(event.vehicleDTO));
            }
        };
    }

    @Override
    public Optional<? extends DynamicPDPTWProblem.Creator<AddParcelEvent>> getParcelCreator() {
        return Optional.of(new DynamicPDPTWProblem.Creator<AddParcelEvent>() {
            @Override
            public boolean create(Simulator sim, AddParcelEvent event) {
                return sim.register(new BeaconParcel(event.parcelDTO));
            }
        });
    }
}