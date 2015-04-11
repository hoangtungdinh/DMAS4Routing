package delegateMAS;


public class Experiment {

  public static void main(String[] args) {
    final int intention = 1;
    final int dynamicRate = 0;
    final int exploration = 40;
    final int failureRate = 0;
    Setting setting = new Setting.SettingBuilder()
    .setTimeWindow(50)
    .setMinTimeSteps(10)
    .setExplorationFreq(exploration)
    .setIntentionFreq(intention)
    .setIntentionChangingThreshold(70)
    .setPheromoneLifeTime(intention + 1)
    .setMapSizeX(100)
    .setMapSizeY(100)
    .setBlockSize(1)
    .setNumberOfAgents(1000)
    .setDynamicRate(dynamicRate)
    .setStopTime(1000 * 1000)
    .setFailureRate(failureRate)
    .build();

    RoutingProblem routingProblem = new RoutingProblem(setting,
        "-ExplorationFreq-" + exploration, false);
    routingProblem.run();
  }
}
