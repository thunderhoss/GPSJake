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
 * classname: BTGPS
 *
 * desc: BTGPS extends SerialGPS for reading GPS data from a bluetooth URL.
 */

package serialgps;

import gpsjake.*;
import java.io.*;
import javax.microedition.io.*;

public class BTGPS extends SerialGPS {
   
    private StreamConnection stream;
    private InputStream in;

    public BTGPS(GPSJakeMIDlet midlet) {
        super(midlet);       
    }

    public void connect(final String url) {
        try {        
            stream = (StreamConnection) Connector.open(url);
            in = stream.openInputStream();
            super.reader = new InputStreamReader(in);
        } catch (IOException e) {
            midlet.importantError(e.toString(), "Error reading GPS.");
        }            
    }

    public void disconnect() {
        try {
            super.reader.close();
            in.close();
            stream.close();
        } catch (IOException e) {
            //midlet.importantError(e.toString(), "Error reading GPS.");
            //Change to log only to prevent ugly dialog appearing when disconnecting from feed.
            midlet.importantErrorLogOnly(e.toString());
        }        
    }       
    
}
