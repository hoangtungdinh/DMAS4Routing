package delegateMAS;

public class Experiment {

  public static void main(String[] args) {
    Setting setting = new Setting.SettingBuilder().build();
    RoutingProblem routingProblem = new RoutingProblem(setting);
    routingProblem.run();
  }

}
