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
 * classname: RecordSoundMenu
 *
 * desc: Class creates record menu for recording vocal prompts.
 */

package gpsjake;

import javax.microedition.lcdui.*;
import geo.GeoImage;
import javax.microedition.media.*;
import java.io.*;

public class RecordSoundMenu extends List implements CommandListener {
    
    private GPSJakeMIDlet midlet;
    
    private Command backCommand;
    private byte [] recordedSound = null;
    private ByteArrayInputStream recordedIS = null;
    private Player playSoundPlayer;
    
    //This constructor is called when adding a control point
    public RecordSoundMenu(GPSJakeMIDlet midlet) {
        super("Record Sound", List.IMPLICIT);
        try {
	        this.midlet = midlet;
	        
	        refreshMenuItems();
	        
	        backCommand = new Command("Back", Command.BACK, 1);        
            addCommand(backCommand);
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("RecordSoundMenu:RecordSoundMenu " + e.toString());
        }
    }
    
    public void refreshMenuItems() {
        this.deleteAll();
        append("Press here to start recording...", null);
        if (recordedSound != null) {
            append("Play recorded sound", null);
            append("Save recorded sound...", null);    
        }
    }
    
    public void commandAction(Command c, Displayable d) {

        try {
            if (c == List.SELECT_COMMAND) {
                int index = getSelectedIndex();
                if (index != -1) { // should never be -1
                    switch (index) {
                        case 0:   // Record
                            midlet.showRecordGauge();
                            break;
                        case 1:   // Play
                            playSound();                         
                            break;
                        case 2:   // Save
                            midlet.showRecordSave();
                            break;
                        default:
                            // can't happen
                            break;
                    }
                }
            } else if (c == backCommand) {
                midlet.backGuidancePoint();
            }
        } catch (Exception e) {
            midlet.fatalError("RecordSoundMenu:commandAction " + e.toString());
        }                
    }

    public void setRecordedSound(byte[] soundBytes) {
        recordedSound = soundBytes;
    }

    public byte[] getRecordedSound() {
        return recordedSound;
    }    
    
    private void playSound() {
        try {            
            if (recordedSound != null) {
                //Inputstream for byte array.
                recordedIS = new ByteArrayInputStream(recordedSound);
                playSoundPlayer = Manager.createPlayer(recordedIS, "audio/x-wav");
                playSoundPlayer.prefetch();
                playSoundPlayer.start();                
            }
        } catch (IOException e) {
            midlet.fatalError("RecordSoundMenu:playSound " + e.toString());
        } catch (MediaException e) {
            midlet.fatalError("RecordSoundMenu:playSound " + e.toString());
        } catch (Exception e) {
            midlet.fatalError("RecordSoundMenu:playSound " + e.toString());
        }
        
    }     
    
}
