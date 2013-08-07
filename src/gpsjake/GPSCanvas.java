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
 * classname: GPSCanvas
 *
 * desc: GPSCanvas class - main screen for displaying map and providing options.
 * Extends MapCanvas.
 */

package gpsjake;

import javax.microedition.lcdui.*;

import geo.GeoImage;
import geo.ImagePoint;
import geo.GuidancePoint;
import geo.ControlPoint;
import geo.OSGridRef;

import java.util.Vector;

public class GPSCanvas extends MapCanvas implements CommandListener {

    private GPSJakeMIDlet midlet;
    private final Command backCommand;
    private final Command showGPSStatusCommand;
    private final Command hideGPSStatusCommand;
    private final Command hideCrossHairCommand;
    private final Command showCrossHairCommand;
    private final Command importGPXCommand;
    private final Command fullScreenOnCommand;
    private final Command fullScreenOffCommand;
    private final Command guidanceOnCommand;
    private final Command guidanceOffCommand;
    private final Command offCourseAlarmOnCommand;
    private final Command offCourseAlarmOffCommand;
    private final Command zoomOutCommand;
    private final Command zoomInCommand;
    private final Command showClockCommand;
    private final Command hideClockCommand;
    private final Command showOSNoticeCommand;
    private final Command hideOSNoticeCommand;   
    private final Command moveImageCommand;
    private final Command moveCrossHairCommand;
    private final Command centreImageCommand;    
    private final Command restoreZoomCommand;    
    private final Command addGuidancePointCommand;
    private final Command showGuidancePoints;
    private final Command addControlPointCommand;
    private final Command showControlPoints;
    private final Command qlCommand;
    private final Command showCompassCommand;
    private final Command hideCompassCommand;
    private final Command showTripComputerCommand;
    private final Command hideTripComputerCommand;
    
    //Trip computer commands
    private final Command resetTripStatsCommand;
    private final Command pauseTripStatsCommand;
    private final Command resumeTripStatsCommand;
    private final Command metricCommand;
    private final Command imperialCommand;    
    
    //Trip computer strings
    private String aveSpeedString, maxSpeedString;
    private String distanceString, distanceNoFixString;
    private String heightGainedString, heightLostString;
    private String updateTimeString;
    private String tripTimeString, tripTimeNoFixString;    
    private boolean paused = false;
    private boolean imperial = false;    
    
    private Image crossHairImage;
    private Image guidPntImage;
    private Image ctlPntImage;
    
    //Clock strings
    private String dateString;
    private String timeString;
    
    private Font fontSmallSystem;
    private Font fontLargeProp;
    private Font fontSmallProp;    
    private Font fontSmallPropBold;    
    
    //Top left of cross hair expressed as canvas coordinates
    private int x_cross = 0;
    private int y_cross = 0;
    //Centre of cross hairs expressed as image coordinates (not canvas coordinates)
    private int real_x_cross;
    private int real_y_cross;
    private int crossHairImageWidth;
    private int crossHairImageHeight;
    private int guidPntImageWidth;
    private int guidPntImageHeight;
    private int ctlPntImageWidth;
    private int ctlPntImageHeight;
    private final int CROSS_MOVEMENT_IN_X = 1;
    private final int CROSS_MOVEMENT_IN_Y = 1;
    //Boolean values defined below are used to determine which
    //menu items should be displayed.
    private boolean moveCrossHairMode = false;
    private boolean showCrossHair = true;
    private boolean showGPSStatus = true;
    private boolean fullScreenMode = false;
    private boolean guidanceOn = false;
    private boolean offCourseAlarmOn = false;
    //image moves with GPS
    private boolean moveWithGPSMode;
    private boolean showClock = true;
    private boolean showOSNotice = true;
    private boolean fixImagePressed = false;
    private boolean zoomOutImagePressed = false;
    private boolean zoomInImagePressed = false;
    private boolean minmaxImagePressed = false;
        
    //Vector of all the commands.  Used to remove them when refreshing the command list.
    private Vector allCommands = new Vector();
    private Image posnImage;
    private int posnImageWidth;
    private int posnImageHeight;
    private Image noFixImage;
    private Image fixImage;
    
    private Image zoomOutImage;
    private int zoomOutImageWidth;
    private int zoomOutImageHeight;
    private int zoomOutImageCanvasX;
    private int zoomOutImageCanvasY;
    
    private Image zoomInImage;
    private int zoomInImageWidth;
    private int zoomInImageHeight;
    private int zoomInImageCanvasX;
    private int zoomInImageCanvasY;

    //minimise and maximise images must be equal dimensions
    private Image maximiseImage;
    private Image minimiseImage;
    private int minmaxImageWidth;
    private int minmaxImageHeight;
    private int minmaxImageCanvasX;
    private int minmaxImageCanvasY;
    
    private Image gpxImage;
    private Image roseImage;

    private int gpxImageWidth;
    private int gpxImageHeight;
    
    //No fix image and no fix image must be equal dimensions.
    private int fixImageWidth;
    private int fixImageHeight;
    private int noFixImageWidth;
    private int noFixImageHeight;
    
    //Compass pointer variables
    private int pointer_start_x;
    private int pointer_start_y;
    private int pointer_end_x;
    private int pointer_end_y;    
    private int pointer_length;
    
    //Strings for compass screen
    private String latString;
    private String lngString;
    private String eastingsString;
    private String northingsString;
    private String headingString;
    private String heightString;
    private String geoidHeightString;
    private String numSatsString;
    private String spdString;
    private GeoImage gimage;
    private ImagePoint gpsPosnImageCoords = new ImagePoint();

    private Image leftArrowImage;    
    private Image rightArrowImage;
    /*
    private ImagePoint gpxImagePoint = new ImagePoint();    
    private ControlPoint controlPoint = new ControlPoint();    
    private GuidancePoint guidancePoint = new GuidancePoint();
     */
    private ImagePoint gpxImagePoint;
    private ControlPoint controlPoint;
    private GuidancePoint guidancePoint;
    private int keyStates;
    
    private int currentTripScreen = 0;
    private final static int DIST_TRIP_TIME = 0;
    private final static int SPEED = 1;
    private final static int HGT = 2;
    
