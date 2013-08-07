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
 * classname: GuidancePointScreen
 *
 * desc: Class creates a form which displays details of guidance point and allows
 * the user to zoom to it's location on the map or delete it.
 */

package gpsjake;

import javax.microedition.lcdui.*;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import java.io.IOException;
import java.io.InputStream;

import geo.GeoImage;
import geo.GuidancePoint;
import geo.ImagePoint;
import geo.OSGridRef;

public class GuidancePointScreen extends Form implements CommandListener {
    
    private GPSJakeMIDlet midlet;
    private GuidancePoint guidancePoint;
    private boolean showPointMode = false;
    private Command backCommand;
    private Command deleteCommand;
    private Command showCommand;
    private Command selectWavFileCommand;
    
    private Command playCommand;    
    private Command deleteSoundCommand;
    private Command addGuidanceCommand;        
    
    private GeoImage gimage;
    private final StringItem imageXStringItem;
    private final StringItem imageYStringItem;
    private final StringItem eastingsStringItem;
    private final StringItem northingsStringItem;    
    private final StringItem wavFileStringItem;
    private String wavFileName;
    private int image_x, image_y;
    private double eastings, northings;    
    
    //This constructor is called when adding a guidance point
    public GuidancePointScreen(GPSJakeMIDlet midlet, int x, int y, GeoImage gimage) {
        super("Guidance Point");
        
        this.midlet = midlet;
        this.image_x = x;
        this.image_y = y;
        this.gimage = gimage;
        
        imageXStringItem = new StringItem("image x:", new Integer(image_x).toString());
        imageYStringItem = new StringItem("image y:", new Integer(image_y).toString());       
        eastingsStringItem = new StringItem("", "");
        northingsStringItem = new StringItem("", "");           
        wavFileStringItem = new StringItem("Sound (wav file):", "");
  
        selectWavFileCommand = new Command("Select sound...", Command.ITEM, 1);
        
        addGuidanceCommand = new Command("Record new sound", Command.ITEM, 1);
        playCommand = new Command("Play sound", Command.ITEM, 1);
        deleteSoundCommand = new Command("Delete sound", Command.ITEM, 1);
        
        backCommand = new Command("Back", Command.BACK, 1);        
        
        try {
            append(imageXStringItem);
            append(imageYStringItem);          
            append(wavFileStringItem);
            
            addCommand(selectWavFileCommand);
            
            
            addCommand(addGuidanceCommand);
            addCommand(playCommand);
            addCommand(deleteSoundCommand);  
            
            addCommand(backCommand);
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("GuidancePointScreen:GuidancePointScreen " + e.toString());
        }
    }
    
    //This constructor is called when displaying a guidance point
    public GuidancePointScreen(GPSJakeMIDlet midlet, GuidancePoint guidancePoint, GeoImage gimage, int index) {
        super("Guidance Point " + (index + 1));
                
        showPointMode = true;
        this.midlet = midlet;
        this.guidancePoint = guidancePoint;
        this.gimage = gimage;
        this.image_x = guidancePoint.image_x;
        this.image_y = guidancePoint.image_y;
        this.eastings = guidancePoint.eastings;
        this.northings = guidancePoint.northings;
        
        imageXStringItem = new StringItem("image x:", new Integer(image_x).toString());
        imageYStringItem = new StringItem("image y:", new Integer(image_y).toString());
        eastingsStringItem = new StringItem("eastings:", new Double(eastings).toString());
        northingsStringItem = new StringItem("northings:", new Double(northings).toString());        
        wavFileStringItem = new StringItem("wav file:", guidancePoint.wavFileName);
        
        showCommand = new Command("Show", Command.ITEM, 1);        
        deleteCommand = new Command("Delete", Command.ITEM, 1);
        backCommand = new Command("Back", Command.BACK, 1);
        
        try {
            append(imageXStringItem);
            append(imageYStringItem);
            append(eastingsStringItem);
            append(northingsStringItem);            
            append(wavFileStringItem);
            
            addCommand(deleteCommand);
            addCommand(showCommand);
            addCommand(backCommand);
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("GuidancePointScreen:GuidancePointScreen " + e.toString());
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        boolean userDefined = false;
        try {
        	wavFileName = "";
        	if (!wavFileStringItem.getText().equals("")) {
        		wavFileName = wavFileStringItem.getText();
                //If the file name ends in .wav then the guidance is
                //user defined and must be added is such so that
                //we can distinguish when playing sounds in the jar
                //and sounds that are files on the phone.
                if (wavFileName.toUpperCase().endsWith(".WAV")) { 
                    userDefined = true;
                    wavFileName = midlet.getGeoImageObj().getDirname() + wavFileName;
                } else {
                    wavFileName = "/res/" + wavFileName + ".wav";
                }
        	}
        	
            if (c == backCommand && !showPointMode && !wavFileName.equals("")) {
                OSGridRef imagePntAsGridRef = gimage.imagePointAsGridRef(new ImagePoint(image_x, image_y));
                
                guidancePoint = new GuidancePoint(image_x, image_y, imagePntAsGridRef.Eastings, imagePntAsGridRef.Northings,
                		wavFileName, userDefined);
                gimage.addGuidancePoint(guidancePoint);
                midlet.saveSettings();
                midlet.backMap();
            } else if (c == backCommand && wavFileName.equals("")) {
            	midlet.backMap();
            } else if (c == backCommand && showPointMode) {
	            midlet.backGuidancePointList();
            } else if (c == deleteCommand) {
                gimage.removeGuidancePoint(guidancePoint);
                midlet.saveSettings();                
                midlet.backGuidancePointList();
            } else if (c == showCommand) {
                midlet.backMap(guidancePoint.image_x, guidancePoint.image_y);
            } else if (c == selectWavFileCommand) {
                midlet.showGuidanceList();
            } else if (c == addGuidanceCommand) {
                midlet.showRecordSound();
            } else if (c == deleteSoundCommand && !wavFileName.equals("")) {
                if (!userDefined) {
                    midlet.showMessage("Cannot remove pre-defined sound.");
                } else {
                   FileConnection fc = (FileConnection) Connector.open(wavFileName);
                   fc.delete();
                   fc.close();
                   fc = null;
                   wavFileStringItem.setText("");
                }      
            } else if (c == playCommand && !wavFileName.equals("")) {
                //Create player and play sound.
                if (!userDefined) {
                    //bundled sound (i.e. in res and not user defined.
                    //Should really have these in a static array
                    //or something.
                    playSound(wavFileName, "audio/x-wav", false);
                } else {
                    playSound(wavFileName, "audio/x-wav", true);                    
                }
            }
        } catch (Exception e) {
        	System.out.println(e.toString());
            midlet.importantErrorLogOnly("GuidancePointScreen:commandAction " + e.toString());
        }
    }
    
    public void setSelected(String wavFileName) {
        wavFileStringItem.setText(wavFileName);
    }
    
    private Player playSound(String filename, String format, boolean userDefined) {
        Player p = null;
        try {
            System.out.println("Creating player...." + filename);
            if (userDefined) {
                p = Manager.createPlayer(filename);
                p.prefetch();
            } else {
                InputStream is = getClass().getResourceAsStream(filename);
                p = Manager.createPlayer(is, format);
                p.prefetch();
            }
            //Added check so that a player isn't restarted if it's already running.
            if (p != null) {
                try {
                    p.stop();
                    p.setMediaTime(0L);
                    p.start();
                } catch (MediaException ex) {
                    // ignore
                }
            }            
        } catch (IOException ex) {
            // ignore
        } catch (MediaException ex) {
            // ignore
        }
        System.out.println("..done.");
        return p;
    }
}
