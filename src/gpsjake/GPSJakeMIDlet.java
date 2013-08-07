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
 * 
 * GPSJake is a J2ME application.  It allows the user to spatially register an OS map image, show their position on the map using GPS and provides various navigation functionality including the defination of prompts played at way points and a trip computer.
 *
 * Functionality as follows:
 * 1. Can read GPS from a bluetooth device, existing NMEA file or internal GPS.
 * 2. Open and display a map image (bmp/png/jpg).
 * 3. Register a map image spatially be adding control points of known geographic position (i.e. know eastings and northings).
 * 4. Edit control point list.
 * 5. Displays clock on top of map image (can be hidden).
 * 6. Displays GPS status on top of map image (can be hidden).
 * 7. Pan image using cursor key / number keys.
 * 8. Switch to crosshair mode so that a crosshair is moved over the map in order to interact with the map.
 * 9. Show the map display in full screen mode.
 * 10. Zoom into the map.
 * 11. Position guidance points on the map and a select or record a prompt to be played when the appropriate location is reached.
 * 12. Edit guidance point list.
 * 13. Import a GPX file so the course is shown on top of the map.
 * 14. Turn on an off course alarm so that the user is warned if their position strays to far from the course.
 * 15. Show a compass displaying heading as pointer on compass rose, position as latitude and longitude, position as eastings and northings.
 * 16. Show a trip computer displaying Trip Distance, Trip Time, Speed, Average Speed, Maximum Speed, Height Gained, Height Lost.
 * 17. Trip computer can be paused, reset, display units in imperial/metric.
 * 18. Save settings to an xml file (given same name as image file).  Settings that are saved are control points and guidance points.
 * 19. Define an image quick list which assigns a map image to a number - this allows for the map image currently being displayed to be quickly switched.
 * 20. Save image quicklist (as recordstore).
 * 21. Record trip as NMEA file.
 * 22. Record trip as GPX file.
 * 23. Record application log for debug purposes.
 * 
 * author: mglynn
 *
 * classname: GPSJakeMIDlet
 *
 * desc: Main midlet/controller class for GPSJake.
 *
 * Interface flow is controlled via this class.
 *
 * Thread list:
 *    Guidance thread - monitors and plays guidance when necessary.
 *    Maintain guidance thread - controls the caching of guidance players.
 *    Off course alarm thread - monitors and plays the off course alarm when necessary.
 *    GPS threads:
 *       NMEA thread - used when reading GPS information from an NMEA file
 *       Internal GPS thread - used when using internal GPS
 *       BT GPS thread -  used when reading from a Bluetooth GPS.
 */

package gpsjake;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.Vector;
import java.util.Date;
import java.util.Calendar;
import geo.ControlPoint;
import geo.GeoImage;
import geo.GPSInfo;
import geo.GuidancePoint;
import serialgps.*;

import bluetooth.BTComms;
import xml.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;
import java.io.*;
import javax.microedition.media.*;
import internalgps.*;
import javax.microedition.location.LocationProvider;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
/**
 *
 * @author glynnm
 */
public class GPSJakeMIDlet extends MIDlet implements Runnable {
    
    private boolean initDone = false;
    private MainMenu mainMenu;
    private ImageFileBrowser imageFileBrowser;
    private NMEAFileBrowser nmeaFileBrowser;
    private NMEALogBrowser nmeaLogBrowser;
    private GPXLogBrowser gpxLogBrowser;
    private GPXFileBrowser gpxFileBrowser;
    private GPXReader gpxReader;
    private DebugBrowser debugBrowser;
    private SettingsMenu settingsMenu; 
    private AboutForm aboutForm;
    private GuidanceSettings guidanceSettings;
    private LoggingMenu loggingMenu;
    private GPSMenu gpsMenu;
    private GPSCanvas gpsCanvas;
    private GeoImage geoImage;
    private GPSInfo gpsInfo;
    private ImageLoader imageLoader;
    private ShowGPS showGPS;
    private BTGPSSetup btGPSSetup;
    private BTGPSList btGPSList;
    private SerialGPS serialGPS;
    private BTComms btComms;
    public BTGPS btGPS;
    public FileGPS fileGPS;
	public InternalGPSListener internalGPS;
    public DebugMenu debugMenu;
    public ControlPointScreen controlPointScreen;
    public ControlPointList controlPointList;
    public GuidancePointScreen guidancePointScreen;
    public GuidanceList guidanceList;
    public GuidancePointList guidancePointList;
    public RecordSoundMenu recordSoundMenu;    
    public RecordGauge recordGauge;
    public RecordSave recordSave;
    public String debugFile;
    private boolean debug;
    private boolean logMinorErrors;
    private PrintStream debugFps;
    private FileConnection debugFileConn;
    private OutputStream debugFos;
    private Alert alert;
    private Guidance guidance;
    private Player alertPlayer;
    private final String ALERT_RES="/res/Beep.wav";
    private MaintainGuidance maintainGuidance;
    private OffCourseAlarm offCourseAlarm;
    public Vector guidancePlayers = new Vector();
    public String gpsSource = "";    
    public Vector imgQLContents = new Vector();
    public ImageQuicklist imgQL;
    public ImageQuicklistBrowser imgQLFileBrowser;
    private int qlItemToBeAdded;
    private ImageLoaderQuicklist imageLoaderQuicklist;
    private ImageLoaderLastUsed imageLoaderLastUsed;
    private static String RS_IMG_QL = "ImgQL";
    private static String RS_SETTINGS = "Settings";
    
