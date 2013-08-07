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
 * classname: MaintainGuidance
 *
 * desc: This class controls the amount of guidance players that are loaded at any one time.
 * It uses a distance (RANGE in pixels) for determining when a player should be loaded or not.
 */

package gpsjake;

import geo.GeoImage;
import geo.OSGridRef;
import geo.GuidancePoint;
import java.util.Vector;
import javax.microedition.media.*;
import java.io.*;

public class MaintainGuidance implements Runnable {

    public volatile Thread maintainGuidanceThread;
    private static final int MILLIS_PER_TICK = 1000;
    //Threshold distance for loading guidance players (metres)
    private static final int RANGE = 200;
    //Maximum number of guidance players to keep loaded
    private static final int MAX_PLAYERS = 3;
    
    private GPSJakeMIDlet midlet;
    private GeoImage gimage;
    private boolean maintainPlayers = false;

    public MaintainGuidance(GPSJakeMIDlet midlet, GeoImage geoImage) {
        this.midlet = midlet;
        this.gimage = geoImage;
    }

    public void maintainPlayers(OSGridRef gridRef) {
        double distance;
        try {
            int i, j;
            GuidancePoint guidancePnt;
            //Maintain player list.
            //Create list of guidance point file names within range.
            Vector withinRangeUserDefined = new Vector();
            Vector withinRangeResPrompts = new Vector();            
            Vector outOfRange = new Vector();
            
            OSGridRef guidanceGridRef = new OSGridRef();
            
            boolean promptBeingAdded;
            
            for (i = 0; i < gimage.guidancePoints.size(); i++) {

                guidancePnt = (GuidancePoint) gimage.guidancePoints.elementAt(i);
                guidanceGridRef.Eastings = guidancePnt.eastings;
                guidanceGridRef.Northings = guidancePnt.northings;
                
                //Add the player if it's within range
                distance = guidanceGridRef.distanceBetween(gridRef);
                if (distance < RANGE) {
                    if (guidancePnt.userDefined) {
                        withinRangeUserDefined.addElement(guidancePnt.wavFileName);
                    } else {
                        withinRangeResPrompts.addElement(guidancePnt.wavFileName);
                    }
                } else {
                    outOfRange.addElement(guidancePnt.wavFileName);
                }
                      
            }
            
            //For each guidance point within the player distance threshold
            //check that it has a player added.
            for (i = 0; i < withinRangeUserDefined.size(); i++) {
                addPlayer((String) withinRangeUserDefined.elementAt(i), true);
            }
            for (i = 0; i < withinRangeResPrompts.size(); i++) {
                addPlayer((String) withinRangeResPrompts.elementAt(i), false);
            }
            
            //First remove players which are out of range.
            for (i = 0; i < outOfRange.size(); i++) {
                //Check to see if the player hasn't been added into withinRangeUserDefined
                //or withinRangeResPrompts.
                promptBeingAdded = false;
                for (j = 0; j < withinRangeUserDefined.size(); j++) {
                    if (withinRangeUserDefined.elementAt(j).equals(outOfRange.elementAt(i))) {
                        promptBeingAdded = true;
                    }
                }
                for (j = 0; j < withinRangeResPrompts.size(); j++) {
                    if (withinRangeResPrompts.elementAt(j).equals(outOfRange.elementAt(i))) {
                        promptBeingAdded = true;
                    }
                }
                if (promptBeingAdded == false) {
                    removePlayer((String) outOfRange.elementAt(i));
                }
            }            
            
        } catch (Exception e) {
            midlet.importantErrorLogOnly("MaintainGuidance:maintainPlayers " + e.toString());
        }
    }

    private void removePlayer(String fileName) {
        GuidancePlayer guidancePlayer;
        try {
            if (midlet.guidancePlayers.size() == 0) return;            
            for (int i = 0; i < midlet.guidancePlayers.size(); i++) {
                guidancePlayer = (GuidancePlayer) midlet.guidancePlayers.elementAt(i);
                if (guidancePlayer.getFileName().equals(fileName)) {
                    System.out.println("Removing " + fileName);
                    midlet.guidancePlayers.removeElementAt(i);
                    return;
                }
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("MaintainGuidance:removePlayer " + e.toString());
        }
    }

    private void addPlayer(String fileName, boolean userDefined) {
        GuidancePlayer guidancePlayer;
        try {
            if (midlet.guidancePlayers.size() == MAX_PLAYERS) return;
            //See of the player needs to be added
            for (int i = 0; i < midlet.guidancePlayers.size(); i++) {
                guidancePlayer = (GuidancePlayer) midlet.guidancePlayers.elementAt(i);
                if (guidancePlayer.getFileName().equals(fileName)) {
                    //Player is already present
                    return;
                }
            }
            //If we've got this far the player needs to be created and added.
            Player player = createSoundPlayer(fileName, "audio/x-wav", userDefined);
            guidancePlayer = new GuidancePlayer(player, fileName);
            midlet.guidancePlayers.addElement(guidancePlayer);
        } catch (Exception e) {
            midlet.importantErrorLogOnly("MaintainGuidance:addPlayer " + e.toString());
        }
    }

    private Player createSoundPlayer(String filename, String format, boolean userDefined) {
        Player p = null;
        try {
            if (userDefined) {
                p = Manager.createPlayer(filename);
                p.prefetch();
            } else {
                InputStream is = getClass().getResourceAsStream(filename);
                p = Manager.createPlayer(is, format);
                p.prefetch();
            }
        } catch (IOException ex) {
           midlet.importantErrorLogOnly("MaintainGuidance:createSoundPlayer " + ex.toString());
        } catch (MediaException ex) {
           midlet.importantErrorLogOnly("MaintainGuidance:createSoundPlayer " + ex.toString());
        } catch (Exception e) {
           midlet.importantErrorLogOnly("MaintainGuidance:createSoundPlayer " + e.toString());
        }

        return p;
    }

    public synchronized void start() {
        try {
            //maintainPlayerList(midlet.getGPSInfo().osGridRef);
            maintainGuidanceThread = new Thread(this);
            maintainGuidanceThread.start();
        } catch (Exception e) {
            midlet.importantErrorLogOnly("MaintainGuidance:start " + e.toString());
        }
    }

    public synchronized void stop() {
        try {
            maintainGuidanceThread = null;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("MaintainGuidance:stop " + e.toString());
        }
    }

    public void run() {
        try {
            Thread currentThread = Thread.currentThread();

            while (maintainGuidanceThread == currentThread) {
                long startTime = System.currentTimeMillis();
                if (!maintainPlayers && midlet.getGPSInfo().getFix() && gimage.guidancePoints.size() > 0 && gimage.registered) {
                    maintainPlayers = true;
                    maintainPlayers(midlet.getGPSInfo().osGridRef);
                    maintainPlayers = false;
                }
                long timeTaken = System.currentTimeMillis() - startTime;
                if (timeTaken < MILLIS_PER_TICK) {
                    synchronized (this) {
                        wait(MILLIS_PER_TICK - timeTaken);
                    }
                } else {
                    Thread.yield();
                }
            }

            stop();
        } catch (Exception e) {
            midlet.importantErrorLogOnly("MaintainGuidance:run " + e.toString());
        }
    }
}
