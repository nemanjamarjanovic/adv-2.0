package com.nemanja.arduino.view;

import com.nemanja.arduino.model.dao.SensorDao;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application
{

    private static final Logger logger = Logger.getLogger("LOG");
    FileHandler fileHandler;

    @Override
    public void start(Stage stage) throws Exception
    {

        fileHandler = new FileHandler("LOG" + System.currentTimeMillis() + ".xml");
        logger.addHandler(fileHandler);

//        if (Files.exists(Paths.get(Constant.FULL_DATABASE_FILE_NAME)))
//        {
//            logger.log(Level.INFO, "BAZA PODATAKA POSTOJI!");
//        }
//        else
//        {
//            logger.log(Level.SEVERE, "NEDOSTAJE BAZA PODATAKA!");
        try
        {
            new SensorDao().executeSQL(SensorDao.SQL_CREATE_TABLE);
        }
        catch (ClassNotFoundException | SQLException ex)
        {
            logger.log(Level.INFO, "SENSOR DATA SQL_CREATE_TABLE FAIL", ex.getLocalizedMessage());
        }
//        }

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        stage.setTitle("Pregled podataka sa senzora");
        stage.getIcons().add(new Image("images/icon.png"));
        stage.setScene(scene);
        stage.show();

    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }

}
