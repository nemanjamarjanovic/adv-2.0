package com.nemanja.arduino.model.table;

import java.text.DecimalFormat;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class SensorTableData
{

    private final SimpleStringProperty period;
    private final SimpleStringProperty min;
    private final SimpleStringProperty max;
    private final SimpleStringProperty avg;
    
    private final DecimalFormat formater = new DecimalFormat("00.00");

    public SensorTableData(String period, Double min, Double max, Double avg)
    {
        this.period = new SimpleStringProperty(period);
        this.min = new SimpleStringProperty(formater.format(min));
        this.max = new SimpleStringProperty(formater.format(max));
        this.avg = new SimpleStringProperty(formater.format(avg));
    }

    public String getPeriod()
    {
        return period.get();
    }

    public String getMin()
    {
        return min.get();
    }

    public String getMax()
    {
        return max.get();
    }

    public String getAvg()
    {
        return avg.get();
    }

}
