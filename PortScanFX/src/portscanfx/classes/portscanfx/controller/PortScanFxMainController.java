package portscanfx.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.InputEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * FXML Controller class for 'PortScanFxMain' ui.
 *
 * @author mkroll
 * @version 20170907
 */
public class PortScanFxMainController implements Initializable {

    private Stage primaryStage;
    private boolean scanning;
    private static boolean firstTableRow;
    private final ArrayList<Integer> MeineArrayList = new ArrayList<>();
    private Element portlist;
    private int counter;
    private Thread task = null;
    private final ExecutorService es = Executors.newFixedThreadPool(40);
    @FXML
    Button StartScanButton;
    @FXML
    Button ExportButton;
    @FXML
    TextField HostAddress;
    @FXML
    TextField PortScanFrom;
    @FXML
    TextField PortScanTo;
    @FXML
    ProgressBar ProgressBar;
    @FXML
    MenuItem Quit;
    @FXML
    TableColumn HostnameTableColumn;
    @FXML
    TableColumn PortTableColumn;
    @FXML
    TableColumn MemoTableColumn;
    @FXML
    TableView<PortScanFxMainController.DataTable> table;

    ObservableList<PortScanFxMainController.DataTable> dataTable;

    public PortScanFxMainController() {
        this.table = new TableView<>();
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            ProgressBar.setVisible(false);
//            Document doc = new SAXBuilder().build(this.getClass().getClassLoader().getResourceAsStream("/portscanfx/data/portlist.xml"));
            Document doc = new SAXBuilder().build(PortScanFxMainController.class.getResourceAsStream("/portscanfx/data/portlist.xml"));
            portlist = doc.getRootElement();
        } catch (JDOMException | IOException ex) {
            Logger.getLogger(PortScanFxMainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Exit application and close all active threads
     *
     * @param event
     */
    @FXML
    protected void exitButtonAction(ActionEvent event) {
        Platform.exit();
    }

    /**
     * Handle key event for HostAddress textfield
     *
     * @param event
     * @throws Exception
     */
    @FXML
    protected void handleHostAddressKeyEvent(final InputEvent event) throws Exception {
        if (!"".equals(HostAddress.getText())) {
            StartScanButton.setDisable(false);
        } else {
            StartScanButton.setDisable(true);
        }
    }

    /**
     * Start portscan
     *
     * @param event
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @FXML
    @SuppressWarnings("CallToThreadStopSuspendOrResumeManager")
    protected void timerButtonAction(ActionEvent event) throws InterruptedException, ExecutionException {
        startStopTimer();
        if (scanning) {
            try {
                long test = 1;
                Thread.currentThread().interrupt();
                es.shutdownNow();
                task.interrupt();
                task.join();
            } catch (InterruptedException e) {
                System.out.println("Main thread Interrupted");
            }
            StartScanButton.setText("Start scanning");
            scanning = false;
            task.stop();
        } else {
            if (es.isShutdown()) {
                System.out.println("'es' is shutdown.");
            }
            scanning = true;
            StartScanButton.setText("Stop scanning");
            task.start();
        }
    }

    /**
     * Start or stop the timer / threads
     */
    private void startStopTimer() throws InterruptedException, ExecutionException {
        final String ip = HostAddress.getText();
        final int timeout = 200;
        final List<Future<Boolean>> futures = new ArrayList<>();

        counter = 0;
        firstTableRow = true;
        ExportButton.setDisable(true);
        ProgressBar.setVisible(true);
        task = new Thread() {
            @Override
            public void run() {
                try {
                    for (int port = Integer.valueOf(PortScanFrom.getText()); port <= Integer.valueOf(PortScanTo.getText()); port++) {
                        futures.add(portIsOpen(es, ip, port, timeout));
                    }
                    es.shutdown();
                    for (final Future<Boolean> f : futures) {
                        if (f.get()) {
                            // 
                        }
                    }
                    for (int i = 0; i < MeineArrayList.size(); i++) {
                        if (firstTableRow) {
                            dataTable = FXCollections.observableArrayList(new PortScanFxMainController.DataTable(ip, String.valueOf(MeineArrayList.get(i)), getPortMemo(String.valueOf(MeineArrayList.get(i)))));
                            HostnameTableColumn.setCellValueFactory(new PropertyValueFactory<>("hostname"));
                            PortTableColumn.setCellValueFactory(new PropertyValueFactory<>("port"));
                            MemoTableColumn.setCellValueFactory(new PropertyValueFactory<>("memo"));
                            table.setItems(dataTable);
                            firstTableRow = false;
                        } else {
                            dataTable.add(new PortScanFxMainController.DataTable(ip, String.valueOf(MeineArrayList.get(i)), getPortMemo(String.valueOf(MeineArrayList.get(i)))));
                        }
                    }
                    ExportButton.setDisable(false);
                    ProgressBar.setVisible(false);
                    scanning = false;
                    task.stop();
                } catch (InterruptedException | ExecutionException e) {
                    Logger.getLogger(PortScanFxMainController.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        };
    }

    /**
     *
     * @param es
     * @param ip
     * @param port
     * @param timeout
     * @return
     */
    public Future<Boolean> portIsOpen(final ExecutorService es, final String ip, final int port, final int timeout) {
        return es.submit(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ip, port), timeout);
                MeineArrayList.add(port);
            } catch (Exception ex) {
                Progress(port);
                return false;
            }
            return true;
        });
    }

    private void Progress(int _port) {
        double port = _port;
        double pb;
        pb = (port / Double.valueOf(PortScanTo.getText()));
        Platform.runLater(() -> {
            ProgressBar.setProgress(pb);
        });
    }

    /**
     * Open about dialog
     *
     * @param event
     */
    @FXML
    protected void aboutButtonAction(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("PortScanFX");
        alert.setContentText("(c)2017 Michael Kroll\n\n");
        alert.showAndWait();

    }

    /**
     * Return port description for given portnumber
     *
     * @param _port
     * @return String with port description
     */
    public String getPortMemo(String _port) {
        String Port = _port;
        String Memo = "Not define";

        for (Element port : portlist.getChildren("port")) {
            if (Port.equals(port.getAttribute("number").getValue())) {
                Element beschreibung = port.getChild("Beschreibung");
                Memo = beschreibung.getText();
            }
        }
        return Memo;
    }

    /**
     * Action event for csv export button
     *
     * @param event
     */
    @FXML
    protected void cvsExportButtonAction(ActionEvent event) {
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(primaryStage);

            writeExcel(selectedDirectory.getAbsolutePath());
        } catch (Exception ex) {
            Logger.getLogger(PortScanFxMainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Set stage given from main class
     *
     * @param primaryStage
     */
    public void setStage(Stage primaryStage) {
        primaryStage = this.primaryStage;
    }

    /**
     * Export TableView to an csv file
     *
     * @param selectedDirectory
     * @throws Exception
     */
    public void writeExcel(String selectedDirectory) throws Exception {
        Writer writer = null;
        try {
            File file = new File(selectedDirectory + "\\" + HostAddress.getText() + ".csv");
            writer = new BufferedWriter(new FileWriter(file));
            String text = "Hostname;Reachable Port;Description\n";
            writer.write(text);
            for (DataTable datTable : dataTable) {
                text = datTable.getHostname() + ";" + datTable.getPort() + ";" + datTable.getMemo() + "\n";
                writer.write(text);
            }
        } catch (IOException ex) {
            Logger.getLogger(PortScanFxMainController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            writer.flush();
            writer.close();
        }
    }

    /**
     * Table definition class
     */
    public static class DataTable {

        private final SimpleStringProperty hostname;
        private final SimpleStringProperty port;
        private final SimpleStringProperty memo;

        private DataTable(String _hostname, String _port, String _memo) {
            this.hostname = new SimpleStringProperty(_hostname);
            this.port = new SimpleStringProperty(_port);
            this.memo = new SimpleStringProperty(_memo);
        }

        public String getHostname() {
            return hostname.get();
        }

        public void setHostname(String fHostname) {
            hostname.set(fHostname);
        }

        public String getPort() {
            return port.get();
        }

        public void setPort(String fPort) {
            port.set(fPort);
        }

        public String getMemo() {
            return memo.get();
        }

        public void setMemo(String fMemo) {
            port.set(fMemo);
        }
    }
}
