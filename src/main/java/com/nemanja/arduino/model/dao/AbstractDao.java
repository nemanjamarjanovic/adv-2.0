/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nemanja.arduino.model.dao;

import com.nemanja.arduino.model.dto.Constant;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author nemanja.marjanovic
 */
public abstract class AbstractDao
{

    protected final static String DRIVER_NAME = "org.h2.Driver";
    protected final static String DATABASE_URL = "jdbc:h2:" + Constant.DATABASE_FILE_NAME;

    public boolean executeSQL(String SQL) throws ClassNotFoundException, SQLException
    {
        Class.forName(DRIVER_NAME);
        try (Connection connection = DriverManager.getConnection(DATABASE_URL))
        {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL);
            preparedStatement.execute();
        }
        return true;
    }
}
