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

import java.nio.file.Path;
import java.util.ArrayList;

public interface USBHandler {
    public ArrayList<String> getDevices();
    public Path getBuildPath();
    public String getDevicePath();
    public Path getDevicePathForDevice(String device);
    public int copyHexToDevice(String friendlyDevice);
    int sendSerialMessage(String friendlyDevice, String message);
}
