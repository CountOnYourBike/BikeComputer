package pl.edu.pg.eti.bikecounter;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class Wheel {

    private final static String TAG = Wheel.class.getSimpleName();
    private String mETRTO;
    private String mInch;
    private double mCirc;

    private Wheel(String ETRTO, String Inch, double Circ){
        this.mETRTO = ETRTO;
        this.mInch = Inch;
        this.mCirc = Circ;
    }

    static List<String> getValuesList(Context context, String system){

        ArrayList<Wheel> wheels = getAllWheels();
        List<String> valuesList = new ArrayList<>();

        if(system.equals(context.getResources().getString(R.string.ETRTO_system))) {
            for (Wheel wheel : wheels) {
                valuesList.add(wheel.mETRTO);
            }
        } else if(system.equals(context.getResources().getString(R.string.inch_system))) {
            for (Wheel wheel : wheels) {
                valuesList.add(wheel.mInch);
            }
        } else if(system.equals(context.getResources().getString(R.string.circ_system))) {
            for(Wheel wheel : wheels) {
                int circAsInteger = (int) Math.floor(wheel.mCirc);
                valuesList.add(circAsInteger + " mm");
            }
        }
        return valuesList;
    }

    static double getCircValue(Context context, String System, String value){

        ArrayList<Wheel> wheels = getAllWheels();

        if(System.equals(context.getResources().getString(R.string.ETRTO_system))) {
            for(Wheel wheel: wheels) {
                if (wheel.mETRTO.equals(value))
                    return wheel.mCirc;
            }
        } else if(System.equals(context.getResources().getString(R.string.inch_system))) {
            for(Wheel wheel: wheels){
                if(wheel.mInch.equals(value))
                    return wheel.mCirc;
            }
        } else if(System.equals(context.getResources().getString(R.string.circ_system))){
            for(Wheel wheel: wheels){
                // mCirc is in double notation (0.0), but value is in mm notation (0 mm)
                if(Double.toString(wheel.mCirc).split("\\.")[0].equals(value.split(" ")[0]))
                    return wheel.mCirc;
            }
        } else {
            throw new IllegalArgumentException("Not correct String value of wheel scale");
        }
        throw new IllegalArgumentException("Not correct String value of wheel scale");
    }

    private static ArrayList<Wheel> getAllWheels(){
        ArrayList<Wheel> wheels = new ArrayList<Wheel>();
        wheels.add(new Wheel("47-203 "," 12\" x1.75 ", 935.));
        wheels.add(new Wheel("54-203 "," 12\" x1.95 ", 940.));
        wheels.add(new Wheel("40-254 "," 14\" x1.50 ", 1020.));
        wheels.add(new Wheel("47-254 "," 14\" x1.75 ", 1055.));
        wheels.add(new Wheel("40-305 "," 16\" x1.50 ", 1185.));
        wheels.add(new Wheel("47-305 "," 16\" x1.75 ", 1195.));
        wheels.add(new Wheel("54-305 "," 16\" x2.00 ", 1245.));
        wheels.add(new Wheel("28-349 "," 16\" x1-1/8 ", 1290.));
        wheels.add(new Wheel("37-349 "," 16\" x1-3/8 ", 1300.));
        wheels.add(new Wheel("32-369 "," 17\" x1-1/4 ", 1340.));
        wheels.add(new Wheel("40-355 "," 18\" x1.50 ", 1340.));
        wheels.add(new Wheel("47-355 "," 18\" x1.75 ", 1350.));
        wheels.add(new Wheel("32-406 "," 20\" x1.25 ", 1450.));
        wheels.add(new Wheel("35-406 "," 20\" x1.35 ", 1460.));
        wheels.add(new Wheel("40-406 "," 20\" x1.50 ", 1490.));
        wheels.add(new Wheel("47-406 "," 20\" x1.75 ", 1515.));
        wheels.add(new Wheel("50-406 "," 20\" x1.95 ", 1565.));
        wheels.add(new Wheel("28-451 "," 20\" x1-1/8 ", 1545.));
        wheels.add(new Wheel("37-451 "," 20\" x1-3/8 ", 1615.));
        wheels.add(new Wheel("37-501 "," 22\" x1-3/8 ", 1770.));
        wheels.add(new Wheel("40-501 "," 22\" x1-1/2 ", 1785.));
        wheels.add(new Wheel("47-507 "," 24\" x1.75 ", 1890.));
        wheels.add(new Wheel("50-507 "," 24\" x2.00 ", 1925.));
        wheels.add(new Wheel("54-507 "," 24\" x2.125 ", 1965.));
        wheels.add(new Wheel("25-520 "," 24\" x1(520) ", 1753.));
        wheels.add(new Wheel("25-520 "," 24\" x3/4 Tubular ", 1785.));
        wheels.add(new Wheel("28-540 "," 24\" x1-1/8 ", 1795.));
        wheels.add(new Wheel("32-540 "," 24\" x1-1/4 ", 1905.));
        wheels.add(new Wheel("25-559 "," 26\" x1(559) ", 1913.));
        wheels.add(new Wheel("32-559 "," 26\" x1.25 ", 1950.));
        wheels.add(new Wheel("37-559 "," 26\" x1.40 ", 2005.));
        wheels.add(new Wheel("40-559 "," 26\" x1.50 ", 2010.));
        wheels.add(new Wheel("47-559 "," 26\" x1.75 ", 2023.));
        wheels.add(new Wheel("50-559 "," 26\" x1.95 ", 2050.));
        wheels.add(new Wheel("54-559 "," 26\" x2.10 ", 2068.));
        wheels.add(new Wheel("57-559 "," 26\" x2.125 ", 2070.));
        wheels.add(new Wheel("58-559 "," 26\" x2.35 ", 2083.));
        wheels.add(new Wheel("75-559 "," 26\" x3.00 ", 2170.));
        wheels.add(new Wheel("28-590 "," 26\" x1-1/8 ", 1970.));
        wheels.add(new Wheel("37-590 "," 26\" x1-3/8 ", 2068.));
        wheels.add(new Wheel("37-584 "," 26\" x1-1/2 ", 2100.));
        wheels.add(new Wheel("650C "," Tubular 26\" x7/8 ", 1920.));
        wheels.add(new Wheel("20-571 "," 650x20C ", 1938.));
        wheels.add(new Wheel("23-571 "," 650x23C ", 1944.));
        wheels.add(new Wheel("25-571 "," 650x25C 26\" x1(571) ", 1952.));
        wheels.add(new Wheel("40-590 "," 650x38A ", 2125.));
        wheels.add(new Wheel("40-584 "," 650x38B ", 2105.));
        wheels.add(new Wheel("25-630 "," 27\" x1(630) ", 2145.));
        wheels.add(new Wheel("28-630 "," 27\" x1-1/8 ", 2155.));
        wheels.add(new Wheel("32-630 "," 27\" x1-1/4 ", 2161.));
        wheels.add(new Wheel("37-630 "," 27\" x1-3/8 ", 2169.));
        wheels.add(new Wheel("40-584 "," 27.5\" x1.50 ", 2079.));
        wheels.add(new Wheel("54-584 "," 27.5\" x2.1 ", 2148.));
        wheels.add(new Wheel("57-584 "," 27.5\" x2.25 ", 2182.));
        wheels.add(new Wheel(" 18-622 "," 700x18C ", 2070.));
        wheels.add(new Wheel("19-622 "," 700x19C ", 2080.));
        wheels.add(new Wheel("20-622 "," 700x20C ", 2086.));
        wheels.add(new Wheel("23-622 "," 700x23C ", 2096.));
        wheels.add(new Wheel("25-622 "," 700x25C ", 2105.));
        wheels.add(new Wheel("28-622 "," 700x28C ", 2136.));
        wheels.add(new Wheel("30-622 "," 700x30C ", 2146.));
        wheels.add(new Wheel("32-622 "," 700x32C ", 2155.));
        wheels.add(new Wheel("700C "," Tubular ", 2130.));
        wheels.add(new Wheel("35-622 "," 700x35C ", 2168.));
        wheels.add(new Wheel("38-622 "," 700x38C ", 2180.));
        wheels.add(new Wheel("40-622 "," 700x40C ", 2200.));
        wheels.add(new Wheel("42-622 "," 700x42C ", 2224.));
        wheels.add(new Wheel("44-622 "," 700x44C ", 2235.));
        wheels.add(new Wheel("45-622 "," 700x45C ", 2242.));
        wheels.add(new Wheel("47-622 "," 700x47C ", 2268.));
        wheels.add(new Wheel("54-622 "," 29\" x2.1 ", 2288.));
        wheels.add(new Wheel("56-622 "," 29\" x2.2 ", 2298.));
        wheels.add(new Wheel("60-622 "," 29\" x2.3 ", 2326.));

        return wheels;
    }
}
