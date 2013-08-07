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
 * classname: GuidanceList
 *
 * desc: Creates interface can select sound to play at guidance point or opt to record a new sound.
 */

package gpsjake;

import javax.microedition.lcdui.*;
import java.util.*;

public class GuidanceList extends List implements CommandListener, FileActionInvoker {
    
    private GPSJakeMIDlet midlet;
    private Command backCommand;
    
    //i.e. TurnLeft, TurnRight, BearLeft, BearRight, StraightAhead in res
    private static final int NO_PREDEFINED_SOUNDS = 5;
    
    // The fileAction object which we use to retrieve the directory contents
    protected FileAction fileAction= null;       
    
    public GuidanceList(GPSJakeMIDlet midlet) {
        
        super("GPSjake", List.IMPLICIT);
        
        this.midlet = midlet;
        fileAction = new FileAction(this);     
        setTitle("Select sound");        
                
        backCommand = new Command("Back", Command.BACK, 1);        
        
        try {

            addListItems();
            
            addCommand(backCommand);
            
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("GuidanceList:GuidanceList " + e.toString());
        }    
    }
    
    public void commandAction(Command c, Displayable d) {
        try {
            int index = getSelectedIndex();
            String listSelection = getString(index);
            if (c == List.SELECT_COMMAND) {
                midlet.backGuidanceSelected(listSelection);
            } else if (c == backCommand) {
                midlet.backGuidancePoint();
            }
        } catch (Exception e) {
            midlet.fatalError("GuidanceList:commandAction " + e.toString());
        }
    }
    
    public void addListItems() {
        this.deleteAll();
        
        //Add pre-defined prompts
        append("TurnLeft", null);
        append("TurnRight", null);
        append("BearLeft", null);
        append("BearRight", null);
        append("StraightAhead", null);
        
        //Add user-defined prompts that reside in same directory as
        //the image.
        // Start the thread to get the content of the chosenDir directory
        // the fileaction object will call the updateDirList method when it has retrieved all the content.
        new Thread(new Runnable() {
            public void run() {
            	System.out.println(midlet.getGeoImageObj().getDirname().substring("file://".length()));
                fileAction.getDirContent(midlet.getGeoImageObj().getDirname().substring("file://".length()));
            }
        }).start();        
    }
    
    public void updateDirList(Enumeration e, boolean isRoot, String returnedDir) {
        // This is a callback method used by the FileAction to send us
        // the directoryContent.
        try {
            // if e is null something went wrong and we don't do anything
            // throw a FileActionException exception.
            if (e == null) {
            	midlet.importantErrorLogOnly("GuidanceList:updateDirList");
            }

            // Add all the directories to the dirList vector
            while (e.hasMoreElements()) {
                String s =(String)e.nextElement();
                if (s.toUpperCase().endsWith(".WAV")) {
                    append(s, null);
                }
            }
            
        } catch (Exception ex) {
        	midlet.importantErrorLogOnly("GuidanceList:updateDirList " + e.toString());
        }
    }    
    

}
