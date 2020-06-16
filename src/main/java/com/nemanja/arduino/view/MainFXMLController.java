package com.nemanja.arduino.view;

import com.nemanja.arduino.controller.NetworkController;
import com.nemanja.arduino.controller.RandomController;
import com.nemanja.arduino.model.dto.Constant;
import com.nemanja.arduino.model.dao.SensorDao;
import com.nemanja.arduino.model.dto.CurrentStateData;
import com.nemanja.arduino.model.dto.Mods;
import com.nemanja.arduino.model.dto.SensorData;
import com.nemanja.arduino.model.table.SensorTableData;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.FixedFormatTickFormatter;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.gillius.jfxutils.chart.StableTicksAxis;

public class MainFXMLController implements Initializable
{

    private static final Logger logger = Logger.getLogger("LOG");

    private XYChart.Series<Number, Number> series;
    private Timeline addDataTimeline;

    private final SensorDao sensorDao = new SensorDao();
    private final RandomController randomController = new RandomController();
    private final NetworkController networkController = new NetworkController();
    private final List<String> alarmList = new ArrayList<>();
    public final Mods MOD = new Mods();

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        locationValue.setText(Constant.LOCATION);
        ipValue.setText(Constant.IP_ADDRESS);

        sensorSelectComboBox.getItems().add("Temperatura:");
        sensorSelectComboBox.getItems().add("Vlažnost:");
        sensorSelectComboBox.getItems().add("CO2:");

        setMod(0);
        fromDatePicker.setValue(LocalDate.now());
        toDatePicker.setValue(LocalDate.now().plusDays(1L));

        setTimeChangeListener(toTime);
        setTimeChangeListener(fromTime);

        setDatePickerFormatter(toDatePicker);
        setDatePickerFormatter(fromDatePicker);

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.YY HH:mm:ss");
        ((StableTicksAxis) chart.getXAxis()).setAxisTickFormatter(
                new FixedFormatTickFormatter(format));

        series = new XYChart.Series<>();

        chart.getData().add(series);

        initZoomUtils();

        initTable(temperatureTable);
        initTable(humidityTable);
        initTable(co2Table);

        populateTable();
        addUpdateEventHandler();

