package delegateMAS;

import java.io.Serializable;
import java.util.List;

import com.github.rinde.rinsim.geom.Point;

public class BenchmarkMap implements Serializable {

  private static final long serialVersionUID = -7550475617196917124L;

  private List<Point> removedNodes;

  public BenchmarkMap(List<Point> removedNodes) {
    this.removedNodes = removedNodes;
  }

  public List<Point> getRemovedNodes() {
    return removedNodes;
  }
}
