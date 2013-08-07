/*
    GPSJake - a J2ME app which allows a user to display their position
	on an Ordnance Survey map image and provides various navigation functionality.
    Copyright (C) 2013  Mike Glynn www.gt140.co.uk

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
	
*/

/*
 * author: mglynn
 *
 * classname: BTComms
 *
 * desc: This class allows a search for other BT devices and populates a
 * vector of discovered devices.
*/

package bluetooth;

import gpsjake.*;
import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

public class BTComms implements DiscoveryListener {
    
    protected UUID uuid = new UUID(0x1101); // serial port profile
    protected int inquiryMode = DiscoveryAgent.GIAC;
    protected int connectionOptions = ServiceRecord.NOAUTHENTICATE_NOENCRYPT;
    
    protected Vector deviceList = new Vector();
    
    private boolean threadDone = false;
    
    private final GPSJakeMIDlet midlet;
    private int serviceSearch;
    
    public BTComms(GPSJakeMIDlet midlet) {
        this.midlet = midlet;
    }
    
    public void stopDeviceInquiry() {
        try {
            getAgent().cancelInquiry(this);
        } catch (Exception e) {
            midlet.fatalError("Failed to stop BT search. " + e.toString());
        }
    }
    
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        try {
            deviceList.addElement(btDevice);
        } catch (Exception e) {
            midlet.fatalError("BTComms:deviceDiscovered " + e.toString());
        }
    }
    
    public void inquiryCompleted(int discType) {
        try {
            midlet.showBTDeviceList(deviceList);
        } catch (Exception e) {
            midlet.fatalError("BTComms:inquiryCompleted " + e.toString());
        }
    }

    public void startServiceSearch(RemoteDevice device) {
        //See if selected BT device supports serial service.
        try {
            UUID uuids[] = new UUID[] { uuid };
            serviceSearch = getAgent().searchServices(null, uuids, device, this);
        } catch (Exception e) {
            midlet.serialGPSError("Failed to start service search.");
        }
    }

    public void servicesDiscovered(int transId, ServiceRecord[] records) {
        //Called when serial service discovered.
        try {
            for (int i = 0; i < records.length; i++) {
                ServiceRecord rec = records[i];
                String url = rec.getConnectionURL(connectionOptions, false);
                getAgent().cancelServiceSearch(serviceSearch);
                //Pass midlet url of connected device.
                midlet.btDeviceConnected(url);
            }
        } catch (Exception e) {
            midlet.fatalError("BTComms:servicesDiscovered " + e.toString());
        }
    }
    
    public void serviceSearchCompleted(int transID, int respCode) {
        try {
            String msg = null;
            switch (respCode) {
                case SERVICE_SEARCH_COMPLETED:
                    msg = "the service search completed normally";
                    break;
                case SERVICE_SEARCH_TERMINATED:
                    msg = "the service search request was cancelled by a call to DiscoveryAgent.cancelServiceSearch()";
                    break;
                case SERVICE_SEARCH_ERROR:
                    msg = "an error occurred while processing the request";
                    break;
                case SERVICE_SEARCH_NO_RECORDS:
                    msg = "no records were found during the service search";
                    break;
                case SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
                    msg = "the device specified in the search request could not be reached or the local device could not establish a connection to the remote device";
                    break;
            }
            
            if (respCode == SERVICE_SEARCH_ERROR) startDeviceInquiry();
        } catch (Exception e) {
            midlet.fatalError("BTComms:serviceSearchCompleted " + e.toString());
        }
    }
    
    public void startDeviceInquiry() {
        try {
            deviceList.removeAllElements();
            getAgent().startInquiry(inquiryMode, this);
        } catch (Exception e) {
            midlet.serialGPSError("Bluetooth problem.  Check to see if bluetooth has been turned on.");
        }
    }
    
    private DiscoveryAgent getAgent() {
        try {
            return LocalDevice.getLocalDevice().getDiscoveryAgent();
        } catch (BluetoothStateException e) {
            midlet.serialGPSError("Bluetooth problem.  Check to see if bluetooth has been turned on.");
            return null;
        }
    }
    
    public String getDeviceStr(RemoteDevice btDevice) {
        try {
            return getFriendlyName(btDevice) + " - 0x" + btDevice.getBluetoothAddress();
        } catch (Exception e) {
            return "No device string";            
        }
    }
    
    private String getFriendlyName(RemoteDevice btDevice) {
        try {
            return btDevice.getFriendlyName(false);
        } catch (IOException e) {
            return "No friendly name";
        }
    }
    
    
}
