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
 * classname: SettingsXMLReader
 *
 * desc: Image settings are stored in an xml file (same name as image).
 * SettingsXMLReader reads and parses this xml file.
 * Registration information and guidance points, specific to the map image, are
 * stored in this file.
 */

package xml;

import gpsjake.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import geo.*;

public class SettingsXMLReader {
    private FileConnection fileConn;
    private InputStream fis;
    private GPSJakeMIDlet midlet;
    private GeoImage gimage;
    
    public SettingsXMLReader(GPSJakeMIDlet midlet, GeoImage geoImage) {
        this.midlet = midlet;
        this.gimage = geoImage;
        try {
            fileConn = (FileConnection)Connector.open(gimage.getXMLFilename(), Connector.READ);
        } catch (IOException e) {
            midlet.fatalError("SettingsXMLReader:SettingsXMLReader " + e.toString());
        }
    }

    public void parseSettingsFile() {
        try {
            String line;
            int ch;
            if (gimage.getControlPoints().size() > 0) {
                gimage.removeAllControlPoints();
            }
            try {
                fis = fileConn.openInputStream();
                ch = fis.read();
                while(ch != -1) {
                    line = "";
                    while(ch != -1 && ch != '\n') {
                        line = line + (char)ch;
                        ch = fis.read();                
                    }
                    //Do trim to remove carriage return.
                    parseLine(line.trim());
                    ch = fis.read();
                }
                fis.close();
                fileConn.close();
                fis = null;
                fileConn = null;
            } catch (IOException e) {
                midlet.fatalError("SettingsXMLReader:parseSettingsFile " + e.toString());
            }
            
            //Register image if there is sufficient control points.
            if (gimage.getControlPoints().size() >= 2) {
                gimage.registerImage();
                gimage.registered = true;
            } else {
                gimage.registered = false;
            }
        } catch (Exception e) {
            midlet.fatalError("SettingsXMLReader:parseSettingsFile " + e.toString());
        }
    }
    
    public boolean fileExists() {
        if (fileConn.exists()) {
            return true;
        }
        return false;
    }
    
    private void parseLine(String line) {
        try {
            int image_x;
            int image_y;
            double eastings;
            double northings;
            String strImage_x, strImage_y, strEastings, strNorthings, strWavFile;
            String startTag, endTag;
            boolean userDefined = false;
            
            if (line.startsWith("<controlPoint>")) {
                //Add the control point
                startTag = "<image_x>";
                endTag = "</image_x>";
                strImage_x = line.substring((line.indexOf(startTag) + startTag.length()), (line.indexOf(endTag)));
                startTag = "<image_y>";
                endTag = "</image_y>";
                strImage_y = line.substring((line.indexOf(startTag) + startTag.length()), (line.indexOf(endTag)));
                startTag = "<eastings>";
                endTag = "</eastings>";
                strEastings = line.substring((line.indexOf(startTag) + startTag.length()), (line.indexOf(endTag)));
                startTag = "<northings>";
                endTag = "</northings>";
                strNorthings = line.substring((line.indexOf(startTag) + startTag.length()), (line.indexOf(endTag)));
                eastings = Double.parseDouble(strEastings);
                northings = Double.parseDouble(strNorthings);
                image_x = Integer.parseInt(strImage_x);
                image_y = Integer.parseInt(strImage_y);
                ControlPoint controlPoint = new ControlPoint(image_x, image_y, eastings, northings);
                gimage.addControlPoint(controlPoint);
            } else if (line.startsWith("<guidancePoint>")) {
                //Add the guidance point
                startTag = "<image_x>";
                endTag = "</image_x>";
                strImage_x = line.substring((line.indexOf(startTag) + startTag.length()), (line.indexOf(endTag)));
                startTag = "<image_y>";
                endTag = "</image_y>";
                strImage_y = line.substring((line.indexOf(startTag) + startTag.length()), (line.indexOf(endTag)));

                startTag = "<eastings>";
                endTag = "</eastings>";
                strEastings = line.substring((line.indexOf(startTag) + startTag.length()), (line.indexOf(endTag)));
                startTag = "<northings>";
                endTag = "</northings>";
                strNorthings = line.substring((line.indexOf(startTag) + startTag.length()), (line.indexOf(endTag)));                

                eastings = Double.parseDouble(strEastings);
                northings = Double.parseDouble(strNorthings);                
                
                startTag = "<wavFile>";
                endTag = "</wavFile>";
                strWavFile = line.substring((line.indexOf(startTag) + startTag.length()), (line.indexOf(endTag)));
                image_x = Integer.parseInt(strImage_x);
                image_y = Integer.parseInt(strImage_y);
                startTag = "</userDefined>";
                if (line.indexOf(startTag) > -1) {
                    userDefined = true;
                }
                GuidancePoint guidancePoint = new GuidancePoint(image_x, image_y, eastings, northings, strWavFile, userDefined);
                gimage.addGuidancePoint(guidancePoint);                
            }
        } catch (Exception e) {
            midlet.fatalError("SettingsXMLReader:parseLine " + e.toString());
        }
    }
}
