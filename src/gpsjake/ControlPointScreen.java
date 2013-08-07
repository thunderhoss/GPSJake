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
 * classname: ControlPointScreen
 *
 * desc: Class creates a form which displays details of control point and allows
 * the user to zoom to it's location on the map or delete it.
 */

package gpsjake;

import javax.microedition.lcdui.*;
import geo.ControlPoint;
import geo.GeoImage;

public class ControlPointScreen extends Form implements CommandListener {
    
    private GPSJakeMIDlet midlet;
    private ControlPoint controlPoint;
    
    private Command backCommand;
    private Command backCommand2;	
    private Command deleteCommand;
    private Command showCommand;    
    private Command gpsCommand;    
    private GeoImage gimage;
    private boolean showPointMode = false;
    private Gauge gauge;
    private final StringItem imageXStringItem;
    private final StringItem imageYStringItem;
    private final StringItem eastingsStringItem;
    private final StringItem northingsStringItem;
    private final StringItem diffFromMeanPixelWidthStringItem;
    private final StringItem diffFromMeanPixelHeightStringItem;
    private final TextField eastingsTextField;
    private final TextField northingsTextField;            
    private int image_x, image_y;
    private double eastings, northings;
    private double diffFromMeanPixelWidth, diffFromMeanPixelHeight;
    private final static int NO_GPS_READS=50;
    
    //This constructor is called when adding a control point
    public ControlPointScreen(GPSJakeMIDlet midlet, int x, int y, GeoImage gimage) {
        super("Control Point");
        
        this.midlet = midlet;
        this.image_x = x;
        this.image_y = y;
        this.gimage = gimage;
        
        imageXStringItem = new StringItem("image x:", new Integer(image_x).toString());
        imageYStringItem = new StringItem("image y:", new Integer(image_y).toString());
        //Initialise but don't add to form
        eastingsStringItem = new StringItem("", "");
        northingsStringItem = new StringItem("", "");
        diffFromMeanPixelWidthStringItem = new StringItem("", "");
        diffFromMeanPixelHeightStringItem = new StringItem("", "");
        eastingsTextField = new TextField("Eastings", "", 6, TextField.NUMERIC);
        northingsTextField = new TextField("Northings", "", 6, TextField.NUMERIC);

        gpsCommand = new Command("Read GPS", Command.ITEM, 1);
        backCommand2 = new Command("Back", Command.ITEM, 2);
        backCommand = new Command("Back", Command.BACK, 1);
        
        try {
            append(imageXStringItem);
            append(imageYStringItem);
            append(eastingsTextField);
            append(northingsTextField);
            
            addCommand(gpsCommand);
            addCommand(backCommand2);            
            addCommand(backCommand);

            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("ControlPointScreen:ControlPointScreen " + e.toString());
        }       
    }
    
    //This constructor is called when displaying a control point
    public ControlPointScreen(GPSJakeMIDlet midlet, ControlPoint controlPoint, GeoImage gimage, int index) {
        super("Control Point " + (index + 1));
        
        this.midlet = midlet;
        showPointMode = true;
        this.controlPoint = controlPoint;
        this.gimage = gimage;
        this.image_x = controlPoint.image_x;
        this.image_y = controlPoint.image_y;
        this.eastings = controlPoint.eastings;
        this.northings = controlPoint.northings;
        this.diffFromMeanPixelWidth = controlPoint.pixelWidthDiffFromMean;
        this.diffFromMeanPixelHeight = controlPoint.pixelHeightDiffFromMean;
        
        imageXStringItem = new StringItem("image x:", new Integer(image_x).toString());
        imageYStringItem = new StringItem("image y:", new Integer(image_y).toString());
        //Initialise but don't add to form
        eastingsTextField = new TextField("", "", 6, TextField.NUMERIC);
        northingsTextField = new TextField("", "", 6, TextField.NUMERIC);
        if (gimage.registered) {
            String strDiffFromMeanPixelWidth = new Double(diffFromMeanPixelWidth).toString();
            String strDiffFromMeanPixelHeight = new Double(diffFromMeanPixelHeight).toString();
            strDiffFromMeanPixelWidth = strDiffFromMeanPixelWidth.substring(0, (strDiffFromMeanPixelWidth.indexOf(".") + 2));
            strDiffFromMeanPixelHeight = strDiffFromMeanPixelHeight.substring(0, (strDiffFromMeanPixelHeight.indexOf(".") + 2));
            diffFromMeanPixelWidthStringItem = new StringItem("Pixel Error Width (m)", strDiffFromMeanPixelWidth);
            diffFromMeanPixelHeightStringItem = new StringItem("Pixel Error Height (m):", strDiffFromMeanPixelHeight);
        } else {
            diffFromMeanPixelWidthStringItem = new StringItem("", "");
            diffFromMeanPixelHeightStringItem = new StringItem("", "");
        }
        eastingsStringItem = new StringItem("Eastings:", this.midlet.getGPSInfo().getEastingsAsStr(eastings));
        northingsStringItem = new StringItem("Northings:", this.midlet.getGPSInfo().getNorthingsAsStr(northings));
        try {
            append(imageXStringItem);
            append(imageYStringItem);
            append(eastingsStringItem);
            append(northingsStringItem);
            append(diffFromMeanPixelWidthStringItem);
            append(diffFromMeanPixelHeightStringItem);
            
            deleteCommand = new Command("Delete", Command.ITEM, 1);
            showCommand = new Command("Show", Command.ITEM, 1);            
            backCommand = new Command("Back", Command.BACK, 1);
            
            addCommand(deleteCommand);
            addCommand(showCommand);
            addCommand(backCommand);
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("ControlPointScreen:ControlPointScreen " + e.toString());
        }      
    }    
    
