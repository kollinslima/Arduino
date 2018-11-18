/* 
  Copyright (c) 2018, Kollins Gabriel Lima <kollins.lima@gmail.com>

  This file is part of SOFIA project - https://project-sofia.gitbook.io/project/.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,Inc
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


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
