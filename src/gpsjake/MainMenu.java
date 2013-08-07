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
 * classname: MainMenu
 *
 * desc: Class creates main menu.  The structure of the menu system is based on J2ME
 * basic game examples provided by Nokia.
 */

package gpsjake;

import javax.microedition.lcdui.*;

class MainMenu extends List implements CommandListener {
    private GPSJakeMIDlet midlet;
    private Command exitCommand;
    
    MainMenu(GPSJakeMIDlet midlet) {
        super("GPSjake", List.IMPLICIT);
        try {
            this.midlet = midlet;
            
            setTitle("GPSjake");
            
            append("Map", null);            
            append("Settings", null);
            append("About", null);
            
            exitCommand = new Command("Exit", Command.EXIT, 1);
            addCommand(exitCommand);
            
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("MainMenu:MainMenu " + e.toString());
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        try {
            if (c == List.SELECT_COMMAND) {
                int index = getSelectedIndex();
                if (index != -1) { // should never be -1
                    switch (index) {
                        case 0:   // Map
                            if(midlet.getGeoImageObj().getImage() == null) {
                                if (!midlet.lastImageFile.equals("")) {
                                    midlet.loadLastImage();
                                } else {
                                    midlet.useSplashAsMap();
                                }
                            } else {
                                midlet.showMap();
                            }
                            break;                        
                        case 1:   // Settings
                            midlet.showSettingsMenu();
                            break;
                        case 2:   // About
                            midlet.showAboutForm();
                            break;                            
                        default:
                            // can't happen
                            break;
                    }
                }
            } else if (c == exitCommand) {
                midlet.quit();
            }
        } catch (Exception e) {
            midlet.fatalError("MainMenu:commandAction " + e.toString());
        }        
    }
    
}
