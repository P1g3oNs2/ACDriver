package acdriver;

import telemetryservice.corsa.RTCarInfo;
import telemetryservice.corsa.TelemetryInterface;
import javafx.application.Platform;

import java.io.IOException;

/**
 * Created by Marcos on 8/15/2015.
 */
public class TelemetryService {
    TelemetryInterface telemetryInterface;
    TractionControl tc;

    boolean stop = false;

    public TelemetryService(TractionControl tc) {
        telemetryInterface = new TelemetryInterface();
        this.tc = tc;
    }

    public void start(){
        telemetryInterface = new TelemetryInterface();
        telemetryInterface.connect();
        stop = false;

        new Thread(new Runnable() {
            public void run() {
                while(!stop){
                    RTCarInfo telemetry = telemetryInterface.getTelemetry();
                    if(telemetry != null){
                        Platform.runLater(() -> {
                            tc.calculate(telemetry);
                        });
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void stop(){
        stop = true;
        telemetryInterface.stop();
    }
}
