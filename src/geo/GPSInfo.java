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
 * classname: GPSInfo
 *
 * desc: This class stores the current GPS data and other calculated data such as trip stats.
 * Provides methods to return this information in various formats.
 */

package geo;

import gpsjake.*;
import xml.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;

public class GPSInfo {

    //Used for setting angle fields to a non-angular value.
    //i.e. something not in the range 0 - 360
    public final static int UNDEFINED = -9999;
    
    public final static byte NMEA_RMC = 0;
    public final static byte NMEA_GGA = 1;
    
    public final static double METRES_TO_MILES = 0.0006214;
    public final static double METRES_TO_FEET = 3.28084;
    public final static double MPS_TO_MPH = 2.23694;
    public final static double MPS_TO_KPH = 3.6;
    
    //Currently set this to zero.  May want to offer as configurable option
    //in future?  This unit is in metres.
    public final static int GPX_EXPORT_THRESHOLD = 25;
    
    private double lat;  //In decimal degress
    private double lng;  //In decimal degress
    private double hgt;  //In metres
    
    private double heading = UNDEFINED; //Heading in decimal degrees
    
    private byte day, month, year, hour, minute, second;
    private double spd = UNDEFINED;  //In metres per second    
    private boolean fix = false;
    public double geoidHgt;
    public int numSats;
    public byte type;
   
    public OSGridRef osGridRef = new OSGridRef();
        
    //Trip meter variables.  All times in seconds since Jan 1 1970 (the epoch).
    //All distance in metres.
    private double totalTimeNoFix = 0, totalTimeWithFix = 0, timeOfLastUpdate = 0, timeSinceLastUpdate = 0;
    private double totalDistanceWithFix = 0, totalDistanceNoFix = 0;
    
    //Distance travelled between GPS readings (in metres).
    private double distance;
    
    private double heightGained;
    private double heightLost;
    
    private double aveSpd;
    private double maxSpd;
    private boolean firstHgt = true;    
    public double previousHgt;
    public double hgtGained;
    public double hgtLost;    
    
    private boolean firstLatLong = true;     
    public double previousLat;
    public double previousLong;
    
    private boolean nmeaLog;
    private PrintStream nmeaFps;
    private FileConnection nmeaFileConn;
    private OutputStream nmeaFos;
    public String nmeaLogFile;
    
    private boolean gpxLog;
    private PrintStream gpxFps;
    private FileConnection gpxFileConn;
    private OutputStream gpxFos;
    public String gpxLogFile;
    
    private GPXXMLWriter gpxXMLWriter;
    
    private GPSJakeMIDlet midlet;
    
    public GPSInfo(GPSJakeMIDlet midlet) {
        this.midlet = midlet;
    }
    
    public GPXXMLWriter getGPXXMLWriter() {
        return gpxXMLWriter;
    }
    
    public PrintStream getNMEAPrintStream() {
        return nmeaFps;
    }
    
    public PrintStream getGPXPrintStream() {
        return gpxFps;
    }
    
    public void turnNMEALoggingOn() {
        //Open nmeaLog file connection
        if (nmeaLogFile != null && !nmeaLogFile.equals("")) {
            try {
                nmeaFileConn = (FileConnection)Connector.open(nmeaLogFile, Connector.READ_WRITE);
                if (nmeaFileConn.exists()) {
                    nmeaFileConn.delete();
                }
                nmeaFileConn.create();
                nmeaFos = nmeaFileConn.openOutputStream();
                nmeaFps = new PrintStream(nmeaFos);
            } catch (IOException e) {
                midlet.fatalError("Unable to turn NMEA logging on.");
            } catch (Exception e) {
                midlet.fatalError("GPSInfo:turnNMEALoggingOn " + e.toString());
            }
            nmeaLog = true;
        }
    }
    
    public void turnNMEALoggingOff() {
        nmeaLog = false;
        try {
            nmeaFps = null;
            nmeaFos.close();
            nmeaFileConn.close();
        } catch (IOException e) {
            midlet.fatalError("Unable to turn NMEA logging off.");
        } catch (Exception e) {
            midlet.fatalError("GPSInfo:turnNMEALoggingOff " + e.toString());
        }
    }
    