    public void commandAction(Command c, Displayable d) {
                
        double totalEastings = 0.0;
        double totalNorthings = 0.0;
        double meanEastings = 0.0;
        double meanNorthings = 0.0;
        
        try {
            if (c == backCommand && !showPointMode && (!eastingsTextField.getString().equals("") && !northingsTextField.getString().equals(""))) {
                eastings = Double.parseDouble(eastingsTextField.getString());
                northings = Double.parseDouble(northingsTextField.getString());
                
                controlPoint = new ControlPoint(image_x, image_y, eastings, northings);
                if(gimage.validateControlPoint(controlPoint)) {
                    gimage.addControlPoint(controlPoint);
                    if (gimage.getControlPoints().size() >= 2) {
                        gimage.registerImage();
                        gimage.registered = true;
                    } else {
                        gimage.registered = false;
                    }
                    midlet.saveSettings();
                    midlet.backMap();
                } else {
                    //11.09.07 - Message needs to be improved here.  Perhaps validateControlPoint could return an integer to enable
                    //the message to be more specific.
                    eastingsTextField.setString("");
                    northingsTextField.setString("");                    
                    midlet.showMessage("Invalid control point.  Control points must have valid eastings and northings.  Control points must also be unique.");
                }

            } else if (c == backCommand && !showPointMode && (eastingsTextField.getString().equals("") || northingsTextField.getString().equals(""))) {
            	midlet.backMap();
            } else if (c == backCommand && showPointMode) {
                midlet.backControlPointList();
            } else if (c == deleteCommand) {
                gimage.removeControlPoint(controlPoint);
                if (gimage.getControlPoints().size() >= 2) {
                    gimage.registerImage();
                    gimage.registered = true;
                } else {
                    gimage.registered = false;
                }
                midlet.saveSettings();
                midlet.backControlPointList();
            } else if (c == showCommand) {
                midlet.backMap(image_x, image_y);                
            } else if (c == gpsCommand) {
                if (this.midlet.getGPSInfo().getFix()) {
                    deleteAll();
                    gauge = new Gauge("Reading GPS...", false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
                    //#ifndef Belle_Emulator
                    //# int gaugeIndex = append(gauge);
                    //#endif
                    
                    //Read GPS 10 times - add to vector and get average
                    for (int i=0; i < NO_GPS_READS; i++) {
                        totalEastings = totalEastings + this.midlet.getGPSInfo().osGridRef.Eastings;
                        totalNorthings = totalNorthings + this.midlet.getGPSInfo().osGridRef.Northings;
                    }
                    meanEastings = totalEastings / NO_GPS_READS;
                    meanNorthings = totalNorthings / NO_GPS_READS;
                    
                    //#ifndef Belle_Emulator
                    //# delete(gaugeIndex);
                    //#endif
                    eastingsTextField.setString(this.midlet.getGPSInfo().getEastingsAsStr(meanEastings));
                    northingsTextField.setString(this.midlet.getGPSInfo().getNorthingsAsStr(meanNorthings));
                    append(imageXStringItem);
                    append(imageYStringItem);
                    append(eastingsTextField);
                    append(northingsTextField);
                } else {
                    midlet.showMessage("No GPS fix.");
                }
            }
        } catch (Exception e) {
            midlet.fatalError("ControlPointScreen:commandAction " + e.toString());
        }        
    }
}
