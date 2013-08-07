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
 * classname: RecordSave
 *
 * desc: Class creates GUI and means to save recorded vocal prompts.
 */

package gpsjake;

import java.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;

public class RecordSave extends Form implements CommandListener {
    private final Command backCommand;
    private final Command saveCommand;    
    private RecordSoundMenu recordSoundMenu;
    private GPSJakeMIDlet midlet;   
    private TextField wavFileNameTextField;
    private StringItem wavFileNameString;
    private FileConnection fc = null;
    private DataOutputStream dos = null;
    
    RecordSave(GPSJakeMIDlet midlet, RecordSoundMenu recordSoundMenu) {

        super("GPSjake");
        
        this.midlet = midlet;
        this.recordSoundMenu = recordSoundMenu;        
       
        wavFileNameTextField = new TextField("Filename:", "", 20, TextField.ANY);
        
        wavFileNameString = new StringItem ("", "The sound files are saved in WAV format in the " +
                "same directory as the selected image.  " +
                "The name of the file must be unique within the directory.");
        
        backCommand = new Command("Back", Command.BACK, 1);
        saveCommand = new Command("Save", Command.ITEM, 2);
        
        try {
            
            append(wavFileNameTextField);
            append(wavFileNameString);
            addCommand(backCommand);
            addCommand(saveCommand);

            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("RecordSave:RecordSave " + e.toString());
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        try {
            if (c == saveCommand) {
                if (wavFileNameTextField.getString().equals("")) {
                    midlet.showMessage("A file name needs to be entered.");
                } else {
                    fc = (FileConnection) Connector.open(midlet.getGeoImageObj().getDirname() + wavFileNameTextField.getString() + ".wav");
                    if (!fc.exists()) {
                        fc.create();
                        dos = fc.openDataOutputStream();
                        dos.write(recordSoundMenu.getRecordedSound());
                        dos.close();                    
                        fc.close();
                    } else {
                        fc.close();
                        midlet.showMessage("File already exists.");
                        return;
                    }               
                    dos = null;    
                    fc = null;
                    
                    midlet.backGuidanceSelected(wavFileNameTextField.getString() + ".wav");
                }
            } else if (c == backCommand) {
                midlet.backRecordSound();                
            }
        } catch (IOException e) {
            midlet.fatalError("RecordSave:commandAction " + e.toString());            
        } catch (Exception e) {
            midlet.fatalError("RecordSave:commandAction " + e.toString());
        }
    }
    
}