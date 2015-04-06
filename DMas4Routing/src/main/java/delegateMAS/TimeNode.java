package delegateMAS;

import javax.annotation.Nullable;

import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Objects;

public class TimeNode {

  public final Point node;
  public final long time;
  private final int hashCode;

  public TimeNode(Point node, long time) {
    this.node = node;
    this.time = time;
    this.hashCode = Objects.hashCode(node, time);
  }

  public Point getNode() {
    return node;
  }

  public long getTime() {
    return time;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }
  
  @Override
  public boolean equals(@Nullable Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }
    // allows comparison with subclasses
    if (!(other instanceof TimeNode)) {
      return false;
    }
    final TimeNode n = (TimeNode) other;
    return node.equals(n.node) && time == n.time;
  }
}
