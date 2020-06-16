package com.nemanja.arduino.model.dto;

import java.sql.Timestamp;

/**
 *
 * @author nemanja.marjanovic
 */
public class SensorData
{

    private final Long id;
    private final String serialNumber;
    private final Timestamp time;
    private final Double temperature;
    private final Double humidity;
    private final Integer co2;
    private final Integer mod;
    private final Boolean airConditionerAcitve;
    private final Boolean ventilatorActive;

    public SensorData()
    {
        this.id = new Long(0);
        this.serialNumber = "UNKNOWN";
        this.time = new Timestamp(System.currentTimeMillis());
        this.temperature = 0.00;
        this.humidity = 0.00;
        this.co2 = 0;
        this.mod = 0;
        this.airConditionerAcitve = false;
        this.ventilatorActive = false;
    }

    public SensorData(Long id, String serialNumber, Timestamp time, Double temperature, Double humidity,
            Integer co2, Integer mod, Boolean airConditionerAcitve, Boolean ventilatorActive)
    {
        this.id = id;
        this.serialNumber = serialNumber;
        this.time = time;
        this.temperature = temperature;
        this.humidity = humidity;
        this.co2 = co2;
        this.mod = mod;
        this.airConditionerAcitve = airConditionerAcitve;
        this.ventilatorActive = ventilatorActive;
    }

    public Timestamp getTime()
    {
        return time;
    }

    public Double getTemperature()
    {
        return temperature;
    }

    public Double getHumidity()
    {
        return humidity;
    }

    public Integer getCo2()
    {
        return co2;
    }

    public Boolean isAirConditionerAcitve()
    {
        return airConditionerAcitve;
    }

    public Boolean isVentilatorActive()
    {
        return ventilatorActive;
    }

    public Long getId()
    {
        return id;
    }

    public String getSerialNumber()
    {
        return serialNumber;
    }

    public Integer getMod()
    {
        return mod;
    }

    @Override
    public String toString()
    {
        return "SensorData{" + "id=" + id + ", serialNumber=" + serialNumber + ", time=" + time + ", temperature=" + temperature + ", humidity=" + humidity + ", co2=" + co2 + ", mod=" + mod + ", airConditionerAcitve=" + airConditionerAcitve + ", ventilatorActive=" + ventilatorActive + '}';
    }

}
