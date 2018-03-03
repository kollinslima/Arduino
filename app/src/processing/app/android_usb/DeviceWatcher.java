package android_usb;

import java.io.File;

public class DeviceWatcher implements Runnable {

    private DeviceSelector deviceSelector;
    private USBHandler usbHandler;

    //To detect end of program;
    private boolean finish;

    public DeviceWatcher(DeviceSelector deviceSelector, USBHandler usbHandler) {
        this.deviceSelector = deviceSelector;
        this.usbHandler = usbHandler;
        finish = false;
    }

    @Override
    public void run() {

        File devWatcher = new File(usbHandler.getDevicePath());
        File[] numDevices = devWatcher.listFiles();

        int oldNumDevices = numDevices.length;

        while (!finish){

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            numDevices = devWatcher.listFiles();

            if(numDevices.length != oldNumDevices){
                oldNumDevices = numDevices.length;
                deviceSelector.loadList();
            }
        }

    }

    public synchronized void setFinish(boolean state){
        finish = state;
    }
}
