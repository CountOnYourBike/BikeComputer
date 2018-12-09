package pl.edu.pg.eti.bikecounter;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class Measurement implements Serializable {
    private Integer numberOfRevolutions;
    private Integer wheelEventTime;

    Measurement(Integer numberOfRevolutions, Integer wheelEventTime) {
        this.numberOfRevolutions = numberOfRevolutions;
        this.wheelEventTime = wheelEventTime;
    }

    @NonNull
    @Override
    public String toString() {
        return numberOfRevolutions.toString() + " " + wheelEventTime.toString();
    }

    static Measurement fromString(String s) {
        String numberOfRevString, wheelEventTimeString;
        numberOfRevString = s.split(" ")[0];
        wheelEventTimeString = s.split(" ")[1];
        return new Measurement(
                Integer.parseInt(numberOfRevString),
                Integer.parseInt(wheelEventTimeString));
    }

    // returning speed in kilometers per hour with two decimal signs
    // parameter circ is circumference of the wheel in millimeters
    double getSpeed(double circ) {
        double speed = circ*numberOfRevolutions / (double)wheelEventTime; // m/s
        // actual unit is millimeters per millisecond, which is equal meters per second
        // to convert it to kilometers per hour we need to multiply it by 3.6
        speed *= 3.6;
        // return number with tho decimal signs only
        return Math.floor(speed*100)/100;
    }

    Integer getNumberOfRevolutions() {
        return numberOfRevolutions;
    }

    // returning distance in kilometres
    // parameter circ is circumference of the wheel in millimeters
    double getDistance(double circ) {
        // we have to divide it by 10e6 to get kilometers from millimeters
        return circ * numberOfRevolutions / 1000000.;
    }
}
