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
 * classname: GPXXMLWriter
 *
 * desc: GPXXMLWriter used to write GPX data to a file.
 */

package xml;

import java.io.*;
import gpsjake.*;

public class GPXXMLWriter {

    private GPSJakeMIDlet midlet;    
    private PrintStream ps;
    public final static String EOL = "\r\n";
  
    /** Creates a new instance of GPXXMLWriter */
    public GPXXMLWriter(GPSJakeMIDlet midlet) {
        this.midlet = midlet;
        ps = midlet.getGPSInfo().getGPXPrintStream();
    }
    
    public void writeHeader() {
        try {
            ps.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + EOL);
            ps.print("<gpx version=\"1.0\" creator=\"GPSjake\">" + EOL);
            ps.print("<trk>" + EOL);
            ps.print("<trkseg>" + EOL);
        } catch (Exception e) {
            midlet.fatalError("GPXXMLWriter:writeHeader " + e.toString());
        }         
    }
    
    public void writeTrackPoint() {
        try {
            String xsdDateTime = this.midlet.getGPSInfo().getDateXSD();
            ps.print("<trkpt lat=\"" + this.midlet.getGPSInfo().getLatitude()
                        + "\" lon=\"" + this.midlet.getGPSInfo().getLongitude() + "\">" + EOL);
            if (xsdDateTime != null) {
                ps.print("<time>" + xsdDateTime + "</time>" + EOL);                
            }
            ps.print("</trkpt>" + EOL);
        } catch (Exception e) {
            midlet.fatalError("GPXXMLWriter:writeTrackPoint " + e.toString());
        }         
    }
    
    public void writeFooter() {
        try {
            ps.flush();
            ps.print("</trkseg>" + EOL);        
            ps.print("</trk>" + EOL);
            ps.print("</gpx>" + EOL);
        } catch (Exception e) {
            midlet.fatalError("GPXXMLWriter:writeFooter " + e.toString());
        }        
    }
}
