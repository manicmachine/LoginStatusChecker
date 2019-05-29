package net.manicmachine.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.manicmachine.model.Computer;
import net.manicmachine.model.CredType;
import net.manicmachine.model.Credential;

import javax.crypto.BadPaddingException;
import java.io.File;
import java.net.URL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
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
    private CipherManager cipherManager;
    private CredentialManager credentialManager;
    private boolean monitorRunning = false;
    private boolean usingCredFile = true;

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

        try {
            File credFile = new File("secure_credentials");

            if (!credFile.exists()) {
                credFile.createNewFile();
            }

            do {
                try {
                    String masterPassword = masterPasswordPrompt();

                    if (masterPassword.isEmpty()) {
                        usingCredFile = false;
                        break;
                    } else {
                        cipherManager = new CipherManager(credFile, masterPassword);
                        credentialManager = new CredentialManager(cipherManager.decrypt());
                        break;
                    }
                } catch (BadPaddingException e) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Incorrect Password");
                    alert.setHeaderText("Incorrect password provided.");

                    ButtonType tryAgain = new ButtonType ("Try Again");
                    ButtonType clearCredentials = new ButtonType("Clear Credentials");

                    alert.getButtonTypes().clear();
                    alert.getButtonTypes().addAll(tryAgain, clearCredentials);

                    ((Button) alert.getDialogPane().lookupButton(tryAgain)).setDefaultButton(tryAgain == tryAgain);

                    Optional<ButtonType> option = alert.showAndWait();

                    if (option.get() == clearCredentials) {
                        credFile.delete();
                        credFile.createNewFile();
                        break;
                    } else {
                        continue;
                    }
                }
            } while(true);

        } catch (IOException e) {
            System.out.println("Error occurred while initializing cipher or credential manager: " + e.getMessage());
        }

        // Listeners
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
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Initialize a PowerShell session in the background while the application launches.
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

    // Button actions
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

    // Menu Actions
    // TODO: Edit this to process the selected file and add the extracted devices to the monitor queue
    @FXML private void openFileChooser() {
        file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            System.out.println(file.toString());
        }
    }

    @FXML private void closeApplication() {
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML private void openCredentialManager() {
        Parent root;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/CredentialManager.fxml"));
            root = loader.load();
            CredentialManagerController controller = loader.getController();
            controller.initData(credentialManager);
            Stage stage = new Stage();
            stage.setTitle("Credential Manager");
            stage.setScene(new Scene(root,600, 400));
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unable to open Credential Manager");
            alert.setHeaderText("An error occurred while opening the Credential Manager.");
            alert.setContentText("Unable to open Credential Manager: " + e.getMessage());
            alert.show();
        }
    }

    // Prompts
    private String masterPasswordPrompt() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input Master Password");
        dialog.setHeaderText("The provided Master Password will be used to encrypt/decrypt your credentials.\n" +
                "Leaving the prompt blank will result in your credentials not being stored.");
        dialog.setContentText("Master Password:\n ");

        Optional<String> password = dialog.showAndWait();
        if (password.isPresent()) {
            System.out.println(password.get());
            return password.get();
        } else {
            return "";
        }
    }

    // Clean up actions
    public void closePsSession() {
        if (computerChecker != null) {
            computerChecker.closeSession();
        }
    }

    public void closeThreadPool() {
        threadPool.shutdownNow();
    }

    public void storeCredentials() {
        if (usingCredFile) {
            try {
                cipherManager.encrypt(credentialManager.getCredentials());
            } catch (Exception e) {
                System.out.println("An error occurred while encrypting credentials: " + e.getMessage());
                System.out.println("Quitting without storing credentials.");
            }
        }
    }
}
