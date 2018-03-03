package android_usb;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class USBManager implements USBHandler {

    private final String ANDROID_PACKAGE_NAME = "ArduinoSimulator";

    //Private files won't work
//    private final Path ANDROID_PATH = Paths.get("Android/data/" + ANDROID_PACKAGE_NAME + "/files");

    private final Path ANDROID_PATH = Paths.get("DCIM/" + ANDROID_PACKAGE_NAME + "/ArduinoCode/");

    private final String LINUX_MTP_PATH = "XDG_RUNTIME_DIR";
    private final String GVFS_DIR = "/gvfs/";
    private String pathToDevices;

    private ArrayList<String> rawDevices;
    private ArrayList<String> friendlyDevices;

    public USBManager(){
        rawDevices = new ArrayList<String>();
        friendlyDevices = new ArrayList<String>();
    }

    @Override
    public ArrayList<String> getDevices() {

        rawDevices.clear();
        friendlyDevices.clear();

        if (System.getProperty("os.name").toLowerCase().contains("windows")){
            return getDevicesWindows();
        }
        else {
            return getDevicesLinux();
        }
    }

    private ArrayList<String> getDevicesWindows(){
        return null;
    }

    private ArrayList<String> getDevicesLinux(){

        //Get mout point location for MTP devices on Linux
        pathToDevices = System.getenv(LINUX_MTP_PATH);
        pathToDevices = pathToDevices.concat(GVFS_DIR);

        //Get all devices in mout point
        File dirDevices = new File(pathToDevices);
        File[] listDirDevices = dirDevices.listFiles();

        for (File file : listDirDevices){
            if (file.isDirectory()){
                System.out.println(file.getName());
                rawDevices.add(file.getName());
            }
        }

        // Extract device info
        Pattern extractData = Pattern.compile("[0-9][0-9][0-9].*[0-9][0-9][0-9]");
        for (String rawDevice : rawDevices) {
            Matcher matcher = extractData.matcher(rawDevice);
            String bus = null, device = null;
            if (matcher.find()) {
                String split = matcher.group();
                bus = split.substring(0, 3);
                device = split.substring(split.length() - 3, split.length());

                System.out.println("Bus: " + bus);
                System.out.println("Device: " + device);
            }

            //Try to get friendly name
            ArrayList<String> lsusbCommand = new ArrayList<String>();
            lsusbCommand.add("lsusb");
            lsusbCommand.add("-s");
            lsusbCommand.add(bus + ":" + device);

            ArrayList<String> devices = runExternal(lsusbCommand);

            if (device == null) {
                return rawDevices;
            }
            else {
                //Extract frendly name from devices
                for (String newDevice : devices) {
                    int idIndex = newDevice.indexOf("ID");
                    int colIndex = newDevice.indexOf(":", idIndex);
                    friendlyDevices.add("[Bus:" + bus + " Device:" + device + "] " +
                            newDevice.substring(colIndex + 5, newDevice.length()).trim());
                }
            }
        }

        return friendlyDevices;

    }

    public synchronized String getDevicePath(){
        return pathToDevices;
    }

    private ArrayList<String> runExternal(ArrayList<String> args){
        try {
            ProcessBuilder pb = new ProcessBuilder (args);
            Process p = pb.start();
            p.waitFor();

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            // read the output from the command
            String s = null;
            ArrayList<String> response = new ArrayList<String>();
            response.clear();
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
                response.add(s);

            }

            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
                return null;    //Return null on error
            }

            return response;
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int copyHexToDevice(String friendlyDevice) {

        //Get directory of selected device
        Path devicePath = Paths.get(pathToDevices + "/" +
                rawDevices.get(friendlyDevices.indexOf(friendlyDevice)));
        System.out.println(devicePath.toString());

        //Find hexFile in system
        File hexFile = getHexFile();

        if (hexFile == null){
            System.out.println(".hex not found");
        }

        hexFile.mkdirs();
        Path sourcePath = hexFile.toPath();

        System.out.println("Source: " + sourcePath);

        //Find target directory
        File directories = new File(devicePath.toString());
        File[] listDirectories = directories.listFiles();

        //Copy to SDCard and MicroSD
        for (File file : listDirectories){
            if (file.isDirectory()){

                File targetFile = new File(file.toString() + "/" + ANDROID_PATH);
                targetFile.mkdirs();

                String targetPath = targetFile.toString().concat("/code.hex");
                System.out.println("Target: " + targetPath);

                //Alguns dispositivos MTP não aceitam manipulação direta, por isso
                //usar comando externo gio copy.

                //Files.copy(sourcePath, targetPath, REPLACE_EXISTING);

                ArrayList<String> gioCopyCommand = new ArrayList<String>();
                gioCopyCommand.add("gio");
                gioCopyCommand.add("copy");
                gioCopyCommand.add(sourcePath.toString());
                gioCopyCommand.add(targetPath.toString());

                if(runExternal(gioCopyCommand) == null){
                    System.out.println("Copy fail");
                }
            }
        }
        return 0;
    }

    private File getHexFile(){
        if (System.getProperty("os.name").toLowerCase().contains("windows")){
            return getHexFileWindows();
        }
        else {
            return getHexFileLinux();
        }
    }

    private File getHexFileWindows(){
        return null;
    }

    private File getHexFileLinux(){

        File hexFile;

        File tmpDirectories = new File("/tmp");
        File[] listTmpDirectories = tmpDirectories.listFiles();

        ArrayList<File> arduinoDir = new ArrayList<File>();

        //Find all arduino_build directories
        for (File file : listTmpDirectories){
            if (file.isDirectory() && file.toString().contains("arduino_build")){
                arduinoDir.add(file);
            }
        }

        //Find most recent modified arduino_build directory
        File recentModifiedDir = arduinoDir.get(0);
        for (int i = 1; i < arduinoDir.size(); i++){
            if (arduinoDir.get(i).lastModified() > recentModifiedDir.lastModified()){
                recentModifiedDir = arduinoDir.get(i);
            }
        }

        //Get hexFile
        File[] listFilesRecentDir = recentModifiedDir.listFiles();
        for (File file : listFilesRecentDir){
            if (file.isFile() && file.toString().contains("ino.hex")){
                return file;
            }
        }

        return null;

    }

}
