package com.nemanja.arduino.model.dao;

import com.nemanja.arduino.model.dto.SensorData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nemanja.marjanovic
 */
public class SensorDao extends AbstractDao
{

    private static final Logger logger = Logger.getLogger("LOG");

    public final static String SQL_CREATE_TABLE = " CREATE TABLE sensor_data "
            + " ( id IDENTITY PRIMARY KEY, serial_number VARCHAR, time TIMESTAMP, "
            + "temperature DOUBLE, humidity DOUBLE, co2 INTEGER, mod INTEGER, aircond BOOLEAN, ventilator BOOLEAN ) ";
    public final static String SQL_DELETE_TABLE = "DROP TABLE sensor_data";
    private final static String SQL_INSERT = " INSERT INTO sensor_data (serial_number, time,"
            + " temperature, humidity, co2, mod, aircond, ventilator) VALUES ( ?, ?, ?, ?, ?, ?, ?, ? ) ";
    private final static String SQL_QUERY_BETWEEN_TIMES = " SELECT * FROM sensor_data WHERE serial_number=? AND time BETWEEN ? AND ? ";
    private final static String SQL_QUERY_LAST_VALUES = " SELECT * FROM sensor_data WHERE serial_number=? ORDER BY time DESC LIMIT 2 ";
    private final static String SQL_QUERY_STATISTIC = " SELECT MIN(temperature),MAX(temperature),AVG(temperature),MIN(humidity),MAX(humidity),AVG(humidity),MIN(co2),MAX(co2),AVG(co2) FROM sensor_data WHERE serial_number=? AND time BETWEEN ? AND ? ";

    public boolean insert(SensorData sensorData)
    {
        try
        {
            Class.forName(DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DATABASE_URL))
            {
                PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT);

                preparedStatement.setString(1, sensorData.getSerialNumber());
                preparedStatement.setTimestamp(2, sensorData.getTime());
                preparedStatement.setDouble(3, sensorData.getTemperature());
                preparedStatement.setDouble(4, sensorData.getHumidity());
                preparedStatement.setInt(5, sensorData.getCo2());
                preparedStatement.setInt(6, sensorData.getMod());
                preparedStatement.setBoolean(7, sensorData.isAirConditionerAcitve());
                preparedStatement.setBoolean(8, sensorData.isVentilatorActive());
                boolean result = preparedStatement.execute();
                logger.log(Level.INFO, "SENSOR DATA INSERTED", sensorData);
                return result;
            }
        }
        catch (ClassNotFoundException | SQLException ex)
        {
            logger.log(Level.WARNING, "SENSOR DATA INSERT", ex.getLocalizedMessage());
            return false;
        }

    }

    public List<SensorData> findBeetwenTimes(String serialNumber, Timestamp before, Timestamp after)
    {
        List<SensorData> result = new ArrayList<>();
        try
        {
            Class.forName(DRIVER_NAME);
            try (Connection connection = DriverManager.getConnection(DATABASE_URL))
            {
                PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY_BETWEEN_TIMES);
                preparedStatement.setString(1, serialNumber);
                preparedStatement.setTimestamp(2, before);
                preparedStatement.setTimestamp(3, after);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next())
                {
                    result.add(new SensorData(
                            resultSet.getLong(1),
                            resultSet.getString(2),
                            resultSet.getTimestamp(3),
                            resultSet.getDouble(4),
                            resultSet.getDouble(5),
                            resultSet.getInt(6),
                            resultSet.getInt(7),
                            resultSet.getBoolean(8),
                            resultSet.getBoolean(9)));
                }
            }
        }
        catch (ClassNotFoundException | SQLException ex)
        {
            logger.log(Level.WARNING, "SENSOR DATA SQL_QUERY_BETWEEN_TIMES " + before + " " + after, ex.getLocalizedMessage());
        }

        logger.log(Level.INFO, "SENSOR DATA SQL_QUERY_BETWEEN_TIMES SUCCESS " + before + " " + after + " " + result.size());
        return result;
    }

    public List<Double> findStatistic(String serialNumber, Timestamp before, Timestamp after)
    {
        List<Double> result = new ArrayList<>();
        try
        {
            Class.forName(DRIVER_NAME);
            try (Connection connection = DriverManager.getConnection(DATABASE_URL))
            {
                PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY_STATISTIC);
                preparedStatement.setString(1, serialNumber);
                preparedStatement.setTimestamp(2, before);
                preparedStatement.setTimestamp(3, after);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next())
                {
                    result.add(resultSet.getDouble(1));
                    result.add(resultSet.getDouble(2));
                    result.add(resultSet.getDouble(3));
                    result.add(resultSet.getDouble(4));
                    result.add(resultSet.getDouble(5));
                    result.add(resultSet.getDouble(6));
                    result.add(resultSet.getDouble(7));
                    result.add(resultSet.getDouble(8));
                    result.add(resultSet.getDouble(9));
                }
            }
        }
        catch (ClassNotFoundException | SQLException ex)
        {
            logger.log(Level.WARNING, "SENSOR DATA SQL_QUERY_BETWEEN_TIMES " + before + " " + after, ex.getLocalizedMessage());
        }

        logger.log(Level.INFO, "SENSOR DATA SQL_QUERY_BETWEEN_TIMES SUCCESS " + before + " " + after + " " + result.size());
        return result;
    }

    public List<SensorData> findLastValues(String serialNumber)
    {
        List<SensorData> result = new ArrayList<>();
        try
        {
            Class.forName(DRIVER_NAME);
            try (Connection connection = DriverManager.getConnection(DATABASE_URL))
            {
                PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY_LAST_VALUES);
                preparedStatement.setString(1, serialNumber);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next())
                {
                    result.add(new SensorData(
                            resultSet.getLong(1),
                            resultSet.getString(2),
                            resultSet.getTimestamp(3),
                            resultSet.getDouble(4),
                            resultSet.getDouble(5),
                            resultSet.getInt(6),
                            resultSet.getInt(7),
                            resultSet.getBoolean(8),
                            resultSet.getBoolean(9)));
                }
            }
        }
        catch (ClassNotFoundException | SQLException ex)
        {
            logger.log(Level.WARNING, "SENSOR DATA SQL_QUERY_LAST_VALUES", ex.getLocalizedMessage());
            return null;
        }

        logger.log(Level.INFO, "SENSOR DATA SQL_QUERY_LAST_VALUES SUCCESS");
        return result;
    }
}
