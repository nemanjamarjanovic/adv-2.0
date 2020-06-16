package com.nemanja.arduino.model.dto;

public class Mod {

    public final Double TEMPERATURE_MIN;
    public final Double TEMPERATURE_MAX;
    public final Double HUMIDITY_MIN;
    public final Double HUMIDITY_MAX;
    public final Integer CO2_MIN;
    public final Integer CO2_MAX;

    public Mod(Double TEMPERATURE_MIN, Double TEMPERATURE_MAX, Double HUMIDITY_MIN, Double HUMIDITY_MAX, Integer CO2_MIN, Integer CO2_MAX) {
        this.TEMPERATURE_MIN = TEMPERATURE_MIN;
        this.TEMPERATURE_MAX = TEMPERATURE_MAX;
        this.HUMIDITY_MIN = HUMIDITY_MIN;
        this.HUMIDITY_MAX = HUMIDITY_MAX;
        this.CO2_MIN = CO2_MIN;
        this.CO2_MAX = CO2_MAX;
    }

}
