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
 * classname: MapCanvas
 *
 * desc: This class defines the main map screen.  It was initially based on the J2ME
 * basic game examples provided by Nokia.  Image scaling, panning and zooming is also based on Nokia example apps.
 *
 */

package gpsjake;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import geo.GeoImage;

class MapCanvas extends GameCanvas implements Runnable {
    
    private static final int MILLIS_PER_TICK = 25;
    public static final float ZOOM_INCREMENT = 0.25f;
    private Graphics g;
    public int canvasWidth;
    public int canvasHeight;
    public final int MOVEMENT_IN_X = 16;
    public final int MOVEMENT_IN_Y = 16;
    
    public int imageWidth;
    public int imageHeight;
    
    //Canvas coordinates - these represent the point on the canvas where the centre
    //of the image is positioned.
    public int x;
    public int y;
    
    //touch properties
    protected int lastPointerX = -1;
    protected int lastPointerY = -1;
    //the size of the last horizontal swipe prior to pointer release
    protected int lastDragX = 0;

    int rgbImageData[];
    int srcImageWidth;
    int srcImageHeight;    
    int tempScaleRatioWidth;
    int tempScaleRatioHeight;
    
    //Array for transparent pixels to layer over canvas area.
    private int canvasTransparentLayer [];    
    
    protected GeoImage geoImage;
    private final GPSJakeMIDlet midlet;
    
    private volatile Thread animationThread = null;
    
    MapCanvas(GPSJakeMIDlet midlet, GeoImage gimage) {
        super(true);   // suppress key events for game keys
        
        this.midlet = midlet;
        
        try {
            
            //geoImage = new GeoImage(); - don't need to instantiate
            geoImage = gimage;
            setTitle("GPSjake Map");        
            
            g = getGraphics();
            
            srcImageWidth = imageWidth = geoImage.getImage().getWidth();
            srcImageHeight = imageHeight = geoImage.getImage().getHeight();
            canvasWidth = getWidth();
            canvasHeight = getHeight();

            //Create transparent layer
            canvasTransparentLayer = new int [canvasWidth * canvasHeight];            
            for(int i=0;i<canvasTransparentLayer.length;i++) {
                //50% opacity white            
                canvasTransparentLayer[i] = 0xBEFFFFFF;
                //0x80AFAFAF; //50% opacity gray       
                //0x80FFFFFF; //50% opacity white
            }                
            
            centreImage();
            
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:MapCanvas " + e.toString());
        }
    }
    
    public void draw() {
       try {
            g.setColor(0x00424242);
            g.fillRect(0, 0, canvasWidth, canvasHeight);
            //g.drawImage(geoImage.getImage(), x, y, Graphics.LEFT | Graphics.TOP);
            g.drawImage(geoImage.getImage(), x, y, Graphics.HCENTER | Graphics.VCENTER);
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:draw " + e.toString());
        }        
    }
    
    //This is called when Displayable detects a change in screen size.
    //It is overridden here so that canvasWidth and canvasHeight can be updated.
    protected void sizeChanged(int aWidth, int aHeight) {
        try {
            super.sizeChanged(aWidth, aHeight);
            //Update canvas dimensions (in case of full screen)
            canvasWidth = aWidth;
            canvasHeight = aHeight;      
            
            //Create transparent layer
            canvasTransparentLayer = new int [canvasWidth * canvasHeight];            
            for(int i=0;i<canvasTransparentLayer.length;i++) {
                //50% opacity white            
                canvasTransparentLayer[i] = 0xBEFFFFFF;
            } 
            
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:sizeChanged " + e.toString());
        }              
    }    
    
