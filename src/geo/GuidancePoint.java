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
 * classname: GuidancePoint
 *
 * desc: Class describes a guidance point - it's location (image coordinates) and the wav file to be played.
 */

package geo;

public class GuidancePoint {
    
    public int image_x;
    public int image_y;
    public double eastings;
    public double northings;    
    public String wavFileName;    
    public long timePlayed = 0;
    public double prevDistance = 0;
    public boolean userDefined = false;
    
    public GuidancePoint() {
        
    }
    
    public GuidancePoint(int image_x, int image_y, double eastings, double northings, String wavFileName, boolean userDefined) {
        this.image_x = image_x;
        this.image_y = image_y;
        this.eastings = eastings;
        this.northings = northings;
        this.wavFileName = wavFileName;
        this.userDefined = userDefined;
    }
    
    public boolean approaching(double currentDistance) {
        if (prevDistance == 0) {
            prevDistance = currentDistance;
            return false;
        } else if (prevDistance > currentDistance) {
            prevDistance = currentDistance;
            return true;
        } else {
            prevDistance = currentDistance;
            return false;
        }
    }

    public boolean playedRecently(long time, int period) {
        if(time - timePlayed <= period) {
            return true;
        } else {
            return false;
        }
    }    
    
}
