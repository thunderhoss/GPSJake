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
 * classname: ImagePoint
 *
 * desc: ImagePoint stores an image coordinate pair (x , y).
 * Provides a method to caluclate distance between this pair and another provided pair.
 */

package geo;

public class ImagePoint {
    
    public int image_x = -9999;
    public int image_y = -9999;
    
    public ImagePoint() {
    }
    
    public ImagePoint(int image_x, int image_y) {
        this.image_x = image_x;
        this.image_y = image_y;
    }
   
    public int distanceBetween(ImagePoint otherImgPnt) {
        return (int) Math.sqrt((otherImgPnt.image_x - image_x) * (otherImgPnt.image_x - image_x)
            + (otherImgPnt.image_y - image_y) * (otherImgPnt.image_y - image_y));
    } 
}
