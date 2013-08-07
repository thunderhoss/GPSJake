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
 * classname: ImageQuicklist
 *
 * desc: Creates interface through which the quick list is managed.  The quicklist
 * assigns an image to a slot which can be used to easily switch between map images
 * from the MapCanvas rather than having to go back to the 'select image file' menu option.
 */

package gpsjake;

import javax.microedition.lcdui.*;

public class ImageQuicklist extends List implements CommandListener {
    
    private GPSJakeMIDlet midlet;
    private Command backCommand;
    private Command saveCommand;
    private Command deleteCommand;
    private boolean createMode;
    
    /** Creates a new instance of ImageQuicklist */
    public ImageQuicklist(GPSJakeMIDlet midlet, boolean createMode) {
        
        super("GPSjake", List.IMPLICIT);
        try {
            this.midlet = midlet;
            this.createMode = createMode;
            setTitle("Map Image Quicklist");

            backCommand = new Command("Back", Command.BACK, 2);
            if (createMode) {
                saveCommand = new Command("Save", Command.ITEM, 0);
                deleteCommand = new Command("Delete", Command.ITEM, 1);
                addCommand(saveCommand);
                addCommand(deleteCommand);
            }
            addCommand(backCommand);
            addListItems();
                    
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("ImageQuicklist:MapKeysMenu " + e.toString());
        }    
    }
    
    public void commandAction(Command c, Displayable d) {
        try {
            int index = getSelectedIndex();
            if (c == List.SELECT_COMMAND) {
                if (createMode) {
                    midlet.showMapImgQLFileBrowser(index);
                } else {
                    String imageFile = (String) midlet.imgQLContents.elementAt(index);
                    if (imageFile.equals("")) {
                        midlet.showMessage("Nothing to load.");
                    } else {
                        midlet.loadImageFromQuicklist(imageFile);
                    }
                }
            } else if (c == backCommand) {
                if (createMode) {
                	midlet.saveImgQL();
                    midlet.backSettingsMenu();
                } else {
                    midlet.backMap();
                }
            }
        } catch (Exception e) {
            midlet.fatalError("ImageQuicklist:commandAction " + e.toString());
        }
    }
    
    public void addListItems() {
        
        int i;
        String imageFile;
        try {                
            this.deleteAll();
            //Add images from vector (only display the filename - the whole
            //path won't fit on the screen.
            for(i = 0; i < midlet.imgQLContents.size(); i++) {
                imageFile = (String) midlet.imgQLContents.elementAt(i);
                if(!imageFile.equals("")) {
                    append("" + (i + 1) + " - " + imageFile.substring(imageFile.lastIndexOf('/') + 1), null);                
                } else {
                    append("" + (i + 1), null);                
                }
            }

            this.setSelectedIndex(0, true);
            
        } catch (Exception e) {
            midlet.fatalError("ImageQuicklist:addListItems " + e.toString());
        }       
    }
    
}
