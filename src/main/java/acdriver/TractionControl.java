package acdriver;

import telemetryservice.corsa.RTCarInfo;

public class TractionControl {

    /**
     * wheel info format:
     * rotational speeds of:
     * 0: front left
     * 1: front right
     * 2: back left
     * 3: back right
     */
    private float[] wheelSpeeds;
    /**
     * true if wheel is travelling too fast
     */
    private boolean[] wheelSlips;
    /**
     * Expected wheel speed
     */
    private double calculatedSpeed;



    double multiplier = 1.0;

    TractionControl(){
        wheelSpeeds = new float[4];
        wheelSlips = new boolean[4];
        calculatedSpeed = 0.0;
    }

    public void setMultiplier(double t){
        this.multiplier = t;
    }

    public void calculate(RTCarInfo tel){
        // get wheel speeds from telemetry
        float[] newWheelSpeeds = tel.getWheelAngularSpeed();

        // initializes wheel slipping array
        boolean[] newWheelSlips = new boolean[4];

        // calculate the expected wheel speed
        // formula: car speed / front left tyre diameter
        // round to 0 if speed <= 0.1
        // TODO: probably needs to be separate per wheel
        float newCalcSpeed = tel.getSpeed_Ms()/tel.getTyreRadius()[0];
        newCalcSpeed = newCalcSpeed > 0.1 ? newCalcSpeed : 0;

        // variable for how close to the calculated speed the wheel speed has to be
        double tolerance = Math.sqrt(newCalcSpeed) * multiplier;

        // sets the wheel slip values for every wheel
        for(int i = 0; i < newWheelSpeeds.length; i++){
            newWheelSlips[i] = Math.abs(newWheelSpeeds[i] - newCalcSpeed) > tolerance;
        }

        // sets the global values to the new values
        wheelSpeeds = newWheelSpeeds;
        calculatedSpeed = newCalcSpeed;
        wheelSlips = newWheelSlips;
    }

    public float[] getWheelSpeeds() {
        return wheelSpeeds;
    }

    public boolean[] getWheelSlips() {
        return wheelSlips;
    }

    public double getCalculatedSpeed() {
        return calculatedSpeed;
    }
}