    //Distance at which to play guidance
    private int minGuidanceDist = 20;
    private int maxGuidanceDist = 50;

    //Distance at which to sound off course alarm
    private int offCourseDist = 50;
    
    //Distance at which to GPX points are display every
    private int GPXDist = 10;
    
    public Image splashImage;
    
    public String lastImageFile;
    
    /** Creates a new instance of GPSJakeMIDlet */
    public GPSJakeMIDlet() {
        try {
            InputStream is = getClass().getResourceAsStream(ALERT_RES);
            alertPlayer = Manager.createPlayer(is, "audio/x-wav");
            alertPlayer.prefetch();
            
            readImgQLFromRecordStore();
            readSettingsFromRecordStore();
            
        } catch (IOException ex) {
            // ignore
        } catch (MediaException ex) {
            // ignore
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:GPSJakeMIDlet " + e.toString());
        }
    }
    
    public void startApp() {
        try {
            Displayable current = Display.getDisplay(this).getCurrent();
            if (current == null) {
                //Show splash screen
                Display.getDisplay(this).setCurrent(new SplashScreen(this));
            }
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:startApp " + e.toString());
        }
    }
    public void pauseApp() {
        
    }
    
    public void destroyApp(boolean unconditional) {
        
    }
    
    public void run() {
        try {
            init();
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:run " + e.toString());
        }
    }
    
    void menuListQuit() {
        //Exit app
        try {
            quit();
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:menuListQuit " + e.toString());
        }
    }
    
    void startGuidance() {
        try {
            //Create guidance object and start thread
            guidance = new Guidance(this, geoImage);
            maintainGuidance = new MaintainGuidance(this, geoImage);
            maintainGuidance.start();
            guidance.start();
        } catch (Exception e) {
            importantError("GPSJakeMIDlet:startGuidance " + e.toString(), "Couldn't start guidance.");
        }
    }

    void stopGuidance() {
        try {
            maintainGuidance.stop();
            guidance.stop();
            maintainGuidance = null;
            guidance = null;
        } catch (Exception e) {
            importantError("GPSJakeMIDlet:stopGuidance " + e.toString(), "Couldn't stop guidance.");
        }
    }
    
    void startOffCourseAlarm() {
        try {
            //Create off course alarm object and start thread
            offCourseAlarm = new OffCourseAlarm(this, geoImage);
            offCourseAlarm.start();
        } catch (Exception e) {
            importantError("GPSJakeMIDlet:startOffCourseAlarm " + e.toString(), "Couldn't start off course alarm.");
        }

    }

    void stopOffCourseAlarm() {
        try {
            offCourseAlarm.stop();
            offCourseAlarm = null;
        } catch (Exception e) {
            importantError("GPSJakeMIDlet:stopOffCourseAlarm " + e.toString(), "Couldn't stop off course alarm.");
        }
    }    
    
    private synchronized void init() {
        try {
            if (!initDone) {
                //Create main class for storing image and GPS data.
                geoImage = new GeoImage();
                gpsInfo = new GPSInfo(this);
                //Create main menu.
                mainMenu = new MainMenu(this);
                initDone = true;
            }
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:init " + e.toString());
        }
    }
    
    void quit() {
        
        //Do some cleaning up.
        
        //Stop any threads
        stopGPSThreads();
        stopGuidanceThreads();
        stopOffCourseThread();
        
        //Stop logging
        if (gpsInfo.isNMEALoggingOn()) {
            gpsInfo.turnNMEALoggingOff();
        }
        
        //Stop logging
        if (gpsInfo.isGPXLoggingOn()) {
            gpsInfo.turnGPXLoggingOff();
        }        
        
        //Stop debugging
        if (isDebugOn()) {
            turnDebugOff();
        }
        
        //Destroy some objects        
        geoImage = null;
        gpsInfo = null;
        gpsCanvas = null;
        mainMenu = null;
        
        destroyApp(false);
        notifyDestroyed();
    }
    
    void splashScreenPainted() {
        new Thread(this).start();  // start background initialization
    }
    
    void splashScreenDone() {
        init();   // if not already done      
        Display.getDisplay(this).setCurrent(mainMenu);
    }
    
    void showSettingsMenu() {
        try {
            settingsMenu = new SettingsMenu(this);
            Display.getDisplay(this).setCurrent(settingsMenu);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showSettingsMenu " + e.toString());
        }
    }
    
    void showAboutForm() {
        try {
            aboutForm = new AboutForm(this);
            Display.getDisplay(this).setCurrent(aboutForm);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showAboutForm " + e.toString());
        }
    }    
    
    void showMap() {
        try {
            //Set image swapper to null.  This will be necessary
            //where the map is to be reshown after the user has selected
            //a new image from the quicklist.
            imageLoaderQuicklist = null;
            //Set the last image loader to null. This will be necessary
            //when the last image used has been loaded on first entering the map
            //canvas.
            imageLoaderLastUsed = null;
            
            gpsCanvas.init();
            Display.getDisplay(this).setCurrent(gpsCanvas);
            gpsCanvas.start();

        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showMap " + e.toString());
        }
    }

    void backMap() {
        try {
            guidancePointList = null;
            guidancePointScreen = null;            
            gpxReader = null;
            gpxFileBrowser = null;
            controlPointScreen = null;
            controlPointList = null;
            imageLoaderQuicklist = null;
            imageLoaderLastUsed = null;
            imgQL = null;
            Display.getDisplay(this).setCurrent(gpsCanvas);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backMap " + e.toString());
        }
    }   
    
