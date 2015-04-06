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
    List<Integer> set = new ArrayList<Integer>();
    set.add(1);
    set.add(2);
    set.add(3);
    set.add(4);
    
    Collections.shuffle(set);
    
    for (Integer i : set) {
      System.out.println(i);
    }
  }
}
