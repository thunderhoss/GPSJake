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
 * classname: LoggingMenu
 *
 * desc: Class creates logging menu through which the user can select
 * to log GPS positions as GPX.
 */

package gpsjake;

import javax.microedition.lcdui.*;

class LoggingMenu extends List implements CommandListener {
    private GPSJakeMIDlet midlet;
    private Command backCommand;
    
    LoggingMenu(GPSJakeMIDlet midlet) {
        
        super("GPSjake", List.IMPLICIT);
        
        try {
            this.midlet = midlet;
            
            setTitle("Logging Menu");
            
            appendListItems();
            
            backCommand = new Command("Back", Command.BACK, 1);
            addCommand(backCommand);
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("LoggingMenu:LoggingMenu " + e.toString());
        }
    }
    
    private void appendListItems() {
        try {
            append("Set GPX file", null);
            if (this.midlet.getGPSInfo().isGPXLoggingOn() == true) {
                append("Turn GPX logging off", null);
            } else {
                append("Turn GPX logging on", null);
            }            
        } catch (Exception e) {
            midlet.fatalError("LoggingMenu:appendListItems " + e.toString());
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        try {
            if (c == List.SELECT_COMMAND) {
                int index = getSelectedIndex();
                if (index != -1) { // should never be -1
                    switch (index) {
                        case 0:
                            midlet.showGPXLogBrowser();
                            break;
                        case 1:
                            if (this.midlet.getGPSInfo().isGPXLoggingOn() == true) {
                                this.midlet.getGPSInfo().turnGPXLoggingOff();
                            } else {
                                this.midlet.getGPSInfo().turnGPXLoggingOn();
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
            midlet.fatalError("LoggingMenu:commandAction " + e.toString());
        }
    }
}