    //This function allows the user to return to the map
    //and pan to a position.  Called using show on GuidancePoint/ControlPoint screen.
    void backMap(int x, int y) {
        try {
            gpsCanvas.panImage(x, y);
            guidancePointList = null;
            guidancePointScreen = null;            
            gpxReader = null;
            gpxFileBrowser = null;
            controlPointScreen = null;
            controlPointList = null;       
            imageLoaderQuicklist = null;
            imgQL = null;            
            Display.getDisplay(this).setCurrent(gpsCanvas);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backMap " + e.toString());
        }
    }       

    void showGPXFileBrowser() {
        try {
            Vector fileTypes = new Vector();
            fileTypes.addElement("gpx");
            gpxFileBrowser = new GPXFileBrowser(this, fileTypes, "Select GPX file:");
            Display.getDisplay(this).setCurrent(gpxFileBrowser);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showGPXFileBrowser " + e.toString());
        }
    }    

    void gpxFileSelected(String gpxFile) {
        try {
            if (gpxReader == null) {
                gpxReader = new GPXReader(this, geoImage, gpxFile);
            }
            Display.getDisplay(this).setCurrent(gpxReader);
            
            //Clean up
            gpxFileBrowser = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:fileBrowserSelected " + e.toString());
        }
    }  
        
    void showMapImageFileBrowser() {
        try {
            Vector fileTypes = new Vector();
            fileTypes.addElement("bmp");
            fileTypes.addElement("png");
            fileTypes.addElement("jpg");
            imageFileBrowser = new ImageFileBrowser(this, fileTypes, "Select image file:");
            Display.getDisplay(this).setCurrent(imageFileBrowser);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showMapImageFileBrowser " + e.toString());
        }
    }
    
    void showBTSetup() {
        try {
            btComms = new BTComms(this);
            btGPSSetup = new BTGPSSetup(this, btComms);
            Display.getDisplay(this).setCurrent(btGPSSetup);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showBTSetup " + e.toString());
        }
    }
    
    void showGuidanceSettings() {
        try {
            guidanceSettings = new GuidanceSettings(this);
            Display.getDisplay(this).setCurrent(guidanceSettings);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showGuidanceSettings " + e.toString());
        }    	
    }

    void showGPS() {
        try {
            showGPS = new ShowGPS(this);
            Display.getDisplay(this).setCurrent(showGPS);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showGPS " + e.toString());
        }
    }    
    
    public void showBTDeviceList(Vector devices) {
        try {
            btGPSList = new BTGPSList(this, devices, btComms);
            Display.getDisplay(this).setCurrent(btGPSList);
            btGPSSetup = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showBTDeviceList " + e.toString());
        }
    }

    private void stopGuidanceThreads() {
        try {
            //Destroy some objects
            if (maintainGuidance != null) {
                maintainGuidance.stop();
            }
            if (guidance != null) {
                guidance.stop();
            }
            guidance = null;
            maintainGuidance = null;
        } catch (Exception e) {
            importantErrorLogOnly("GPSJakeMIDlet:stopGuidanceThreads " + e.toString());
        }
    }    
    
    private void stopOffCourseThread() {
        try {
            if (offCourseAlarm != null) {
                offCourseAlarm.stop();
            }
            offCourseAlarm = null;
        } catch (Exception e) {
            importantErrorLogOnly("GPSJakeMIDlet:stopOffCourseThread " + e.toString());
        }
    } 
    
    public void stopGPSThreads() {
        try {
            //Shut down any BT GPS thread.
            if (btGPS != null) {
                btGPS.stop();
                btGPS = null;
            }
            
            //Stop any previous thread
            if (fileGPS != null) {
                fileGPS.stop();
                fileGPS = null;
            }
   

            if (internalGPS != null) {
            	internalGPS.stop();
            	internalGPS = null;
            }
            
            gpsSource = "";
            
        } catch (Exception e) {
            importantErrorLogOnly("GPSJakeMIDlet:stopGPSThreads " + e.toString());
        }
    }
    

	public void internalGPSConnected() {
		Alert modalAlert;
		stopGPSThreads();
		gpsSource = "Internal GPS - JSR179";
		internalGPS = new InternalGPSListener(this);
		if(internalGPS.locationProvider != null) {
			internalGPS.start();
			modalAlert = new Alert("GPSJake", "Connected to internal GPS...", null, AlertType.INFO);
		} else {
			modalAlert = new Alert("GPSJake", "Unable to connect to internal GPS...", null, AlertType.INFO);            
		}
		modalAlert.setCommandListener(new CommandListener() {
			public void commandAction(Command arg0, Displayable arg1) {
				backSettingsMenu();
			}
		});
		modalAlert.setTimeout(Alert.FOREVER);
		Display.getDisplay(this).setCurrent(modalAlert);
	 
		//playAlertTone();          
	 }

    
    public void btDeviceConnected(String url) {
        try {
            stopGPSThreads();
            gpsSource = "BT GPS - " + url;                        
            btGPS = new BTGPS(this);
            btGPS.connect(url);
            btGPS.start();
            serialGPS = btGPS;
            
            btComms = null;
            
            Alert modalAlert = new Alert("GPSJake", "Connected to BT GPS...", null, AlertType.INFO);
            modalAlert.setCommandListener(new CommandListener() {
                public void commandAction(Command arg0, Displayable arg1) {
                    backGPSMenu();
                }
            });
            modalAlert.setTimeout(Alert.FOREVER);
            Display.getDisplay(this).setCurrent(modalAlert);
            playAlertTone();            

        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:btDeviceConnected " + e.toString());
        }
    }
    
