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
 * classname: Guidance
 *
 * desc: Class sets up a thread and constantly monitors position.
 * Plays guidance at appropriate position.
 */

package gpsjake;

import geo.GuidancePoint;
import geo.GeoImage;
import geo.OSGridRef;

import javax.microedition.media.*;

public class Guidance implements Runnable {

    private static final int MILLIS_PER_TICK = 1000;
    private GPSJakeMIDlet midlet;
    private GeoImage gimage;
    private GuidancePoint guidancePnt;

    //Distances in metres
    //private static final int MIN_GUID_DIST = 10;
    //private static final int MAX_GUID_DIST = 50;
    //Time in milliseconds
    private static final int GUID_TIME_BETWEEN = 10000;
    public volatile Thread guidanceThread;
    public boolean runGuidance = false;

    public Guidance(GPSJakeMIDlet midlet, GeoImage geoImage) {
        this.midlet = midlet;
        this.gimage = geoImage;
    }

    private Player getPlayer(String fileName) {
        GuidancePlayer guidancePlayer;
        try {
            for (int i = 0; i < midlet.guidancePlayers.size(); i++) {
                guidancePlayer = (GuidancePlayer) midlet.guidancePlayers.elementAt(i);
                if (guidancePlayer.getFileName().equals(fileName)) {
                    return guidancePlayer.getPlayer();
                }
            }
            return null;
        } catch (Exception e) {
            midlet.fatalError("Guidance:getPlayer " + e.toString());
            return null;
        }
    }

    public void runGuidance(OSGridRef gridRef) {
        //Checks position and plays guidance.  Called from thread.
        try {
            
        	OSGridRef guidanceGridRef = new OSGridRef();

            for (int i = 0; i < gimage.guidancePoints.size(); i++) {
                guidancePnt = (GuidancePoint) gimage.guidancePoints.elementAt(i);
                guidanceGridRef.Eastings = guidancePnt.eastings;
                guidanceGridRef.Northings = guidancePnt.northings;

                double distance = guidanceGridRef.distanceBetween(gridRef);

                //If the point is within the guidance distance range (GPSJakeMIDlet),
                //we're approaching it and the associated guidance hasn't been
                //played in the last GUID_TIME_BETWEEN seconds then play the sound
                long currentTime = System.currentTimeMillis();
                boolean playedRecently = guidancePnt.playedRecently(currentTime, GUID_TIME_BETWEEN);
                boolean approachingGP = guidancePnt.approaching(distance);

                if (approachingGP && (distance >= midlet.getMinGuidanceDist() && distance < midlet.getMaxGuidanceDist()) && !playedRecently) {
                    //startSound(guidancePnt.player);
                    Player player = getPlayer(guidancePnt.wavFileName);
                    if (player != null) {
                        guidancePnt.timePlayed = currentTime;
                        startSound(getPlayer(guidancePnt.wavFileName));
                    }
                }
            }
        } catch (Exception e) {
            midlet.fatalError("Guidance:runGuidance " + e.toString());
        }

    }

    private void startSound(Player player) {
        try {
            //Added check so that a player isn't restarted if it's already running.
            if (player != null && player.getState() != Player.STARTED) {
                try {
                    player.stop();
                    player.setMediaTime(0L);
                    player.start();
                } catch (MediaException ex) {
                    // ignore
                }
            }
        } catch (Exception e) {
            midlet.fatalError("Guidance:startSound " + e.toString());
        }
    }

    public synchronized void start() {
        try {
            guidanceThread = new Thread(this);
            guidanceThread.start();
        } catch (Exception e) {
            midlet.fatalError("Guidance:start " + e.toString());
        }
    } 

    public synchronized void stop() {
        try {
            guidanceThread = null;
        } catch (Exception e) {
            midlet.fatalError("Guidance:stop " + e.toString());
        }
    }

    public void run() {
        try {
            Thread currentThread = Thread.currentThread();

            while (guidanceThread == currentThread) {
                long startTime = System.currentTimeMillis();
                //If we've got a fix and there's some guidance points then
                //run guidance maintenance.
                if (midlet.getGPSInfo().getFix() && gimage.guidancePoints.size() > 0 && gimage.registered && !runGuidance) {
                    runGuidance = true;
                    runGuidance(midlet.getGPSInfo().osGridRef);
                    runGuidance = false;
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
            midlet.fatalError("Guidance:run " + e.toString());
        }
    }
}
