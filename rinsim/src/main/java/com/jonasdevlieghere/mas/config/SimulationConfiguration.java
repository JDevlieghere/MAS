package com.jonasdevlieghere.mas.config;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import org.apache.commons.math3.random.MersenneTwister;
import rinde.sim.core.Simulator;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.pdptw.common.AddParcelEvent;
import rinde.sim.pdptw.common.AddVehicleEvent;
import rinde.sim.pdptw.common.DynamicPDPTWProblem;
import rinde.sim.pdptw.experiment.DefaultMASConfiguration;
import rinde.sim.util.SupplierRng;

public class SimulationConfiguration extends DefaultMASConfiguration {

    private RuntimeConfiguration runtimeConfiguration;

    public SimulationConfiguration(RuntimeConfiguration runtimeConfiguration){
        this.runtimeConfiguration = runtimeConfiguration;
    }

    @Override
    public ImmutableList<? extends SupplierRng<? extends Model<?>>> getModels() {
        return ImmutableList.of(BeaconModel.supplier(), new CommunicationModelSupplier());
    }

    @Override
    public DynamicPDPTWProblem.Creator<AddVehicleEvent> getVehicleCreator() {
        return new DynamicPDPTWProblem.Creator<AddVehicleEvent>() {
            @Override
            public boolean create(Simulator sim, AddVehicleEvent event) {
                return sim.register(new BeaconTruck(event.vehicleDTO,
                        sim.getRandomGenerator().nextInt(),
                        runtimeConfiguration.getBeaconRadius(),
                        runtimeConfiguration.getCommunicationReliability(),
                        runtimeConfiguration.getCommunicationRadius(),
                        runtimeConfiguration.getPickupStrategy(),
                        runtimeConfiguration.getDeliveryStrategy(),
                        runtimeConfiguration.isDoExchange(),
                        runtimeConfiguration.isDoExplore()));
            }
        };
    }

    @Override
    public Optional<? extends DynamicPDPTWProblem.Creator<AddParcelEvent>> getParcelCreator() {
        return Optional.of(new DynamicPDPTWProblem.Creator<AddParcelEvent>() {
            @Override
            public boolean create(Simulator sim, AddParcelEvent event) {
                return sim.register(new BeaconParcel(event.parcelDTO, runtimeConfiguration.getBeaconRadius()));
            }
        });
    }

    private static final class CommunicationModelSupplier implements SupplierRng<CommunicationModel> {
        @Override public CommunicationModel get(long seed) {
            return new CommunicationModel(new MersenneTwister(seed), true);
        }
    }
}
