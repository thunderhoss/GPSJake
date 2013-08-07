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
 * classname: ShowGPS
 *
 * desc: Class creates form which displays the current GPS.
 * A menu option is also provided which allows the GPS to be stopped.
 */

package gpsjake;

import javax.microedition.lcdui.*;

public class ShowGPS extends Form implements CommandListener {

    private final Command backCommand;
    private StringItem gpsString;
    private GPSJakeMIDlet midlet;    
    
    ShowGPS(GPSJakeMIDlet midlet) {
        super("GPSjake");
        
        this.midlet = midlet;
        
        backCommand = new Command("Back", Command.BACK, 1);
        
        try {
            
            if (midlet.gpsSource.equals("")) {
                gpsString = new StringItem(null, "Currently not using GPS.");
            } else {
                gpsString = new StringItem(null, midlet.gpsSource);
            }
            append(gpsString);
            addCommand(backCommand);
            
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("ShowGPS:ShowGPS " + e.toString());
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        try {
            if (c == backCommand) {
                midlet.backGPSMenu();
            }
        } catch (Exception e) {
            midlet.fatalError("ShowGPS:commandAction " + e.toString());
        }
    }
    
}