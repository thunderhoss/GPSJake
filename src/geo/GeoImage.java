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
 * classname: GeoImage
 *
 * desc: This class is used to store the currently loaded image and provides methods
 * for registering that image.  In addition control points, guidance points and gpx
 * points are also stored here.
*/

package geo;

import java.util.*;
import javax.microedition.lcdui.*;

public class GeoImage {
    
    private String imageFile = "";
    private String imageDir;
    private String xmlFile;
    private Image image;
    
    public Vector controlPoints = new Vector();
    public Vector guidancePoints = new Vector();
    //Vector of gpx points converted to OS grid
    public Vector gpxPoints = new Vector();
    public double pixelHeight;
    public double pixelWidth;
    public double imageOriginEastings;
    public double imageOriginNorthings;
    public double imageBottomRightEastings;
    public double imageBottomRightNorthings;
    
    public double minLat, maxLat, minLon, maxLon;
    
    public boolean registered;
    //i.e. the control point which produces the pixel size
    //with the smallest difference from the mean.
    private ControlPoint bestControlPoint;
    
    private float scaleIndex = 1;

	private static final int MAX_CONTROL_PTS = 10;

    private static final int MIN_EASTINGS = 0;
    private static final int MIN_NORTHINGS = 0;    
    private static final int MAX_EASTINGS = 700000;
    private static final int MAX_NORTHINGS = 1300000;   
      
    private CoordSys coordSys;
    
    /** Creates a new instance of GeoImage */
    public GeoImage() {
    }
    
    public void setImage(Image i) {
        image = i;
    }
    
    public Image getImage() {
        return image;
    }
    
    public void setFilename(String filename) {
        imageFile = filename;
        imageDir = filename.substring(0, filename.lastIndexOf('/') + 1);
        xmlFile = filename.substring(0, (filename.lastIndexOf('.'))) + ".xml";
    }
   
    public String getDirname() {
        return imageDir;
    }      
    
    public String getFilename() {
        return imageFile;
    }
    
    public String getXMLFilename () {
        return xmlFile;
    }
    
    public void addGuidancePoint(GuidancePoint guidancePoint) {
      guidancePoints.addElement(guidancePoint);
    }
    
    public void addGPXPointAsOSGridRef(OSGridRef osGridRef) {
      gpxPoints.addElement(osGridRef);
    }    

    public void removeAllGPXPoints() {
        gpxPoints.removeAllElements();
    }    
    
    public Vector getGPXPoints() {
      return gpxPoints;
    }
    
    public void removeGuidancePoint(GuidancePoint guidancePoint) {
      guidancePoints.removeElement(guidancePoint);
    }
    
    public void removeAllGuidancePoints() {
        guidancePoints.removeAllElements();
    }
    
    public Vector getGuidancePoints() {
      return guidancePoints;
    }    
    
    public void addControlPoint(ControlPoint controlPoint) {
      controlPoints.addElement(controlPoint);
    }

    public void removeControlPoint(ControlPoint controlPoint) {
      controlPoints.removeElement(controlPoint);
    }
    
    public void removeAllControlPoints() {
        controlPoints.removeAllElements();
    }

    public boolean availableControlPoints() {
      if (controlPoints.size() < MAX_CONTROL_PTS) {
          return true;
      } else {
          return false;
      }
    }

    public Vector getControlPoints() {
      return controlPoints;
    }
    
    public boolean validateControlPoint(ControlPoint controlPoint) {
        
        ControlPoint controlPointI;        
        
        //First check that the eastings and northings are reasonable.
        if (controlPoint.eastings < MIN_EASTINGS || controlPoint.eastings > MAX_EASTINGS ||
                controlPoint.northings < MIN_NORTHINGS || controlPoint.northings > MAX_NORTHINGS) {
            return false;
        }
        
        //Check control point against all other control points added so far.
        //The new control point should be unique in image_x, image_y, eastings and
        //northings.
        for (int i = 0; i < controlPoints.size(); i++) {
            controlPointI = (ControlPoint) controlPoints.elementAt(i);
            if(controlPoint.image_x == controlPointI.image_x || controlPoint.image_y == controlPointI.image_y ||
                    controlPoint.eastings == controlPointI.eastings || controlPoint.northings == controlPointI.northings) {
                return false;
            }
        }
        
        return true;
    }
    
