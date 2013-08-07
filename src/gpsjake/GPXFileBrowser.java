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
 * classname: GPXFileBrowser
 *
 * desc: File browser class for selecting GPX file to display on the map canvas.
 */

package gpsjake;

import java.util.*;

public class GPXFileBrowser extends FileBrowser {

    public GPXFileBrowser(GPSJakeMIDlet midlet, Vector fileTypes, String title) {
        super(midlet, fileTypes, title);
    }
    
    public void back() {
        try {
            midlet.backMap();
        } catch (Exception e) {
            midlet.fatalError("GPXFileBrowser:back " + e.toString());
        }
    }
    
    public void fileSelected() {
        try {      
            midlet.gpxFileSelected(getSelectedFile());
        } catch (Exception e) {
            midlet.fatalError("GPXFileBrowser:fileSelected " + e.toString());
        }
    }
}
