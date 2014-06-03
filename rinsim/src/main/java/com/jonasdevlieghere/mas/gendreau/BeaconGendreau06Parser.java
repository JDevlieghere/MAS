/**
 * 
 */
package com.jonasdevlieghere.mas.gendreau;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.math.DoubleMath;
import rinde.sim.core.graph.Point;
import rinde.sim.pdptw.common.*;
import rinde.sim.pdptw.common.DynamicPDPTWScenario.ProblemClass;
import rinde.sim.pdptw.gendreau06.GendreauProblemClass;
import rinde.sim.scenario.ScenarioBuilder;
import rinde.sim.scenario.ScenarioBuilder.ScenarioCreator;
import rinde.sim.scenario.TimedEvent;
import rinde.sim.util.TimeWindow;

import javax.annotation.Nullable;
import java.io.*;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static rinde.sim.core.model.pdp.PDPScenarioEvent.*;

/**
 * Parser for files from the Gendreau et al. (2006) data set. The parser allows
 * to customize some of the properties of the scenarios.
 * <p>
 * <b>Format specification: (columns)</b>
 * <ul>
 * <li>1: request arrival time</li>
 * <li>2: pick-up service time</li>
 * <li>3 and 4: x and y position for the pick-up</li>
 * <li>5 and 6: service window time of the pick-up</li>
 * <li>7: delivery service time</li>
 * <li>8 and 9:x and y position for the delivery</li>
 * <li>10 and 11: service window time of the delivery</li>
 * </ul>
 * All times are expressed in seconds.
 * <p>
 * <b>References</b>
 * <ul>
 * <li>[1]. Gendreau, M., Guertin, F., Potvin, J.-Y., and Séguin, R.
 * Neighborhood search heuristics for a dynamic vehicle dispatching problem with
 * pick-ups and deliveries. Transportation Research Part C: Emerging
 * Technologies 14, 3 (2006), 157–174.</li>
 * </ul>
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public final class BeaconGendreau06Parser {

  private static final String REGEX = ".*req_rapide_(1|2|3|4|5)_(450|240)_(24|33)";
  private static final double TIME_MULTIPLIER = 1000d;
  private static final int TIME_MULTIPLIER_INTEGER = 1000;
  private static final int PARCEL_MAGNITUDE = 0;

  private int numVehicles;
  private boolean allowDiversion;
  private boolean online;
  private long tickSize;
  private final ImmutableMap.Builder<String, InputStream> streams;
  @Nullable
  private ImmutableList<ProblemClass> problemClasses;

  private BeaconGendreau06Parser() {
    allowDiversion = false;
    online = true;
    numVehicles = -1;
    tickSize = 1000L;
    streams = ImmutableMap.builder();
  }

  /**
   * @return A {@link rinde.sim.pdptw.gendreau06.Gendreau06Parser}.
   */
  public static BeaconGendreau06Parser parser() {
    return new BeaconGendreau06Parser();
  }

  /**
   * Convenience method for parsing a single file.
   * @param file The file to parse.
   * @return The scenario as described by the file.
   */
  public static BeaconGendreau06Scenario parse(File file) {
    return parser().addFile(file).parse().get(0);
  }

  /**
   * Add the specified file to the parser.
   * @param file The file to add.
   * @return This, as per the builder pattern.
   */
  public BeaconGendreau06Parser addFile(File file) {
    checkValidFileName(file.getName());
    try {
      streams.put(file.getName(), new FileInputStream(file));
    } catch (final FileNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
    return this;
  }

  /**
   * Add the specified file to the parser.
   * @param file The file to add.
   * @return This, as per the builder pattern.
   */
  public BeaconGendreau06Parser addFile(String file) {
    return addFile(new File(file));
  }

  /**
   * Add the specified stream to the parser. A file name needs to be specified
   * for identification of this particular scenario.
   * @param stream The stream to use for the parsing of the scenario.
   * @param fileName The file name that identifies the scenario's class and
   *          instance.
   * @return This, as per the builder pattern.
   */
  public BeaconGendreau06Parser addFile(InputStream stream, String fileName) {
    checkValidFileName(fileName);
    streams.put(fileName, stream);
    return this;
  }

  /**
   * Adds all Gendreau scenario files in the specified directory.
   * @param dir The directory to search.
   * @return This, as per the builder pattern.
   */
  public BeaconGendreau06Parser addDirectory(String dir) {
    return addDirectory(new File(dir));
  }

  /**
   * Adds all Gendreau scenario files in the specified directory.
   * @param dir The directory to search.
   * @return This, as per the builder pattern.
   */
  public BeaconGendreau06Parser addDirectory(File dir) {
    final File[] files = dir.listFiles(
        new FileFilter() {
          @Override
          public boolean accept(@Nullable File file) {
            checkNotNull(file);
            return isValidFileName(file.getName());
          }
        });
    for (final File f : files) {
      addFile(f);
    }
    return this;
  }

  /**
   * When this method is called all scenarios generated by this parser will
   * allow vehicle diversion. For more information about the vehicle diversion
   * concept please consult the class documentation of
   * {@link rinde.sim.pdptw.common.PDPRoadModel}.
   * @return This, as per the builder pattern.
   */
  public BeaconGendreau06Parser allowDiversion() {
    allowDiversion = true;
    return this;
  }

  /**
   * When this method is called, all scenarios generated by this parser will be
   * offline scenarios. This means that all parcel events will arrive at time
   * <code>-1</code>, which means that everything is known beforehand. By
   * default the parser uses the original event arrival times as defined by the
   * scenario file.
   * @return This, as per the builder pattern.
   */
  public BeaconGendreau06Parser offline() {
    online = false;
    return this;
  }

  /**
   * This method allows to override the number of vehicles in the scenarios. For
   * the default values see {@link rinde.sim.pdptw.gendreau06.GendreauProblemClass}.
   * @param num The number of vehicles that should be used in the parsed
   *          scenarios. Must be positive.
   * @return This, as per the builder pattern.
   */
  public BeaconGendreau06Parser setNumVehicles(int num) {
    checkArgument(num > 0, "The number of vehicles must be positive.");
    numVehicles = num;
    return this;
  }

  /**
   * Change the default tick size of <code>1000 ms</code> into something else.
   * @param tick Must be positive.
   * @return This, as per the builder pattern.
   */
  public BeaconGendreau06Parser setTickSize(long tick) {
    checkArgument(tick > 0L, "Tick size must be positive.");
    tickSize = tick;
    return this;
  }

  /**
   * Filters out any files that are added to this parser which are <i>not</i> of
   * one of the specified problem classes.
   * @param classes The problem classes which should be parsed.
   * @return This, as per the builder pattern.
   */
  public BeaconGendreau06Parser filter(GendreauProblemClass... classes) {
    problemClasses = ImmutableList.<ProblemClass> copyOf(classes);
    return this;
  }

  /**
   * Parses the files which are added to this parser. In case
   * {@link #filter(rinde.sim.pdptw.gendreau06.GendreauProblemClass...)} has been called, only files in one
   * of these problem classes will be parsed.
   * @return A list of scenarios in order of adding them to the parser.
   */
  public ImmutableList<BeaconGendreau06Scenario> parse() {
    final ImmutableList.Builder<BeaconGendreau06Scenario> scenarios = ImmutableList
        .builder();
    for (final Entry<String, InputStream> entry : streams.build().entrySet()) {
      boolean include = false;
      if (problemClasses == null) {
        include = true;
      } else {
        for (final ProblemClass pc : problemClasses) {
          if (entry.getKey().endsWith(pc.getId())) {
            include = true;
            break;
          }
        }
      }
      if (include) {
        scenarios.add(parse(entry.getValue(), entry.getKey(), numVehicles,
            tickSize, allowDiversion, online));
      }
    }
    return scenarios.build();
  }

  // public static Gendreau06Scenario parse(String file) {
  // return parse(file, -1);
  // }
  //
  // public static Gendreau06Scenario parse(String file, int numVehicles) {
  // return parse(file, numVehicles, false);
  // }
  //
  // public static Gendreau06Scenario parse(String file, int numVehicles,
  // boolean allowDiversion) {
  // FileReader reader;
  // try {
  // reader = new FileReader(file);
  // } catch (final FileNotFoundException e) {
  // throw new IllegalArgumentException("File not found: " + e.getMessage());
  // }
  // return parse(new BufferedReader(reader), new File(file).getName(),
  // numVehicles, 1000L, allowDiversion);
  // }
  //
  // public static Gendreau06Scenario parse(BufferedReader reader,
  // String fileName, int numVehicles) {
  // return parse(reader, fileName, numVehicles, 1000L);
  // }
  //
  // public static Gendreau06Scenario parse(BufferedReader reader,
  // String fileName, int numVehicles, final long tickSize) {
  // return parse(reader, fileName, numVehicles, tickSize, false);
  // }

  static boolean isValidFileName(String name) {
    return Pattern.compile(REGEX).matcher(name).matches();
  }

  static void checkValidFileName(String name) {
    checkArgument(isValidFileName(name),
        "The filename must conform to the following regex: %s input was: %s",
        REGEX, name);
  }

  private static BeaconGendreau06Scenario parse(InputStream inputStream,
      String fileName, int numVehicles, final long tickSize,
      final boolean allowDiversion, boolean online) {

    final ScenarioBuilder sb = new ScenarioBuilder(ADD_PARCEL, ADD_DEPOT,
        ADD_VEHICLE, TIME_OUT);

    final BufferedReader reader = new BufferedReader(new InputStreamReader(
        inputStream));

    final Matcher m = Pattern.compile(REGEX).matcher(fileName);
    checkArgument(m.matches(),
        "The filename must conform to the following regex: %s input was: %s",
        REGEX, fileName);

    final int instanceNumber = Integer.parseInt(m.group(1));
    final long minutes = Long.parseLong(m.group(2));
    final long totalTime = minutes * 60000L;
    final long requestsPerHour = Long.parseLong(m.group(3));

    final GendreauProblemClass problemClass = GendreauProblemClass.with(
        minutes, requestsPerHour);

    final int vehicles = numVehicles == -1 ? problemClass.vehicles
        : numVehicles;

    final Point depotPosition = new Point(2.0, 2.5);
    final double truckSpeed = 30;
    sb.addEvent(new AddDepotEvent(-1, depotPosition));
    for (int i = 0; i < vehicles; i++) {
      sb.addEvent(new AddVehicleEvent(-1, new VehicleDTO(depotPosition,
          truckSpeed, 0, new TimeWindow(0, totalTime))));
    }
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        final String[] parts = line.split(" ");
        final long requestArrivalTime = DoubleMath.roundToLong(
            Double.parseDouble(parts[0]) * TIME_MULTIPLIER,
            RoundingMode.HALF_EVEN);
        // FIXME currently filtering out first and last lines of file. Is
        // this ok?
        if (requestArrivalTime >= 0) {
          final long pickupServiceTime = Long.parseLong(parts[1])
              * TIME_MULTIPLIER_INTEGER;
          final double pickupX = Double.parseDouble(parts[2]);
          final double pickupY = Double.parseDouble(parts[3]);
          final long pickupTimeWindowBegin = DoubleMath.roundToLong(
              Double.parseDouble(parts[4]) * TIME_MULTIPLIER,
              RoundingMode.HALF_EVEN);
          final long pickupTimeWindowEnd = DoubleMath.roundToLong(
              Double.parseDouble(parts[5]) * TIME_MULTIPLIER,
              RoundingMode.HALF_EVEN);
          final long deliveryServiceTime = Long.parseLong(parts[6])
              * TIME_MULTIPLIER_INTEGER;
          final double deliveryX = Double.parseDouble(parts[7]);
          final double deliveryY = Double.parseDouble(parts[8]);
          final long deliveryTimeWindowBegin = DoubleMath.roundToLong(
              Double.parseDouble(parts[9]) * TIME_MULTIPLIER,
              RoundingMode.HALF_EVEN);
          final long deliveryTimeWindowEnd = DoubleMath.roundToLong(
              Double.parseDouble(parts[10]) * TIME_MULTIPLIER,
              RoundingMode.HALF_EVEN);

          // when an offline scenario is desired, all times are set to -1
          final long arrTime = online ? requestArrivalTime : -1;

          final ParcelDTO dto = new ParcelDTO(new Point(pickupX, pickupY),
              new Point(deliveryX, deliveryY), new TimeWindow(
                  pickupTimeWindowBegin, pickupTimeWindowEnd), new TimeWindow(
                  deliveryTimeWindowBegin, deliveryTimeWindowEnd),
              PARCEL_MAGNITUDE, arrTime, pickupServiceTime,
              deliveryServiceTime);
          sb.addEvent(new AddParcelEvent(dto));
        }
      }
      sb.addEvent(new TimedEvent(TIME_OUT, totalTime));
      reader.close();
    } catch (final IOException e) {
      throw new IllegalArgumentException(e);
    }

    return sb.build(new ScenarioCreator<BeaconGendreau06Scenario>() {
      @Override
      public BeaconGendreau06Scenario create(List<TimedEvent> eventList,
          Set<Enum<?>> eventTypes) {
        return new BeaconGendreau06Scenario(eventList, eventTypes, tickSize,
            problemClass, instanceNumber, allowDiversion);
      }
    });
  }
}