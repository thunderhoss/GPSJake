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
 * classname: BTGPSSetup
 *
 * desc: BTGPSSetup creates Form for bluetooth GPS setup.
 */

package gpsjake;

import javax.microedition.lcdui.*;
import bluetooth.*;

public class BTGPSSetup extends Form implements CommandListener {
    
    private GPSJakeMIDlet midlet;
    private Command backCommand;
    private Command stopCommand;
    private Command okCommand;
    private Gauge gauge;
    private BTComms btComms;
    
    public BTGPSSetup(GPSJakeMIDlet midlet, BTComms btComms) {
        super("GPSjake");
        
        try {
            this.midlet = midlet;
            this.btComms = btComms;
            btComms = new BTComms(midlet);
            
            //Prevents no data appearing since there will be an item on the form
            //when the next SringItem is removed.
            append(new StringItem(null, ""));
            append(new StringItem(null, "Search for Bluetooth GPS?"));
            okCommand = new Command("OK", Command.OK, 1);
            backCommand = new Command("Back", Command.BACK, 1);
            stopCommand = new Command("Stop", Command.STOP, 1);
            addCommand(okCommand);
            addCommand(backCommand);
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("BTGPSSetup:BTGPSSetup " + e.toString());
        }        
    }

    public void commandAction(Command c, Displayable d) {
        try {
            if (c == okCommand) {
                //remove Search question
                delete(1);
                removeCommand(backCommand);
                removeCommand(okCommand);
                addCommand(stopCommand);
                gauge = new Gauge("Searching for BT devices...", false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
                //#ifndef Belle_Emulator
                //# append(gauge);
                //#endif
                btComms.startDeviceInquiry();
            } else if(c == stopCommand) {
                btComms.stopDeviceInquiry();
            } else {
                midlet.backGPSMenu();
            }
        } catch (Exception e) {
            midlet.fatalError("BTGPSSetup:commandAction " + e.toString());
        }     
    }
    
}
