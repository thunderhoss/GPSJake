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
 * classname: GuidancePlayer
 *
 * desc: Class defines a media player for a guidance prompt.
 */

package gpsjake;

import javax.microedition.media.*;

public class GuidancePlayer {
    
    private Player player;
    private String fileName;
    
    public GuidancePlayer(Player player, String fileName) {
        this.player = player;
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public Player getPlayer() {
        return player;
    }
}
