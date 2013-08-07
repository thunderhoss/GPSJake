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
 * classname: ImageLoader
 *
 * desc: Class provides GUI for selecting an image file and loading it.
 * The image is read and stored as a whole.  Due to the memory limitations of
 * some phones a maximum image size had to be set to prevent the phone hanging.
 * If the image was bigger than the maximum size (geo.ImageInfo class) a warning
 * is shown.  Maximum size for a given phone is handled using the NetBeans pre-processor.
 *
 * This class is extended so that different screens can be reached when the menu is dismissed.
 */

package gpsjake;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;

import geo.ImageInfo;

class ImageLoader extends Form implements CommandListener {
    public final Command okCommand;
    public final Command backCommand;
    public GPSJakeMIDlet midlet;
    private Gauge gauge;
    public String imageFile;    
    
    private static final int CHUNK_SIZE = 1024;
    
    ImageLoader(GPSJakeMIDlet midlet, String filename, String prompt) {
        super("GPSjake");
        
        imageFile = filename;
        this.midlet = midlet;
        
        //Prevents no data appearing since there will be an item on the form
        //when the next SringItem is removed.
        
        okCommand = new Command("Yes", Command.OK, 1);
        backCommand = new Command("No", Command.BACK, 1);
        
        try {
            append(new StringItem(null, ""));
            append(new StringItem(null, prompt));
            addCommand(okCommand);
            addCommand(backCommand);
            
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("ImageLoader:ImageLoader " + e.toString());
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        try {
            if (c == okCommand) {
//                if (!loadImage()) {
//                    if (imageSize >= maxImageSize) {
//                        midlet.showMessage("Image too big.");
//                    } else {
//                        midlet.showMessage("Error loading image.");
//                    }
//                } else {
//                    midlet.imageLoaderOK(imageFile);                     
//                    onTo();
//                }
                if (loadImage()) {
                    midlet.imageLoaderOK(imageFile);                     
                    onTo();
                }            	
            } else {
                backTo();
            }
        } catch (Exception e) {
            midlet.fatalError("ImageLoader:commandAction " + e.toString());
        }
    }
    
    //onTo and backTo can be overridden to redirect the user
    //elsewhere.
    public void onTo() {
        midlet.backSettingsMenu();        
    }
    
    public void backTo() {
        midlet.backSettingsMenu();        
    }
    
    public boolean loadImage() {
        
        try {        
            
            //Use different file connection objects to check file size.
            FileConnection testFileConn = (FileConnection)Connector.open(imageFile, Connector.READ);
            InputStream testFis = testFileConn.openInputStream();
            
            //Use public domain ImageInfo to check file size.
            ImageInfo ii = new ImageInfo();
            ii.setInput(testFis); // in can be InputStream or RandomAccessFile
            ii.setDetermineImageNumber(true); // default is false
            ii.setCollectComments(true); // default is false
            //Check to see if supported file format (by ImageInfo)
            if (!ii.check()) {
                ii = null;
                midlet.setGeoImage(null);
                testFis.close();
                testFileConn.close();
                return false;
            }
            
            System.out.println(ii.getFormatName() + ", " + ii.getMimeType() +
                    ", " + ii.getWidth() + " x " + ii.getHeight() + " pixels, " +
                    ii.getBitsPerPixel() + " bits per pixel, " + ii.getNumberOfImages() +
                    " image(s), " + ii.getNumberOfComments() + " comment(s).");
            
//            imageSize = ii.getWidth() * ii.getHeight();
//            
//            if (imageSize > maxImageSize) {
//                ii = null;
//                testFis.close();
//                testFileConn.close();
//                return false;
//            }
            //Clean up after checking file size.
            ii = null;
            testFis.close();
            testFileConn.close();
            testFis = null;
            testFileConn = null;
            
            FileConnection fileConn = (FileConnection)Connector.open(imageFile, Connector.READ);
            // load the image data in memory
            // Read data in CHUNK_SIZE chunks
            InputStream fis = fileConn.openInputStream();
            
            //remove Use question
            delete(1);
            gauge = new Gauge("Loading image...", false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
            append(gauge);

            removeCommand(okCommand);
            removeCommand(backCommand);
            
            //Set the current image to null
            midlet.setGeoImage(null);
            //Run the garbage collector to free some memory
            Runtime.getRuntime().gc();    
            
            midlet.setGeoImage(Image.createImage(fis));
            fis.close();
            fileConn.close();
            fis = null;
            fileConn = null;
            
            return true;
            
        } catch (Error e) {
            if (e instanceof OutOfMemoryError) {
                midlet.showMessage("File is too large to display");
            } else {
                midlet.showMessage("Failed to display this file. " + e.getMessage());
            }
            return false;            
        } catch (IOException e) {
            return false;
        } catch (Exception e) {
            midlet.fatalError("ImageLoader:loadImage " + e.toString());
            return false;
        }
    }
}