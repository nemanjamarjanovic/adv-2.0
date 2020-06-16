package com.nemanja.arduino.model.dto;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javafx.scene.paint.Color;

/**
 *
 * @author nemanja.marjanovic
 */
public class CurrentStateData
{

    private final SensorData last, current;
    public final String temperature, temperatureChange,
            humidity, humidityChange,
            co2, co2Change,
            airc, ventilator, status;
    public final Color temperatureColor, humidityColor, co2Color, temperatureCColor, humidityCColor, co2CColor, aircColor, ventilatorColor;
    public final int mod;
    
    public final Mods MOD = new Mods();

    public CurrentStateData(List<SensorData> list)
    {
        this.last = list.get(1);
        this.current = list.get(0);

        temperature = getDoubleString(this.current.getTemperature());
        humidity = getDoubleString(this.current.getHumidity());
        co2 = this.current.getCo2().toString();

        Double tempchDouble = this.current.getTemperature() - this.last.getTemperature();
        Double humchDouble = this.current.getHumidity() - this.last.getHumidity();
        Integer co2ch = this.current.getCo2() - this.last.getCo2();

        temperatureChange = getChangeString(tempchDouble);
        humidityChange = getChangeString(humchDouble);
        co2Change = ((co2ch > 0) ? "+" : "") + co2ch;

        temperatureCColor = getChangeColor(tempchDouble);
        humidityCColor = getChangeColor(humchDouble);
        co2CColor = getChangeColor(co2ch.doubleValue());

          mod = this.current.getMod();
        
        temperatureColor = getValueColor(this.current.getTemperature(), MOD.getMod(mod).TEMPERATURE_MIN, MOD.getMod(mod).TEMPERATURE_MAX);
        humidityColor = getValueColor(this.current.getHumidity(), MOD.getMod(mod).HUMIDITY_MIN, MOD.getMod(mod).HUMIDITY_MAX);
        co2Color = getValueColor(this.current.getCo2().doubleValue(), MOD.getMod(mod).CO2_MIN.doubleValue(),MOD.getMod(mod).CO2_MAX.doubleValue());

        airc = (this.current.isAirConditionerAcitve()) ? "UKLJUČEN" : "ISKLJUČEN";
        aircColor = (this.current.isAirConditionerAcitve()) ? Color.RED : Color.BLUE;
        ventilator = (this.current.isVentilatorActive()) ? "UKLJUČEN" : "ISKLJUČEN";
        ventilatorColor = (this.current.isVentilatorActive()) ? Color.RED : Color.BLUE;

        String date = new SimpleDateFormat("dd.MM.YYYY").format(this.current.getTime());
        String time = new SimpleDateFormat("HH:mm:ss").format(this.current.getTime());
        status = "Posljednje mjerenje: " + date + " u " + time;

      

    }

    private String getDoubleString(Double value)
    {
        return new DecimalFormat("00.00").format(value);
    }

    private String getChangeString(Double value)
    {
        return ((value > 0) ? "+" : "") + new DecimalFormat("00.00").format(value);
    }

    private Color getChangeColor(Double value)
    {
        if (value < 0)
        {
            return Color.LIGHTBLUE;
        }
        else if (value > 0)
        {
            return Color.DARKBLUE;
        }
        else
        {
            return Color.BLUE;
        }
    }

    private Color getValueColor(Double value, Double min, Double max)
    {

        if ((value > (min - 1) && value < (min + 1)) || (value > (max - 1) && value < (max + 1)))
        {
            return Color.ORANGE;
        }
        else if (value < (min - 1) || (value > (max + 1)))
        {
            return Color.RED;
        }
        else
        {
            return Color.GREEN;
        }
    }
}
