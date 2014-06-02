package rinde.sim.core.model.pdp;

import rinde.sim.core.TimeLapse;

/**
 * Defines the public interface for a model for pickup-and-delivery problems with transshipments.
 * This models is only responsible for the picking up and delivery operations,
 * i.e. it is not responsible for movement.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * @author Dennis Degryse <dennisdegryse@gmail.com>
 * @author Dennis Frett <dennis.frett@live.com>
 * @auther Dieter Castel <dietercastel@gmail.com>
 * @author Jonas Devlieghere <info@jonasdevlieghere.com> 
 */
public abstract class TripleDJPDPModel extends PDPModel {

  /**
   * The specified {@link rinde.sim.core.model.pdp.Vehicle} attempts to transship the {@link rinde.sim.core.model.pdp.Parcel} to
   * the other {@link rinde.sim.core.model.pdp.Vehicle} at its current location. Preconditions:
   * <ul>
   * <li>Both {@link rinde.sim.core.model.pdp.Vehicle}s must exist in {@link rinde.sim.core.model.road.RoadModel}.</li>
   * <li>Both {@link rinde.sim.core.model.pdp.Vehicle}s must be in {@link rinde.sim.core.model.pdp.PDPModel.VehicleState#IDLE} state.</li>
   * <li>{@link rinde.sim.core.model.pdp.Vehicle} must contain the specified {@link rinde.sim.core.model.pdp.Parcel}.</li>
   * <li>Both {@link rinde.sim.core.model.pdp.Vehicle}s must be at the same position.</li>
   * </ul>
   * If any of the preconditions is not met this method throws an {@link IllegalArgumentException}.
   * 
   * @param vehicleFrom The {@link rinde.sim.core.model.pdp.Vehicle} that wishes to hand over a {@link rinde.sim.core.model.pdp.Parcel} in the transshipment.
   * @param vehicleTo The {@link rinde.sim.core.model.pdp.Vehicle} that wishes to receive a {@link rinde.sim.core.model.pdp.Parcel} in the transshipment.
   * @param parcel The {@link rinde.sim.core.model.pdp.Parcel} that is to be transshiped.
   * @param time The {@link rinde.sim.core.TimeLapse} that is available for the transshipment.
   */
  public abstract void transship(Vehicle vehicleFrom, Vehicle vehicleTo, Parcel parcel, TimeLapse time);
}
