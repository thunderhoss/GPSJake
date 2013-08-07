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
 * classname: DebugMenu
 *
 * desc: Class creates debug menu through which the user can select
 * to log raised exceptions for debug purposes.
 */

package gpsjake;

import javax.microedition.lcdui.*;

public class DebugMenu extends List implements CommandListener {
    private GPSJakeMIDlet midlet;
    private Command backCommand;

    DebugMenu(GPSJakeMIDlet midlet) {
        super("GPSjake", List.IMPLICIT);
        try {
            this.midlet = midlet;
            
            setTitle("Debug Menu");
            
            appendListItems();
            
            backCommand = new Command("Back", Command.BACK, 1);
            addCommand(backCommand);
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("DebugMenu:DebugMenu " + e.toString());
        }     
    }

    private void appendListItems() {
        try {
            append("Set debug file", null);
            if (this.midlet.isDebugOn() == true) {
                append("Turn debug logging off", null);
            } else {
                append("Turn debug logging on", null);
            }
            if (this.midlet.isLogMinorErrorsOn() == true) {
                append("Log minor errors: off", null);
            } else {
                append("Log minor errors: on", null);
            }
            append("Set NMEA file", null);
            if (this.midlet.getGPSInfo().isNMEALoggingOn() == true) {
                append("Turn NMEA logging off", null);
            } else {
                append("Turn NMEA logging on", null);
            }            
            
        } catch (Exception e) {
            midlet.fatalError("DebugMenu:appendListItems " + e.toString());
        }     
    }    
    
    public void commandAction(Command c, Displayable d) {
        try {
            if (c == List.SELECT_COMMAND) {
                int index = getSelectedIndex();
                if (index != -1) { // should never be -1
                    switch (index) {
                        case 0:   // Debug file
                            midlet.showDebugBrowser();
                            break;
                        case 1:
                            if (this.midlet.isDebugOn() == true) {
                                this.midlet.turnDebugOff();
                            } else {
                                this.midlet.turnDebugOn();
                            }
                            deleteAll();
                            appendListItems();
                            break;
                        case 2:
                            if (this.midlet.isLogMinorErrorsOn() == true) {
                                this.midlet.turnLogMinorErrorsOff();
                            } else {
                                this.midlet.turnLogMinorErrorsOn();
                            }
                            deleteAll();
                            appendListItems();
                            break;
                        case 3:   // NMEA directory
                            midlet.showNMEALogBrowser();
                            break;
                        case 4:
                            if (this.midlet.getGPSInfo().isNMEALoggingOn() == true) {
                                this.midlet.getGPSInfo().turnNMEALoggingOff();
                            } else {
                                this.midlet.getGPSInfo().turnNMEALoggingOn();
                            }
                            deleteAll();
                            appendListItems();
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
            midlet.fatalError("DebugMenu:commandAction " + e.toString());
        }     
    }
}
