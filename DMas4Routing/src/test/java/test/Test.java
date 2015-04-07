package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class Test {

  public static void main(String[] args) {
    double a = 3.0;
    double b = Double.longBitsToDouble(Double
        .doubleToLongBits(a) + 1);
    System.out.println(a + " " + b);
  }
}
