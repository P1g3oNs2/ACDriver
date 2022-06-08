package acdriver;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;

public class MainCtrl {

    TelemetryService telemetryService;
    TractionControl tc;
    ControllerInput ci;
    ControllerOutput co;
    boolean running = false;

    double multiplier = 1.0;

    public MainCtrl() {
        tc = new TractionControl();
        telemetryService = new TelemetryService(tc);
        ci = new ControllerInput(this, tc);
    }

    @FXML
    private Button button;

    @FXML
    private Label calcWheelLabel;

    @FXML
    private Label wheel1Label;

    @FXML
    private Label wheel2Label;

    @FXML
    private Label wheel3Label;

    @FXML
    private Label wheel4Label;

    @FXML
    private TextField toleranceField;

    @FXML
    private Button controllerButton;

    @FXML
    private Label inputLabel;

    List<Label> wheelLabels;

    @FXML
    void onControllerButtonClick() {
        ci.initialize();
    }

    @FXML
    void onButtonClick() {
        if(running){
            running = false;
            new Thread(new Runnable() {
                public void run() {
                    telemetryService.stop();
                }
            }).start();
            button.setText("Start tracking");
        } else {
            running = true;
            startTelemetryTracking();

            new Thread(new Runnable() {
                public void run() {
                    tc.setMultiplier(multiplier);
                    telemetryService.start();
                }
            }).start();

            button.setText("Stop tracking");
        }

        toleranceField.setText(String.valueOf(multiplier));
    }

    private void startTelemetryTracking() {
        wheelLabels = List.of(getWheel1Label(), getWheel2Label(), getWheel3Label(), getWheel4Label());



        new Thread(new Runnable() {
            public void run() {
                while(running){
                    // tc shit
                    float[] wheelSpeeds = tc.getWheelSpeeds();
                    boolean[] wheelSlips = tc.getWheelSlips();
                    double calculatedSpeed = tc.getCalculatedSpeed();
                    if(wheelSpeeds != null && wheelSlips != null){
                        Platform.runLater(() -> {
                            setLabels(wheelSpeeds, wheelSlips, calculatedSpeed);
                        });
                    }
                    // abs

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @FXML
    void onTextChanged() {
        try {
            multiplier = Double.parseDouble(toleranceField.getText());
        } catch (NumberFormatException e) {
        }

    }

    public void setLabels(float[] wheelSpeeds, boolean[] wheelSlips, double calcSpeed){
        getCalcWheelLabel().setText(String.valueOf(calcSpeed));

        double tolerance = Math.sqrt(calcSpeed) * multiplier;

        getToleranceField().setText(String.valueOf(tolerance));

        // set text and color for every wheel
        for(int i = 0; i < wheelSpeeds.length; i++){
            wheelLabels.get(i).setText(String.valueOf(wheelSpeeds[i]));
            wheelLabels.get(i).setStyle("");

            // check for wheel slip
            if(wheelSlips[i]){
                wheelLabels.get(i).setStyle("-fx-background-color: red");
            }
        }
    }

    public void setInputLabel(String input){
        inputLabel.setText(input);
    }

    public void setControllerButtonText(String input){
        controllerButton.setText(input);
    }

    public Label getCalcWheelLabel() {
        return calcWheelLabel;
    }

    public Label getWheel1Label() {
        return wheel1Label;
    }

    public Label getWheel2Label() {
        return wheel2Label;
    }

    public Label getWheel3Label() {
        return wheel3Label;
    }

    public Label getWheel4Label() {
        return wheel4Label;
    }

    public TextField getToleranceField() {
        return toleranceField;
    }
}