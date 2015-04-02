package delegateMAS;

import java.util.Comparator;

public class ReservationStartComparator implements Comparator<Reservation> {

  @Override
  public int compare(Reservation o1, Reservation o2) {
    return o1.compareTo(o2);
  }
  
}
