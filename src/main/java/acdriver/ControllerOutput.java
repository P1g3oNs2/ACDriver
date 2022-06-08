package acdriver;

import javafx.application.Platform;
import redlaboratory.jvjoyinterface.VJoy;
import redlaboratory.jvjoyinterface.VjdStat;

public class ControllerOutput {

    private int rID;

    private VJoy vJoy;

    private boolean[] buttons;

    boolean polling = false;

    private TractionControl tc;

    ControllerOutput(int rID, TractionControl tc){
        this.rID = rID;

        vJoy = new VJoy();

        this.tc = tc;

        buttons = new boolean[32];
        for(int i = 0; i < buttons.length; i++){
            buttons[i] = false;
        }

        System.out.println("\n--------- vJoy ---------");

        if (!vJoy.vJoyEnabled()) {
            System.out.println("vJoy driver not enabled: Failed Getting vJoy attributes.");

            return;
        } else {
            System.out.println("Vender: " + vJoy.getvJoyManufacturerString());
            System.out.println("Product: " + vJoy.getvJoyProductString());
            System.out.println("Version Number: " + vJoy.getvJoyVersion());
        }

        if (vJoy.driverMatch()) {
            System.out.println("Version of Driver Matches DLL Version {0}");
        } else {
            System.out.println("Version of Driver {0} does NOT match DLL Version {1}");
        }

        VjdStat status = vJoy.getVJDStatus(rID);
        if ((status == VjdStat.VJD_STAT_OWN) ||
            ((status == VjdStat.VJD_STAT_FREE) && (!vJoy.acquireVJD(rID)))) {
            System.out.println("Failed to acquire vJoy device number " + rID);
        } else {
            System.out.println("Acquired: vJoy device number "+ rID);
        }

        System.out.println("number of buttons: " + vJoy.getVJDButtonNumber(rID));
        System.out.println("------------------------\n");
    }

    public void startPollingTC(){
        polling = true;
        new Thread(new Runnable() {
            public void run() {
                while(polling){
                    // traction control shit
                    boolean[] wheelSlips = tc.getWheelSlips();
                    if(wheelSlips != null){
                        for(int i = 0; i < wheelSlips.length + 1; i++){
                            if(i < wheelSlips.length && wheelSlips[i]){
                                vJoy.setAxis(16384, rID, VJoy.HID_USAGE_Z);
                                break;
                            }
                            if(i == wheelSlips.length){
                                ZAxis(zValue);
                            }
                        }
                    }

                    // abs shit

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    private double mapValue(double value){
        value *= -1;
        value = Math.max(value, -0.99);
        value = Math.min(value, 0.99);

        value += 0.99;

        value /= 1.98;

        value *= 32768;

        return value;
    }

    private double zValue = 0;

    public void ZAxis(double value){ // map
        zValue = value;

        value = mapValue(value);

        // apply tc and abs

        vJoy.setAxis((int)value, rID, VJoy.HID_USAGE_Z);
    }

    public void XAxis(double value){ // map
        value = mapValue(value);

        // apply tc and abs

        vJoy.setAxis((int)value, rID, VJoy.HID_USAGE_X);
    }

    public void YRotation(double value){ // map
        value = mapValue(value);



        vJoy.setAxis((int)value, rID, VJoy.HID_USAGE_RY);
    }

    public void Button(int num, double value){
        buttons[num] = !buttons[num];
        vJoy.setBtn(buttons[num], rID, num + 1);
    }

    public void vJoyTest() {

        vJoy.setAxis(0, rID, VJoy.HID_USAGE_X);
        vJoy.setAxis(16384, rID, VJoy.HID_USAGE_Y);
        vJoy.setAxis(32768, rID, VJoy.HID_USAGE_Z);

        vJoy.setAxis(0, rID, VJoy.HID_USAGE_RX);
        vJoy.setAxis(16384, rID, VJoy.HID_USAGE_RY);
        vJoy.setAxis(32768, rID, VJoy.HID_USAGE_RZ);

        vJoy.setAxis(0, rID, VJoy.HID_USAGE_SL0);
        vJoy.setAxis(16384, rID, VJoy.HID_USAGE_SL1);

        for (int i = 1; i <= 32; i++) vJoy.setBtn(Math.random() > 0.5 ? true : false, rID, i);
    }
}
