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
 * classname: SettingsMenu
 *
 * desc: Class creates settings menu.
 */

package gpsjake;

import javax.microedition.lcdui.*;

class SettingsMenu extends List implements CommandListener {
    private GPSJakeMIDlet midlet;
    private Command backCommand;
    
    SettingsMenu(GPSJakeMIDlet midlet) {
        super("GPSjake", List.IMPLICIT);
        try {
            this.midlet = midlet;
            
            setTitle("Settings Menu");
            
            append("GPS", null);
            append("Select Map Image", null);
            append("Edit Map Image Quicklist", null);
            append("Guidance", null);            
            append("Record GPX file", null);
            append("Debug", null);
            
            backCommand = new Command("Back", Command.BACK, 1);
            addCommand(backCommand);
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("SettingsMenu:SettingsMenu " + e.toString());
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        try {
            if (c == List.SELECT_COMMAND) {
                int index = getSelectedIndex();
                if (index != -1) { // should never be -1
                    switch (index) {
                        case 0:   // GPS
                            midlet.showGPSMenu();
                            break;
                        case 1:   // Map Image
                            midlet.showMapImageFileBrowser();
                            break;
                        case 2:
                            midlet.showImgQL(true);
                            break;
                        case 3:
                            midlet.showGuidanceSettings();
                            break;                        	
                        case 4: // Logging menu / Record GPX file
                            midlet.showLoggingMenu();
                            break;
                        case 5: // Show debug menu
                            midlet.showDebugMenu();
                            break;
                        default:
                            // can't happen
                            break;
                    }
                }
            } else if (c == backCommand) {
                midlet.backMainMenu();
            }
        } catch (Exception e) {
            midlet.fatalError("SettingsMenu:commandAction " + e.toString());
        }
    }
}