    void showNMEAFileBrowser() {
        try {
            Vector fileTypes = new Vector();
            fileTypes.addElement("nmea");
            nmeaFileBrowser = new NMEAFileBrowser(this, fileTypes, "Select NMEA file:");
            Display.getDisplay(this).setCurrent(nmeaFileBrowser);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showNMEAFileBrowser " + e.toString());
        }
    }
    
    void showNMEALogBrowser() {
        try {
            nmeaLogBrowser = new NMEALogBrowser(this, "Select dir:");
            Display.getDisplay(this).setCurrent(nmeaLogBrowser);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showNMEALogBrowser " + e.toString());
        }
    }
    
    void nmeaLogBrowserSelected(String nmeaLogDir) {
        try {
            gpsInfo.nmeaLogFile = nmeaLogDir + getTimeStamp() + ".nmea";
            
            Alert modalAlert = new Alert("GPSJake", "NMEA log is: " + gpsInfo.nmeaLogFile, null, AlertType.INFO);
            modalAlert.setCommandListener(new CommandListener() {
                public void commandAction(Command arg0, Displayable arg1) {
                    backDebugMenu();
                }
            });
            modalAlert.setTimeout(Alert.FOREVER);
            Display.getDisplay(this).setCurrent(modalAlert);
            playAlertTone();             
            
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:nmeaLogBrowserSelected " + e.toString());
        }
    }

    void showGPXLogBrowser() {
        try {
            gpxLogBrowser = new GPXLogBrowser(this, "Select dir:");
            Display.getDisplay(this).setCurrent(gpxLogBrowser);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showGPXLogBrowser " + e.toString());
        }
    }
    
    void gpxLogBrowserSelected(String gpxLogDir) {
        try {
            gpsInfo.gpxLogFile = gpxLogDir + getTimeStamp() + ".gpx";

            Alert modalAlert = new Alert("GPSJake", "GPX log is: " + gpsInfo.gpxLogFile, null, AlertType.INFO);
            modalAlert.setCommandListener(new CommandListener() {
                public void commandAction(Command arg0, Displayable arg1) {
                    backLoggingMenu();
                }
            });
            modalAlert.setTimeout(Alert.FOREVER);
            Display.getDisplay(this).setCurrent(modalAlert);
            playAlertTone();      
            
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:gpxLogBrowserSelected " + e.toString());
        }
    }    
    
    void showDebugBrowser() {
        try {
            debugBrowser = new DebugBrowser(this, "Select dir:");
            Display.getDisplay(this).setCurrent(debugBrowser);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showDebugBrowser " + e.toString());
        }
    }
    
    void debugBrowserSelected(String debugDir) {
        try {
            debugFile = debugDir + getTimeStamp() + ".log";

            Alert modalAlert = new Alert("GPSJake", "Debug log is: " + debugFile, null, AlertType.INFO);
            modalAlert.setCommandListener(new CommandListener() {
                public void commandAction(Command arg0, Displayable arg1) {
                    backDebugMenu();
                }
            });
            modalAlert.setTimeout(Alert.FOREVER);
            Display.getDisplay(this).setCurrent(modalAlert);
            playAlertTone();               
            
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:debugBrowserSelected " + e.toString());
        }
    }
    
    void nmeaFileSelected(String nmeaFile) {
        try {
            stopGPSThreads();

            gpsSource = "NMEA GPS Sim - " + nmeaFile;                
            
            fileGPS = new FileGPS(this);
            fileGPS.connect(nmeaFile);
            fileGPS.start();
            serialGPS = fileGPS;

            Alert modalAlert = new Alert("GPSJake", "Reading from NMEA file...", null, AlertType.INFO);
            modalAlert.setCommandListener(new CommandListener() {
                public void commandAction(Command arg0, Displayable arg1) {
                    backGPSMenu();
                }
            });
            modalAlert.setTimeout(Alert.FOREVER);
            Display.getDisplay(this).setCurrent(modalAlert);
            playAlertTone();               
            
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:nmeaFileSelected " + e.toString());
        }
    }
    
    void fileBrowserSelected(String imageFile) {
        try {
            if (imageLoader == null) {
                imageLoader = new ImageLoader(this, imageFile, "Use " + imageFile + "?");
            }
            Display.getDisplay(this).setCurrent(imageLoader);
            
            //Clean up
            imageFileBrowser = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:fileBrowserSelected " + e.toString());
        }
    }
    
    void useSplashAsMap() {
        try {        
            geoImage.setImage(splashImage);
            gpsCanvas = null;
            gpsCanvas = new GPSCanvas(this, geoImage);
            gpsCanvas.init();
            Display.getDisplay(this).setCurrent(gpsCanvas);
            gpsCanvas.start();        
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:useSplashAsMap " + e.toString());
        }            
    }
    
