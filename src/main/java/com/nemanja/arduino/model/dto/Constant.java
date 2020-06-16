package com.nemanja.arduino.model.dto;

/**
 *
 * @author nemanja.marjanovic
 */
public class Constant {

    public static final String DATABASE_FILE_NAME = "./arduino_baza_podataka";

    public static final String IP_ADDRESS = "192.168.1.106";
    public static final String PORT = "8888";
    public static final String SERIAL_NUMBER = "1000456";
    public static final String LOCATION = "Prostorija 1";

    public static final Long UPDATE_PERIOD = 20L;
    public static final Integer NETWORK_TIMEOUT = 18;

    public static final Long SECOND = 1000L;
    public static final Long MINUTE = 60 * 1000L;
    public static final Long HOUR = 60 * 60 * 1000L;
    public static final Long DAY = 24 * 60 * 60 * 1000L;
    public static final Long WEEK = 7 * 24 * 60 * 60 * 1000L;

}
