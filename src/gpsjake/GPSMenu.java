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
 * classname: GPSMenu
 *
 * desc: Class creates GPS menu through which the user can select
 * to connect to a bluetooth, file or internal GPS receiver.
 */

package gpsjake;

import javax.microedition.lcdui.*;

import javax.microedition.location.Criteria;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

class GPSMenu extends List implements CommandListener {
    
    private GPSJakeMIDlet midlet;
    private Command backCommand;
    
    GPSMenu(GPSJakeMIDlet midlet) {
        super("GPSjake", List.IMPLICIT);
        
        this.midlet = midlet;
        try {
            setTitle("GPS Menu");
            
            append("Find Bluetooth GPS", null);
            append("Read from NMEA file", null);
            append("Show current GPS", null);
            append("Stop current GPS", null);

            append("Use internal GPS", null);
            
            backCommand = new Command("Back", Command.BACK, 1);
            addCommand(backCommand);
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("GPSMenu:GPSMenu " + e.toString());
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        try {
            if (c == List.SELECT_COMMAND) {
                int index = getSelectedIndex();
                if (index != -1) { // should never be -1
                    switch (index) {
                        case 0:   // Bluetooth GPS
                            midlet.showBTSetup();
                            break;
                        case 1:   // File GPS
                            midlet.showNMEAFileBrowser();
                            break;
                        case 2: // Show current GPS
                            midlet.showGPS();
                            break;
                        case 3: // Stop current GPS
                            midlet.stopGPSThreads();
                            midlet.gpsSource = "";
                            break;                            
                        case 4: //Internal GPS
                        	getInternalGPS();
                        	break;
                        default:
                            // can't happen
                            break;
                    }
                }
            } else if (c == backCommand) {
                midlet.backSettingsMenu();
            }
        } catch (Exception e) {
            midlet.fatalError("GPSMenu:commandAction " + e.toString());
        }
    }   
    

    private void getInternalGPS() {
		// Create a Criteria object for defining desired selection criteria
    	midlet.internalGPSConnected();
    }

}
