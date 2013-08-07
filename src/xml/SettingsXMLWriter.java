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
 * classname: SettingsXMLWriter
 *
 * desc: Writes image settings (registration information, guidance points) to an xml file.
 */
package xml;

import gpsjake.*;
import java.util.Vector;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import geo.*;

public class SettingsXMLWriter {
        
    private FileConnection fileConn;
    private OutputStream fos;
    private PrintStream fps;
    private GPSJakeMIDlet midlet;
    public final static String EOL = "\r\n";
    
    public SettingsXMLWriter(GPSJakeMIDlet midlet, String xmlFile) {
        this.midlet = midlet;
        
        try {
            fileConn = (FileConnection)Connector.open(xmlFile, Connector.READ_WRITE);
            if (fileConn.exists()) {
                fileConn.delete();
            }
            fileConn.create();
        } catch (IOException e) {
            midlet.fatalError("SettingsXMLWriter:SettingsXMLWriter " + e.toString());
        }
        
    }
    
    public void startWriting() {
        try {
            fos = fileConn.openOutputStream();
            fps = new PrintStream(fos);
            fps.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + EOL);
            fps.print("<settings>" + EOL);
        } catch (IOException e) {
            midlet.fatalError("SettingsXMLWriter:startWriting " + e.toString());
        }
    }
    
    public void stopWriting() {
        try {
            fps.print("</settings>" + EOL);
            fps = null;
            fos.close();
            fileConn.close();
        } catch (IOException e) {
            midlet.fatalError("SettingsXMLWriter:stopWriting " + e.toString());
        }
    }
    
    public void writeControlPoints(Vector controlPoints) {
        try {
            ControlPoint controlPoint;
            
            for (int i = 0; i < controlPoints.size(); i++) {
                controlPoint = (ControlPoint) controlPoints.elementAt(i);
                fps.print("<controlPoint><image_x>" + controlPoint.image_x + "</image_x><image_y>" + controlPoint.image_y +
                        "</image_y><eastings>" + controlPoint.eastings + "</eastings><northings>" +
                        controlPoint.northings + "</northings></controlPoint>" + EOL);
            }
        } catch (Exception e) {
            midlet.fatalError("SettingsXMLWriter:writeControlPoints " + e.toString());
        }
    }

    public void writeGuidancePoints(Vector guidancePoints) {
        try {
            GuidancePoint guidancePoint;
            
            for (int i = 0; i < guidancePoints.size(); i++) {
                guidancePoint = (GuidancePoint) guidancePoints.elementAt(i);
                fps.print("<guidancePoint>" +
                			"<image_x>" + guidancePoint.image_x + "</image_x>" +
                			"<image_y>" + guidancePoint.image_y + "</image_y>" +
                			"<eastings>" + guidancePoint.eastings + "</eastings>" +
                			"<northings>" + guidancePoint.northings + "</northings>" +                			
                			"<wavFile>" + guidancePoint.wavFileName + "</wavFile>");
                if (guidancePoint.userDefined) {
                    fps.print("</userDefined>");
                }
                fps.print("</guidancePoint>" + EOL);
            }
        } catch (Exception e) {
            midlet.fatalError("SettingsXMLWriter:writeGuidancePoints " + e.toString());
        }
    }    
    
}
