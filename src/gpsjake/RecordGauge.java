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
 * classname: RecordGauge
 *
 * desc: Class creates GUI and means to record vocal prompts.
 */

package gpsjake;

import java.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.RecordControl;

public class RecordGauge extends Form implements CommandListener {
    private final Command backCommand;
    private RecordSoundMenu recordSoundMenu;
    private Gauge gauge;
    private String gaugeString;    
    private GPSJakeMIDlet midlet;   
    
    //Outputstream for byte array.
    private ByteArrayOutputStream recordedOS = null;  

    private RecordControl recordControl;

    private Player recordPromptPlayer;    
    
    RecordGauge(GPSJakeMIDlet midlet, RecordSoundMenu recordSoundMenu) {

        super("GPSjake");
        
        this.midlet = midlet;
        this.recordSoundMenu = recordSoundMenu;        
        backCommand = new Command("Back", Command.BACK, 1);
     
        try {
            addCommand(backCommand);

            gauge = new Gauge("Recording...", false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);    
            //#ifndef Belle_Emulator
            //# append(gauge);
            //#endif
            
            recordSound();

            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("RecordGauge:RecordGauge " + e.toString());
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        try {
            stopRecording();
            midlet.backRecordSound();
        } catch (Exception e) {
            midlet.fatalError("RecordGauge:commandAction " + e.toString());
        }
    }
    
    private void stopRecording() {
        try {
            recordControl.stopRecord();
            recordControl.commit();
            recordPromptPlayer.close();            
            recordSoundMenu.setRecordedSound(recordedOS.toByteArray());
            recordedOS.close();
        } catch (IOException e) {
            midlet.fatalError("RecordGauge:stopRecording " + e.toString());
        } catch (Exception e) {
            midlet.fatalError("RecordGauge:stopRecording " + e.toString());
        }            
    }
    
    private void recordSound() {
        try {
            
            //Record to a byte array - I was recording to direct to a wav file using the rc.setRecordLocation
            //but had problems on older Nokia phones (6630) playing the clip immediately after recording.
            
            recordPromptPlayer=Manager.createPlayer("capture://audio");
            recordPromptPlayer.realize();
            
            recordControl = (RecordControl) recordPromptPlayer.getControl("RecordControl");
            
            recordedOS = new ByteArrayOutputStream();
            
            recordControl.setRecordStream(recordedOS);
            recordControl.startRecord();
            
            recordPromptPlayer.start();
                       
        } catch (IOException e) {
            midlet.fatalError("RecordGauge:recordPrompt " + e.toString());
        } catch (MediaException e) {
            midlet.fatalError("RecordGauge:recordPrompt " + e.toString());
        } catch (Exception e) {
            midlet.fatalError("RecordGauge:recordPrompt " + e.toString());
        }
    }       
    
}