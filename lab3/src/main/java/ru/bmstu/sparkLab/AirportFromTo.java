package ru.bmstu.sparkLab;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

import java.util.Map;

import static java.lang.Integer.parseInt;

public class AirportFromTo {
    private static final String REQEX_SPLITTER = ",(?! )";
    private static final String FIRSTLINEofAIRPORTS = "Code,Description";
    private static final String REPLACEABLE_COLON = "\"";
    private static final String REPLACEMENT_NULL = "";
    private static final String REQEX = ",";
    private static final int NUMBER_ORIGIN_AIRPORT_ID = 11;
    private static final int NUMBER_DEST_AIRPORT_ID = 14;
    private static final int NUMBER_CANCELLED = 19;
    private static final int NUMBER_ARR_DELAY = 18;
    private static final String FIRSTLINEofFIGHTS = "YEAR";
    private static final float NULL_TIME = 0;

    private static float getArrDellaytofArray(String elem){
        elem= elem.replaceAll(REPLACEABLE_COLON, REPLACEMENT_NULL);
        if (!elem.isEmpty()){
            System.out.println("Элемент равен " + Float.parseFloat(elem));
            return Float.parseFloat(elem);
        }else
            System.out.println("Элемент равен " + NULL_TIME);
            return NULL_TIME;
    }



    public static void main ( String [] args){
        SparkConf conf = new SparkConf().setAppName("lab5");
        JavaSparkContext sc = new JavaSparkContext (conf);

        JavaRDD<String> fileWithAirports = sc.textFile("/IDandName.csv");
        JavaRDD<String> airports = fileWithAirports.filter(s-> !s.contains(FIRSTLINEofAIRPORTS)).map(s-> s.replaceAll(REPLACEABLE_COLON, REPLACEMENT_NULL));
        JavaRDD<String[]> id_and_airport = airports.map(s -> s.split(REQEX_SPLITTER));
        JavaPairRDD<Integer, String> pairIdName = id_and_airport.mapToPair(s -> new Tuple2<>(parseInt(s[0]), s[1]));


        JavaRDD<String> fileWithFlight = sc.textFile("/IDandTime.csv");
        JavaRDD<String[]> features_flight = fileWithFlight.map(s -> s.split(REQEX));


        JavaPairRDD<Tuple2<Integer,Integer>, FlightSerializable> pairId_one_and_two = features_flight.filter(s ->!s[0].contains(FIRSTLINEofFIGHTS)).mapToPair(s -> new Tuple2<>(new Tuple2<>(Integer.parseInt(s[NUMBER_ORIGIN_AIRPORT_ID]),Integer.parseInt(s[NUMBER_DEST_AIRPORT_ID])),new FlightSerializable (getArrDellaytofArray(s[NUMBER_ARR_DELAY]), (int)Float.parseFloat(s[NUMBER_CANCELLED]))));

        JavaPairRDD<Tuple2<Integer,Integer>, FlightSerializable> key_result = pairId_one_and_two.combineByKey(
                v -> new FlightSerializable(v.getArr_delay_new(), 1, v.getCancelled(), (v.getArr_delay_new() > 0)? 1:0),
                (v, element)-> new FlightSerializable((v.getMaxArr_delay()< element.getArr_delay_new()) ? element.getArr_delay_new(): v.getMaxArr_delay(), v.getNum_flight() + 1, (element.getCancelled() == 0)? v.getNumCancelled() :v.getNumCancelled() + 1, (element.getArr_delay_new() != (float)0)? v.getNum_dellay() +1: v.getNum_dellay()),
                (com1, com2)-> new FlightSerializable((com1.getMaxArr_delay()< com2.getMaxArr_delay()) ? com2.getMaxArr_delay(): com1.getMaxArr_delay(), com1.getNum_flight() + com2.getNum_flight(), com1.getNumCancelled() + com2.getNumCancelled(),com1.getNum_dellay() + com2.getNum_dellay()));

        Map<Integer,String> key_resultAsMap = pairIdName.collectAsMap();

        final Broadcast<Map<Integer,String>> airportsBroadcasted = sc.broadcast(key_resultAsMap);


        JavaRDD<String> result = key_result.map(t -> FlightSerializable.combine(t._1, t._2, airportsBroadcasted.value()));
        result.saveAsTextFile("output");
    }


}