    void imageLoaderOK(String filename) {
        try {
            SettingsXMLReader settingsXMLReader;
            
            geoImage.setFilename(filename);

            //Remove any control points, guidance points and gpx points
            //loaded/created for a previous image.
            geoImage.removeAllControlPoints();
            geoImage.removeAllGuidancePoints();
            geoImage.removeAllGPXPoints();
            geoImage.registered = false;
            
            lastImageFile = filename;
            //Write settings to record store
            writeSettingsToRecordStore();            
            
            try {
                //Read in settings if a settings file exists.
                settingsXMLReader = new SettingsXMLReader(this, geoImage);
                if (settingsXMLReader.fileExists()) {
                    settingsXMLReader.parseSettingsFile();
                }
            } catch (SecurityException e) {
                //Catch security exception.  Thrown if user answers 'No' to prompt
                //displayed when checking if the xml settings file exists.
                //Do nothing.
            } finally {    
                settingsXMLReader = null;
            }        
            
            gpsCanvas = null;
            gpsCanvas = new GPSCanvas(this, geoImage);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:imageLoaderOK " + e.toString());
        }
    } 

    void loadImageFromQuicklist(String imageFile) {        
        try {
            if (imageLoaderQuicklist == null) {
                imageLoaderQuicklist = new ImageLoaderQuicklist(this, imageFile, "Use " + imageFile + " from quicklist?");
            }
            Display.getDisplay(this).setCurrent(imageLoaderQuicklist);
            imgQL = null;
        } catch (Exception e) {
            importantErrorLogOnly("GPSJakeMIDlet:loadImageFromQuicklist " + e.toString());
        }
    }  
    
    void loadLastImage() {        
        try {
            if (imageLoaderLastUsed == null) {
                imageLoaderLastUsed = new ImageLoaderLastUsed(this, lastImageFile, lastImageFile + " was the last image used.  Load this image?");
            }
            Display.getDisplay(this).setCurrent(imageLoaderLastUsed);
        } catch (Exception e) {
        	importantErrorLogOnly("GPSJakeMIDlet:loadLastImage " + e.toString());
        }
    }        
    
    void addControlPoint(int x, int y) {
        try {
            controlPointScreen = new ControlPointScreen(this, x, y, geoImage);
            Display.getDisplay(this).setCurrent(controlPointScreen);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:addControlPoint " + e.toString());
        }
    }
    
    void showControlPoint(ControlPoint controlPoint, int index) {
        try {
            controlPointScreen = new ControlPointScreen(this, controlPoint, geoImage, index);
            Display.getDisplay(this).setCurrent(controlPointScreen);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showControlPoint " + e.toString());
        }
    }
    
    void showControlPointList() {
        try {
            controlPointList = new ControlPointList(this, geoImage);
            Display.getDisplay(this).setCurrent(controlPointList);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showControlPointList " + e.toString());
        }
    }
    
    void backControlPointList() {
        try {
            controlPointList.refreshPointList();
            Display.getDisplay(this).setCurrent(controlPointList);
            controlPointScreen = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backControlPointList " + e.toString());
        }
    }
    
    void showGuidancePoint(GuidancePoint guidancePoint, int index) {
        try {
            guidancePointScreen = new GuidancePointScreen(this, guidancePoint, geoImage, index);
            Display.getDisplay(this).setCurrent(guidancePointScreen);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showGuidancePoint " + e.toString());
        }
    }
    
    void showGuidancePointList() {
        try {
            guidancePointList = new GuidancePointList(this, geoImage);
            Display.getDisplay(this).setCurrent(guidancePointList);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showGuidancePointList " + e.toString());
        }
    }
    
    void showRecordSound() {
        try {        
            recordSoundMenu = new RecordSoundMenu(this);
            Display.getDisplay(this).setCurrent(recordSoundMenu);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showRecordSound " + e.toString());
        }            
    }
    
    void backRecordSound() {
        try {
            recordSave = null;
            recordGauge = null;
            recordSoundMenu.refreshMenuItems();
            Display.getDisplay(this).setCurrent(recordSoundMenu);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backRecordSound " + e.toString());
        }                
    }
    
    void showRecordGauge() {
        try {        
            recordGauge = new RecordGauge(this, recordSoundMenu);
            Display.getDisplay(this).setCurrent(recordGauge);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showRecordGauge " + e.toString());
        }                      
    }
    
    void showRecordSave() {
        try {
            recordSave = new RecordSave(this, recordSoundMenu);
            Display.getDisplay(this).setCurrent(recordSave);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showRecordSave " + e.toString());
        }            
    }
    
    void addGuidancePoint(int x, int y) {
        try {
            guidancePointScreen = new GuidancePointScreen(this, x, y, geoImage);
            Display.getDisplay(this).setCurrent(guidancePointScreen);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:addGuidancePoint " + e.toString());
        }
    }
    
    void showGuidanceList() {
        try {
            guidanceList = new GuidanceList(this);
            Display.getDisplay(this).setCurrent(guidanceList);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showGuidanceList " + e.toString());
        }
    }
    
    void backGuidanceList() {
        try {
            guidanceList.addListItems();
            Display.getDisplay(this).setCurrent(guidanceList);
            recordSoundMenu = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backGuidanceList " + e.toString());
        }
    }    
    
    void showDebugMenu() {
        try {
            debugMenu = new DebugMenu(this);
            Display.getDisplay(this).setCurrent(debugMenu);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showDebugMenu " + e.toString());
        }
    }
    
    void showLoggingMenu() {
        try {
            loggingMenu = new LoggingMenu(this);
            Display.getDisplay(this).setCurrent(loggingMenu);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showLoggingMenu " + e.toString());
        }
    }
    
    void showImgQL(boolean createMode) {
        try {
            imgQL = new ImageQuicklist(this, createMode);
            Display.getDisplay(this).setCurrent(imgQL);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showImgQL " + e.toString());
        }
    }    
    
