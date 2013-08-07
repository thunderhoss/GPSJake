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
 * classname: GPXReader
 *
 * desc: Class imports a GPX file.  The points from the file are stored in a
 * vector property of the GeoImage class.
 * Note: for consistency the reading of the GPX should probably be handled by a seperate
 * class in the xml package.
 */

package gpsjake;

import java.io.*;
import geo.OSGridRef;
import geo.ImagePoint;
import geo.GeoImage;
import geo.LatLong;
import geo.CoordSys;

import xml.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;

/**
 * ImageLoader
 */
class GPXReader extends Form implements CommandListener {
    private final Command okCommand;
    private final Command backCommand;
    private GPSJakeMIDlet midlet;
    private GeoImage gimage;
    private String gpxFile;
    private FileConnection fileConn;
    private InputStream fis;
    private CoordSys coordSys;
    private LatLong prevLatLong;
    private OSGridRef prevOSGridRef;
    private Gauge gauge;
    private long fileSize;
    private long readAmount;
    // The gpx file is read in single bytes.
    private static final int READ_INCREMENT=1;
    
    GPXReader(GPSJakeMIDlet midlet, GeoImage geoImage, String gpxFile) {
        super("GPSjake");
        
        this.midlet = midlet;
        this.gimage = geoImage;
        this.gpxFile = gpxFile;
        
        okCommand = new Command("OK", Command.OK, 1);
        backCommand = new Command("Back", Command.BACK, 1);
        
        try {
            append(new StringItem(null, ""));
            append(new StringItem(null, "Use " + gpxFile + "?"));
            addCommand(okCommand);
            addCommand(backCommand);
            
            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("GPXReader:GPXReader " + e.toString());
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        try {
            if (c == okCommand) {
                if (parseGPXFile()) {
                    midlet.backMap();
                }
            } else {
                midlet.backMap();
            }
        } catch (Exception e) {
            midlet.fatalError("GPXReader:commandAction " + e.toString());
        }
    }
    
    private boolean parseGPXFile() {
        String line = "";
        int ch;
        double percent;
        
        this.coordSys = new CoordSys();
        this.prevLatLong = new LatLong();
        this.prevOSGridRef = new OSGridRef();
        
        if (gimage.getGPXPoints().size() > 0) {
            gimage.removeAllGPXPoints();
        }
        try {
            
            delete(1);
            gauge = new Gauge("Importing gpx file...", false, 100, 0);
            //#ifndef Belle_Emulator
            //# append(gauge);
            //#endif
            removeCommand(okCommand);
            removeCommand(backCommand);
            
            fileConn = (FileConnection)Connector.open(gpxFile, Connector.READ);
            fis = fileConn.openInputStream();
            fileSize = fileConn.fileSize();
            readAmount = 0;
            
            //read() returns a byte from the file.
            ch = fis.read();
            //Add 2 to readAmount - chars are 2 bytes.
            readAmount = readAmount + READ_INCREMENT;
            while(ch != -1) {
                line = "";
                while(ch != -1 && ch != '\n') {
                    line = line + (char)ch;
                    ch = fis.read();
                    readAmount = readAmount + READ_INCREMENT;
                }
                //Do trim to remove carriage return.
                parseLine(line.trim());
                ch = fis.read();
                readAmount = readAmount + READ_INCREMENT;
                percent = readAmount / (double) fileSize;
                gauge.setValue((int) (percent * 100));
            }
            
        } catch (XMLParseException e) {
            cleanUp();
            midlet.importantError("GPXReader:parseGPXFile " + e.toString(), "Error parsing GPX file.");
            return false;
        } catch (IOException e) {
            cleanUp();
            midlet.fatalError("GPXReader:parseGPXFile " + e.toString());
            return false;
        } catch (Exception e) {
            cleanUp();
            midlet.fatalError("GPXReader:parseGPXFile " + e.toString());
            return false;
        }
        return true;
    }
    
    private void cleanUp() {
        try {
            fis.close();
            fileConn.close();
            fis = null;
            fileConn = null;
            coordSys = null;
            prevLatLong = null;
            prevOSGridRef = null;
        } catch (IOException e) {
            midlet.fatalError("GPXReader:cleanUp " + e.toString());
        } finally {
            fis = null;
            fileConn = null;
            coordSys = null;
            prevLatLong = null;
            prevOSGridRef = null;
        }
    }
    
    private String readLine() {
        String line = "";
        int ch;
        try {
            ch = fis.read();
            readAmount = readAmount + READ_INCREMENT;
            while(ch != -1 && ch != '\n') {
                line = line + (char)ch;
                ch = fis.read();
                readAmount = readAmount + READ_INCREMENT;
            }
            //Do trim to remove carriage return.
            return line.trim();
        } catch (IOException e) {
            midlet.fatalError("GPXReader:readLine " + e.toString());
            return null;
        } catch (Exception e) {
            midlet.fatalError("GPXReader:readLine " + e.toString());
            return null;            
        }
    }
    
    private void parseLine(String line) throws XMLParseException {
        try {
            String nextLine;
            
            if (line.startsWith("<trkpt")) {
                if (!line.endsWith("<\trkpt>")) {
                    nextLine = readLine();
                    while (!nextLine.endsWith("</trkpt>") && !nextLine.equals("")) {
                        line = line + nextLine;
                        nextLine = readLine();
                    }
                    line = line + nextLine;
                }
                //Parse <trkpt
                parseTrkPtElement(line);
            }
        } catch (XMLParseException e) {
            throw new XMLParseException("Encountered invalid element.", XMLParseException.IMPORTANT);
        } catch (Exception e) {
            midlet.fatalError("GPXReader:parseLine " + e.toString());
        }
    }
    
    private void parseTrkPtElement(String element) throws XMLParseException {
        double lat, lon;
        OSGridRef osGridRef;
        ImagePoint imagePoint = new ImagePoint();
        LatLong latLong;
        boolean withinPrevious = false;
        double distance;
        String startLatField="lat=";
        String startLonField="lon=";
        try {
            if(element.indexOf(startLatField) == -1 || element.indexOf(startLonField) == -1) {
                throw new XMLParseException("Invalid <trkpt> element.", XMLParseException.IMPORTANT);
            }
            
            lat = Double.parseDouble(element.substring(element.indexOf(startLatField) + startLatField.length() + 1,
                    element.indexOf("\"", element.indexOf(startLatField) + startLatField.length() + 1)));
            lon = Double.parseDouble(element.substring(element.indexOf(startLonField) + startLonField.length() + 1,
                    element.indexOf("\"", element.indexOf(startLonField) + startLonField.length() + 1)));
            latLong = new LatLong(lat, lon);
            
            //Don't use calculated distance.  Use distance between pixels.
            //Use size of marker as threshold.
            
            if (prevLatLong.Latitude != -9999.99 && prevLatLong.Longitude != -9999.99) {
                //If point is equal to previous point then don't add to
                //array
                if (latLong.Latitude == prevLatLong.Latitude && latLong.Longitude == prevLatLong.Longitude) {
                    return;
                }
            }
            
            //If point is not within image don't add.
            if (!(lat >= gimage.minLat && lat <= gimage.maxLat && lon >= gimage.minLon && lon <= gimage.maxLon)) {
                return;
            }
            
            //Convert GPX track point to OS coords.
            osGridRef = coordSys.getOSGridRef(lat, lon);
            
            if (prevOSGridRef.Eastings != -9999.99 && prevOSGridRef.Northings != -9999.99) {
                //If OS Grid Ref is equal to previous don't add
                if (osGridRef.Eastings == prevOSGridRef.Eastings && osGridRef.Northings == prevOSGridRef.Northings) {
                    return;
                }
                
                //Calculate distance from the previous point.  If distance is less than minimum (size of
                //route marker) don't add.
                distance = osGridRef.distanceBetween(prevOSGridRef);
                if (distance <= midlet.getGPXDist()) {
                    withinPrevious = true;
                }
            }
            
            if (!withinPrevious) {
                gimage.addGPXPointAsOSGridRef(osGridRef);
                prevOSGridRef.Eastings = osGridRef.Eastings;
                prevOSGridRef.Northings = osGridRef.Northings;
            }
            osGridRef = null;
            prevLatLong.Latitude = latLong.Latitude;
            prevLatLong.Longitude = latLong.Longitude;
            latLong = null;
            
        } catch (Exception e) {
            midlet.fatalError("GPXReader:parseTrkPtElement " + e.toString());
        }
    }
}
