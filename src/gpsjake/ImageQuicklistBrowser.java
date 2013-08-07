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
 * classname: ImageQuicklistBrowser
 *
 * desc: File browser class for selecting an image to assign to a quicklist
 * slot (key / number).
 */

package gpsjake;

import java.util.*;

public class ImageQuicklistBrowser extends FileBrowser {
    
    /** Creates a new instance of ImageFileBrowser */
    public ImageQuicklistBrowser(GPSJakeMIDlet midlet, Vector fileTypes, String title) {
        super(midlet, fileTypes, title);
    }
    
    public void back() {
        try {
            midlet.backImgQL();
        } catch (Exception e) {
            midlet.fatalError("ImageQuicklistBrowser:back " + e.toString());
        }
    }
    
    public void fileSelected() {
        try {
            midlet.imgQLFileSelected(getSelectedFile(), getSelectedDir());
        } catch (Exception e) {
            midlet.fatalError("ImageQuicklistBrowser:fileSelected " + e.toString());
        }
    }
}
