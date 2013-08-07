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
 * classname: NMEAParser
 *
 * desc: NMEAParser provides functions to parse an NMEA string.
 */

package geo;

import java.util.Vector;

public class NMEAParser
{
  private String nmeaPrefix = "GP";
  public final static short NMEA_MAX_CHARS = 80;  //This doesn't include the EOS
  public final static String NMEA_EOS = "\r\n";
  public final static double KNOTS_TO_MPS=0.51444;
  
  private CoordSys coordSys = new CoordSys();

  private GPSInfo gpsInfo;
  
  //Constructor
  public NMEAParser(GPSInfo gpsInfo) {
      this.gpsInfo = gpsInfo;
  }

  public String getNMEAPrefix()
  { return this.nmeaPrefix; }

  public void setNMEAPrefix(String s)
  { this.nmeaPrefix = s; }

  private static Vector splitString(String s, String delimiter) {
      
      Vector returnVector = new Vector();
      
      while(s.indexOf(delimiter) != -1) {
          returnVector.addElement(s.substring(0, s.indexOf(delimiter)));
          s = s.substring(s.indexOf(delimiter) + 1);
      }
      returnVector.addElement(s.substring(0));
      
      return returnVector;
  }
  
  public void throwNMEAParseException(String s) throws NMEAParseException {
      //This is called from SerialGPS when NMEA sentence length is exceeded
      throw new NMEAParseException(s, NMEAParseException.MINOR);      
  }
  
  public void parseNMEASentence(String s) throws NMEAParseException {
      
    //Need to check for valid NMEASentence here....should use checksum.
    //For now extract portion of string between $ (include) and * (ignore).
      
    //Check for number of fields returned by split string in each parser.
    //If incorrect throw an error that is caught by SerialGPS.
      
      
    String nmeaType = "";
    String nmeaSentence = "";
    
    try {
        //Check to to see if * follows $ and that $ and * are present
        if (s.indexOf("*") < s.indexOf("$") || s.indexOf("*") == -1 || s.indexOf("$") == -1) {
            throw new NMEAParseException("Invalid NMEA sentence: " + s, NMEAParseException.MINOR);
        }
        nmeaSentence = s.substring(s.indexOf("$"), s.indexOf("*"));    

        if (nmeaSentence.indexOf(",") != -1) {
            nmeaType = nmeaSentence.substring(3, nmeaSentence.indexOf(",", 3));
            if (nmeaType.equals("RMC")) {
                gpsInfo.type = GPSInfo.NMEA_RMC;
                parseRMC(nmeaSentence);
            } else if (nmeaType.equals("GGA")) {
                gpsInfo.type = GPSInfo.NMEA_GGA;
                parseGGA(nmeaSentence);
            } else {
                //Unknown NMEA type.
                throw new NMEAParseException("Unrecognised NMEA type: " + nmeaSentence, NMEAParseException.MINOR);
            }     
        }
    } catch (NMEAParseException e) {
        //Catch and re-throw this error so that it get's caught by SerialGPS.
        //Otherwise it get's caught by the more general Exception clause below
        //and is re-thrown as an important error.
        throw new NMEAParseException("Unrecognised NMEA type: " + nmeaSentence, NMEAParseException.MINOR);        
    } catch (Exception e) {
        throw new NMEAParseException(e.toString() + ".  Parsing failed: " + nmeaSentence, NMEAParseException.IMPORTANT);
    }

  }
  
