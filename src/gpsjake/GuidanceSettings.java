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
 * classname: ControlPointScreen
 *
 * desc: Class creates a form which displays details of control point and allows
 * the user to zoom to it's location on the map or delete it.
 */

package gpsjake;

import javax.microedition.lcdui.*;
import geo.ControlPoint;
import geo.GeoImage;

public class GuidanceSettings extends Form implements CommandListener {
    
    private GPSJakeMIDlet midlet;
    
    private Command backCommand;    
    private Command okCommand;
    private final TextField minGuidDistTextField;
    private final TextField maxGuidDistTextField;    
    private final TextField offCrseDistTextField; 
    private final TextField dispGPXDistTextField;                
    
    public GuidanceSettings(GPSJakeMIDlet midlet) {
        super("Control Point");
        
        this.midlet = midlet;
        
        minGuidDistTextField = new TextField("Min. Guidance Dist.", new Integer(midlet.getMinGuidanceDist()).toString(), 3, TextField.NUMERIC);
        maxGuidDistTextField = new TextField("Max. Guidance Dist.", new Integer(midlet.getMaxGuidanceDist()).toString(), 3, TextField.NUMERIC);        
        offCrseDistTextField = new TextField("Off Course Dist.", new Integer(midlet.getOffCourseDist()).toString(), 3, TextField.NUMERIC);
        dispGPXDistTextField = new TextField("Display GPX Pnt Dist.", new Integer(midlet.getGPXDist()).toString(), 3, TextField.NUMERIC);

        backCommand = new Command("Back", Command.BACK, 1);
        okCommand = new Command("OK", Command.OK, 1);
        
        try {

            append(minGuidDistTextField);
            append(maxGuidDistTextField);
            append(offCrseDistTextField);
            append(dispGPXDistTextField);
            
            
            addCommand(backCommand);
            addCommand(okCommand);
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("GuidanceSettings:GuidanceSettings " + e.toString());
        }       
    }
        
    public void commandAction(Command c, Displayable d) {
        
        try {
            if (c == okCommand) {
            	if (minGuidDistTextField.getString().equals("") || maxGuidDistTextField.getString().equals("")
            			|| offCrseDistTextField.getString().equals("") || dispGPXDistTextField.getString().equals("")) {
            		midlet.showMessage("Enter distances for guidance/off course alarm/gpx pnt distance.");
            	} else if (Integer.parseInt(minGuidDistTextField.getString()) >= Integer.parseInt(maxGuidDistTextField.getString())) {
            		midlet.showMessage("Min. guidance distance cannot be greater than max.");
            	} else {
            		int minGuidDist = Integer.parseInt(minGuidDistTextField.getString());
            		int maxGuidDist = Integer.parseInt(maxGuidDistTextField.getString());            		
            		int offCrseDist = Integer.parseInt(offCrseDistTextField.getString());
            		int dispGPXDist = Integer.parseInt(dispGPXDistTextField.getString());

                    midlet.setMinGuidanceDist(minGuidDist);
                    midlet.setMaxGuidanceDist(maxGuidDist);
                    midlet.setOffCourseDist(offCrseDist);
                    midlet.setGPXDist(dispGPXDist);                    
            		midlet.backSettingsMenu();
            	}

            } else if (c == backCommand) {
                midlet.backSettingsMenu();
            }
        } catch (Exception e) {
            midlet.fatalError("GuidanceSettings:commandAction " + e.toString());
        }        
    }
}