        //addDataTimeline = new Timeline(new KeyFrame(Duration.millis(Constant.UPDATE_PERIOD * 1000L), this));
        addDataTimeline.setCycleCount(Animation.INDEFINITE);
        addDataTimeline.play();

    }

    @FXML
    public void addDataToChart()
    {

        Timestamp t1 = Timestamp.valueOf(fromDatePicker.getValue().atStartOfDay());
        Timestamp t2 = Timestamp.valueOf(toDatePicker.getValue().atStartOfDay());

        t1.setTime(t1.getTime() + getTimeValue(fromTime));
        t2.setTime(t2.getTime() + getTimeValue(toTime));

        series.getData().clear();
        chart.getXAxis().setAutoRanging(true);
        chart.getYAxis().setAutoRanging(true);
        //There seems to be some bug, even with the default NumberAxis, that simply setting the
        //auto ranging does not recompute the ranges. So we clear all chart data then re-add it.
        //Hopefully I find a more proper way for this, unless it's really bug, in which case I hope
        //it gets fixed.
        ObservableList<XYChart.Series<Number, Number>> data = chart.getData();
        chart.setData(FXCollections.<XYChart.Series<Number, Number>>emptyObservableList());
        chart.setData(data);

        List<SensorData> list = sensorDao.findBeetwenTimes(Constant.SERIAL_NUMBER, t1, t2);

        if (list.isEmpty())
        {
            return;
        }

        final StableTicksAxis xAxis = (StableTicksAxis) chart.getXAxis();

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(list.get(0).getTime().getTime());
        xAxis.setUpperBound(list.get(list.size() - 1).getTime().getTime());

        series.setName(sensorSelectComboBox.getSelectionModel().getSelectedItem());

        if (sensorSelectComboBox.getSelectionModel().isSelected(0)) {
            for (SensorData sensorData : list) {
                series.getData().add(new XYChart.Data<Number, Number>(sensorData.getTime().getTime(), sensorData.getTemperature()));
            }

        } else if (sensorSelectComboBox.getSelectionModel().isSelected(1)) {
            for (SensorData sensorData : list) {
                series.getData().add(new XYChart.Data<Number, Number>(sensorData.getTime().getTime(), sensorData.getHumidity()));
            }
        } else if (sensorSelectComboBox.getSelectionModel().isSelected(2)) {
            for (SensorData sensorData : list) {
                series.getData().add(new XYChart.Data<Number, Number>(sensorData.getTime().getTime(), sensorData.getCo2()));
            }
        }

    }

    private void setMod(int mod) {

        modtminLabel.setText(MOD.getMod(mod).TEMPERATURE_MIN.toString());
        modtmaxLabel.setText(MOD.getMod(mod).TEMPERATURE_MAX.toString());

        modvminLabel.setText(MOD.getMod(mod).HUMIDITY_MIN.toString());
        modvmaxLabel.setText(MOD.getMod(mod).HUMIDITY_MAX.toString());

        modcminLabel.setText(MOD.getMod(mod).CO2_MIN.toString());
        modcmaxLabel.setText(MOD.getMod(mod).CO2_MAX.toString());
    }

    private void initTable(TableView tableView) {

        TableColumn tc1 = (TableColumn) tableView.getColumns().get(0);
        tc1.setCellValueFactory(new PropertyValueFactory<SensorTableData, String>("period"));
        TableColumn tc2 = (TableColumn) tableView.getColumns().get(1);
        tc2.setCellValueFactory(new PropertyValueFactory<SensorTableData, Double>("min"));
        TableColumn tc3 = (TableColumn) tableView.getColumns().get(2);
        tc3.setCellValueFactory(new PropertyValueFactory<SensorTableData, Double>("max"));
        TableColumn tc4 = (TableColumn) tableView.getColumns().get(3);
        tc4.setCellValueFactory(new PropertyValueFactory<SensorTableData, Double>("avg"));
    }

    private void populateTable() {

        Timestamp t1 = new Timestamp(System.currentTimeMillis());
        Timestamp t2 = new Timestamp(t1.getTime() - Constant.MINUTE);
        List<Double> minuteTableData = sensorDao.findStatistic(Constant.SERIAL_NUMBER, t2, t1);

        t1 = new Timestamp(System.currentTimeMillis());
        t2 = new Timestamp(t1.getTime() - Constant.HOUR);
        List<Double> hourTableData = sensorDao.findStatistic(Constant.SERIAL_NUMBER, t2, t1);

        t1 = new Timestamp(System.currentTimeMillis());
        t2 = new Timestamp(t1.getTime() - Constant.DAY);
        List<Double> dayTableData = sensorDao.findStatistic(Constant.SERIAL_NUMBER, t2, t1);

        t1 = new Timestamp(System.currentTimeMillis());
        t2 = new Timestamp(t1.getTime() - Constant.WEEK);
        List<Double> weekTableData = sensorDao.findStatistic(Constant.SERIAL_NUMBER, t2, t1);

        try {
            ObservableList<SensorTableData> datat = FXCollections.observableArrayList(
                    new SensorTableData("Minut", minuteTableData.get(0), minuteTableData.get(1), minuteTableData.get(2)),
                    new SensorTableData("Sat", hourTableData.get(0), hourTableData.get(1), hourTableData.get(2)),
                    new SensorTableData("Dan", dayTableData.get(0), dayTableData.get(1), dayTableData.get(2)),
                    new SensorTableData("Sedmica", weekTableData.get(0), dayTableData.get(1), dayTableData.get(2))
            );
            temperatureTable.setItems(datat);

            ObservableList<SensorTableData> datah = FXCollections.observableArrayList(
                    new SensorTableData("Minut", minuteTableData.get(3), minuteTableData.get(4), minuteTableData.get(5)),
                    new SensorTableData("Sat", hourTableData.get(3), hourTableData.get(4), hourTableData.get(5)),
                    new SensorTableData("Dan", dayTableData.get(3), dayTableData.get(4), dayTableData.get(5)),
                    new SensorTableData("Sedmica", weekTableData.get(3), dayTableData.get(4), dayTableData.get(5))
            );
            humidityTable.setItems(datah);

            ObservableList<SensorTableData> datac = FXCollections.observableArrayList(
                    new SensorTableData("Minut", minuteTableData.get(6), minuteTableData.get(7), minuteTableData.get(8)),
                    new SensorTableData("Sat", hourTableData.get(6), hourTableData.get(7), hourTableData.get(8)),
                    new SensorTableData("Dan", dayTableData.get(6), dayTableData.get(7), dayTableData.get(8)),
                    new SensorTableData("Sedmica", weekTableData.get(6), dayTableData.get(7), dayTableData.get(8))
            );
            co2Table.setItems(datac);
        } catch (Exception e) {
            logger.log(Level.INFO, "NEMA STATISTIKE");
        }
    }

    private void setDeviceActivity(boolean active) {
        deviceStatusValue.setText((active) ? "Aktivan" : "Nedostupan");
    }

    private void setCurrentValues(CurrentStateData currentStateData) {
        temperatureValue.setText(currentStateData.temperature);
        humidityValue.setText(currentStateData.humidity);
        co2Value.setText(currentStateData.co2);

        temperatureChangeValue.setText(currentStateData.temperatureChange);
        humidityChangeValue.setText(currentStateData.humidityChange);
        co2ChangeValue.setText(currentStateData.co2Change);

        temperatureChangeValue.setTextFill(currentStateData.temperatureCColor);
        humidityChangeValue.setTextFill(currentStateData.humidityCColor);
        co2ChangeValue.setTextFill(currentStateData.co2CColor);

        temperatureValue.setTextFill(currentStateData.temperatureColor);
        humidityValue.setTextFill(currentStateData.humidityColor);
        co2Value.setTextFill(currentStateData.co2Color);

        statusLabel.setText(currentStateData.status);
        modValue.setText(String.valueOf(currentStateData.mod));

        fanLabel.setText(currentStateData.ventilator);
        acLabel.setText(currentStateData.airc);

        setMod(currentStateData.mod);
    }

    private void addUpdateEventHandler() {
        addDataTimeline = new Timeline(new KeyFrame(Duration.millis(Constant.UPDATE_PERIOD * 1000L), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                startUpdate();

                populateTable();
            }
        }));
    }

    private void addAlarm() {
        while (alarmList.size() > 10) {
            alarmList.remove(alarmList.size() - 1);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY HH:mm:ss");
        alarmList.add(0, sdf.format(new Timestamp(System.currentTimeMillis()))
                + ": Uredjaj na lokaciji " + locationValue.getText() + " nije dostupan!\n");

        logLabel.setText("");
        for (String line : alarmList) {
            logLabel.setText(logLabel.getText() + line);
        }
    }

    private long getTimeValue(final TextField text) {
        try {
            String value = text.getText();
            if (value != null && !value.isEmpty()) {
                String split[] = value.split(":");
                long hours = Long.parseLong(split[0]);
                long minutes = Long.parseLong(split[1]);

                return hours * Constant.HOUR + minutes * Constant.MINUTE;
            } else {
                return 0L;
            }
        } catch (Exception e) {
            return 0L;
        }
    }

    private void setTimeChangeListener(final TextField text) {
        text.textProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        if (newValue.matches("\\d{1}")) {
                            text.setText(newValue);
                        } else if (newValue.matches("\\d{2}")) {
                            text.setText(newValue + ":");
                        } else if (newValue.matches("\\d{1,2}:")) {
                            if (Long.parseLong(newValue.split(":")[0]) > 23) {
                                text.setText("23:");
                                text.positionCaret(text.getLength());
                            }
                        } else if (newValue.matches("\\d{1,2}:\\d{1,2}")) {
                            if (Long.parseLong(newValue.split(":")[1]) > 59) {
                                text.setText(newValue.split(":")[0] + ":59");
                                text.positionCaret(text.getLength());
                            }
                        } else {
                            text.setText(oldValue);
                            text.positionCaret(text.getLength());
                        }
                    }
                });

        text.focusedProperty().addListener(
                new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable,
                            Boolean oldValue, Boolean newValue) {
                        if (!newValue) {
                            if (!text.getText().matches("\\d{1,2}:\\d{1,2}")) {
                                text.setText("00:00");
                            }
                        }
                    }
                });
    }

    private void setDatePickerFormatter(final DatePicker datePicker) {
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.YYYY.");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };

        datePicker.setConverter(converter);
    }

    private void initZoomUtils() {
        chart.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                final StableTicksAxis xAxis = (StableTicksAxis) chart.getXAxis();
                final StableTicksAxis yAxis = (StableTicksAxis) chart.getYAxis();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY HH:mm:ss");
                DecimalFormat df = new DecimalFormat("#00.00");
                pointedValueLabel.setText("Vrijeme: " + sdf.format(xAxis.getValueForDisplay(mouseEvent.getX()))
                        + " - Vrijednost: " + df.format(yAxis.getValueForDisplay(mouseEvent.getY())));
            }
        });

        //Panning works via either secondary (right) mouse or primary with ctrl held down
        ChartPanManager panner = new ChartPanManager(chart);
        panner.setMouseFilter(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.SECONDARY
                        || (mouseEvent.getButton() == MouseButton.PRIMARY
                        && mouseEvent.isShortcutDown())) {
                    //let it through
                } else {
                    mouseEvent.consume();
                }
            }
        });
        panner.start();

        //Zooming works only via primary mouse button without ctrl held down
        JFXChartUtil.setupZooming(chart, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() != MouseButton.PRIMARY
                        || mouseEvent.isShortcutDown()) {
                    mouseEvent.consume();
                }
            }
        });
    }

    @FXML
    private Label temperatureValue;

    @FXML
    private Label humidityValue;

    @FXML
    private Label co2Value;

    @FXML
    private Label temperatureChangeValue;

    @FXML
    private Label humidityChangeValue;

    @FXML
    private Label co2ChangeValue;

    @FXML
    private Label logLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private LineChart<Number, Number> chart;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private Label pointedValueLabel;

    @FXML
    private Label locationValue;

    @FXML
    private Label ipValue;

    @FXML
    private Label modValue;

    @FXML
    private Label deviceStatusValue;

    @FXML
    private ComboBox<String> sensorSelectComboBox;

    @FXML
    private TextField fromTime;

    @FXML
    private TextField toTime;

    @FXML
    private Label modtminLabel;

    @FXML
    private Label modtmaxLabel;

    @FXML
    private Label modvminLabel;

    @FXML
    private Label modvmaxLabel;

    @FXML
    private Label modcminLabel;

    @FXML
    private Label modcmaxLabel;

    @FXML
    private Label acLabel;

    @FXML
    private Label fanLabel;

    @FXML
    private TableView<SensorTableData> temperatureTable;

    @FXML
    private TableView<SensorTableData> humidityTable;

    @FXML
    private TableView<SensorTableData> co2Table;

    private Task deviceDataUpdateTask;

    private void startUpdate()
    {
        deviceDataUpdateTask = new Task()
        {

            @Override
            protected Object call() throws Exception
            {
                final SensorData newSensorData = networkController.getCurrentDataFromDevice();
                // SensorData newSensorData = randomController.getRandomValue();
                Platform.runLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (newSensorData != null)
                        {
                            sensorDao.insert(newSensorData);
                            logger.log(Level.INFO, "DEVICE ONLINE");
                            setDeviceActivity(true);
                        }
                        else
                        {
                            logger.log(Level.INFO,
                                    "DEVICE OFFLINE "
                                    + new SimpleDateFormat("dd.MM.YYYY HH:mm:ss").format(new Timestamp(System.currentTimeMillis())));
                            setDeviceActivity(false);
                            addAlarm();
                        }

                        List<SensorData> sensorDataList = sensorDao.findLastValues(Constant.SERIAL_NUMBER);
                        if (sensorDataList != null)
                        {
                            setCurrentValues(new CurrentStateData(sensorDataList));
                        }
                    }
                });
                return null;
            }
        };
        
        Thread thread = new Thread(deviceDataUpdateTask);
        thread.setDaemon(true);
        thread.start();
    }
}
