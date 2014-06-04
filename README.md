MAS
===

Project for the course [Multi Agent Systems (B-KUL-H02H4A)](http://onderwijsaanbod.kuleuven.be/syllabi/e/H02H4AE.htm) using the [RinSim](https://github.com/rinde/RinSim) multi-agent system simulator.

## Automated Experiments

The class `BulkExperiment` automates experiment execution. Based on a list containing data sets and a list containing configurations it performs an experiment for every configuration combined with every dataset. For each configuration the results are written to files in the `output` directory. Adding or removing configuration is as easy as setting the proper lists in the main method.

```java
public static void main(String[] args){
    ArrayList<RuntimeConfiguration> runtimeConfigurations = new ArrayList<RuntimeConfiguration>();
    ArrayList<String> datasets = new ArrayList<String>(GENDREAU);

    runtimeConfigurations.addAll(RADIUS_CONFIGURATIONS);
    runtimeConfigurations.addAll(STRATEGY_CONFIGURATIONS);
    runtimeConfigurations.addAll(EXCHANGE_CONFIGURATIONS);

    BulkExperiment tester = new BulkExperiment(runtimeConfigurations, datasets);
    tester.run();
}
```