    private int currentCompassScreen = 0;
    private final static int HEADING = 0;
    private final static int LAT_LONG = 1;
    private final static int GRID_COORDS = 2;

    private final static int MAP = 0;
    private final static int COMPASS = 1;
    private final static int TRIP_COMPUTER = 2;
    private int state = MAP;
    private boolean touch;
    
    private final static String OS_COPYRIGHT_NOTICE = "OS © Crown copyright.";
    
    /** Creates a new instance of GPSCanvas */
    public GPSCanvas(GPSJakeMIDlet midlet, GeoImage gimage) {
    	    	
        //super(midlet, gimage);
        super(midlet, gimage);
        
        this.midlet = midlet;
        this.gimage = gimage;
        
        //Check if touch screen
    	String keyboardType = System.getProperty("com.nokia.keyboard.type");
    	if (keyboardType.equals("None")) {
            // full touch device detected
            touch = true;
        }        

        //Create pointer image
        crossHairImage = midlet.createImage("/res/main_crosshair.png");
        guidPntImage = midlet.createImage("/res/marker_rec.png");
        ctlPntImage = midlet.createImage("/res/control_point.png");

        crossHairImageWidth = crossHairImage.getWidth();
        crossHairImageHeight = crossHairImage.getHeight();

        //Position crosshair in middle of canvas.
        x_cross = x;
        y_cross = y;

        //Ensure crosshair coordinates are initialised.
		real_x_cross = canvasX2imageX(x_cross);
        real_y_cross = canvasY2imageY(y_cross);		

        guidPntImageWidth = guidPntImage.getWidth();
        guidPntImageHeight = guidPntImage.getHeight();
        ctlPntImageWidth = ctlPntImage.getWidth();
        ctlPntImageHeight = ctlPntImage.getHeight();

        fixImage = midlet.createImage("/res/sat_on.png");
        fixImageWidth = fixImage.getWidth();
        fixImageHeight = fixImage.getHeight();

        noFixImage = midlet.createImage("/res/sat_off.png");
        noFixImageWidth = noFixImage.getWidth();
        noFixImageHeight = noFixImage.getHeight();
        
        zoomOutImage = midlet.createImage("/res/zoom_out.png");
        zoomOutImageWidth = zoomOutImage.getWidth();
        zoomOutImageHeight = zoomOutImage.getHeight();        

        zoomInImage = midlet.createImage("/res/zoom_in.png");
        zoomInImageWidth = zoomInImage.getWidth();
        zoomInImageHeight = zoomInImage.getHeight();
        
        minimiseImage = midlet.createImage("/res/minimise.png");
        maximiseImage = midlet.createImage("/res/maximise.png");
        minmaxImageWidth = maximiseImage.getWidth();
        minmaxImageHeight = maximiseImage.getHeight();  
        
        //Route markers are square.
        gpxImage = midlet.createImage("/res/route_marker_11_pixels.png");
        gpxImageWidth = gpxImage.getWidth();
        gpxImageHeight = gpxImageWidth;
        //gimage.setGPXMinDist(gpxImageWidth);

        //Create position marker image    
        posnImage = midlet.createImage("/res/position_str4.png");
        posnImageWidth = posnImage.getWidth();
        posnImageHeight = posnImage.getHeight();

        //Compass rose image - must be square
        roseImage = midlet.createImage("/res/rose.png");
        pointer_length = roseImage.getHeight() / 2;
        
        //Fonts
        fontSmallSystem = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        fontLargeProp = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_LARGE);
        fontSmallProp = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        fontSmallPropBold = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
        
        //Left and right arrow images
        leftArrowImage = midlet.createImage("/res/leftArrow.PNG");
        rightArrowImage = midlet.createImage("/res/rightArrow.PNG");

        moveImageCommand = new Command("Move Image", Command.ITEM, 0);
        allCommands.addElement(moveImageCommand);
        moveCrossHairCommand = new Command("Move Crosshair", Command.ITEM, 0);
        allCommands.addElement(moveCrossHairCommand);
        
        centreImageCommand = new Command("Centre Image", Command.ITEM, 1);
        allCommands.addElement(centreImageCommand);
        
        restoreZoomCommand = new Command("Reset Zoom", Command.ITEM, 2);
        allCommands.addElement(restoreZoomCommand);        
        
        fullScreenOnCommand = new Command("Full Screen on", Command.ITEM, 3);
        allCommands.addElement(fullScreenOnCommand);
        fullScreenOffCommand = new Command("Full Screen off", Command.ITEM, 3);
        allCommands.addElement(fullScreenOffCommand);
        
        zoomInCommand = new Command("Zoom In", Command.ITEM, 4);
        allCommands.addElement(zoomInCommand);
        zoomOutCommand = new Command("Zoom Out", Command.ITEM, 4);
        allCommands.addElement(zoomOutCommand);
        