    public boolean isNMEALoggingOn() {
        return nmeaLog;
    }
    
    public void turnGPXLoggingOn() {
        //Open gpxLog file connection
        if (gpxLogFile != null && !gpxLogFile.equals("")) {
            try {
                gpxFileConn = (FileConnection)Connector.open(gpxLogFile, Connector.READ_WRITE);
                if (gpxFileConn.exists()) {
                    gpxFileConn.delete();
                }
                gpxFileConn.create();
                gpxFos = gpxFileConn.openOutputStream();
                gpxFps = new PrintStream(gpxFos);
                gpxXMLWriter = new GPXXMLWriter(this.midlet);
                gpxXMLWriter.writeHeader();
            } catch (IOException e) {
                midlet.fatalError("Unable to turn GPX logging on.");
            } catch (Exception e) {
                midlet.fatalError("GPSInfo:turnGPXLoggingOn " + e.toString());
            }
            gpxLog = true;
        }
    }
    
    public void turnGPXLoggingOff() {
        gpxLog = false;
        try {
            gpxXMLWriter.writeFooter();
            gpxXMLWriter = null;
            gpxFps = null;
            gpxFos.close();
            gpxFileConn.close();
        } catch (IOException e) {
            midlet.fatalError("Unable to turn GPX logging off.");
        } catch (Exception e) {
            midlet.fatalError("GPSInfo:turnGPXLoggingOff " + e.toString());
        }
    }
    
    public boolean isGPXLoggingOn() {
        return gpxLog;
    }
    
    public void reset() {
        //reset() is called when GPS reads have stopped.
        //Ensures that no GPS strings are reported when not reading
        //from a receiver.
        try {    
            midlet.gpsSource = "";
            this.fix = false;            
        } catch (Exception e) {
            midlet.fatalError("GPSInfo:reset " + e.toString());
        }             
    }   
    
    public void setFix(boolean fix) {
        try {    
            this.fix = fix;         
        } catch (Exception e) {
            midlet.fatalError("GPSInfo:setFix " + e.toString());
        }           
    }
    
    public void updateStats() {
        //This should get called approx once per second
        //once an RMC sentence has been parsed.
        //RMC sentences are output every second by B10.
        double currentTime=System.currentTimeMillis() / 1000.0;        
        
        //Don't do anything on 1st update.        
        if (timeOfLastUpdate == 0) {
            timeOfLastUpdate = currentTime;
        } else {
            timeSinceLastUpdate = currentTime - timeOfLastUpdate;
            timeOfLastUpdate = currentTime; 
            
            //Possible to have a fix but have no speed info.  (Particularly when using
            //an internal GPS receiver e.g. N95.
            if (fix && spd != GPSInfo.UNDEFINED) {
                totalTimeWithFix = totalTimeWithFix + timeSinceLastUpdate;
                distance = spd * timeSinceLastUpdate;
                totalDistanceWithFix = totalDistanceWithFix + distance;
                aveSpd = totalDistanceWithFix / totalTimeWithFix;
                //Only update the height stats if receiver is moving.
                if (spd > 0) {
                    if (hgt < previousHgt) {
                        hgtLost = hgtLost + (previousHgt - hgt);
                    } else {
                        hgtGained = hgtGained + (hgt - previousHgt);
                    }
                }
            } else {
                totalTimeNoFix = totalTimeNoFix + timeSinceLastUpdate;
                //Assume average speed is maintained when there is no fix.
                distance = aveSpd * timeSinceLastUpdate;
                totalDistanceNoFix = totalDistanceNoFix + distance;   
            }
        } 
    }
     
    public void setSpeed(double sp) {
        try {            
            this.spd = sp;
            if (sp > maxSpd) {
                maxSpd = sp;
            }
        } catch (Exception e) {
            midlet.fatalError("GPSInfo:setSpeed " + e.toString());
        }             
    }
    
