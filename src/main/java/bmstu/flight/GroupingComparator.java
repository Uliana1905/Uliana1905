package bmstu.flight;

import org.apache.hadoop.io.WritableComparator;

public class GroupingComparator extends WritableComparator {

    public GroupingComparator () {
        super (FlightWritableComparable.class, true);
    }

    public int compare(FlightWritableComparable a, FlightWritableComparable b) {
        FlightWritableComparable a1 = (FlightWritableComparable) a;
        FlightWritableComparable b1 = (FlightWritableComparable) b;

        if (a1.getDes_air() > b1.getDes_air()){
            return 1;
        }
        if (a1.getDes_air() < b1.getDes_air()){
            return -1;
        }else{
            return 0;
        }
    }
}
