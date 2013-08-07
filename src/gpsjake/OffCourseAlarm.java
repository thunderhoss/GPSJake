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
 * classname: OffCourseAlarm
 *
 * desc: Class defines a thread which monitors current position against GPX points.
 * If position is outside a certain threshold (image units) an alarm sounds.
 */

package gpsjake;

import geo.ImagePoint;
import geo.GeoImage;
import geo.OSGridRef;

import java.io.InputStream;
import javax.microedition.media.*;

public class OffCourseAlarm implements Runnable {

    //Thread interval is every 10 seconds.
    private static final int MILLIS_PER_TICK = 10000;
    private GPSJakeMIDlet midlet;
    private GeoImage gimage;

    //Distances in pixels
    private static final int OFFCOURSE_THRESHOLD = 20;
    private final String OFFCOURSE_ALARM_RES="/res/Radio.wav";    
    public volatile Thread offCourseAlarmThread;
    public boolean offCourseAlarm = false;

    private Player offCourseAlarmPlayer;
    
    public OffCourseAlarm(GPSJakeMIDlet midlet, GeoImage geoImage) {
        this.midlet = midlet;
        this.gimage = geoImage;
    }

    public void runOffCourseAlarm(OSGridRef gridRef) {
        try {
            double distance;

            OSGridRef gpxImagePoint;                
            int withinRangeCount=0;

            for (int i = 0; i < gimage.gpxPoints.size(); i++) {
                gpxImagePoint = (OSGridRef) gimage.gpxPoints.elementAt(i);
                distance = gpxImagePoint.distanceBetween(gridRef);
                if (distance <= midlet.getOffCourseDist()) {
                    withinRangeCount++;
                }
            }
            
            if (withinRangeCount < 1) {
               soundOffCourseAlarm();
            }
            
        } catch (Exception e) {
            midlet.fatalError("OffCourseAlarm:runGuidance " + e.toString());
        }

    }

    private void soundOffCourseAlarm() {
        try {
            //Added check so that a player isn't restarted if it's already running.
            if (offCourseAlarmPlayer != null && offCourseAlarmPlayer.getState() != Player.STARTED) {
                try {
                    offCourseAlarmPlayer.stop();
                    offCourseAlarmPlayer.setMediaTime(0L);
                    offCourseAlarmPlayer.start();
                } catch (MediaException ex) {
                    // ignore
                }
            }
        } catch (Exception e) {
            midlet.fatalError("OffCourseAlarm:startSound " + e.toString());
        }
    }

    public synchronized void start() {
        try {
            //Create off course alarm sound
            InputStream is = getClass().getResourceAsStream(OFFCOURSE_ALARM_RES);
            offCourseAlarmPlayer = Manager.createPlayer(is, "audio/x-wav");
            offCourseAlarmPlayer.prefetch();
            
            offCourseAlarmThread = new Thread(this);
            offCourseAlarmThread.start();
        } catch (Exception e) {
            midlet.fatalError("OffCourseAlarm:start " + e.toString());
        }
    } 

    public synchronized void stop() {
        try {
            offCourseAlarmThread = null;
        } catch (Exception e) {
            midlet.fatalError("OffCourseAlarm:stop " + e.toString());
        }
    }

    public void run() {
        try {
            Thread currentThread = Thread.currentThread();

            while (offCourseAlarmThread == currentThread) {
                long startTime = System.currentTimeMillis();
                //If we've got a fix and there's some guidance points then
                //run guidance maintenance.
                if (midlet.getGPSInfo().getFix() && gimage.gpxPoints.size() > 0 && gimage.registered && !offCourseAlarm) {
                    offCourseAlarm = true;
                    runOffCourseAlarm(midlet.getGPSInfo().osGridRef);
                    offCourseAlarm = false;
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
            midlet.fatalError("OffCourseAlarm:run " + e.toString());
        }
    }
}
