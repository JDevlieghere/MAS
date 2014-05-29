package com.jonasdevlieghere.mas.simulation;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.jonasdevlieghere.mas.beacon.ActionUser;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import org.apache.commons.math3.random.MersenneTwister;
import rinde.sim.core.Simulator;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.pdptw.common.AddParcelEvent;
import rinde.sim.pdptw.common.AddVehicleEvent;
import rinde.sim.pdptw.common.DynamicPDPTWProblem;
import rinde.sim.pdptw.experiment.DefaultMASConfiguration;
import rinde.sim.util.SupplierRng;


public class BeaconConfiguration extends DefaultMASConfiguration {


    @Override
    public ImmutableList<? extends SupplierRng<? extends Model<?>>> getModels() {
        return ImmutableList.of(BeaconModel.supplier(), new CommunicationModelSupplier());
    }

    @Override
    public DynamicPDPTWProblem.Creator<AddVehicleEvent> getVehicleCreator() {
        return new DynamicPDPTWProblem.Creator<AddVehicleEvent>() {
            @Override
            public boolean create(Simulator sim, AddVehicleEvent event) {
                return sim.register(new ActionUser(event.vehicleDTO));
            }
        };
    }

    @Override
    public Optional<? extends DynamicPDPTWProblem.Creator<AddParcelEvent>> getParcelCreator() {
        return Optional.of(new DynamicPDPTWProblem.Creator<AddParcelEvent>() {
            @Override
            public boolean create(Simulator sim, AddParcelEvent event) {
                // all parcels are accepted by default
                return sim.register(new BeaconParcel(event.parcelDTO));
            }
        });
    }

    private static final class CommunicationModelSupplier implements SupplierRng<CommunicationModel> {
        @Override public CommunicationModel get(long seed) {
            return new CommunicationModel(new MersenneTwister(seed), true);
        }
    }
}