    public void scaleImage(float scale) {

        try {
        	
        	//scaleImage called with increase/decrease of ZOOM_INCREMENT
        	//negative ZOOM_INCREMENT zooms out, positive zooms in.
            geoImage.setScaleIndex(geoImage.getScaleIndex() + scale);
            
            //Minimum is 50% of original
            if (geoImage.getScaleIndex() < 0.5f) {
            	geoImage.setScaleIndex(0.5f);
            } else if (geoImage.getScaleIndex() > 2.0f) {
            	geoImage.setScaleIndex(2.0f);
            }
            
            //Create array of original image pixels
            if(rgbImageData == null){
            	rgbImageData = new int[srcImageWidth * srcImageHeight];
            	geoImage.getImage().getRGB(rgbImageData, 0, imageWidth, 0, 0, imageWidth, imageHeight);
            }
            
            int newImageWidth = (int) (srcImageWidth * geoImage.getScaleIndex());
            int newImageHeight = (int) (srcImageHeight * geoImage.getScaleIndex());

            int rgbImageScaledData[] = new int[newImageWidth * newImageHeight];

            // calculations and bit shift operations to optimize the for loop
            tempScaleRatioWidth = ((srcImageWidth << 16) / newImageWidth);
            tempScaleRatioHeight = ((srcImageHeight << 16) / newImageHeight);

            int i = 0;
            for (int localY = 0; localY < newImageHeight; localY++) {
                for (int localX = 0; localX < newImageWidth; localX++) {
                    rgbImageScaledData[i++] = rgbImageData[(srcImageWidth * ((localY * tempScaleRatioHeight) >> 16)) + ((localX * tempScaleRatioWidth) >> 16)];
                }
            }

            //Create an RGB image from rgbImageScaledData array
            geoImage.setImage(Image.createRGBImage(rgbImageScaledData, newImageWidth, newImageHeight, true));
            imageWidth = newImageWidth;
            imageHeight = newImageHeight;
        } catch (OutOfMemoryError e) {
        	geoImage.setScaleIndex(geoImage.getScaleIndex() - scale);
            e.printStackTrace();
            midlet.fatalError("MapCanvas:scaleImage Out of memory "  + e.getMessage());
        }

    }           

    public boolean inCanvas(int image_x, int image_y) {
        //Returns true if the image point supplied is currently contained
        //by the canvas.
    	if((imageX2canvasX(image_x) >= 0 && imageX2canvasX(image_x) < canvasWidth) &&
    		(imageY2canvasY(image_y) >= 0 && imageY2canvasY(image_y) < canvasHeight)) {
    		return true;
    	} else {
    		return false;
        }
    }     
    
    public int imageX2canvasX(int image_x) {
    	return (int) (x + geoImage.getScaleIndex()*(image_x - srcImageWidth / 2));
    }
    
    public int imageY2canvasY(int image_y) {
    	return (int) (y + geoImage.getScaleIndex()*(image_y - srcImageHeight / 2));
    }
    
    public int canvasX2imageX(int canvas_x) {
    	return (int) (((canvas_x - x) / geoImage.getScaleIndex()) + srcImageWidth / 2);
    }
    
    public int canvasY2imageY(int canvas_y) {
    	return (int) (((canvas_y - y) / geoImage.getScaleIndex()) + srcImageHeight / 2);
    }
    
    public void centreImage() {
        try {
        	x = (canvasWidth / 2);
        	y = (canvasHeight / 2);
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:centreImage " + e.toString());
        }        
    } 
    
    public void panImage(int pan_to_x, int pan_to_y) {
        try {
            x = (int) (imageWidth / 2 - (pan_to_x * geoImage.getScaleIndex()) + canvasWidth / 2);
            y = (int) (imageHeight / 2 - (pan_to_y * geoImage.getScaleIndex())+ canvasHeight / 2);        	
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:panImage " + e.toString());
        }        
    }    
    
    public void drawImage(Image image, int x, int y) {
        try {        
        	g.drawImage(image, x, y, Graphics.LEFT | Graphics.TOP);
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:drawImage " + e.toString());
        }               
    }
    
    public void drawImage(Image image, int x, int y, int anchor) {
        try {        
            g.drawImage(image, x, y, anchor);
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:drawImage " + e.toString());
        }               
    }    
    
    public void drawLine(int x1, int y1, int x2, int y2) {
        try { 
	    	g.setStrokeStyle(Graphics.SOLID);
	        g.setColor(0x000000000);
	        g.drawLine(x1, y1, x2, y2);
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:drawLine " + e.toString());
        }
    }
    
    public void drawText(String text, int x, int y, int anchor) {
        try {
        	g.drawString(text, x, y, anchor); 
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:drawText " + e.toString());            
        }
    }
    
