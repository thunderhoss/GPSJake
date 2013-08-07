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
 * classname: ControlPointList
 *
 * desc: Class creates a list of currently defined control points.  The user can select
 * the control point they are interested in from this list.
 */

package gpsjake;

import javax.microedition.lcdui.*;
import geo.ControlPoint;
import geo.GeoImage;

public class ControlPointList extends List implements CommandListener {
 
    private Command backCommand;
    private GeoImage gimage;
    private GPSJakeMIDlet midlet;
    
    public ControlPointList(GPSJakeMIDlet midlet, GeoImage gimage) {
        super("Control Points", List.IMPLICIT);
        
        try {
            this.midlet = midlet;
            this.gimage = gimage;
            
            for (int i = 0; i < gimage.getControlPoints().size(); i++) {
                append("Control Pt " + (i + 1), null);
            }
            
            backCommand = new Command("Back", Command.BACK, 1);
            
            addCommand(backCommand);
            
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("ControlPointList:ControlPointList " + e.toString());
        }        

    }

    public void commandAction(Command c, Displayable d) {
        try {
            if (c == List.SELECT_COMMAND) {
                midlet.showControlPoint((ControlPoint) gimage.getControlPoints().elementAt(getSelectedIndex()), getSelectedIndex());
            } else if (c == backCommand) {
                midlet.backMap();
            }
        } catch (Exception e) {
            midlet.fatalError("ControlPointList:commandAction " + e.toString());
        }        
    }
    
    public void refreshPointList() {
        try {
            deleteAll();
            for (int i = 0; i < gimage.getControlPoints().size(); i++) {
                append("Control Pt " + (i + 1), null);
            }
        } catch (Exception e) {
            midlet.fatalError("ControlPointList:refreshPointList " + e.toString());
        }
    }

}