  private void parseGGA(String s) throws NMEAParseException {
    /* e.g.
     * $GPGGA,140859.986,5300.2694,N,00216.1674,W,1,07,1.0,267.4,M,48.5,M,0.0,0000*60
     */
     Vector nmeaFields = new Vector();
     nmeaFields = splitString(s, ",");
     
     if(nmeaFields.size() != 15) {
        throw new NMEAParseException("Invalid no fields for NMEA type GGA: " + s, NMEAParseException.IMPORTANT);
     }
     
     int fixQuality = Integer.parseInt(nmeaFields.elementAt(6).toString());
     
     if (fixQuality > 0) {
       gpsInfo.setFix(true);

       double l = Double.parseDouble(nmeaFields.elementAt(2).toString());
       String latSgn = nmeaFields.elementAt(3).toString();
       double g = Double.parseDouble(nmeaFields.elementAt(4).toString());
       String lonSgn = nmeaFields.elementAt(5).toString();

       int intL = (int)l/100;    
       double m = ((l/100.0)-intL) * 100.0;
       m *= (100.0/60.0);
       l = intL + (m/100.0);
       if (latSgn.equals("S")) l *= -1.0;

       int intG = (int)g/100;
       m = ((g/100.0)-intG) * 100.0;
       m *= (100.0/60.0);
       g = intG + (m/100.0);
       if (lonSgn.equals("W")) g *= -1.0;
       
       double h = Double.parseDouble(nmeaFields.elementAt(9).toString());
       double gh = Double.parseDouble(nmeaFields.elementAt(11).toString());
       gpsInfo.setTime(nmeaFields.elementAt(1).toString());       
       int noSats = Integer.parseInt(nmeaFields.elementAt(7).toString());
       
       gpsInfo.setLatLong(l, g);
       gpsInfo.setHeight(h);
       gpsInfo.geoidHgt = gh;
       gpsInfo.numSats = noSats;

       gpsInfo.setOSGridRef(coordSys.getOSGridRef(l, g));

     } else {
       gpsInfo.setFix(false);
     }
  }  
  
  private void parseRMC(String s) throws NMEAParseException {
    /* e.g.
     * $GPRMC,004007,A,3748.410,N,12226.632,W,000.0,360.0,130102,015.7,E*6F
    */
    Vector nmeaFields = new Vector();
    nmeaFields = splitString(s, ",");
    
    //13 fields as of NMEA release 2.3.  Prior to this there was 12.
    if(nmeaFields.size() < 12  || nmeaFields.size() > 13) {
        throw new NMEAParseException("Invalid no fields for NMEA type RMC: " + s, NMEAParseException.IMPORTANT);
    }    
    
    String voidFix = nmeaFields.elementAt(2).toString();

    if (voidFix.equals("A")) {
         
       gpsInfo.setFix(true);
       double l = Double.parseDouble(nmeaFields.elementAt(3).toString());
       String latSgn = nmeaFields.elementAt(4).toString();
       double g = Double.parseDouble(nmeaFields.elementAt(5).toString());
       String lonSgn = nmeaFields.elementAt(6).toString();
     
       int intL = (int)l/100;    
       double m = ((l/100.0)-intL) * 100.0;
       m *= (100.0/60.0);
       l = intL + (m/100.0);
       if (latSgn.equals("S")) l *= -1.0;
  
       int intG = (int)g/100;
       m = ((g/100.0)-intG) * 100.0;
       m *= (100.0/60.0);
       g = intG + (m/100.0);
       if (lonSgn.equals("W")) g *= -1.0;
       
       //The speed field may not be present e.g. N95 internal GPS receiver.
       //If it is missing assign the undefined value.
       //If the speed is undefined it is not used in any trip computer calculation (GPSInfo.updateStats).
       if (!nmeaFields.elementAt(7).toString().equals("")) {
           double sp = Double.parseDouble(nmeaFields.elementAt(7).toString());
           //Speed in RMC sentence is expressed in knots - convert to metres per second.
           sp = sp * KNOTS_TO_MPS;           
           gpsInfo.setSpeed(sp);       
       } else {
           gpsInfo.setSpeed(GPSInfo.UNDEFINED);
       }
       
        //The heading field may not always be populated with a value.
        if (!nmeaFields.elementAt(8).toString().equals("")) {
            double h = Double.parseDouble(nmeaFields.elementAt(8).toString());
            gpsInfo.setHeading(h);
        } else {
            gpsInfo.setHeading(GPSInfo.UNDEFINED);
        }
       
       gpsInfo.setTime(nmeaFields.elementAt(1).toString());
       gpsInfo.setDate(nmeaFields.elementAt(9).toString());
       
       gpsInfo.setLatLong(l, g);
       
       gpsInfo.setOSGridRef(coordSys.getOSGridRef(l, g));
              
    } else {
       gpsInfo.setFix(false);
    }
 
  } 
  
}