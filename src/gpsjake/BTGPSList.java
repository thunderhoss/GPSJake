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
 * classname: BTGPSList
 *
 * desc: BTGPSList provides list of bluetooth devices which user can select from.
 */

package gpsjake;

import javax.microedition.lcdui.*;
import javax.bluetooth.RemoteDevice;
import java.util.Vector;
import bluetooth.BTComms;

public class BTGPSList extends List implements CommandListener {
    
    private GPSJakeMIDlet midlet;
    private Command backCommand;
    private Vector deviceList;
    private BTComms btComms;
    
    /** Creates a new instance of BTGPSList */
    public BTGPSList(GPSJakeMIDlet midlet, Vector deviceList, BTComms btComms) {

        super("GPSjake", List.IMPLICIT);
        
        try {
            this.midlet = midlet;
            this.deviceList = deviceList;
            this.btComms = btComms;

            for (int i = 0; i < deviceList.size(); i++) {
                append(btComms.getDeviceStr((RemoteDevice) deviceList.elementAt(i)), null);
            }

            backCommand = new Command("Back", Command.BACK, 1);
            addCommand(backCommand);

            setCommandListener(this);       
        } catch (Exception e) {
            midlet.fatalError("BTGPSList:BTGPSList " + e.toString());                            
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        try {
            if (c == List.SELECT_COMMAND) {
                btComms.startServiceSearch((RemoteDevice) deviceList.elementAt(getSelectedIndex()));
            } else if (c == backCommand) {
                midlet.backGPSMenu();
            }
        } catch (Exception e) {
            midlet.fatalError("BTGPSList:commandAction " + e.toString());                            
        }
    }
}
