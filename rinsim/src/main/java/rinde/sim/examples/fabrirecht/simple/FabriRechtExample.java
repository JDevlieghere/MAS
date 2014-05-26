/**
 * 
 */
package rinde.sim.examples.fabrirecht.simple;

import java.io.FileReader;
import java.io.IOException;

import rinde.sim.core.Simulator;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.RoadModels;
import rinde.sim.core.model.road.RoadUser;
import rinde.sim.pdptw.common.AddVehicleEvent;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.DefaultVehicle;
import rinde.sim.pdptw.common.DynamicPDPTWProblem;
import rinde.sim.pdptw.common.DynamicPDPTWProblem.Creator;
import rinde.sim.pdptw.common.VehicleDTO;
import rinde.sim.pdptw.fabrirecht.FabriRechtParser;
import rinde.sim.pdptw.fabrirecht.FabriRechtScenario;
import rinde.sim.scenario.ConfigurationException;

import com.google.common.base.Predicate;

/**
 * Simplest example showing how the Fabri & Recht problem can be configured
 * using a custom vehicle.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class FabriRechtExample {

  public static void main2(String[] args) throws IOException,
      ConfigurationException {
    // we load a problem instance from disk, we instantiate it with 8
    // trucks, each with a capacity of 20 units
    final FabriRechtScenario scenario = FabriRechtParser
        .fromJson(new FileReader(
            "../problem/data/test/fabri-recht/lc101.scenario"), 8, 20);

    // instantiate the problem using the scenario and a random seed (which
    // will not be used in this example)
    final DynamicPDPTWProblem problem = new DynamicPDPTWProblem(scenario, 123);

    // we plug our custom vehicle in by specifying a creator
    problem.addCreator(AddVehicleEvent.class, new Creator<AddVehicleEvent>() {
      @Override
      public boolean create(Simulator sim, AddVehicleEvent event) {
        return sim.register(new Truck(event.vehicleDTO));
      }
    });

    // enable the default UI
    problem.enableUI();

    // start the simulation
    problem.simulate();

    // simulation is done, lets print the statistics!
    System.out.println(problem.getStatistics());
  }
}

/**
 * This truck implementation only picks parcels up, it does not deliver them.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
class Truck extends DefaultVehicle {

  public Truck(VehicleDTO pDto) {
    super(pDto);
  }

  @Override
  protected void tickImpl(TimeLapse time) {
    final RoadModel rm = roadModel.get();
    final PDPModel pm = pdpModel.get();
    // we always go to the closest available parcel
    final DefaultParcel closest = (DefaultParcel) RoadModels
        .findClosestObject(rm.getPosition(this), rm, new Predicate<RoadUser>() {
          @Override
          public boolean apply(RoadUser input) {
            return input instanceof DefaultParcel
                && pm.getParcelState(((DefaultParcel) input)) == ParcelState.AVAILABLE;
          }
        });

    if (closest != null) {
      rm.moveTo(this, closest, time);
      if (rm.equalPosition(closest, this)
          && pm
              .getTimeWindowPolicy()
              .canPickup(closest.getPickupTimeWindow(), time.getTime(),
                  closest.getPickupDuration())) {
        pm.pickup(this, closest, time);
      }
    }
  }
}
