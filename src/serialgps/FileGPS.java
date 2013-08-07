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
 * classname: FileGPS
 *
 * desc: FileGPS extends SerialGPS for reading GPS data from an NMEA file.
 */

package serialgps;

import gpsjake.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;

public class FileGPS extends SerialGPS {
    public final static int THREAD_PAUSE=500;
    public FileConnection fileConn;
    public InputStream fis;    
    
    public FileGPS(GPSJakeMIDlet midlet) {
        super(midlet);
    }
   
    public void connect(final String url) {
        try {        
            fileConn = (FileConnection)Connector.open(url, Connector.READ);
            fis = fileConn.openInputStream();   
            super.reader = new InputStreamReader(fis);            
        } catch (IOException e) {
            midlet.importantErrorLogOnly("FileGPS:sleepThread:Error connecting GPS: " + e.toString());
        } catch (Exception e) {
            midlet.fatalError("FileGPS:connect" + e.toString());
        }            
    }
    
    public void sleepThread(long millis) {
        //When reading from a file implement this method.
        //This is an attempt to ensure around second elapses between each RMC string
        //when reading from a file.
        try {
            if (millis < THREAD_PAUSE) Thread.sleep((long)THREAD_PAUSE - millis);
        } catch (InterruptedException e) {
            midlet.importantErrorLogOnly("FileGPS:sleepThread:Error delaying next read: " + e.toString());
        } catch (Exception e) {
            midlet.fatalError("FileGPS:sleepThread " + e.toString());
        }
    }
    
    public void disconnect() {
        try {
            super.reader.close();
            fis.close();
            fileConn.close();
        } catch (IOException e) {
            midlet.importantErrorLogOnly("FileGPS:disconnect:Error disconnecting GPS: " + e.toString());
        } catch (Exception e) {
            midlet.fatalError("FileGPS:disconnect" + e.toString());
        }
    }
       
}