    public void registerImage() {
        double totalPixelWidth, totalPixelHeight;
        double totalPixelWidthMinusMean, totalPixelHeightMinusMean;
        ControlPoint controlPointI;
        ControlPoint controlPointJ;
        ImagePoint imagePointBottomRight;
        OSGridRef osGridRefBottomRight;
        LatLong latLongImageOrigin;
        LatLong latLongBottomRight;
        
        //Loop round control points and calculate pixel height/width
        //to other control points.
        for (int i = 0; i < controlPoints.size(); i++) {
            
            controlPointI = (ControlPoint) controlPoints.elementAt(i);
            Vector pixels = new Vector();
            
            for (int j = 0; j < controlPoints.size(); j++) {
                controlPointJ = (ControlPoint) controlPoints.elementAt(j);
                if (controlPointI != controlPointJ) {
                    //Calculate pixel height and width between i and j.
                    Pixel pixel = new Pixel();
                    pixel.Width = Math.abs((controlPointI.eastings - controlPointJ.eastings) / (controlPointI.image_x - controlPointJ.image_x));
                    pixel.Height = Math.abs((controlPointI.northings - controlPointJ.northings) / (controlPointI.image_y - controlPointJ.image_y));
                    pixels.addElement(pixel);
                }
            }
            
            //Get average
            totalPixelWidth = 0.0;            
            totalPixelHeight = 0.0;
            for (int j = 0; j < pixels.size(); j++) {
                Pixel pixel = new Pixel();               
                pixel = (Pixel) pixels.elementAt(j);
                totalPixelWidth = totalPixelWidth + pixel.Width;                
                totalPixelHeight = totalPixelHeight + pixel.Height;                
            }
            controlPointI.pixelWidth = totalPixelWidth / pixels.size();
            controlPointI.pixelHeight = totalPixelHeight / pixels.size();
            
        }
        
        //Set the pixel height, width of the image to the average.
        totalPixelWidth = 0.0;
        totalPixelHeight = 0.0;        
        for (int i = 0; i < controlPoints.size(); i++) {
            controlPointI = (ControlPoint) controlPoints.elementAt(i);
            totalPixelWidth = totalPixelWidth + controlPointI.pixelHeight;                
            totalPixelHeight = totalPixelHeight + controlPointI.pixelWidth;                
        }
        
        this.pixelWidth = totalPixelWidth / controlPoints.size();
        this.pixelHeight = totalPixelHeight / controlPoints.size();
        
        //Populate the difference from the mean for each control point
        for (int i = 0; i < controlPoints.size(); i++) {
            controlPointI = (ControlPoint) controlPoints.elementAt(i);
            controlPointI.pixelWidthDiffFromMean = controlPointI.pixelWidth - this.pixelWidth;                
            controlPointI.pixelHeightDiffFromMean = controlPointI.pixelHeight - this.pixelHeight;
        }       
        
        //Identify the point with the smallest positional difference from the mean.
        //This point is used to calculate the eastings and northings of the
        //image origin (top left hand corner).
        double diffAsLength;
        double minLength = 0.0;
        for (int i = 0; i < controlPoints.size(); i++) {
            controlPointI = (ControlPoint) controlPoints.elementAt(i);
            diffAsLength = Math.sqrt((controlPointI.pixelWidthDiffFromMean * controlPointI.pixelWidthDiffFromMean) +
                    (controlPointI.pixelHeightDiffFromMean * controlPointI.pixelHeightDiffFromMean));       
            if (i == 0) {
                minLength = diffAsLength;
                bestControlPoint = controlPointI;
            } else {
                if (diffAsLength < minLength) {
                    bestControlPoint = controlPointI;
                }
            }
        }
        
        //Calculate image origin - top left hand corner.
        imageOriginEastings = bestControlPoint.eastings - pixelWidth * (bestControlPoint.image_x);
        imageOriginNorthings = bestControlPoint.northings + pixelHeight * (bestControlPoint.image_y);
        
        //Calcuate OS coords of bottom right corner.
        imagePointBottomRight = new ImagePoint(image.getWidth(), image.getHeight());
        osGridRefBottomRight = imagePointAsGridRef(imagePointBottomRight);
        
        //Calculate min max latitude and longitude covered by image
        //Origin as lat long.
        coordSys = new CoordSys();
        latLongImageOrigin = coordSys.getWGS84LatLong(imageOriginEastings, imageOriginNorthings);
        //Bottom right corner as lat long.
        latLongBottomRight = coordSys.getWGS84LatLong(osGridRefBottomRight.Eastings, osGridRefBottomRight.Northings);
        
        minLat = latLongBottomRight.Latitude;
        maxLat = latLongImageOrigin.Latitude;
        minLon = latLongImageOrigin.Longitude;
        maxLon = latLongBottomRight.Longitude;
        coordSys = null;
    }
    
    public ImagePoint osGridRefAsImagePoint(OSGridRef osGridRef) {
        
        double image_x, image_y;
        int intImage_x, intImage_y;

        image_x = (osGridRef.Eastings - imageOriginEastings) / pixelWidth;
        image_y = (imageOriginNorthings - osGridRef.Northings) / pixelHeight;
        intImage_x = (int) image_x;
        intImage_y = (int) image_y;
        ImagePoint imagePoint = new ImagePoint(intImage_x, intImage_y);
        return imagePoint;
    }
    
    public OSGridRef imagePointAsGridRef(ImagePoint imagePoint) {
        
        double eastings, northings;
        int intEastings, intNorthings;

        eastings = (pixelWidth * imagePoint.image_x) + imageOriginEastings;
        northings = imageOriginNorthings - (pixelHeight * imagePoint.image_y);
        intEastings = (int) eastings;
        intNorthings = (int) northings;
        OSGridRef osGridRef = new OSGridRef(intEastings, intNorthings);
        return osGridRef;
    }
    
    public float getScaleIndex() {
		return scaleIndex;
	}

	public void setScaleIndex(float scaleIndex) {
		this.scaleIndex = scaleIndex;
	}

}
