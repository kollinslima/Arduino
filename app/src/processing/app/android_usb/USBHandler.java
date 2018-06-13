package android_usb;

import java.nio.file.Path;
import java.util.ArrayList;

public interface USBHandler {
    public ArrayList<String> getDevices();
    public String getDevicePath();
    public Path getDevicePathForDevice(String device);
    public int copyHexToDevice(String friendlyDevice);
    int sendSerialMessage(String friendlyDevice, String message);
}
