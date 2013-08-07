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
 * classname: InternalGPSListener
 *
 * desc: This class is used to communicate with an internal GPS receiver.
 * The processUpdate method of the UpdateHandler class populates the GPSInfo structure
 * (with a call to the NMEAParser) and needs to call the same methods called in SerialGPS.run
 * to ensure positions are logged (GPX and NMEA) etc.
 * To use the internal GPS receiver on the phone (N95) I only enabled Assisted GPS and Integrated GPS
 * in Phone Settings->General->Positioning->Positioning Methods
 */

package internalgps;

import geo.GPSInfo;
import geo.NMEAParser;
import geo.NMEAParseException;
import gpsjake.GPSJakeMIDlet;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.LocationListener;

public class InternalGPSListener implements LocationListener {

    public LocationProvider locationProvider = null;
    private UpdateHandler handler;
    private boolean done;
    public final GPSJakeMIDlet midlet;
    public NMEAParser nmeaParser;
    /** Selected criteria */
    private Criteria criteria = null;
    /** Array of free Criterias. */
    private static Criteria[] freeCriterias = null;
    

    static {
        // 1. Create pre-defined free criterias

        freeCriterias = new Criteria[2];

        Criteria crit1 = new Criteria();
        crit1.setCostAllowed(false);
        crit1.setSpeedAndCourseRequired(true);
        crit1.setAltitudeRequired(true);
        crit1.setPreferredPowerConsumption(Criteria.POWER_USAGE_MEDIUM);

        freeCriterias[0] = crit1;

        Criteria crit2 = new Criteria();
        crit2.setCostAllowed(false); // allowed to cost
        crit2.setSpeedAndCourseRequired(false);
        crit2.setAltitudeRequired(true);
        crit1.setPreferredPowerConsumption(Criteria.POWER_USAGE_MEDIUM);

        freeCriterias[1] = crit2;

    }

    public InternalGPSListener(GPSJakeMIDlet midlet) {

        this.midlet = midlet;
        try {
            nmeaParser = new NMEAParser(midlet.getGPSInfo());

            //locationProvider = LocationProvider.getInstance(criteria);
            locationProvider = autoSearch();

            //Need to do something here if locationProvider is null.

            //Set the location listener.
            //setLocationListener(LocationListener listener, int interval, int timeout, int maxage)

            //The interval is set to 1.
            //-1 indicates the default values for the timeout and maxage parameters.
            if (locationProvider != null) {
                locationProvider.setLocationListener(this, 1, -1, -1);             
            }
        } catch (Exception e) {
            midlet.fatalError("InternalGPSListener:InternalGPSListener " + e.toString());
        }        
    }

    public void start() {
        try {
            done = false;
            handler = new UpdateHandler();
            new Thread(handler).start();
        } catch (Exception e) {
            midlet.fatalError("InternalGPSListener:start " + e.toString());
        }        
    }

    public void stop() {
        try {
            done = true;
        } catch (Exception e) {
            midlet.fatalError("InternalGPSListener:stop " + e.toString());
        }          
    }

    public void locationUpdated(LocationProvider provider, Location location) {
        try {
            handler.handleUpdate(location);
        } catch (Exception e) {
            midlet.fatalError("InternalGPSListener:locationUpdated " + e.toString());
        }
    }

    public void providerStateChanged(LocationProvider provider, int newState) {
        //Should probably handle this somehow.
        try {
            midlet.importantErrorLogOnly("State changed");
        } catch (Exception e) {
            midlet.fatalError("InternalGPSListener:locationUpdated " + e.toString());
        }
    }

    public LocationProvider autoSearch() {
        LocationProvider provider;

        try {
            for (int i = 0; i < freeCriterias.length; i++) {
                criteria = freeCriterias[i];
                provider = LocationProvider.getInstance(criteria);
                if (provider != null) {
                    return provider;
                }
            }

        } catch (LocationException le) {

        } catch (Exception e) {
            midlet.fatalError("InternalGPSListener:autoSearch " + e.toString());
        }
        return null;
    }

    class UpdateHandler implements Runnable {

        private Location updatedLocation = null;
        public final static int THREAD_PAUSE = 500;

        public void run() {
            Location locationToBeHandled = null;
            try {
                while (!done) {
                    synchronized (this) {
                        if (updatedLocation == null) {
                            try {
                                wait();
                            } catch (Exception e) {
                                // Handle interruption
                            }
                        }
                        locationToBeHandled = updatedLocation;
                        updatedLocation = null;
                    }
                    // The benefit of the MessageListener is here.
                    // This thread could via similar triggers be
                    // handling other kind of events as well in
                    // addition to just receiving the location updates.
                    // glynnm: Maybe handle changes in state here too.
                    if (locationToBeHandled != null) {
                        processUpdate(locationToBeHandled);
                    }
                }
            } catch (Exception e) {
                midlet.fatalError("InternalGPSListener:UpdateHandler:run " + e.toString() + ". Error reading GPS.");               
            }
            stop();
            midlet.getGPSInfo().reset();            
        }