    void saveImgQL() {
        try {
             writeImgQLToRecordStore();
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showImgQL " + e.toString());
        }
    }        
    
    void showMapImgQLFileBrowser(int mapKey) {
        try {
            qlItemToBeAdded = mapKey;
            Vector fileTypes = new Vector();
            //These should really be in a static vector or something.
            fileTypes.addElement("bmp");
            fileTypes.addElement("png");
            fileTypes.addElement("jpg");
            imgQLFileBrowser = new ImageQuicklistBrowser(this, fileTypes, "Select image file:");
            Display.getDisplay(this).setCurrent(imgQLFileBrowser);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showMapImgQLFileBrowser " + e.toString());
        }
    }    
    
    void imgQLFileSelected(String imageFile, String dir) {
        try {
            //Insert file name into vector.
            imgQLContents.setElementAt(imageFile, qlItemToBeAdded);
            imgQL.addListItems();
            Display.getDisplay(this).setCurrent(imgQL);
            //Clean up
            imgQLFileBrowser = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:imgQLFileSelected " + e.toString());
        }
    }    
    
    void backImgQL() {
        try {
            Display.getDisplay(this).setCurrent(imgQL);
            imgQLFileBrowser = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backImgQL " + e.toString());
        }
    }    

    private void deleteRecordStore(String recordStore) {
        try {
            RecordStore.deleteRecordStore(recordStore);
        } catch (RecordStoreException e) {
            importantErrorLogOnly(e.toString());
        } catch (Exception e) {
            importantErrorLogOnly(e.toString());
        }
    }

    private void writeSettingsToRecordStore() {
        
        //Attempt to write imgQLContents to MapKey recordstore.
        RecordStore rs = null;
        byte[] settingsBytes;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        
        try {
            deleteRecordStore(RS_SETTINGS);
            
            rs = RecordStore.openRecordStore(RS_SETTINGS, true);
            dout.writeUTF(lastImageFile);
            
            settingsBytes = bout.toByteArray();
            
            dout.close();
            bout.close();
            dout = null;
            bout = null;
            
            rs.addRecord(settingsBytes, 0, settingsBytes.length); 
            rs.closeRecordStore();            

        } catch (RecordStoreException e) {
            importantErrorLogOnly("GPSJakeMIDlet:writeSettingsToRecordStore " + e.toString());
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:writeSettingsToRecordStore " + e.toString());            
        }
   
    }
    
    public void readSettingsFromRecordStore() {
        //Attempt to read imgQLContents from QL (Quicklist) recordstore.
        //If the recordstore does not exist it will be created.

        RecordStore rs = null;
        byte[] settingsBytes;
        try {      
       
            rs = RecordStore.openRecordStore(RS_SETTINGS, false);
            settingsBytes =  rs.getRecord(1); 
            rs.closeRecordStore();
            
            ByteArrayInputStream bin = new ByteArrayInputStream(settingsBytes);
            DataInputStream din = new DataInputStream(bin);
           
            lastImageFile = din.readUTF();     
            
            din.close();            
            bin.close();            
            din = null;
            bin = null;
        } catch (RecordStoreException e) {
            importantErrorLogOnly("GPSJakeMIDlet:readSettingsFromRecordStore " + e.toString());
            lastImageFile = "";       
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:readSettingsFromRecordStore " + e.toString());
        }
    }    
      
    public void writeImgQLToRecordStore() {
        
        //Attempt to write imgQLContents to MapKey recordstore.
        RecordStore rs = null;
        String imageFile;
        byte[] qlBytes;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        
        try {
            deleteRecordStore(RS_IMG_QL);
            rs = RecordStore.openRecordStore(RS_IMG_QL, true);
            for (int i = 0; i < 9; i++) {
                imageFile = (String) imgQLContents.elementAt(i);
                dout.writeUTF(imageFile);
            }
            
            qlBytes = bout.toByteArray();
            
            dout.close();
            bout.close();
            dout = null;
            bout = null;
            
            rs.addRecord(qlBytes, 0, qlBytes.length);  
            rs.closeRecordStore();            

        } catch (RecordStoreException e) {
            importantErrorLogOnly("GPSJakeMIDlet:writeImgQLToRecordStore " + e.toString());
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:writeImgQLToRecordStore " + e.toString());            
        }
   
    }
    
    public void readImgQLFromRecordStore() {
        //Attempt to read imgQLContents from QL (Quicklist) recordstore.
        //If the recordstore does not exist it will be created.

        RecordStore rs = null;
        byte[] qlBytes;
        try {
            String imageFile;        
            imgQLContents.removeAllElements();            
            rs = RecordStore.openRecordStore(RS_IMG_QL, false);
            
            qlBytes = rs.getRecord(1); 
            rs.closeRecordStore();
            
            ByteArrayInputStream bin = new ByteArrayInputStream(qlBytes);
            DataInputStream din = new DataInputStream(bin);
           
            for (int i = 0; i < 9; i++) {
                imageFile = din.readUTF();     
                imgQLContents.addElement(imageFile);                
            }
            
            din.close();            
            bin.close();            
            din = null;
            bin = null;
        } catch (RecordStoreException e) {
            importantErrorLogOnly("GPSJakeMIDlet:readImgQLFromRecordStore " + e.toString());
            //Create a vector containing zero length strings
            //i.e. a blank quicklist.
            for (int i = 0; i < 9; i++) {
                imgQLContents.addElement("");
            }            
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:readImgQLFromRecordStore " + e.toString());
        }
    }    
    