    public void setHeight(double h) {
        if (firstHgt) {
            hgt = h;
            previousHgt = hgt;
            firstHgt = false;
        } else {
            previousHgt = hgt;
            hgt = h;
        }
    }
    
    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lng;
    }       
    
    public double getDistance() {
        return distance;
    }
    
    public double getTotalTimeNoFix() {
        return totalTimeNoFix;
    }
    
    public void setLatLong(double l, double g) {
        if (firstLatLong) {
            lat = l;
            lng = g;
            previousLat = lat;
            previousLong = lng;
            firstLatLong = false;
        } else {
            previousLat = lat;
            previousLong = lng;
            lat = l;
            lng = g;
        }
    }
    
    public void setHeading(double h) {
        heading = h;
    }
    
    public String getHeadingDegMinDec() {
        try {
            if (fix && heading != UNDEFINED) {
                String headingStr = new Double(heading).toString();
                headingStr = noDps(headingStr, (byte)3);
                
                return headingStr + "°";
            } else {
                return "---°";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getHeadingDegMinDec " + e.toString());
            return "---°";
        }
    }
    
    public double getHeading() {
        return heading;
    }
    
    public void setOSGridRef(OSGridRef gridRef) {
        osGridRef = gridRef;
    }
    
    public void resetTrip() {
        timeOfLastUpdate = 0;
        hgtLost = 0;
        hgtGained = 0;
        firstHgt = true;
        totalTimeWithFix = 0;
        totalTimeNoFix = 0;
        totalDistanceWithFix = 0;
        totalDistanceNoFix = 0;
        maxSpd = 0;
    }
    
    public String getLatInDegMinDec() {
        try {
            if (fix) {
                int degree = (int)lat;
                String degreeStr = new Integer(degree).toString();
                String sgn = (degree>=0)?"N":"S";
                double minutes = Math.abs(lat - degree);
                double hexMin = 100.0 * minutes * (6.0/10.0);
                String hexMinStr = new Double(hexMin).toString();
                hexMinStr = noDps(hexMinStr, (byte)3);
                
                return degreeStr + "° " + hexMinStr + "' " + sgn;
            } else {
                return "---°--.--' -";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getLatInDegMinDec " + e.toString());
            return "---°--.--' -";
        }
    }
    
    public String getLngInDegMinDec() {
        try {
            if (fix) {
                int degree = (int)lng;
                String degreeStr = new Integer(degree).toString();
                String sgn = (degree>=0)?"E":"W";
                double minutes = Math.abs(lng - degree);
                double hexMin = 100.0 * minutes * (6.0/10.0);
                String hexMinStr = new Double(hexMin).toString();
                hexMinStr = noDps(hexMinStr, (byte)3);
                
                return degreeStr + "° " + hexMinStr + "' " + sgn;
            } else {
                return "---°--.--' -";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getLngInDegMinDec " + e.toString());
            return "---°--.--' -";
        }
    }
    
    public String getSpdMph() {
        try {
            if (fix && spd != UNDEFINED) {
                String spdStr = new Double(spd * MPS_TO_MPH).toString();
                spdStr = noDps(spdStr, (byte)2);
                return spdStr + " mph";
            } else {
                return "---.-- mph";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getSpdInMph " + e.toString());
            return "---.-- mph";
        }
    }

    public String getSpdKph() {
        try {
            if (fix && spd != UNDEFINED) {
                String spdStr = new Double(spd * MPS_TO_KPH).toString();
                spdStr = noDps(spdStr, (byte)2);
                return spdStr + " kph";
            } else {
                return "---.-- kph";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getSpdInKph " + e.toString());
            return "---.-- kph";
        }
    }    
    
    public String getHgtM() {
        try {
            if (fix) {
                String hgtStr = new Double(hgt).toString();
                hgtStr = noDps(hgtStr, (byte)2);
                return hgtStr + " m";
            } else {
                return "---.-- m";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getHgtM " + e.toString());
            return "---.-- m";
        }
    }
    
    public String getHgtGainedM() {
        try {
            if (fix) {
                String hgtStr = new Double(hgtGained).toString();
                hgtStr = noDps(hgtStr, (byte)2);
                return hgtStr + " m";
            } else {
                return "---.-- m";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getHgtGainedM " + e.toString());
            return "---.-- m";
        }
    }    
    
    public String getHgtLostM() {
        try {
            if (fix) {
                String hgtStr = new Double(hgtLost).toString();
                hgtStr = noDps(hgtStr, (byte)2);
                return hgtStr + " m";
            } else {
                return "---.-- m";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getHgtLostM " + e.toString());
            return "---.-- m";
        }
    }
    
    public String getHgtFt() {
        try {
            if (fix) {
                String hgtStr = new Double(hgt * METRES_TO_FEET).toString();
                hgtStr = noDps(hgtStr, (byte)0);
                return hgtStr + " ft";
            } else {
                return "--- ft";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getHgtFt " + e.toString());
            return "--- ft";
        }
    }
    
    public String getHgtGainedFt() {
        try {
            if (fix) {
                String hgtStr = new Double(hgtGained * METRES_TO_FEET).toString();
                hgtStr = noDps(hgtStr, (byte)0);
                return hgtStr + " ft";
            } else {
                return "--- ft";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getHgtGainedFt " + e.toString());
            return "--- ft";
        }
    }    
    
    public String getHgtLostFt() {
        try {
            if (fix) {
                String hgtStr = new Double(hgtLost * METRES_TO_FEET).toString();
                hgtStr = noDps(hgtStr, (byte)0);
                return hgtStr + " ft";
            } else {
                return "--- ft";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getHgtLostFt " + e.toString());
            return "--- ft";
        }
    }
    
    public String getGeoidHgtM() {
        try {
            if (fix) {
                String geoidHgtStr = new Double(geoidHgt).toString();
                geoidHgtStr = noDps(geoidHgtStr, (byte)2);
                return geoidHgtStr + " m";
            } else {
                return "---.-- m";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getGeoidHgtM " + e.toString());
            return "---.-- m";
        }
    }
    
    public String getAveSpdMph() {
        try {
            String aveSpdStr;        
            aveSpdStr = new Double(aveSpd * MPS_TO_MPH).toString();
            aveSpdStr = noDps(aveSpdStr, (byte)2) + " mph";
            return aveSpdStr;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getAveSpdInMph " + e.toString());
            return "---.-- mph";
        }
    }

    public String getAveSpdKph() {
        try {
            String aveSpdStr;        
            aveSpdStr = new Double(aveSpd * MPS_TO_KPH).toString();
            aveSpdStr = noDps(aveSpdStr, (byte)2) + " kph";
            return aveSpdStr;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getAveSpdKph " + e.toString());
            return "---.-- kph";
        }
    }    
    
    public String getMaxSpdMph() {
        try {
            String maxSpdStr;        
            maxSpdStr = new Double(maxSpd * MPS_TO_MPH).toString();
            maxSpdStr = noDps(maxSpdStr, (byte)2) + " mph";
            return maxSpdStr;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getMaxSpdMph " + e.toString());
            return "---.-- mph";
        }
    }  
    
    public String getMaxSpdKph() {
        try {
            String maxSpdStr;        
            maxSpdStr = new Double(maxSpd * MPS_TO_KPH).toString();
            maxSpdStr = noDps(maxSpdStr, (byte)2) + " kph";
            return maxSpdStr;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getMaxSpdKph " + e.toString());
            return "---.-- kph";
        }
    }    
    
    public String getTotalTripTime() {
        try {
            double tripTime, remainder;
            int tripTimeHrs, tripTimeMins, tripTimeSecs;
            String strTripTime;
            tripTime = totalTimeNoFix + totalTimeWithFix;
            tripTimeHrs = (int)(tripTime / 3600);
            remainder = tripTime - (tripTimeHrs * 3600);
            tripTimeMins = (int)(remainder / 60);
            remainder = remainder - (tripTimeMins * 60);
            tripTimeSecs = (int)remainder;
            strTripTime = tripTimeHrs + " h " + tripTimeMins + " m " + tripTimeSecs + " s";
            
            return strTripTime;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getTripTime " + e.toString());
            return "-- h  -- m -- s";
        }
    }

    public String getFixTripTime() {
        try {
            double remainder;
            int tripTimeHrs, tripTimeMins, tripTimeSecs;
            String strTripTime;
            
            tripTimeHrs = (int)(totalTimeWithFix / 3600);
            remainder = totalTimeWithFix - (tripTimeHrs * 3600);
            tripTimeMins = (int)(remainder / 60);
            remainder = remainder - (tripTimeMins * 60);
            tripTimeSecs = (int)remainder;
            strTripTime = tripTimeHrs + " h " + tripTimeMins + " m " + tripTimeSecs + " s";
            
            return strTripTime;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getTripTime " + e.toString());
            return "-- h  -- m -- s";
        }
    }    

    public String getNoFixTripTime() {
        try {
            double remainder;
            int tripTimeHrs, tripTimeMins, tripTimeSecs;
            String strTripTime;

            tripTimeHrs = (int)(totalTimeNoFix / 3600);
            remainder = totalTimeNoFix - (tripTimeHrs * 3600);
            tripTimeMins = (int)(remainder / 60);
            remainder = remainder - (tripTimeMins * 60);
            tripTimeSecs = (int)remainder;
            strTripTime = tripTimeHrs + " h " + tripTimeMins + " m " + tripTimeSecs + " s";
            
            return strTripTime;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getTripTime " + e.toString());
            return "-- h  -- m -- s";
        }
    }    
    
    public String getTimeSinceLastUpdate() {
        try {
            String strTime;
            strTime = new Double(timeSinceLastUpdate).toString();
            strTime = noDps(strTime, (byte)2);
            strTime = strTime + " s";
            return strTime;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getDistanceTravelledInMiles " + e.toString());
            return "-.-- s";
        }
    }
    
    public String getTotalDistMiles() {
        try {
            String miles;
            miles = new Double((totalDistanceWithFix + totalDistanceNoFix) * METRES_TO_MILES).toString();
            miles = noDps(miles, (byte)2) + " miles";
            return miles;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getDistanceTravelledInMiles " + e.toString());
            return "---.-- miles";
        }
    }
    
    public String getFixDistMiles() {
        try {
            String miles;
            miles = new Double(totalDistanceWithFix * METRES_TO_MILES).toString();
            miles = noDps(miles, (byte)2) + " miles";
            return miles;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getFixDistMiles " + e.toString());
            return "---.-- miles";
        }
    }    
    
    public String getNoFixDistMiles() {
        try {
            String miles;
            miles = new Double(totalDistanceNoFix * METRES_TO_MILES).toString();
            miles = noDps(miles, (byte)2) + " miles";
            return miles;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getNoFixDistMiles " + e.toString());
            return "---.-- miles";
        }
    }    
    
    public String getTotalDistKm() {
        try {
            String metres;
            metres = new Double((totalDistanceWithFix + totalDistanceNoFix) / 1000).toString();
            metres = noDps(metres, (byte)2) + " km";
            return metres;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getTotalDistKm " + e.toString());
            return "---.-- km";
        }
    }
    
    public String getFixDistKm() {
        try {
            String metres;
            metres = new Double(totalDistanceWithFix / 1000).toString();
            metres = noDps(metres, (byte)2) + " km";
            return metres;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getFixDistKm " + e.toString());
            return "---.-- km";
        }
    }    
    
    public String getNoFixDistKm() {
        try {
            String metres;
            metres = new Double(totalDistanceNoFix / 1000).toString();
            metres = noDps(metres, (byte)2) + " km";
            return metres;
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getNoFixDistKm " + e.toString());
            return "---.-- km";
        }
    }      
    
    public String getTimeHHMMSS() {
        try {
            if (fix) {
                return leadingZeroes(hour, (byte)2) + ":" +
                        leadingZeroes(minute, (byte)2) + ":" + leadingZeroes(second, (byte)2);
            } else {
                return "--:--:-- UTC";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getTimeAsString " + e.toString());
            return "--:--:-- UTC";
        }
    }
    
    public String getDateDDMMYY() {
        try {
            if (fix) {
                return leadingZeroes(day, (byte)2) + "/" +
                        leadingZeroes(month, (byte)2) + "/" + leadingZeroes(year, (byte)2);
            } else {
                return "--/--/--";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getDateAsString " + e.toString());
            return "--/--/--";
        }
    }
    
    public String getNumSats() {
        try {
            if (fix) {
                String numSatsStr = Integer.toString(numSats);
                return numSatsStr;
            } else {
                return "--";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getNumSatsAsString " + e.toString());
            return "--";
        }
    }
    
    public String getEastingsAsStr() {
        try {
            if (fix) {
                String eastingsStr = new Double(osGridRef.Eastings).toString();
                eastingsStr = noDps(eastingsStr, (byte)0);
                return eastingsStr + " m";
            } else {
                return "------ m";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getEastingsAsString " + e.toString());
            return "------ m";
        }
    }
    
    public String getNorthingsAsStr() {
        try {
            if (fix) {
                String northingsStr = new Double(osGridRef.Northings).toString();
                northingsStr = noDps(northingsStr, (byte)0);
                return northingsStr + " m";
            } else {
                return "------ m";
            }
        } catch (Exception e) {
            midlet.importantErrorLogOnly("GPSInfo:getNorthingsAsString " + e.toString());
            return "------ m";
        }
    }
    
    public String getEastingsAsStr(double eastings) {
        String eastingsStr = new Double(eastings).toString();
        eastingsStr = eastingsStr.substring(0, (eastingsStr.indexOf(".")));
        return eastingsStr;
    }
    
    public String getNorthingsAsStr(double northings) {
        String northingsStr = new Double(northings).toString();
        northingsStr = northingsStr.substring(0, (northingsStr.indexOf(".")));
        return northingsStr;
    }
    
    public boolean getFix() {
        return fix;
    }
    
    public String getDateXSD() {
        //[-]CCYY-MM-DDThh:mm:ss[Z|(+|-)hh:mm]
        //e.g 2008-01-25T14:00:28Z
        String dt;
        String ti;
        if (fix) {
            dt = "20" + leadingZeroes(year, (byte)2) + "-" +
                    leadingZeroes(month, (byte)2) + "/" + leadingZeroes(day, (byte)2);
            ti = "T" + leadingZeroes(hour, (byte)2) + ":" +
                    leadingZeroes(minute, (byte)2) + ":" + leadingZeroes(second, (byte)2);
            return dt + ti;
        } else {
            return null;
        }
    }
    
    public void setTime(String time) {
        time = leadingZeroes(noDps(time, (byte)0), (byte)6);
        hour = Byte.parseByte(time.substring(0,2));
        minute = Byte.parseByte(time.substring(2,4));
        second = Byte.parseByte(time.substring(4,6));
    }
    
    public void setDate(String date) {
        date = leadingZeroes(date, (byte)6);
        day = Byte.parseByte(date.substring(0,2));
        month = Byte.parseByte(date.substring(2,4));
        year = Byte.parseByte(date.substring(4,6));
    }
    
    private String leadingZeroes(String str, byte length) {
        String zeroes = "";
        if (str.length() >= str.length() - length) return str;
        for (int i=0; i < length; i++) {
            zeroes = zeroes + "0";
        }
        return zeroes + str;
    }
    
    private String leadingZeroes(byte strByte, byte length) {
        String zeroes = "";
        String str = new Integer(strByte).toString();
        if (str.length() >= length) return str;
        for (int i=0; i < length - str.length(); i++) {
            zeroes = zeroes + "0";
        }
        return zeroes + str;
    }
    
    private String noDps(String str, byte noDps) {
        String zeroes = "";
        
        if(str.indexOf(".") == -1) return str;
        
        if(noDps == 0) return str.substring(0, str.indexOf("."));
        //If the string currently has less decimal places than the no required
        //add zeroes.
        if(str.substring(str.indexOf(".") + 1).length() <= noDps) {
            for (int i=0; i < noDps - str.substring(str.indexOf(".") + 1).length(); i++) {
                zeroes = zeroes + "0";
            }
            return str + zeroes;
        }
        return str.substring(0, str.indexOf(".") + 1 + noDps);
    }
    
}