        public synchronized void handleUpdate(Location update) {
            try {
                updatedLocation = update;
                notify();
            } catch (Exception e) {
                midlet.fatalError("InternalGPSListener:UpdateHandler:handleUpdate " + e.toString());
            }
        }

        private void processUpdate(Location update) {
            //This method get's called (fingers crossed) every second.
            //This is determined by the interval parameter passed to setLocationListener.
            //This is assigned in the class constructor.
            
            String bufferAsString = "";
            String nmeaStrings;
            
            try {
                
                nmeaStrings = update.getExtraInfo("application/X-jsr179-location-nmea");
                
                if (nmeaStrings == null) {
                    throw new NMEAParseException("InternalGPSListener:UpdateHandler:processUpdate:nmeaStrings is null.", NMEAParseException.IMPORTANT);
                }            
                
                //If the nmeaStrings don't contain the NMEA_EOS characters then put them
                //in.  I noticed that (after a firmware upgrade?) the internal GPS receiver on the N95
                //was dropping these characters.
                if (nmeaStrings.indexOf(NMEAParser.NMEA_EOS) < 0 && nmeaStrings.indexOf("$") > -1) {
                    nmeaStrings = insert_NMEA_EOS(nmeaStrings);        
                }
                
                while (nmeaStrings.indexOf(NMEAParser.NMEA_EOS) > -1) {               
                    bufferAsString = nmeaStrings.substring(nmeaStrings.indexOf("$"), nmeaStrings.indexOf(NMEAParser.NMEA_EOS));
                    try {
                        //If logging is on write the string to the log file
                        if (midlet.getGPSInfo().isNMEALoggingOn()) {
                            midlet.getGPSInfo().getNMEAPrintStream().print(bufferAsString + NMEAParser.NMEA_EOS);
                        }
                
                        //Write GPX point if we have a fix, logging is turned on and we have travelled the required
                        //minimum distance (reduces number of points written to file).
                        if (midlet.getGPSInfo().isGPXLoggingOn() && midlet.getGPSInfo().getFix()
                                        && midlet.getGPSInfo().getDistance() >= GPSInfo.GPX_EXPORT_THRESHOLD) {

                            midlet.getGPSInfo().getGPXXMLWriter().writeTrackPoint();

                        }

                        nmeaParser.parseNMEASentence(bufferAsString);

                        if (midlet.getGPSInfo().type == GPSInfo.NMEA_RMC) {
                            midlet.getGPSInfo().updateStats();
                            //May have to pause thread here?
                            //long elapsedTime = System.currentTimeMillis() - startTime;
                            //sleepThread(elapsedTime);
                        }                        
                        
                    } catch (NMEAParseException e) {
                        if (e.severity == NMEAParseException.IMPORTANT) {
                            midlet.importantErrorLogOnly(e.toString());
                        } else {
                            midlet.minorError(e.toString());
                        }
                    } finally {
                        nmeaStrings = nmeaStrings.substring(nmeaStrings.indexOf(NMEAParser.NMEA_EOS) + NMEAParser.NMEA_EOS.length());
                    }
                }
            } catch (NMEAParseException e) {
                midlet.importantErrorLogOnly(e.toString());
                midlet.getGPSInfo().setFix(false);
                midlet.getGPSInfo().updateStats();
            } catch (Exception e) {
                midlet.fatalError("InternalGPSListener:UpdateHandler:processUpdate" + e.toString() +
                        ". Error reading GPS: "  + bufferAsString);
            }
        }
        
        private String insert_NMEA_EOS(String string) {
            //Finds every occurence of a dollar sign (start of a NMEA sentence)
            //and precedes it with the NMEA_EOS characters.
            
            String outputString = "";
            
            try {
                for (int i = 0; i < string.length(); i++) {
                    if (string.charAt(i) =='$') {
                        outputString = outputString + NMEAParser.NMEA_EOS + "$";
                    } else {
                        outputString = outputString + string.charAt(i);
                    }
                }

                if (outputString.startsWith(NMEAParser.NMEA_EOS)) {
                    outputString = outputString.substring(NMEAParser.NMEA_EOS.length(), outputString.length());
                }

                if (!outputString.endsWith(NMEAParser.NMEA_EOS)) {
                    outputString = outputString + NMEAParser.NMEA_EOS;
                }

                return outputString;
            } catch (Exception e) {
                midlet.fatalError("InternalGPSListener:UpdateHandler:insert_NMEA_EOS:" + e.toString());
                return null;
            }
        }
        
//        public void sleepThread(long millis) {
//
//        }        
    }
}