        showGPSStatusCommand = new Command("Show GPS Status", Command.ITEM, 5);
        allCommands.addElement(showGPSStatusCommand);
        hideGPSStatusCommand = new Command("Hide GPS Status", Command.ITEM, 5);
        allCommands.addElement(hideGPSStatusCommand);
        hideCrossHairCommand = new Command("Hide Crosshair", Command.ITEM, 6);
        allCommands.addElement(hideCrossHairCommand);
        showCrossHairCommand = new Command("Show Crosshair", Command.ITEM, 6);        
        allCommands.addElement(showCrossHairCommand);
        showClockCommand = new Command("Show Clock", Command.ITEM, 7);
        allCommands.addElement(showClockCommand);
        hideClockCommand = new Command("Hide Clock", Command.ITEM, 7);
        allCommands.addElement(hideClockCommand);
        showOSNoticeCommand = new Command("Show OS Copyright", Command.ITEM, 8);
        allCommands.addElement(showOSNoticeCommand);
        hideOSNoticeCommand = new Command("Hide OS Copyright", Command.ITEM, 9);
        allCommands.addElement(hideOSNoticeCommand);        
        addControlPointCommand = new Command("Add Control Pnt...", Command.ITEM, 9);
        allCommands.addElement(addControlPointCommand);
        showControlPoints = new Command("Edit Control Pnts...", Command.ITEM, 10);
        allCommands.addElement(showControlPoints);
        addGuidancePointCommand = new Command("Add Guidance Pnt...", Command.ITEM, 11);
        allCommands.addElement(addGuidancePointCommand);
        showGuidancePoints = new Command("Edit Guidance Pnts...", Command.ITEM, 12);
        allCommands.addElement(showGuidancePoints);
        guidanceOnCommand = new Command("Guidance on", Command.ITEM, 13);
        allCommands.addElement(guidanceOnCommand);
        guidanceOffCommand = new Command("Guidance off", Command.ITEM, 13);
        allCommands.addElement(guidanceOffCommand);
        importGPXCommand = new Command("Import GPX file...", Command.ITEM, 14);
        allCommands.addElement(importGPXCommand);
        offCourseAlarmOnCommand = new Command("Offcourse alarm on", Command.ITEM, 15);
        allCommands.addElement(offCourseAlarmOnCommand);
        offCourseAlarmOffCommand = new Command("Offcourse alarm off", Command.ITEM, 15);
        allCommands.addElement(offCourseAlarmOffCommand);
        qlCommand = new Command("Map Image Quicklist", Command.ITEM, 16);
        allCommands.addElement(qlCommand);
        showCompassCommand = new Command("Show Compass", Command.ITEM, 17);
        allCommands.addElement(showCompassCommand);
        hideCompassCommand = new Command("Hide Compass", Command.ITEM, 17);
        allCommands.addElement(hideCompassCommand);
        showTripComputerCommand = new Command("Show Trip Computer", Command.ITEM, 18);
        allCommands.addElement(showTripComputerCommand);
        hideTripComputerCommand = new Command("Hide Trip Computer", Command.ITEM, 18);
        allCommands.addElement(hideTripComputerCommand);
        backCommand = new Command("Back", Command.BACK, 19);
        allCommands.addElement(backCommand);
        
        //Commands for trip computer
        resetTripStatsCommand = new Command("Reset", Command.ITEM, 21);   
        allCommands.addElement(resetTripStatsCommand);        
        pauseTripStatsCommand = new Command("Pause", Command.ITEM, 22);
        allCommands.addElement(pauseTripStatsCommand);        
        resumeTripStatsCommand = new Command("Resume", Command.ITEM, 23);
        allCommands.addElement(resumeTripStatsCommand);        
        metricCommand = new Command("Metric", Command.ITEM, 24);
        allCommands.addElement(metricCommand);        
        imperialCommand = new Command("Imperial", Command.ITEM, 25);            
        allCommands.addElement(imperialCommand);        

