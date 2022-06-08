package acdriver;

import javafx.application.Platform;
import net.java.games.input.*;
import redlaboratory.jvjoyinterface.VJoy;
import redlaboratory.jvjoyinterface.VjdStat;

import java.util.Scanner;

public class ControllerInput {

    Event event = new Event();
    ControllerOutput co;
    MainCtrl mc;

    boolean running = false;

    public ControllerInput(MainCtrl mainCtrl, TractionControl tc) {
        mc = mainCtrl;
        co = new ControllerOutput(1, tc);
        mc.co = this.co;
    }

    public void initialize(){
        if(!running){
            Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

            for (int i = 0; i < controllers.length; i++) {
                System.out.println(i + ": " + controllers[i]);
            }

            Scanner scanner = new Scanner(System.in);

            int selection = Integer.valueOf(scanner.next());

            startPolling(controllers[selection]);

            mc.setControllerButtonText("Stop polling");
            co.startPollingTC();
        } else {
            running = false;
            mc.setControllerButtonText("Start polling");
            co.polling = false;
        }


        //co.vJoyTest();


    }

    private void startPolling(Controller controller){
        running = true;
        new Thread(new Runnable() {
            public void run() {
                while(running){
                    /* Remember to poll each one */
                    controller.poll();

                    /* Get the controllers event queue */
                    EventQueue queue = controller.getEventQueue();

                    /* For each object in the queue */
                    while (queue.getNextEvent(event)) {
                        /* Get event component */
                        Component comp = event.getComponent();
                        String name = comp.getName();
                        if(name.equals("Z Axis")) {
                            co.ZAxis(comp.getPollData());
                        } else if(name.equals("X Axis")){
                            co.XAxis(comp.getPollData());
                        } else if(name.equals("Y Rotation")){
                            co.YRotation(comp.getPollData());
                        } else if(name.contains("Button ")){
                            co.Button(Integer.parseInt(Character.toString(name.charAt(name.length() - 1))), comp.getPollData());
                        }
                        Platform.runLater(() -> {
                            mc.setInputLabel(comp.getName() + ": " + comp.getPollData());
                        });
                    }
                }
            }
        }).start();
    }
}
