package com.nemanja.arduino.controller;

import com.nemanja.arduino.model.dto.Constant;
import com.nemanja.arduino.model.dto.SensorData;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nemanja.marjanovic
 */
public class NetworkController
{

    private final static Logger logger = Logger.getLogger("LOG");

    public SensorData getCurrentDataFromDevice()
    {
        DatagramSocket socket = null;
        try
        {
            socket = new DatagramSocket();
            socket.setSoTimeout(Constant.NETWORK_TIMEOUT * 1000);
            InetSocketAddress address = new InetSocketAddress(Constant.IP_ADDRESS, Integer.parseInt(Constant.PORT));
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];
            DatagramPacket sendPacket = new DatagramPacket("V".getBytes(), "V".getBytes().length, address);
            socket.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            socket.close();
            String response = new String(receivePacket.getData());
            System.out.println(response);
            logger.log(Level.INFO, "RECEIVED: " + response);

            response = response.replace(",", ".");
            response = response.trim();
            String[] values = response.split(";");
            for (String value : values)
            {
                value = value.trim();
            }

            //SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YY-HH:mm:ss");
            SensorData result = new SensorData(null, values[0], new Timestamp(System.currentTimeMillis()),
                    Double.parseDouble(values[1]), Double.parseDouble(values[2]), Integer.parseInt(values[3]),
                    Integer.parseInt(values[6]),
                    values[4].equals("DA"), values[5].equals("DA"));

            return result;
        }
        catch (IOException e)
        {
            if (socket != null)
            {
                socket.close();
            }
            logger.log(Level.INFO, "NETWORK ERROR: ", e);
            return null;
        }
        catch (Exception e)
        {

            logger.log(Level.INFO, "UNKNOWN ERROR: ", e);
            return null;
        }
    }

}