    public void drawText(String text, int x, int y, int anchor, Font f) {
        try {
        	g.setFont(f);
        	g.drawString(text, x, y, anchor);
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:drawText " + e.toString());            
        }
    }  
    
    public void drawText(String text, int x, int y, int anchor, Font f, int c) {
        try {
        	g.setColor(c);
        	g.setFont(f);
        	g.drawString(text, x, y, anchor);
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:drawText " + e.toString());            
        }
    }      
    
    public void addTransparentLayer() {
    	try {
          g.drawRGB(canvasTransparentLayer,0,canvasWidth,0,0,
              canvasWidth,canvasHeight,true);
	    } catch (Exception e) {
	        midlet.fatalError("MapCanvas:addTransparentLayer " + e.toString());            
	    }          
    }
    
    public void moveImage(int direction) {
        try {
            switch (direction) {
                case LEFT:
                    x = x + MOVEMENT_IN_X;
                    break;
                case RIGHT:
                    x = x - MOVEMENT_IN_X;
                    break;
                case UP:
                    y = y + MOVEMENT_IN_Y;
                    break;
                case DOWN:
                    y = y - MOVEMENT_IN_Y;
                    break;
            }

        } catch (Exception e) {
            midlet.fatalError("MapCanvas:moveImage " + e.toString());
        }
    }
    
    void init() {
        // reinit level
    }
    
    public synchronized void start() {
        try {
            animationThread = new Thread(this);
            animationThread.start();
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:start " + e.toString());
        }
    }
    
    public synchronized void stop() {
        animationThread = null;
    }
    
    public void run() {
        Thread currentThread = Thread.currentThread();
        
        try {
            // This ends when animationThread is set to null, or when
            // it is subsequently set to a new thread; either way, the
            // current thread should terminate
            while (currentThread == animationThread) {
                long startTime = System.currentTimeMillis();
                // Don't draw if canvas is covered by
                // a system screen.
                if (isShown()) {
                    tick();
                    draw();
                    flushGraphics();
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
        } catch (InterruptedException ex) {
            // won't be thrown
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:run " + e.toString()); 
        }
    }
    
    void tick() {
        try {
            int keyStates = getKeyStates();
            
            if (((keyStates & LEFT_PRESSED) != 0) && ((keyStates & RIGHT_PRESSED) == 0)) {
                moveImage(LEFT);
            } else if (((keyStates & RIGHT_PRESSED) != 0) && ((keyStates & LEFT_PRESSED) == 0)) {
                moveImage(RIGHT);
            } else if (((keyStates & UP_PRESSED) != 0) && ((keyStates & DOWN_PRESSED) == 0)) {
                moveImage(UP);
            } else if (((keyStates & DOWN_PRESSED) != 0) && ((keyStates & UP_PRESSED) == 0)) {
                moveImage(DOWN);
            }
        } catch (Exception e) {
            midlet.fatalError("MapCanvas:tick " + e.toString());
        }
    }
    
    protected void pointerPressed(int pointerX, int pointerY) {
    	try {
	    	lastPointerX = pointerX;
	    	lastPointerY = pointerY;
	    } catch (Exception e) {
	        midlet.fatalError("MapCanvas:pointerPressed " + e.toString());
	    }	    
    }
    
    protected void pointerReleased(int pointerX, int pointerY) {
	    try {
	    	lastPointerX = -1;
	    	lastPointerY = -1;
	    	lastDragX = 0;
	    } catch (Exception e) {
	        midlet.fatalError("MapCanvas:pointerReleased " + e.toString());
	    }	    	
    }
    
    protected void pointerDragged(int pointerX, int pointerY) {
	    try {    	
	    	scrollImage(-(lastPointerX - pointerX), -(lastPointerY - pointerY));
	    	lastDragX = -(lastPointerX - pointerX);
	    	lastPointerX = pointerX;
	    	lastPointerY = pointerY;
	    } catch (Exception e) {
	        midlet.fatalError("MapCanvas:pointerDragged " + e.toString());
	    }	
    }
    
    protected void scrollImage(int deltaX, int deltaY) {
	    try {
			x = x + deltaX;
			y = y + deltaY;
	    } catch (Exception e) {
	        midlet.fatalError("MapCanvas:scrollImage " + e.toString());
	    }		
    }
}
