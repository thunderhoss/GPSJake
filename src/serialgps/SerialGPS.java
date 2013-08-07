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
 * classname: SerialGPS
 *
 * desc: SerialGPS sets up a thread to read a stream.
 * This class is abstract and extended by BTGPS or FileGPS to read from a bluetooth URL or a file
 * respectively.
 * The stream is read in a thread and sent to the parser (NMEAParser).
 * NMEAParser populates the GPSInfo class which contains current GPS data.
 */

package serialgps;

import gpsjake.*;
import java.io.*;
import geo.GPSInfo;
import geo.NMEAParser;
import geo.NMEAParseException;
import geo.OSGridRef;

public abstract class SerialGPS implements Runnable {
    
    public final GPSJakeMIDlet midlet;
    
    private String bufferAsString;
    public NMEAParser nmeaParser;
    public volatile Thread gpsReadThread;
    //InputStreamReader gets instantiated in extended class
    public InputStreamReader reader;
    public static final int CARRIAGE_RETURN = 13;
    private OSGridRef gpxPnt;    
    
    SerialGPS(GPSJakeMIDlet midlet) {
        
        this.midlet = midlet;
        try {
            nmeaParser = new NMEAParser(midlet.getGPSInfo());
        } catch (Exception e) {
            midlet.fatalError("SerialGPS:SerialGPS " + e.toString());
        }
    }
    
    public synchronized void start() {
        try {
            gpsReadThread = new Thread(this);
            gpsReadThread.start();
        } catch (Exception e) {
            midlet.fatalError("SerialGPS:start " + e.toString());
        }
    }
    
    public synchronized void stop() {
        try {
            gpsReadThread = null;
            disconnect();
        } catch (Exception e) {
            midlet.fatalError("SerialGPS:stop " + e.toString());
        }
    }
    
    public void run() {
        int input = 0;
        long startTime = 0;        
        Thread currentThread = Thread.currentThread();
        while(gpsReadThread == currentThread  && input != -1) {
            try {
                startTime = System.currentTimeMillis();
                String output = new String();
                
                while ((input = reader.read()) != CARRIAGE_RETURN) {
                    if (input == -1) break;
                    output += (char) input;
                    if (output.length() > NMEAParser.NMEA_MAX_CHARS * 3) {
                        nmeaParser.throwNMEAParseException("NMEA sentence exceeds max length: " + output);
                    }
                }
                //read the new line character.
                input = reader.read();
                
                //bufferString now contains NMEA sentence minus the NMEA line delimiter (\r\n).
                bufferAsString = output;
                
                //If logging is on write the string to the log file
                if (midlet.getGPSInfo().isNMEALoggingOn()) {
                    midlet.getGPSInfo().getNMEAPrintStream().print(bufferAsString + NMEAParser.NMEA_EOS);
                }
                
                //Write GPX point if we have a fix, logging is turned on and we have travelled the required
                //minimum distance (reduces number of points written to file).
                
                if (midlet.getGPSInfo().isGPXLoggingOn() && midlet.getGPSInfo().getFix()) {
                    if (gpxPnt == null) {
                        gpxPnt = new OSGridRef();
                        gpxPnt = midlet.getGPSInfo().osGridRef;
                    } else { 
                        if (gpxPnt.distanceBetween(midlet.getGPSInfo().osGridRef) >= GPSInfo.GPX_EXPORT_THRESHOLD) {
                            midlet.getGPSInfo().getGPXXMLWriter().writeTrackPoint();
                            gpxPnt = midlet.getGPSInfo().osGridRef;
                        }
                    }
                }
                
                nmeaParser.parseNMEASentence(bufferAsString);
                
                if (midlet.getGPSInfo().type == GPSInfo.NMEA_RMC) {
                    midlet.getGPSInfo().updateStats();
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    sleepThread(elapsedTime);
                }

            } catch (IOException e) {
                //Changed this to log only because when GPS read was stopped from show GPS screen
                //a read exception occured which looked nasty.
                //Admittedly this is not ideal because GPS read problems could now go unnoticed if the user
                //is not logging debug messages.
                midlet.importantErrorLogOnly("SerialGPS:run " + e.toString() + ". Failed to read GPS.");
                //midlet.serialGPSError("SerialGPS:run " + e.toString() + ". Failed to read GPS.");
                break;
            } catch (NMEAParseException e) {
                if (e.severity == NMEAParseException.IMPORTANT) {
                    midlet.importantErrorLogOnly(e.toString());
                } else {
                    midlet.minorError(e.toString());
                }
            } catch (Exception e) {
                midlet.fatalError("SerialGPS:run " + e.toString() + ". Error reading GPS: "  + bufferAsString);
                break;
            }
        }
        stop();
        midlet.getGPSInfo().reset();
    }
    
    public void connect() {

    }
    
    public void sleepThread(long millis) {

    }
    
    public void disconnect() {

    }
    
    public String getBufferAsString() {
        return bufferAsString;
    }
}