    void showGPSMenu() {
        try {
            gpsMenu = new GPSMenu(this);
            Display.getDisplay(this).setCurrent(gpsMenu);
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showGPSMenu " + e.toString());
        }
    }
    
    void backGuidanceSelected(String wavFileName) {
        try {
            guidancePointScreen.setSelected(wavFileName);
            Display.getDisplay(this).setCurrent(guidancePointScreen);
            recordSave = null;
            guidanceList = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backGuidanceSelected " + e.toString());
        }
    }
    
    void backGuidancePointList() {
        try {
            guidancePointList.refreshPointList();
            Display.getDisplay(this).setCurrent(guidancePointList);
            guidancePointScreen = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backGuidancePointList " + e.toString());
        }
    }
    
    void backGuidancePoint() {
        try {
            Display.getDisplay(this).setCurrent(guidancePointScreen);
            guidanceList = null;
            recordSoundMenu = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backGuidancePoint " + e.toString());
        }
    }
    
    void backSettingsMenu() {
        try {
            Display.getDisplay(this).setCurrent(settingsMenu);
            imageFileBrowser = null;
            imageLoader = null;
            loggingMenu = null;
            guidanceSettings = null;
            gpsMenu = null;
            debugMenu = null;
            imgQL = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backSettingsMenu " + e.toString());
        }
    }
    
    void backGPSMenu() {
        try {
            Display.getDisplay(this).setCurrent(gpsMenu);
            showGPS = null;
            btGPSSetup = null;
            btGPSList = null;
            nmeaFileBrowser = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backGPSMenu " + e.toString());
        }
    }
    
    void backMainMenu() {
        try {
            Display.getDisplay(this).setCurrent(mainMenu);
            settingsMenu = null;
            aboutForm = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backMainMenu " + e.toString());
        }
    }
    
    void backDebugMenu() {
        try {
            Display.getDisplay(this).setCurrent(debugMenu);
            debugBrowser = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backDebugMenu " + e.toString());
        }
    }
    
    void backLoggingMenu() {
        try {
            Display.getDisplay(this).setCurrent(loggingMenu);
            nmeaLogBrowser = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:backLoggingMenu " + e.toString());
        }
    }
    
    public void minorError(String error) {
        try {
            if (debug && logMinorErrors) {
                debugFps.println(getTimeStamp() + " M_ERROR: " + error);
                debugFps.flush();
            }
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:minorError " + e.toString());
        }
    }
    
    public void debug(String debugStr) {
        try {
            String timeStamp = getTimeStamp();
            if (debug) {
                debugFps.println(timeStamp + " DEBUG: " + debugStr);
                debugFps.flush();
                System.out.println(timeStamp + " DEBUG: " + debugStr);
            }
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:debug " + e.toString());
        }        
    }

    public void importantErrorLogOnly(String error) {
        try {
            if (debug) {
                debugFps.println(getTimeStamp() + " I_ERROR: " + error);
                debugFps.flush();
            }
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:importantErrorLogOnly " + e.toString());
        }
    }       
    
    public void importantError(String error, String message) {
        try {
            if (debug) {
                debugFps.println(getTimeStamp() + " I_ERROR: " + error);
                debugFps.flush();
            }

            Alert modalAlert = new Alert("GPSJake", message, null, AlertType.WARNING);
            modalAlert.setTimeout(Alert.FOREVER);
            Display.getDisplay(this).setCurrent(modalAlert);
            playAlertTone();
            
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:importantError " + e.toString());
        }
    }     

    public void showMessage(String message) {
        try {
            Alert modalAlert = new Alert("GPSJake", message, null, AlertType.WARNING);
            modalAlert.setTimeout(Alert.FOREVER);
            Display.getDisplay(this).setCurrent(modalAlert);
            playAlertTone();    
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:showMessage " + e.toString());
        }
    }    
    
    public void serialGPSError(String error) {
        try {
            
            if (debug) {
                debugFps.println(getTimeStamp() + " I_ERROR: " + error);
                debugFps.flush();
            }
            
            //Set current display to main menu.
            //This prevents an unexpected exception being thrown if the
            //fatal error is displayed before a previous alert has been dismissed.
            Display.getDisplay(this).setCurrent(mainMenu);

            Alert modalAlert = new Alert("GPSJake", error, null, AlertType.WARNING);
            modalAlert.setCommandListener(new CommandListener() {
                public void commandAction(Command arg0, Displayable arg1) {
                    if (btComms != null) {
                        btComms = null;
                    }
                    if (serialGPS != null) {
                        serialGPS.stop();
                        serialGPS = null;
                    }
                    gpsInfo.reset();
                    //backSettingsMenu();
                    backMainMenu();
                }
            });
            modalAlert.setTimeout(Alert.FOREVER);
            Display.getDisplay(this).setCurrent(modalAlert);
            playAlertTone();            
            
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:serialGPSError " + e.toString());
        }
    }
    
    public void fatalError(String error) {
        stopGPSThreads();
        stopGuidanceThreads();
        stopOffCourseThread();
        
        if (debug) {
            debugFps.println(getTimeStamp() + " F_ERROR: " + error);
            debugFps.flush();
        }        

        //Set current display to main menu.
        //This prevents an unexpected exception being thrown if the
        //fatal error is displayed before a previous alert has been dismissed.
        Display.getDisplay(this).setCurrent(mainMenu);
        
        Alert modalAlert = new Alert("GPSJake", error, null, AlertType.ERROR);
        modalAlert.setCommandListener(new CommandListener() {
            public void commandAction(Command arg0, Displayable arg1) {
                quit();
            }
        });
        modalAlert.setTimeout(Alert.FOREVER);
        Display.getDisplay(this).setCurrent(modalAlert);
        playAlertTone();             
    
    }
    
