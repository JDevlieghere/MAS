package rinde.sim.examples.pdptw.gradientfield;

import com.jonasdevlieghere.mas.BeaconParcel;
import com.jonasdevlieghere.mas.DeliveryTruck;
import rinde.sim.core.Simulator;
import rinde.sim.core.model.Model;
import rinde.sim.pdptw.common.AddParcelEvent;
import rinde.sim.pdptw.common.AddVehicleEvent;
import rinde.sim.pdptw.common.DynamicPDPTWProblem.Creator;
import rinde.sim.pdptw.experiment.DefaultMASConfiguration;
import rinde.sim.util.SupplierRng;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class GradientFieldConfiguration extends DefaultMASConfiguration {

  @Override
  public ImmutableList<? extends SupplierRng<? extends Model<?>>> getModels() {
    return ImmutableList.of(GradientModel.supplier());
  }

  @Override
  public Creator<AddVehicleEvent> getVehicleCreator() {
    return new Creator<AddVehicleEvent>() {
      @Override
      public boolean create(Simulator sim, AddVehicleEvent event) {
        return sim.register(new DeliveryTruck(event.vehicleDTO));
      }
    };
  }

  @Override
  public Optional<? extends Creator<AddParcelEvent>> getParcelCreator() {
    return Optional.of(new Creator<AddParcelEvent>() {
      @Override
      public boolean create(Simulator sim, AddParcelEvent event) {
        // all parcels are accepted by default
        return sim.register(new BeaconParcel(event.parcelDTO));
      }
    });
  }

}
