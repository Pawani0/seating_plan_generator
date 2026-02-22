package com.seatingplan;

import com.seatingplan.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Main entry point for the Seating Plan Generator application.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            MainWindow mainWindow = new MainWindow(primaryStage);
            mainWindow.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("Failed to start application");
            alert.setContentText(e.getMessage() + "\n\n" + getStackTrace(e));
            alert.showAndWait();
        }
    }

    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement elem : e.getStackTrace()) {
            if (sb.length() > 500) {
                sb.append("...");
                break;
            }
            sb.append(elem.toString()).append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Error starting application: " + e.getMessage(),
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
