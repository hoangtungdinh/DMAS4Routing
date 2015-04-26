package delegateMAS;


public class Experiment {

  public static void main(String[] args) {
    final int intention = 1;
    final int numOfAgents = 1000;
    Setting setting = new Setting.SettingBuilder()
    .setTimeWindow(30)
    .setMinTimeSteps(10)
    .setExplorationFreq(5)
    .setIntentionFreq(intention)
    .setIntentionChangingThreshold(70)
    .setPheromoneLifeTime(intention + 1)
    .setMapSizeX(100)
    .setMapSizeY(100)
    .setBlockSize(1)
    .setNumberOfAgents(numOfAgents)
    .setDynamicRate(0)
    .setStopTime(10000 * 1000)
    .setFailureRate(0)
    .build();

    RoutingProblem routingProblem = new RoutingProblem(setting,
        "32x32_" + numOfAgents, false);
    routingProblem.run();
  }
}
