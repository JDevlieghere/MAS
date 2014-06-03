package rinde.sim.core.model.pdp;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.pdp.twpolicy.LiberalPolicy;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.event.EventAPI;
import rinde.sim.event.EventDispatcher;
import rinde.sim.util.CategoryMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Assumptions of the model, any vehicle can pickup any (kind of) parcel (as
 * long as size constraints are met).
 * 
 * Currently supports three kinds of objects:
 * <ul>
 * <li> {@link rinde.sim.core.model.pdp.Parcel}</li>
 * <li> {@link rinde.sim.core.model.pdp.Vehicle}</li>
 * <li> {@link rinde.sim.core.model.pdp.Depot}</li>
 * </ul>
 * 
 * A parcel must be in one of three locations: on a vehicle, in a depot or on a
 * road (roadmodel).
 * 
 * Variable pickup and delivery times are supported. Even when pickup time spans
 * multiple simulation ticks, the {@link rinde.sim.core.model.pdp.DefaultPDPModel} ensures time
 * consistency.
 * 
 * TODO write more about assumptions in model <br/>
 * TODO write about extensibility
 * 
 * 
 * 
 * {@link rinde.sim.core.model.pdp.Parcel}s can be added on any node in the {@link rinde.sim.core.model.road.RoadModel}.
 * {@link rinde.sim.core.model.pdp.Depot}s have no real function in the current implementation.
 * 
 * 
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public final class DefaultTripleDJPDPModel extends TripleDJPDPModel {

	/**
	 * The {@link rinde.sim.event.EventDispatcher} used for generating events.
	 */
	protected final EventDispatcher eventDispatcher;

	/**
	 * Reference to the {@link rinde.sim.core.model.road.RoadModel} on which the pdp objects are situated.
	 */
	protected Optional<RoadModel> roadModel;

	/**
	 * Multimap for keeping references to the contents of {@link rinde.sim.core.model.pdp.Container}s.
	 */
	protected final Multimap<Container, Parcel> containerContents;

	/**
	 * Map for keeping the size of the contents of {@link rinde.sim.core.model.pdp.Container}s.
	 */
	protected final Map<Container, Double> containerContentsSize;

	/**
	 * Map for keeping the capacity of {@link rinde.sim.core.model.pdp.Container}s.
	 */
	protected final Map<Container, Double> containerCapacities;

	/**
	 * Map that stores the state of {@link rinde.sim.core.model.pdp.Vehicle}s.
	 */
	protected volatile Map<Vehicle, VehicleState> vehicleState;

	/**
	 * Map that stores the state of {@link rinde.sim.core.model.pdp.Parcel}s.
	 */
	protected volatile CategoryMap<ParcelState, Parcel> parcelState;

	/**
	 * The current time.
	 */
	protected long currentTime;

	/**
	 * The {@link rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy} that is used.
	 */
	protected final TimeWindowPolicy timeWindowPolicy;

	/**
	 * Map that stores any pending {@link rinde.sim.core.model.pdp.DefaultTripleDJPDPModel.Action}s of {@link rinde.sim.core.model.pdp.Vehicle}s.
	 */
	final Map<Vehicle, Action> pendingVehicleActions;

	/**
	 * Initializes the model using a {@link rinde.sim.core.model.pdp.twpolicy.LiberalPolicy} as
	 * {@link rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy}.
	 */
	public DefaultTripleDJPDPModel() {
		this(new LiberalPolicy());
	}

	/**
	 * Initializes the PDPModel.
	 * 
	 * @param twp
	 *            The {@link rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy} which is used in the model.
	 */
	public DefaultTripleDJPDPModel(TimeWindowPolicy twp) {
		timeWindowPolicy = twp;

		containerContents = LinkedHashMultimap.create();
		containerContentsSize = newLinkedHashMap();
		containerCapacities = newLinkedHashMap();
		pendingVehicleActions = newLinkedHashMap();
		vehicleState = newLinkedHashMap();
		parcelState = CategoryMap.create();

		eventDispatcher = new EventDispatcher(PDPModelEventType.values());
		roadModel = Optional.absent();
	}

	@Override
	public ImmutableSet<Parcel> getContents(Container container) {
		synchronized (this) {
			checkArgument(containerCapacities.containsKey(container));
			return ImmutableSet.copyOf(containerContents.get(container));
		}
	}

	@Override
	public double getContentsSize(Container container) {
		synchronized (this) {
			return containerContentsSize.get(container);
		}
	}

	@Override
	public double getContainerCapacity(Container container) {
		synchronized (this) {
			return containerCapacities.get(container);
		}
	}

	@Override
	public void pickup(Vehicle vehicle, Parcel parcel, TimeLapse time) {
		synchronized (this) {
			/* 1 */checkVehicleInRoadModel(vehicle);
			/* 2 */checkArgument(roadModel.get().containsObject(parcel), "parcel does not exist in RoadModel");
			final ParcelState ps = parcelState.getKeys(parcel);
			/* 3 */checkArgument(ps == ParcelState.AVAILABLE || ps == ParcelState.ANNOUNCED,
					"Parcel must be registered and must be either ANNOUNCED or AVAILABE, it is: %s. Parcel: %s.", ps,
					parcel);
			/* 4 */checkArgument(vehicleState.get(vehicle) == VehicleState.IDLE,
					"vehicle must be registered and must be available");
			/* 5 */checkArgument(roadModel.get().equalPosition(vehicle, parcel),
					"vehicle must be at the same location as the parcel it wishes to pickup");
			final double newSize = containerContentsSize.get(vehicle) + parcel.getMagnitude();
			/* 6 */checkArgument(newSize <= containerCapacities.get(vehicle),
					"parcel does not fit in vehicle. Parcel size: %s, current contents size: %s, capacity: %s.",
					parcel.getMagnitude(), containerContentsSize.get(vehicle), containerCapacities.get(vehicle));

			checkArgument(
					timeWindowPolicy
							.canPickup(parcel.getPickupTimeWindow(), time.getTime(), parcel.getPickupDuration()),
					"parcel pickup is not allowed according to the time window policy: %s, current time: %s, time window %s.",
					timeWindowPolicy, time.getTime(), parcel.getPickupTimeWindow());

			checkArgument(parcel.canBePickedUp(vehicle, time.getTime()), "the parcel does not allow pickup now");

			eventDispatcher.dispatchEvent(new PDPModelEvent(PDPModelEventType.START_PICKUP, self, time.getTime(),
					parcel, vehicle));

			// remove the parcel such that no other attempts to pickup can be
			// made
			roadModel.get().removeObject(parcel);

			// in this case we know we cannot finish this action with the
			// available time. We must continue in the nextDeliverable tick.
			if (time.getTimeLeft() < parcel.getPickupDuration()) {
				vehicleState.put(vehicle, VehicleState.PICKING_UP);
				parcelState.put(ParcelState.PICKING_UP, parcel);

				pendingVehicleActions.put(vehicle, new PickupAction(this, vehicle, parcel, parcel.getPickupDuration()
						- time.getTimeLeft()));
				time.consumeAll();
			} else {
				time.consume(parcel.getPickupDuration());
				doPickup(vehicle, parcel, time.getTime());
			}
		}
	}

	/**
	 * Checks whether the vehicle exists in the RoadModel.
	 * 
	 * @param vehicle
	 *            The vehicle to check.
	 */
	protected void checkVehicleInRoadModel(Vehicle vehicle) {
		checkArgument(roadModel.get().containsObject(vehicle), "vehicle does not exist in RoadModel");
	}

	/**
	 * Actual pickup, updates the {@link rinde.sim.core.model.pdp.Vehicle} contents.
	 * 
	 * @param vehicle
	 *            The {@link rinde.sim.core.model.pdp.Vehicle} that performs the pickup.
	 * @param parcel
	 *            The {@link rinde.sim.core.model.pdp.Parcel} that is picked up.
	 * @param time
	 *            The current time.
	 * @see #pickup(rinde.sim.core.model.pdp.Vehicle, rinde.sim.core.model.pdp.Parcel, rinde.sim.core.TimeLapse)
	 */
	protected void doPickup(Vehicle vehicle, Parcel parcel, long time) {
		synchronized (this) {
			containerContents.put(vehicle, parcel);
			containerContentsSize.put(vehicle, containerContentsSize.get(vehicle) + parcel.getMagnitude());

			parcelState.put(ParcelState.IN_CARGO, parcel);
			LOGGER.info("{} end pickup of {} by {}", time, parcel, vehicle);
			eventDispatcher.dispatchEvent(new PDPModelEvent(PDPModelEventType.END_PICKUP, self, time, parcel, vehicle));
		}
	}

	@Override
	public void deliver(Vehicle vehicle, Parcel parcel, TimeLapse time) {
		synchronized (this) {
			/* 1 */checkVehicleInRoadModel(vehicle);
			/* 2 */checkArgument(vehicleState.get(vehicle).equals(VehicleState.IDLE),
					"vehicle must be idle but is: %s ", vehicleState.get(vehicle));
			/* 3 */checkArgument(containerContents.get(vehicle).contains(parcel), "vehicle does not contain parcel");
			/* 4 */checkArgument(parcel.getDestination().equals(roadModel.get().getPosition(vehicle)),
					"parcel must be delivered at its destination, vehicle should move there first");

			checkArgument(
					timeWindowPolicy.canDeliver(parcel.getDeliveryTimeWindow(), time.getTime(),
							parcel.getDeliveryDuration()),
					"parcel delivery is not allowed at this time (%s) according to the time window policy: %s",
					time.getTime(), timeWindowPolicy);

			checkArgument(parcel.canBeDelivered(vehicle, time.getTime()), "the parcel does not allow a delivery now");

			eventDispatcher.dispatchEvent(new PDPModelEvent(PDPModelEventType.START_DELIVERY, self, time.getTime(),
					parcel, vehicle));
			if (time.getTimeLeft() < parcel.getDeliveryDuration()) {
				vehicleState.put(vehicle, VehicleState.DELIVERING);
				parcelState.put(ParcelState.DELIVERING, parcel);
				pendingVehicleActions.put(vehicle,
						new DeliverAction(this, vehicle, parcel, parcel.getDeliveryDuration() - time.getTimeLeft()));
				time.consumeAll();
			} else {
				time.consume(parcel.getDeliveryDuration());
				doDeliver(vehicle, parcel, time.getTime());
			}
		}
	}

	/**
	 * The actual delivery of the specified {@link rinde.sim.core.model.pdp.Parcel} by the specified
	 * {@link rinde.sim.core.model.pdp.Vehicle}.
	 * 
	 * @param vehicle
	 *            The {@link rinde.sim.core.model.pdp.Vehicle} that performs the delivery.
	 * @param parcel
	 *            The {@link rinde.sim.core.model.pdp.Parcel} that is delivered.
	 * @param time
	 *            The current time.
	 */
	protected void doDeliver(Vehicle vehicle, Parcel parcel, long time) {
		synchronized (this) {
			containerContents.remove(vehicle, parcel);
			containerContentsSize.put(vehicle, containerContentsSize.get(vehicle) - parcel.getMagnitude());

			parcelState.put(ParcelState.DELIVERED, parcel);
			LOGGER.info("{} end delivery of {} by {}", time, parcel, vehicle);
			eventDispatcher
					.dispatchEvent(new PDPModelEvent(PDPModelEventType.END_DELIVERY, self, time, parcel, vehicle));
		}
	}

	@Override
	public void drop(Vehicle vehicle, Parcel parcel, TimeLapse time) {
		synchronized (this) {
			/* 1 */checkVehicleInRoadModel(vehicle);
			/* 2 */checkArgument(vehicleState.get(vehicle).equals(VehicleState.IDLE),
					"vehicle must be idle but is: %s ", vehicleState.get(vehicle));
			/* 3 */checkArgument(containerContents.get(vehicle).contains(parcel), "vehicle does not contain parcel");

			eventDispatcher.dispatchEvent(new PDPModelEvent(PDPModelEventType.START_DELIVERY, self, time.getTime(),
					parcel, vehicle));
			if (time.getTimeLeft() < parcel.getDeliveryDuration()) {
				vehicleState.put(vehicle, VehicleState.DELIVERING);
				parcelState.put(ParcelState.DELIVERING, parcel);
				pendingVehicleActions.put(vehicle, new DropAction(this, vehicle, parcel, parcel.getDeliveryDuration()
						- time.getTimeLeft()));
				time.consumeAll();
			} else {
				time.consume(parcel.getDeliveryDuration());
				doDrop(vehicle, parcel, time.getTime());
			}
		}
	}

	/**
	 * The actual dropping of the specified {@link rinde.sim.core.model.pdp.Parcel} by the specified
	 * {@link rinde.sim.core.model.pdp.Vehicle}.
	 * 
	 * @param vehicle
	 *            The {@link rinde.sim.core.model.pdp.Vehicle} that performs the dropping.
	 * @param parcel
	 *            The {@link rinde.sim.core.model.pdp.Parcel} that is dropped.
	 * @param time
	 *            The current time.
	 */
	protected void doDrop(Vehicle vehicle, Parcel parcel, long time) {
		synchronized (this) {
			containerContents.remove(vehicle, parcel);
			containerContentsSize.put(vehicle, containerContentsSize.get(vehicle) - parcel.getMagnitude());
			roadModel.get().addObjectAtSamePosition(parcel, vehicle);
			parcelState.put(ParcelState.AVAILABLE, parcel);
			LOGGER.info("{} dropped {} by {}", time, parcel, vehicle);
			eventDispatcher.dispatchEvent(new PDPModelEvent(PDPModelEventType.PARCEL_AVAILABLE, self, time, parcel,
					null));
		}
	}

	@Override
	public void transship(Vehicle fromVehicle, Vehicle toVehicle, Parcel parcel, TimeLapse time) {
		synchronized (this) {
			/* 1 */checkVehicleInRoadModel(fromVehicle);
			/* 2 */checkVehicleInRoadModel(toVehicle);
			/* 3 */checkArgument(vehicleState.get(fromVehicle).equals(VehicleState.IDLE),
					"vehicle must be idle but is: %s ", vehicleState.get(fromVehicle));
			/* 4 */checkArgument(vehicleState.get(toVehicle).equals(VehicleState.IDLE),
					"vehicle must be idle but is: %s ", vehicleState.get(toVehicle));
			/* 5 */checkArgument(containerContents.get(fromVehicle).contains(parcel), "vehicle does not contain parcel");
			/* 6 */checkArgument(roadModel.get().getPosition(fromVehicle).equals(roadModel.get().getPosition(toVehicle)),
					"both vehicles must be at the same position");

			// Allow instant transshipment:
			doTransship(fromVehicle, toVehicle, parcel, time.getTime());
		}
	}
	
	protected void doTransship(Vehicle fromVehicle, Vehicle toVehicle, Parcel parcel, long time) {
		synchronized (this) {
			containerContents.remove(fromVehicle, parcel);
			containerContentsSize.put(fromVehicle, containerContentsSize.get(fromVehicle) - parcel.getMagnitude());
			
			containerContents.put(toVehicle, parcel);
			containerContentsSize.put(toVehicle, containerContentsSize.get(toVehicle) + parcel.getMagnitude());

			LOGGER.info("{} transshipment of {} from {} to {}", time, parcel, fromVehicle, toVehicle);
		}
	}
	
	@Override
	public void addParcelIn(Container container, Parcel parcel) {
		synchronized (this) {
			/* 1 */checkArgument(!roadModel.get().containsObject(parcel),
					"this parcel is already added to the roadmodel");
			/* 2 */checkArgument(parcelState.getKeys(parcel) == ParcelState.AVAILABLE,
					"parcel must be registered and in AVAILABLE state, current state: %s", parcelState.getKeys(parcel));
			/* 3 */checkArgument(containerCapacities.containsKey(container), "the parcel container is not registered");
			/* 4 */checkArgument(roadModel.get().containsObject(container),
					"the parcel container is not on the roadmodel");
			final double newSize = containerContentsSize.get(container) + parcel.getMagnitude();
			/* 5 */checkArgument(
					newSize <= containerCapacities.get(container),
					"parcel does not fit in container. Capacity is %s, current content size is %s, new parcel size is %s",
					containerCapacities.get(container), containerContentsSize.get(container), parcel.getMagnitude());

			containerContents.put(container, parcel);
			containerContentsSize.put(container, newSize);
			parcelState.put(ParcelState.IN_CARGO, parcel);
		}
	}

	@Override
	public Collection<Parcel> getParcels(ParcelState state) {
		synchronized (this) {
			return parcelState.get(state);
		}
	}

	@Override
	public Collection<Parcel> getParcels(ParcelState... states) {
		synchronized (this) {
			return parcelState.getMultiple(states);
		}
	}

	@Override
	public Set<Vehicle> getVehicles() {
		synchronized (this) {
			return unmodifiableSet(vehicleState.keySet());
		}
	}

	@Override
	public ParcelState getParcelState(Parcel parcel) {
		synchronized (this) {
			return parcelState.getKeys(parcel);
		}
	}

	@Override
	public VehicleState getVehicleState(Vehicle vehicle) {
		synchronized (this) {
			checkArgument(vehicleState.containsKey(vehicle), "vehicle must be registered");
			return vehicleState.get(vehicle);
		}
	}

	// TODO create a similar method but with a parcel as key
	@Override
	public PDPModel.VehicleParcelActionInfo getVehicleActionInfo(Vehicle vehicle) {
		synchronized (this) {
			final VehicleState state = vehicleState.get(vehicle);
			checkArgument(state == VehicleState.DELIVERING || state == VehicleState.PICKING_UP,
					"the vehicle must be in either DELIVERING or PICKING_UP state, but it is %s.", state);
			return (PDPModel.VehicleParcelActionInfo) pendingVehicleActions.get(vehicle);
		}
	}

	@Override
	protected boolean doRegister(PDPObject element) {
		synchronized (this) {
			LOGGER.info("{} register {}", currentTime, element);
			if (element.getType() == PDPType.PARCEL) {
				checkArgument(!parcelState.containsValue(element));
				final Parcel p = (Parcel) element;
				final ParcelState state = currentTime < p.getPickupTimeWindow().begin ? ParcelState.ANNOUNCED
						: ParcelState.AVAILABLE;
				parcelState.put(state, (Parcel) element);
				eventDispatcher.dispatchEvent(new PDPModelEvent(PDPModelEventType.NEW_PARCEL, self, currentTime, p,
						null));
				// if the parcel is immediately available, we send this event as
				// well
				if (state == ParcelState.AVAILABLE) {
					eventDispatcher.dispatchEvent(new PDPModelEvent(PDPModelEventType.PARCEL_AVAILABLE, self,
							currentTime, p, null));
				}
			} else {
				// it is a vehicle or a depot
				final Container container = (Container) element;
				checkArgument(!containerCapacities.containsKey(container));
				containerCapacities.put(container, container.getCapacity());
				containerContentsSize.put(container, 0d);

				if (element.getType() == PDPType.VEHICLE) {
					final Vehicle v = (Vehicle) element;
					vehicleState.put(v, VehicleState.IDLE);
					eventDispatcher.dispatchEvent(new PDPModelEvent(PDPModelEventType.NEW_VEHICLE, self, currentTime,
							null, v));
				}
			}
			element.initPDPObject(self);

			return true;
		}
	}

	@Override
	public boolean unregister(PDPObject element) {
		synchronized (this) {
			LOGGER.info("unregister {}", element);
			if (element instanceof Container) {
				containerCapacities.remove(element);
				containerContentsSize.remove(element);
				containerContents.removeAll(element);
			}

			if (element instanceof Parcel) {
				parcelState.removeValue((Parcel) element);
			}

			if (element instanceof Vehicle) {
				vehicleState.remove(element);
				pendingVehicleActions.remove(element);
			}
		}
		return true;
	}

	@Override
	public EventAPI getEventAPI() {
		return eventDispatcher.getPublicEventAPI();
	}

	@Override
	public boolean containerContains(Container container, Parcel parcel) {
		synchronized (this) {
			return containerContents.containsEntry(container, parcel);
		}
	}

	@Override
	protected void continuePreviousActions(Vehicle vehicle, TimeLapse time) {
		synchronized (this) {
			if (pendingVehicleActions.containsKey(vehicle)) {
				final Action action = pendingVehicleActions.get(vehicle);
				action.perform(time);
				if (action.isDone()) {
					pendingVehicleActions.remove(vehicle);
					checkState(!pendingVehicleActions.containsKey(vehicle));
					checkState(vehicleState.get(vehicle) == VehicleState.IDLE);
				}
			}
		}
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		synchronized (this) {
			// TODO this can be optimized by scheduling events upon registering
			currentTime = timeLapse.getStartTime();
			final Collection<Parcel> parcels = parcelState.get(ParcelState.ANNOUNCED);
			final List<Parcel> newAvailables = newArrayList();
			for (final Parcel p : parcels) {
				if (timeLapse.getStartTime() >= p.getPickupTimeWindow().begin) {
					newAvailables.add(p);
				}
			}
			for (final Parcel p : newAvailables) {
				parcelState.put(ParcelState.AVAILABLE, p);
				eventDispatcher.dispatchEvent(new PDPModelEvent(PDPModelEventType.PARCEL_AVAILABLE, self, currentTime,
						p, null));
			}
		}
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
	}

	@Override
	public TimeWindowPolicy getTimeWindowPolicy() {
		return timeWindowPolicy;
	}

	@Override
	public void registerModelProvider(ModelProvider mp) {
		synchronized (this) {
			roadModel = Optional.fromNullable(mp.getModel(RoadModel.class));
		}
	}

	@Override
	public void service(Vehicle vehicle, Parcel parcel, TimeLapse time) {
		if (getContents(vehicle).contains(parcel)) {
			deliver(vehicle, parcel, time);
		} else {
			pickup(vehicle, parcel, time);
		}
	}

	/**
	 * Represents an action that takes time. This is used for actions that can
	 * not be done at once (since there is not enough time available), using
	 * this interface actions can be performed in steps.
	 * 
	 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
	 */
	interface Action {
		/**
		 * Performs the action using the specified amount of time.
		 * 
		 * @param time
		 *            The time to use.
		 */
		void perform(TimeLapse time);

		/**
		 * @return <code>true</code> when this action is completed,
		 *         <code>false</code> otherwise.
		 */
		boolean isDone();
	}

	abstract static class VehicleParcelAction implements Action, PDPModel.VehicleParcelActionInfo {
		protected final DefaultTripleDJPDPModel modelRef;
		protected final Vehicle vehicle;
		protected final Parcel parcel;
		protected long timeNeeded;

		VehicleParcelAction(DefaultTripleDJPDPModel model, Vehicle v, Parcel p, long pTimeNeeded) {
			modelRef = model;
			vehicle = v;
			parcel = p;
			timeNeeded = pTimeNeeded;
		}

		@Override
		public void perform(TimeLapse time) {
			// there is enough time to finish action
			if (time.getTimeLeft() >= timeNeeded) {
				time.consume(timeNeeded);
				timeNeeded = 0;
				finish(time);
			} else {
				// there is not enough time to finish action in this step
				timeNeeded -= time.getTimeLeft();
				time.consumeAll();
			}
		}

		protected abstract void finish(TimeLapse time);

		@Override
		public boolean isDone() {
			return timeNeeded == 0;
		}

		@Override
		public long timeNeeded() {
			return timeNeeded;
		}

		@Override
		public Parcel getParcel() {
			return parcel;
		}

		@Override
		public Vehicle getVehicle() {
			return vehicle;
		}
	}

	static class PickupAction extends VehicleParcelAction {
		PickupAction(DefaultTripleDJPDPModel model, Vehicle v, Parcel p, long pTimeNeeded) {
			super(model, v, p, pTimeNeeded);
		}

		@Override
		public void finish(TimeLapse time) {
			modelRef.vehicleState.put(vehicle, VehicleState.IDLE);
			modelRef.doPickup(vehicle, parcel, time.getTime());
		}
	}

	static class DropAction extends VehicleParcelAction {
		DropAction(DefaultTripleDJPDPModel model, Vehicle v, Parcel p, long pTimeNeeded) {
			super(model, v, p, pTimeNeeded);
		}

		@Override
		protected void finish(TimeLapse time) {
			modelRef.vehicleState.put(vehicle, VehicleState.IDLE);
			modelRef.doDrop(vehicle, parcel, time.getTime());
		}

	}

	static class DeliverAction extends VehicleParcelAction {
		DeliverAction(DefaultTripleDJPDPModel model, Vehicle v, Parcel p, long pTimeNeeded) {
			super(model, v, p, pTimeNeeded);
		}

		@Override
		public void finish(TimeLapse time) {
			modelRef.vehicleState.put(vehicle, VehicleState.IDLE);
			modelRef.doDeliver(vehicle, parcel, time.getTime());
		}
	}
}