    public Image createImage(String filename) {
        Image image = null;
        try {
            image = Image.createImage(filename);
        } catch (IOException e) {
            importantErrorLogOnly("GPSJakeMIDlet:createImage " + e.toString());
        } catch (Exception e) {
            importantErrorLogOnly("GPSJakeMIDlet:createImage " + e.toString());
        }
        return image;
    }
    
    void saveSettings() {
        try {
            saveXMLSettings();
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:saveSettings " + e.toString());
        }
    }
    
    public void saveXMLSettings() {
        try {
            SettingsXMLWriter xmlWriter = new SettingsXMLWriter(this, geoImage.getXMLFilename());
            xmlWriter.startWriting();
            //Control points have been added then save.
            if (geoImage.getControlPoints().size() > 0) {
                xmlWriter.writeControlPoints(geoImage.getControlPoints());
            }
            //Guidance points have been added then save.
            if (geoImage.getGuidancePoints().size() > 0) {
                xmlWriter.writeGuidancePoints(geoImage.getGuidancePoints());
            }            
            xmlWriter.stopWriting();
            xmlWriter = null;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:saveXMLSettings " + e.toString());
        }
    }
    
    public void setGeoImage(Image image) {
        geoImage.setImage(image);
    }
    
    public Image getGeoImage() {
        return geoImage.getImage();
    }
    
    public GeoImage getGeoImageObj() {
        return geoImage;
    }
    
    public GPSInfo getGPSInfo() {
        return gpsInfo;
    }
    
    public void turnDebugOn() {
        try {
            //Open log file connection
            if (debugFile != null && !debugFile.equals("")) {
                try {
                    debugFileConn = (FileConnection)Connector.open(debugFile, Connector.READ_WRITE);
                    if (debugFileConn.exists()) {
                        debugFileConn.delete();
                    }
                    debugFileConn.create();
                    debugFos = debugFileConn.openOutputStream();
                    debugFps = new PrintStream(debugFos);
                    debugFps.println("Debug session started at timestamp: " + getTimeStamp());
                    debugFps.flush();
                } catch (IOException e) {
                    fatalError(e.toString());
                }
                debug = true;
            }
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:turnDebugOn " + e.toString());
        }
    }
    
    private String getTimeStamp() {
        String hour, minute, second, day, month, year;
        try {
            long timeStamp = System.currentTimeMillis();
            Date currentDate = new Date(timeStamp);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            second = Integer.toString(calendar.get(Calendar.SECOND));
            if (second.length() < 2) second = "0" + second;
            minute = Integer.toString(calendar.get(Calendar.MINUTE));
            if (minute.length() < 2) minute = "0" + minute;
            hour = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
            if (hour.length() < 2) hour = "0" + hour;
            day = Integer.toString(calendar.get(Calendar.DATE));
            if (day.length() < 2) day = "0" + day;
            month = Integer.toString(calendar.get(Calendar.MONTH) + 1);
            if (month.length() < 2) month = "0" + month;
            year = Integer.toString(calendar.get(Calendar.YEAR));
            if (year.length() < 2) year = "0" + year;
            return day + month + year + hour + minute + second;
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:getTimeStamp " + e.toString());
            return null;
        }
    }
    
    public void turnDebugOff() {
        debug = false;
        try {
            debugFps.println("Debug session ended at timestamp: " + getTimeStamp());
            debugFps.flush();
            debugFps = null;
            debugFos.close();
            debugFileConn.close();
        } catch (Exception e) {
            fatalError("GPSJakeMIDlet:turnDebugOff " + e.toString());
        }
    }
    
    public boolean isDebugOn() {
        return debug;
    }
    
    public void turnLogMinorErrorsOn() {
        logMinorErrors = true;
    }
        
    public void turnLogMinorErrorsOff() {
        logMinorErrors = false;
    }
    
    public boolean isLogMinorErrorsOn() {
        return logMinorErrors;
    }
    
    public void playAlertTone() {
        try {
            if (alertPlayer != null) {
                try {
                    alertPlayer.stop();
                    alertPlayer.setMediaTime(0L);
                    alertPlayer.start();
                } catch (MediaException ex) {
                    importantErrorLogOnly("GPSJakeMIDlet:playAlertTone " + ex.toString());
                }
            }
        } catch (Exception e) {
            importantErrorLogOnly("GPSJakeMIDlet:playAlertTone " + e.toString());
        }
    }

	public int getMinGuidanceDist() {
		return minGuidanceDist;
	}

	public void setMinGuidanceDist(int minGuidanceDist) {
		this.minGuidanceDist = minGuidanceDist;
	}

	public int getMaxGuidanceDist() {
		return maxGuidanceDist;
	}

	public void setMaxGuidanceDist(int maxGuidanceDist) {
		this.maxGuidanceDist = maxGuidanceDist;
	}

	public int getOffCourseDist() {
		return offCourseDist;
	}

	public void setOffCourseDist(int offCourseDist) {
		this.offCourseDist = offCourseDist;
	}

	public int getGPXDist() {
		return GPXDist;
	}

	public void setGPXDist(int GPXDist) {
		this.GPXDist = GPXDist;
	}
    
}