        try {

            refreshCommands();

            setCommandListener(this);
        } catch (Exception e) {
            midlet.fatalError("GPSCanvas:GPSCanvas " + e.toString());
        }
    }

    private void refreshCommands() {
        try {

            Vector visibleCommands = new Vector();

            //Remove all the visible commands.
            for (int i = 0; i < allCommands.size(); i++) {
                removeCommand((Command) allCommands.elementAt(i));
            }

            //Decide whether to add into visible vector            
            
            if (state == MAP) {

                //Some menu items shouldn't be added unless a map image has been
                //selected by the user.
                if (!gimage.getFilename().equals("")) {
                    
                    if (!moveCrossHairMode) {
                        visibleCommands.addElement(moveCrossHairCommand);                                        
                        if (!showCrossHair) {
                            visibleCommands.addElement(showCrossHairCommand);
                        } else {
                            visibleCommands.addElement(hideCrossHairCommand);
                        }                        
                    } else {
                        visibleCommands.addElement(moveImageCommand);
                    }               
                    
                    visibleCommands.addElement(centreImageCommand);
                    visibleCommands.addElement(restoreZoomCommand);
                    
                    if (!touch) {                       
                    	visibleCommands.addElement(zoomOutCommand);
                    	visibleCommands.addElement(zoomInCommand);
                    }
                    
                    if (gimage.registered) {
	                    if (offCourseAlarmOn) {
	                        visibleCommands.addElement(offCourseAlarmOffCommand);
	                    } else {
	                        visibleCommands.addElement(offCourseAlarmOnCommand);
	                    }
                    }

                    if (gimage.registered) {
	                    if (guidanceOn) {
	                        visibleCommands.addElement(guidanceOffCommand);
	                    } else {
	                        visibleCommands.addElement(guidanceOnCommand);
	                    }                    
                    }

                    if (gimage.registered && midlet.gpsSource.equals("")) {
	                    visibleCommands.addElement(addGuidancePointCommand);
	                    visibleCommands.addElement(showGuidancePoints);
                    }
                    visibleCommands.addElement(addControlPointCommand);
                    visibleCommands.addElement(showControlPoints);
                    
                    if (gimage.registered) {
                    	visibleCommands.addElement(importGPXCommand);
                    }
                    
                }
                
                if(!touch) {
	                if (fullScreenMode) {
	                    visibleCommands.addElement(fullScreenOffCommand);
	                } else {
	                    visibleCommands.addElement(fullScreenOnCommand);
	                }
                }
                
                if (showClock) {
                    visibleCommands.addElement(hideClockCommand);
                } else {
                    visibleCommands.addElement(showClockCommand);
                }
                
                if (showOSNotice) {
                    visibleCommands.addElement(hideOSNoticeCommand);
                } else {
                    visibleCommands.addElement(showOSNoticeCommand);
                }                
                
                if (showGPSStatus) {
                    visibleCommands.addElement(hideGPSStatusCommand);
                } else {
                    visibleCommands.addElement(showGPSStatusCommand);
                }
                
                //Add commands that are permanently visible                
                visibleCommands.addElement(qlCommand);
                visibleCommands.addElement(showCompassCommand);
                visibleCommands.addElement(showTripComputerCommand);                
                visibleCommands.addElement(backCommand);                
                
            } else if (state == COMPASS) {
            	if(!touch) {
	                if (fullScreenMode) {
	                    visibleCommands.addElement(fullScreenOffCommand);
	                } else {
	                    visibleCommands.addElement(fullScreenOnCommand);
	                }
            	}
                visibleCommands.addElement(hideCompassCommand);
            } else if (state == TRIP_COMPUTER) {
                visibleCommands.addElement(resetTripStatsCommand);
            	if(!touch) {
	                if (fullScreenMode) {
	                    visibleCommands.addElement(fullScreenOffCommand);
	                } else {
	                    visibleCommands.addElement(fullScreenOnCommand);
	                }
            	}
                if (paused) {
                    visibleCommands.addElement(resumeTripStatsCommand);
                } else {
                    visibleCommands.addElement(pauseTripStatsCommand);
                }
                if (imperial) {
                    visibleCommands.addElement(metricCommand);
                } else {
                    visibleCommands.addElement(imperialCommand);
                }
                visibleCommands.addElement(hideTripComputerCommand);
            }

            //Add commands which should be visible
            for (int i = 0; i < visibleCommands.size(); i++) {
                addCommand((Command) visibleCommands.elementAt(i));
            }

        } catch (Exception e) {
            midlet.fatalError("GPSCanvas:refreshCommands " + e.toString());
        }
    }

    public void commandAction(Command c, Displayable d) {
        try {
            if (c == backCommand) {
                midlet.backMainMenu();
            } else if (c == addControlPointCommand) {
                if (gimage.availableControlPoints() == false) {
                    midlet.showMessage("Max no. control points reached.");
                } else if (real_x_cross >= super.srcImageWidth || real_y_cross >= super.srcImageHeight) {
                    midlet.showMessage("Point is outside image area.");
                } else {
                    midlet.addControlPoint(real_x_cross, real_y_cross);
                }
            } else if (c == showControlPoints) {
                midlet.showControlPointList();
            } else if (c == fullScreenOnCommand) {
                fullScreenMode = true;
                this.setFullScreenMode(fullScreenMode);
            } else if (c == fullScreenOffCommand) {
                fullScreenMode = false;
                this.setFullScreenMode(fullScreenMode);
            } else if (c == offCourseAlarmOnCommand) {
                offCourseAlarmOn = true;
                midlet.startOffCourseAlarm();
            } else if (c == offCourseAlarmOffCommand) {
                offCourseAlarmOn = false;
                midlet.stopOffCourseAlarm();
            } else if (c == guidanceOnCommand) {
                guidanceOn = true;
                midlet.startGuidance();
            } else if (c == guidanceOffCommand) {
                guidanceOn = false;
                midlet.stopGuidance();
            } else if (c == zoomInCommand) {
                super.scaleImage(MapCanvas.ZOOM_INCREMENT);
            } else if (c == zoomOutCommand) {
            	super.scaleImage(-MapCanvas.ZOOM_INCREMENT);
            } else if (c == showGPSStatusCommand) {
                showGPSStatus = true;
            } else if (c == hideGPSStatusCommand) {
                showGPSStatus = false;
            } else if (c == showClockCommand) {
                showClock = true;
            } else if (c == hideClockCommand) {
                showClock = false;
            } else if (c == showOSNoticeCommand) {
                showOSNotice = true;
            } else if (c == hideOSNoticeCommand) {
            	showOSNotice = false;                
            } else if (c == showCompassCommand) {
                state = COMPASS;
            } else if (c == hideCompassCommand) {
                state = MAP;
            } else if (c == showTripComputerCommand) {
                state = TRIP_COMPUTER;
            } else if (c == hideTripComputerCommand) {
                state = MAP;
            } else if (c == showCrossHairCommand) {
                showCrossHair = true;
            } else if (c == hideCrossHairCommand) {
                showCrossHair = false;         
            } else if (c == importGPXCommand) {
                midlet.showGPXFileBrowser();
            } else if (c == moveImageCommand) {
                moveCrossHairMode = false;
            } else if (c == moveCrossHairCommand) {
                showCrossHair = true;
                moveCrossHairMode = true;
            } else if (c == centreImageCommand) {
                super.centreImage();
            } else if (c == restoreZoomCommand) {
                super.scaleImage(1 - geoImage.getScaleIndex());                     
            } else if (c == addGuidancePointCommand) {
                if (real_x_cross >= super.srcImageWidth || real_y_cross >= super.srcImageHeight) {
                    midlet.showMessage("Point is outside image area.");
                } else {
                    midlet.addGuidancePoint(real_x_cross, real_y_cross);
                }
            } else if (c == showGuidancePoints) {
                midlet.showGuidancePointList();
            } else if (c == qlCommand) {
                midlet.showImgQL(false);
            } else if (c == resetTripStatsCommand) {
                midlet.getGPSInfo().resetTrip();
                paused = false;
            } else if (c == pauseTripStatsCommand) {
                paused = true;
                if (midlet.getGPSInfo().getTotalTimeNoFix() > 0) {
                    midlet.showMessage("GPS fix lost when using the trip computer.  Trip stats may not be accurate.");
                }
            } else if (c == resumeTripStatsCommand) {
                paused = false;
            } else if (c == metricCommand) {
                imperial = false;
            } else if (c == imperialCommand) {
                imperial = true;
            }

            refreshCommands();

        } catch (Exception e) {
            midlet.fatalError("GPSCanvas:commandAction " + e.toString());
        }
    }

    public void draw() {
        try {
            super.draw();
            //Draw the map to the buffer.  If the user hasn't selected an image
            //for display use the splash screen.
            if (!gimage.getFilename().equals("")) {
                //Show OS copyright message if selected.
                if (showOSNotice) {
                    super.drawText(OS_COPYRIGHT_NOTICE, 0,
                    		canvasHeight - fontSmallProp.getHeight() * 2,
                            Graphics.LEFT | Graphics.TOP, fontSmallProp);
                }                
            } else {
                super.drawText("No map image selected.", 0,
                		canvasHeight - fontSmallProp.getHeight() * 2,
                		Graphics.LEFT | Graphics.TOP, fontSmallProp, 0xFFFFFF);                
            }

            if (!gimage.getFilename().equals("")) {
                //Draw any guidance points
                for (int i = 0; i < gimage.getGuidancePoints().size(); i++) {
                    guidancePoint = (GuidancePoint) gimage.getGuidancePoints().elementAt(i);
                    if (super.inCanvas(guidancePoint.image_x, guidancePoint.image_y)) {
                        super.drawImage(guidPntImage,
                        		imageX2canvasX(guidancePoint.image_x) - (guidPntImageWidth / 2),
                        		imageY2canvasY(guidancePoint.image_y) - (guidPntImageHeight / 2));
                        super.drawText("" + (i + 1), imageX2canvasX(guidancePoint.image_x),
                        		imageY2canvasY(guidancePoint.image_y), Graphics.RIGHT | Graphics.BOTTOM);
                    }
                }

                //Draw any control points
                for (int i = 0; i < gimage.getControlPoints().size(); i++) {
                    controlPoint = (ControlPoint) gimage.getControlPoints().elementAt(i);
                    if (super.inCanvas(controlPoint.image_x, controlPoint.image_y)) {
                        super.drawImage(ctlPntImage,
                        		imageX2canvasX(controlPoint.image_x) - (ctlPntImageWidth / 2),
                        		imageY2canvasY(controlPoint.image_y) - (ctlPntImageHeight / 2));
                        super.drawText("" + (i + 1), imageX2canvasX(controlPoint.image_x),
                        		imageY2canvasY(controlPoint.image_y), Graphics.RIGHT | Graphics.BOTTOM);
                    }
                }

                //Draw any gpx points that have been imported.
                for (int i = 0; i < gimage.gpxPoints.size(); i++) {
                	gpxImagePoint = gimage.osGridRefAsImagePoint((OSGridRef) gimage.gpxPoints.elementAt(i));
                    if (super.inCanvas(gpxImagePoint.image_x, gpxImagePoint.image_y)) {
                        super.drawImage(gpxImage,
                                imageX2canvasX(gpxImagePoint.image_x) - (gpxImageWidth / 2),
                                imageY2canvasY(gpxImagePoint.image_y) - (gpxImageHeight / 2));
                    }
                }
            }
            
            //Add crosshair - don't show crosshair if the user hasn't selected a map image.
            if (showCrossHair && !gimage.getFilename().equals("")) {
                super.drawImage(crossHairImage, x_cross, y_cross, Graphics.HCENTER | Graphics.VCENTER);
                //although x_cross, y_cross can remain the same when the user is panning the image
                //the image coordinates to which they refer will change.  Therefore we need to update
                //them on every tick.
        		real_x_cross = canvasX2imageX(x_cross);
                real_y_cross = canvasY2imageY(y_cross);		                
            }

            //If we have GPS fix show draw position marker
            if (this.midlet.getGPSInfo().getFix()) {
                if (gimage.registered) {
                    if (super.inCanvas(gpsPosnImageCoords.image_x, gpsPosnImageCoords.image_y)) {
                        super.drawImage(posnImage,
                        		imageX2canvasX(gpsPosnImageCoords.image_x) - (posnImageWidth / 2),
                        		imageY2canvasY(gpsPosnImageCoords.image_y) - (posnImageHeight / 2));
                    }
                }

                //Draw clock if we have a fix
                if (showClock) {
                    super.drawText(timeString + " " + dateString, 0, 5,
                            Graphics.LEFT | Graphics.TOP, fontSmallPropBold);
                }
            }            
            
            if (state == COMPASS || state == TRIP_COMPUTER) {
                super.addTransparentLayer();
            }
            
            if (showGPSStatus) {
                if (this.midlet.getGPSInfo().getFix()) {
                    super.drawImage(fixImage, super.canvasWidth - noFixImageWidth, 0);
                } else {
                    super.drawImage(noFixImage, super.canvasWidth - fixImageWidth, 0);
                }
            }
            
            //Draw touch GUI elements
            if (touch) {
            	zoomInImageCanvasX = super.canvasWidth - zoomInImageWidth;
            	zoomInImageCanvasY = (super.canvasHeight / 2) - 2 * zoomInImageHeight;
            	zoomOutImageCanvasX = super.canvasWidth - zoomOutImageWidth;
            	zoomOutImageCanvasY = (super.canvasHeight / 2) + 2 * zoomOutImageHeight;
            	
                super.drawImage(zoomInImage, zoomInImageCanvasX, zoomInImageCanvasY);
                super.drawImage(zoomOutImage, zoomOutImageCanvasX, zoomOutImageCanvasY);
                
                minmaxImageCanvasX = super.canvasWidth - minmaxImageWidth;
                minmaxImageCanvasY = super.canvasHeight - minmaxImageHeight;
                
                if (fullScreenMode) {
                    super.drawImage(minimiseImage, minmaxImageCanvasX, minmaxImageCanvasY);
                } else {
                	super.drawImage(maximiseImage, minmaxImageCanvasX, minmaxImageCanvasY);
                }
            }
            
            
            if (state == COMPASS) {
                switch (currentCompassScreen) {                     
                    case LAT_LONG:       
                        super.drawText("latitude:", fontSmallProp.charWidth('w'), fontSmallProp.getHeight(), Graphics.LEFT | Graphics.TOP, fontSmallProp);
                        super.drawText(latString, fontSmallProp.charWidth('w'), fontSmallProp.getHeight() * 2, Graphics.LEFT | Graphics.TOP, fontLargeProp);                        
                        super.drawText("longitude:", fontSmallProp.charWidth('w'), canvasHeight / 2, Graphics.LEFT | Graphics.TOP, fontSmallProp);                             
                        super.drawText(lngString, fontSmallProp.charWidth('w'), canvasHeight / 2 + fontSmallProp.getHeight(), Graphics.LEFT | Graphics.TOP, fontLargeProp);                                                     
                        break;
                    case GRID_COORDS:
                        super.drawText("eastings:", fontSmallProp.charWidth('w'), fontSmallProp.getHeight(), Graphics.LEFT | Graphics.TOP, fontSmallProp);
                        super.drawText(eastingsString, fontSmallProp.charWidth('w'), fontSmallProp.getHeight() * 2, Graphics.LEFT | Graphics.TOP, fontLargeProp);
                        super.drawText("northings:", fontSmallProp.charWidth('w'), canvasHeight / 2, Graphics.LEFT | Graphics.TOP, fontSmallProp);                        
                        super.drawText(northingsString, fontSmallProp.charWidth('w'), canvasHeight / 2 + fontSmallProp.getHeight(), Graphics.LEFT | Graphics.TOP, fontLargeProp);                        
                        break;
                    case HEADING:
                        super.drawText("heading:", fontSmallProp.charWidth('w'), fontSmallProp.getHeight(), Graphics.LEFT | Graphics.TOP, fontSmallProp);
                        super.drawText(headingString, fontSmallProp.charWidth('w'), fontSmallProp.getHeight() * 2, Graphics.LEFT | Graphics.TOP, fontLargeProp);                        
                        //Calculate coordinates of compass pointer
                        pointer_start_x = canvasWidth / 2;
                        pointer_start_y = canvasHeight / 2;
                        //Draw compass rose
                        super.drawImage(roseImage, pointer_start_x, pointer_start_y, Graphics.VCENTER | Graphics.HCENTER);
                        
                        if (!headingString.startsWith("-")) {
                            
                            pointer_end_x = pointer_start_x + (int) (Math.sin(Math.toRadians(midlet.getGPSInfo().getHeading())) * pointer_length);
                            pointer_end_y = pointer_start_y - (int) (Math.cos(Math.toRadians(midlet.getGPSInfo().getHeading())) * pointer_length);
                            
                            super.drawLine(pointer_start_x, pointer_start_y, pointer_end_x, pointer_end_y);
                        }
                        break;                        
                    }
                    
                    //Show arrows indicating the user can scroll the screen to the left and the right
                    if (currentCompassScreen == HEADING) {
                        super.drawImage(rightArrowImage, canvasWidth - rightArrowImage.getWidth(), canvasHeight / 2);
                    } else if (currentCompassScreen == GRID_COORDS) {
                        super.drawImage(leftArrowImage, canvasWidth - (leftArrowImage.getWidth() * 2), canvasHeight / 2);
                    } else {
                        super.drawImage(rightArrowImage, canvasWidth - rightArrowImage.getWidth(), canvasHeight / 2);
                        super.drawImage(leftArrowImage, canvasWidth - (leftArrowImage.getWidth() * 2), canvasHeight / 2);                        
                    }

            } else if (state == TRIP_COMPUTER) {
                switch (currentTripScreen) {                     
                    case DIST_TRIP_TIME:
                        super.drawText("dist:", fontSmallProp.charWidth('w'), fontSmallProp.getHeight(), Graphics.LEFT | Graphics.TOP, fontSmallProp);                        
                        super.drawText(distanceString, fontSmallProp.charWidth('w'), fontSmallProp.getHeight() * 2, Graphics.LEFT | Graphics.TOP, fontLargeProp);    
                        super.drawText("time:", fontSmallProp.charWidth('w'), canvasHeight / 2, Graphics.LEFT | Graphics.TOP, fontSmallProp);    
                        super.drawText(tripTimeString, fontSmallProp.charWidth('w'), canvasHeight / 2 + fontSmallProp.getHeight(), Graphics.LEFT | Graphics.TOP, fontLargeProp);                            
                        break;
                    case SPEED:
                        super.drawText("spd:", fontSmallProp.charWidth('w'), fontSmallProp.getHeight(), Graphics.LEFT | Graphics.TOP, fontSmallProp);                        
                        super.drawText(spdString, fontSmallProp.charWidth('w'), fontSmallProp.getHeight() * 2, Graphics.LEFT | Graphics.TOP, fontLargeProp);
                        super.drawText("ave spd:", fontSmallProp.charWidth('w'), canvasHeight / 3, Graphics.LEFT | Graphics.TOP, fontSmallProp);
                        super.drawText(aveSpeedString, fontSmallProp.charWidth('w'), canvasHeight / 3 + fontSmallProp.getHeight(), Graphics.LEFT | Graphics.TOP, fontLargeProp);                        
                        super.drawText("max spd:", fontSmallProp.charWidth('w'), 2 * (canvasHeight / 3), Graphics.LEFT | Graphics.TOP, fontSmallProp);                        
                        super.drawText(maxSpeedString, fontSmallProp.charWidth('w'), 2 * (canvasHeight / 3) + fontSmallProp.getHeight(), Graphics.LEFT | Graphics.TOP, fontLargeProp);                                                                                              
                        break;
                    case HGT:
                        super.drawText("hgt gained:", fontSmallProp.charWidth('w'), fontSmallProp.getHeight(), Graphics.LEFT | Graphics.TOP, fontSmallProp);
                        super.drawText(heightGainedString, fontSmallProp.charWidth('w'), fontSmallProp.getHeight() * 2, Graphics.LEFT | Graphics.TOP, fontLargeProp);                        
                        super.drawText("hgt lost:", fontSmallProp.charWidth('w'), canvasHeight / 2, Graphics.LEFT | Graphics.TOP, fontSmallProp);                        
                        super.drawText(heightLostString, fontSmallProp.charWidth('w'), canvasHeight / 2 + fontSmallProp.getHeight(), Graphics.LEFT | Graphics.TOP, fontLargeProp);                                                
                        break;
                    }
//                super.drawTextToBuffer("Distance (no fix): " + distanceNoFixString, 0, 15, Graphics.LEFT | Graphics.TOP);
//                super.drawTextToBuffer("Trip time (no fix): " + tripTimeNoFixString, 0, 105, Graphics.LEFT | Graphics.TOP);    
//                super.drawTextToBuffer("Current update time: " + updateTimeString, 0, 120, Graphics.LEFT | Graphics.TOP);                   
//                super.drawTextToBuffer("WGS84 hgt: " + heightString, 0, 75, Graphics.LEFT | Graphics.TOP);            
//                super.drawTextToBuffer("Geoid hgt: " + geoidHeightString, 0, 90, Graphics.LEFT | Graphics.TOP);     
//                super.drawTextToBuffer("No sats: " + numSatsString, 0, 105, Graphics.LEFT | Graphics.TOP);                 
                
                    //Show arrows indicating the user can scroll the screen to the left and the right
                    if (currentTripScreen == DIST_TRIP_TIME) {
                        super.drawImage(rightArrowImage, canvasWidth - rightArrowImage.getWidth(), canvasHeight / 2);
                    } else if (currentTripScreen == HGT) {
                        super.drawImage(leftArrowImage, canvasWidth - (leftArrowImage.getWidth() * 2), canvasHeight / 2);
                    } else {
                        super.drawImage(rightArrowImage, canvasWidth - rightArrowImage.getWidth(), canvasHeight / 2);
                        super.drawImage(leftArrowImage, canvasWidth - (leftArrowImage.getWidth() * 2), canvasHeight / 2);                        
                    }                
            }

        } catch (Exception e) {
            midlet.fatalError("GPSCanvas:draw " + e.toString());
        }
    }

    public void moveCrossHair(int direction) {
        try {

            switch (direction) {
                case LEFT:
                    if (x_cross - CROSS_MOVEMENT_IN_X >= 0) {
                        x_cross = x_cross - CROSS_MOVEMENT_IN_X;
                    }
                    break;
                case RIGHT:
                    if (x_cross + CROSS_MOVEMENT_IN_X <= super.canvasWidth) {
                        x_cross = x_cross + CROSS_MOVEMENT_IN_X;
                    }
                    break;
                case UP:
                    if (y_cross - CROSS_MOVEMENT_IN_Y >= 0) {
                        y_cross = y_cross - CROSS_MOVEMENT_IN_Y;
                    }
                    break;
                case DOWN:
                    if (y_cross + CROSS_MOVEMENT_IN_Y <= super.canvasHeight) {
                        y_cross = y_cross + CROSS_MOVEMENT_IN_Y;
                    }
                    break;
            }
            real_x_cross = canvasX2imageX(x_cross);
            real_y_cross = canvasY2imageY(y_cross);
        } catch (Exception e) {
            midlet.fatalError("GuidanceCanvas:moveCrossHair " + e.toString());
        }
    } 
    
    //touch move crosshair
    public void moveCrossHair(int deltaX, int deltaY) {
	    try {
	    	if (x_cross + deltaX >= 0 && x_cross + deltaX <= super.canvasWidth) {
	    		x_cross = x_cross + deltaX;
	    	}
			
	    	if (y_cross + deltaY >= 0 && y_cross + deltaY <= super.canvasHeight) {
	    		y_cross = y_cross + deltaY;
	    	}
			
			real_x_cross = canvasX2imageX(x_cross);
            real_y_cross = canvasY2imageY(y_cross);			
	    } catch (Exception e) {
	        midlet.fatalError("MapCanvas:moveCrossHair " + e.toString());
	    }		
    }

    public void tick() {
        try {
            keyStates = getKeyStates();

            //Get GPS coord/ date and time if there's a fix
            if (this.midlet.getGPSInfo().getFix()) {
                gpsPosnImageCoords = gimage.osGridRefAsImagePoint(this.midlet.getGPSInfo().osGridRef);
                dateString = midlet.getGPSInfo().getDateDDMMYY();
                timeString = midlet.getGPSInfo().getTimeHHMMSS();
            }         
 
            //Handle kep presses for the various states
            if (state == MAP) {
                if (moveCrossHairMode) {
                    //In move crosshair mode so move it.
                    if (((keyStates & LEFT_PRESSED) != 0) && ((keyStates & RIGHT_PRESSED) == 0)) {
                        moveCrossHair(LEFT);
                    } else if (((keyStates & RIGHT_PRESSED) != 0) && ((keyStates & LEFT_PRESSED) == 0)) {
                        moveCrossHair(RIGHT);
                    } else if (((keyStates & UP_PRESSED) != 0) && ((keyStates & DOWN_PRESSED) == 0)) {
                        moveCrossHair(UP);
                    } else if (((keyStates & DOWN_PRESSED) != 0) && ((keyStates & UP_PRESSED) == 0)) {
                        moveCrossHair(DOWN);
                    }
                } else {
                    //In move image mode so move the image.
                    if (((keyStates & LEFT_PRESSED) != 0) && ((keyStates & RIGHT_PRESSED) == 0)) {
                        super.moveImage(LEFT);
                        moveWithGPSMode = false;
                    } else if (((keyStates & RIGHT_PRESSED) != 0) && ((keyStates & LEFT_PRESSED) == 0)) {
                        super.moveImage(RIGHT);
                        moveWithGPSMode = false;
                    } else if (((keyStates & UP_PRESSED) != 0) && ((keyStates & DOWN_PRESSED) == 0)) {
                        super.moveImage(UP);
                        moveWithGPSMode = false;
                    } else if (((keyStates & DOWN_PRESSED) != 0) && ((keyStates & UP_PRESSED) == 0)) {
                        super.moveImage(DOWN);
                        moveWithGPSMode = false;
                    } else if ((keyStates & FIRE_PRESSED) != 0 && this.midlet.getGPSInfo().getFix() && gimage.registered) {
                        moveWithGPSMode = true;
                    } else if ((keyStates & FIRE_PRESSED) != 0 && (!this.midlet.getGPSInfo().getFix() || !gimage.registered)) {
                        super.centreImage();
                        moveWithGPSMode = false;
                    }
                    //If move with GPS mode set x/y to current GPS position expressed as image coords
                    if (moveWithGPSMode) {
                    	panImage(gpsPosnImageCoords.image_x, gpsPosnImageCoords.image_y);
                    }
                }
            } else if (state == TRIP_COMPUTER) {
                if (((keyStates & LEFT_PRESSED) != 0) && ((keyStates & RIGHT_PRESSED) == 0) && currentTripScreen > DIST_TRIP_TIME) {
                    currentTripScreen--;
                } else if (((keyStates & RIGHT_PRESSED) != 0) && ((keyStates & LEFT_PRESSED) == 0) && currentTripScreen < HGT) {
                    currentTripScreen++;
                }
            } else if (state == COMPASS) {
                if (((keyStates & LEFT_PRESSED) != 0) && ((keyStates & RIGHT_PRESSED) == 0) && currentCompassScreen > HEADING) {
                    currentCompassScreen--;
                } else if (((keyStates & RIGHT_PRESSED) != 0) && ((keyStates & LEFT_PRESSED) == 0) && currentCompassScreen < GRID_COORDS) {
                    currentCompassScreen++;
                }
            }

            latString = midlet.getGPSInfo().getLatInDegMinDec();
            lngString = midlet.getGPSInfo().getLngInDegMinDec();
            eastingsString = midlet.getGPSInfo().getEastingsAsStr();
            northingsString = midlet.getGPSInfo().getNorthingsAsStr();
            headingString = midlet.getGPSInfo().getHeadingDegMinDec();            
            heightString = midlet.getGPSInfo().getHgtM();
            geoidHeightString = midlet.getGPSInfo().getGeoidHgtM();
            numSatsString = midlet.getGPSInfo().getNumSats();          
            
            if (!paused) {
                if (imperial) {
                    spdString = midlet.getGPSInfo().getSpdMph();                                
                    distanceString = midlet.getGPSInfo().getTotalDistMiles();
                    distanceNoFixString = midlet.getGPSInfo().getNoFixDistMiles();           
                    aveSpeedString = midlet.getGPSInfo().getAveSpdMph();       
                    maxSpeedString = midlet.getGPSInfo().getMaxSpdMph();     
                    heightGainedString = midlet.getGPSInfo().getHgtGainedFt();
                    heightLostString = midlet.getGPSInfo().getHgtLostFt();
                } else {
                    spdString = midlet.getGPSInfo().getSpdKph();
                    distanceString = midlet.getGPSInfo().getTotalDistKm();
                    distanceNoFixString = midlet.getGPSInfo().getNoFixDistKm();           
                    aveSpeedString = midlet.getGPSInfo().getAveSpdKph();       
                    maxSpeedString = midlet.getGPSInfo().getMaxSpdKph();     
                    heightGainedString = midlet.getGPSInfo().getHgtGainedM();
                    heightLostString = midlet.getGPSInfo().getHgtLostM();
                }
                tripTimeString = midlet.getGPSInfo().getTotalTripTime();             
                tripTimeNoFixString = midlet.getGPSInfo().getNoFixTripTime();            
                updateTimeString = midlet.getGPSInfo().getTimeSinceLastUpdate();                    
            }            
            
        } catch (Exception e) {
            midlet.fatalError("GPSCanvas:tick " + e.toString());
        }
    }
    
    protected void pointerReleased(int pointerX, int pointerY) {
    	
    	if (state == TRIP_COMPUTER) {
    		if (lastDragX > 0) {
    			currentTripScreen--;
    		} else if (lastDragX < 0 && currentTripScreen < HGT) {
    			currentTripScreen++;
    		}
    	} else if (state == COMPASS) {
    		if (lastDragX > 0 && currentCompassScreen > HEADING) {
    			currentCompassScreen--;
    		} else if (lastDragX < 0 && currentCompassScreen < GRID_COORDS) {
    			currentCompassScreen++;
    		}
    	} else if (state == MAP) {
    		//if pointer is released in the gps fix image then set moveWithGPSMode
        	if (pointerX >= super.canvasWidth - noFixImageWidth && pointerX <= super.canvasWidth
        			&& pointerY >= 0 && pointerY <= noFixImageHeight
        			&& fixImagePressed && this.midlet.getGPSInfo().getFix() && gimage.registered
        			) {
        		moveWithGPSMode = true;
        	} else if (pointerX >= zoomInImageCanvasX && pointerX <= zoomInImageCanvasX + zoomInImageWidth
        			&& pointerY >= zoomInImageCanvasY && pointerY <= zoomInImageCanvasY + zoomInImageHeight
        			&& zoomInImagePressed) {
        		super.scaleImage(MapCanvas.ZOOM_INCREMENT);
        	} else if (pointerX >= zoomOutImageCanvasX && pointerX <= zoomOutImageCanvasX + zoomOutImageWidth
        			&& pointerY >= zoomOutImageCanvasY && pointerY <= zoomOutImageCanvasY + zoomOutImageHeight
        			&& zoomOutImagePressed) {
        		super.scaleImage(-MapCanvas.ZOOM_INCREMENT);
        	} else if (pointerX >= minmaxImageCanvasX && pointerX <= minmaxImageCanvasX + minmaxImageWidth
        			&& pointerY >= minmaxImageCanvasY && pointerY <= minmaxImageCanvasY + minmaxImageHeight
        			&& minmaxImagePressed) {

        		fullScreenMode = !fullScreenMode;
                this.setFullScreenMode(fullScreenMode);
        	}	    		
    	}

    	fixImagePressed = false;
    	zoomInImagePressed = false;
    	zoomOutImagePressed = false;
    	minmaxImagePressed = false;
    	
    	super.pointerReleased(pointerX, pointerY);
    }
    
    protected void pointerPressed(int pointerX, int pointerY) {
    	//If pointer is pressed in the gps fix image then set flag
    	if (pointerX >= super.canvasWidth - noFixImageWidth && pointerX <= super.canvasWidth
    			&& pointerY >= 0 && pointerY <= noFixImageHeight
    				) {
    		fixImagePressed = true;
    	} else if (pointerX >= zoomInImageCanvasX && pointerX <= zoomInImageCanvasX + zoomInImageWidth
    			&& pointerY >= zoomInImageCanvasY && pointerY <= zoomInImageCanvasY + zoomInImageHeight) {
    		zoomInImagePressed = true;
    	} else if (pointerX >= zoomOutImageCanvasX && pointerX <= zoomOutImageCanvasX + zoomOutImageWidth
    			&& pointerY >= zoomOutImageCanvasY && pointerY <= zoomOutImageCanvasY + zoomOutImageHeight) {
    		zoomOutImagePressed = true;
    	} else if (pointerX >= minmaxImageCanvasX && pointerX <= minmaxImageCanvasX + minmaxImageWidth
    			&& pointerY >= minmaxImageCanvasY && pointerY <= minmaxImageCanvasY + minmaxImageHeight) {
    		minmaxImagePressed = true;
    	}

    	super.pointerPressed(pointerX, pointerY);
	    
    }    
    
    protected void scrollImage(int deltaX, int deltaY) {
    	if (state == MAP) {
    		if (moveCrossHairMode) {
    			moveCrossHair(deltaX, deltaY);
    		} else {
    			moveWithGPSMode = false;
    			super.scrollImage(deltaX, deltaY);
    		}
        }
    }    
}
