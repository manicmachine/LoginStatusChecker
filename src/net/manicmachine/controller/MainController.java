package net.manicmachine.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.manicmachine.model.Computer;
import net.manicmachine.model.CredType;
import net.manicmachine.model.Credential;

import java.io.File;
import java.net.URL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class MainController implements Initializable {

    // Define the maximum number of threads for the threadpool based upon the number of available processors
    final private int MAX_THREADS = Runtime.getRuntime().availableProcessors() + 1;

    @FXML MenuItem fileOpenMenu;
    @FXML TextField computersText;
    @FXML Button beginBtn;
    @FXML Button connectBtn;
    @FXML ListView<Computer> queueList;
    @FXML ListView<Computer> resultsList;
    @FXML Label lastUpdateLbl;

    private FileChooser fileChooser;
    private Stage stage;
    private File file;
    private ArrayList<String> allComputers;
    private ObservableList<Computer> queue;
    private ObservableList<Computer> results;
    private ExecutorService threadPool;
    private PsSessionListener psSessionListener;
    private ComputerChecker computerChecker;
    private boolean monitorRunning = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open File Containing a List of Computers");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        allComputers = new ArrayList<>();
        queue = FXCollections.observableArrayList();
        results = FXCollections.observableArrayList();
        threadPool = Executors.newFixedThreadPool(MAX_THREADS);
        initializePsSession();

        queueList.setItems(queue);
        resultsList.setItems(results);

        // Disable/enable 'Begin' button based upon if there's text entered
        computersText.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && computerChecker != null) {
                beginBtn.setDisable(false);
            } else {
                beginBtn.setDisable(true);
            }
        });

        computersText.setOnAction(event -> {
            if (computerChecker != null) {
                addToMonitor();
            }
        });

        // Clear list selection when selecting an item in another list
        queueList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            connectBtn.setDisable(true);
            Platform.runLater(() -> resultsList.getSelectionModel().clearSelection());
        });

        resultsList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            connectBtn.setDisable(false);
            Platform.runLater(() -> queueList.getSelectionModel().clearSelection());
        });

        try {
            File credFile = new File("secure_credentials");

            if (!credFile.exists()) {
                credFile.createNewFile();
            }

            // TODO: Change 'secretKey' to a master password provided by the user
            CipherManager cipherManager = new CipherManager(credFile, "test");
            if (credFile.exists() && credFile.isFile()) {
                // Decrypt and load credentials
                cipherManager.decrypt();
            } else {
                // TODO: Move encryption to it's own function to be invoked when app is closed via MainWindow
                // Test
                ArrayList<Credential> credentials = new ArrayList<>();
                Credential credential_1 = new Credential("test", "sathercd3383-lab", "test", "lab machines", CredType.PATTERN);
                Credential credential_2 = new Credential("another", "sathercd3383-srv", "test", "srv machines", CredType.OU);

                credentials.add(credential_1);
                credentials.add(credential_2);

                cipherManager.encrypt(credentials);
                // End test
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Initialize a PowerShell session in the background while the application launches. Doing so in the
    // foreground causes the app to take ~10 seconds to launch, whereas using a thread reduces that to ~1.
    public void initializePsSession() {
        PsSessionCreator sessionCreator = new PsSessionCreator();
        Thread psThread = new Thread(sessionCreator);
        psThread.setPriority(Thread.MAX_PRIORITY); // Increase priority to reduce wait time

        // Callback listener
        psSessionListener = () -> {
            computerChecker = new ComputerChecker(sessionCreator.getSession());

            Platform.runLater(() -> beginBtn.setText("Begin!"));
            if (computersText.getText().length() > 0) {
                beginBtn.setDisable(false);
            }
        };

        sessionCreator.setPsSessionListener(psSessionListener);
        psThread.start();
    }

    public void closePsSession() {
        if (computerChecker != null) {
            computerChecker.closeSession();
        }
    }

    public void closeThreadPool() {
        threadPool.shutdownNow();
    }

    // TODO: Edit this to process the selected file and add the extracted devices to the monitor queue
    @FXML private void openFileChooser() {
        file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            System.out.println(file.toString());
        }
    }

    @FXML private void addToMonitor() {
        NetworkWorker networkWorker;
        NetworkListener networkListener;
        String[] computers = computersText.getText().split("\\s,\\s|\\s,|,\\s|,|\\s");
        computersText.clear();

        for (String computerId: computers) {
            // Skip if the computer id is empty or has already been added to the monitor
            if (computerId.isEmpty() || allComputers.contains(computerId)) {
                continue;
            } else {
                allComputers.add(computerId);
            }

            // Generate a thread per network request to speed up connectivity testing
            networkWorker = new NetworkWorker();
            networkListener = (completedNetworkWorker) -> {
                Computer computer = completedNetworkWorker.getComputer();
                if (computer.isReachable()) {
                    Platform.runLater(() -> queue.add(computer));
                } else {
                    Platform.runLater(() -> results.add(computer));
                }
            };
            networkWorker.setNetworkListener(networkListener);
            networkWorker.setComputerId(computerId);
            threadPool.execute(networkWorker);
        }

        if (!monitorRunning) {
            beginMonitoring();
            beginBtn.setText("Add");
        }
    }

    @FXML private void beginMonitoring() {
        monitorRunning = true;
        NetworkWorker networkWorker;
        NetworkListener networkListener;

        for (Computer computer: queue) {
            networkWorker = new NetworkWorker();
            networkListener = (completedNetworkWorker) -> {
                if (completedNetworkWorker.getComputer().isReachable()) {
                    // TODO: Determine if the computer is available
                } else {
                    Platform.runLater(() -> results.add(computer));
                }
            };
            networkWorker.setNetworkListener(networkListener);
            networkWorker.setComputer(computer);
            threadPool.execute(networkWorker);
        }
    }

    // TODO: Implement this
    @FXML private void connectRemotely() {
        System.out.println("Connecting remotely too: " + resultsList.getSelectionModel().getSelectedItem().getIdentifier());
    }

    @FXML private void openCredentialWindow() {

    }
}