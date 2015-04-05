package test;

import java.util.HashMap;
import java.util.Map;



public class Test {

  public static void main(String[] args) {
    ATest x = new ATest(3);
    Map<Long, ATest> map = new HashMap<Long, ATest>();
    Long a1 = 2l;
    Long a2 = 3l;
    map.put(a1, x);
    map.put(a2, x);
    
    ATest y = map.get(a1);
    y.setA(444);
    System.out.println(map.get(a2).getA());
  }
}
