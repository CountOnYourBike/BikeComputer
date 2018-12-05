package pl.edu.pg.eti.bikecounter;

import java.io.Serializable;

public class Measurement implements Serializable {
    Integer numberOfRevolutions;
    Integer wheelEventTime;

    public Measurement(Integer numberOfRevolutions, Integer wheelEventTime) {
        this.numberOfRevolutions = numberOfRevolutions;
        this.wheelEventTime = wheelEventTime;
    }

    @Override
    public String toString() {
        return numberOfRevolutions.toString() + " " + wheelEventTime.toString();
    }

    public static Measurement fromString(String s) {
        String numberOfRevString, wheelEventTimeString;
        numberOfRevString = s.split(" ")[0];
        wheelEventTimeString = s.split(" ")[1];
        return new Measurement(Integer.parseInt(numberOfRevString), Integer.parseInt(wheelEventTimeString));
    }

    public double getSpeed(Double circ) {    // wheelCirc w mm
        double speed = circ*numberOfRevolutions / (double)wheelEventTime; // m/s
        speed = speed * 3.6;
        return Math.floor(speed*100)/100;
    }

    public Integer getNumberOfRevolutions() {
        return numberOfRevolutions;
    }

    public double getDistance(Double circ) { // in km
        return circ * numberOfRevolutions / 1000000.;
    }
}
