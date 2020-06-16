package com.nemanja.arduino.controller;

import com.nemanja.arduino.model.dto.Constant;
import com.nemanja.arduino.model.dto.SensorData;
import java.sql.Timestamp;
import java.util.Random;

/**
 *
 * @author nemanja.marjanovic
 */
public class RandomController
{

    public SensorData getRandomValue()
    {
        Random random = new Random(System.currentTimeMillis());

        if (random.nextBoolean() && random.nextBoolean() && random.nextBoolean())
        {
            return null;
        }

        double temp = random.nextDouble() * 10 + 12;
        double vl = random.nextDouble() * 30 + 70;
        int co2 = random.nextInt(1200) + 4800;
        Boolean k = random.nextBoolean();
        Boolean v = random.nextBoolean();
        int mod = random.nextInt(3) + 1;

        return new SensorData(null, Constant.SERIAL_NUMBER, new Timestamp(System.currentTimeMillis()), temp, vl, co2, mod, k, v);
    }
}
