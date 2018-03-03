package android_usb;

import java.util.ArrayList;

public interface USBHandler {
    public ArrayList<String> getDevices();
    public String getDevicePath();
    public int copyHexToDevice(String friendlyDevice);
}
