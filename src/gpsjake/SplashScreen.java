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
 * classname: SplashScreen
 *
 * desc: Displays splashscreen on canvas.
 */

package gpsjake;

import javax.microedition.lcdui.*;

class SplashScreen extends Canvas implements Runnable {
    private final GPSJakeMIDlet midlet;
    private volatile boolean dismissed = false;
    
    SplashScreen(GPSJakeMIDlet midlet) {
        
        this.midlet = midlet;
        try {
            setFullScreenMode(true);
            new Thread(this).start();
        }  catch (Exception e) {
            midlet.fatalError("SplashScreen:SplashScreen " + e.toString());
        }
    }
    
    public void run() {
        synchronized(this) {
            try {
                wait(5000L);   // 5 seconds
            } catch (InterruptedException e) {
                // can't happen in MIDP: no Thread.interrupt method
            }  catch (Exception e) {
                midlet.fatalError("SplashScreen:run " + e.toString());
            }
        }
        dismiss();
    }
    
    public void paint(Graphics g) {
        try {
            int width = getWidth();
            int height = getHeight();
            g.setColor(0x00424242);  // grey
            //g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, width, height);
            
            if (getSplashImage((width - 16), (height - 16)) != null) {
                g.drawImage(midlet.splashImage,
                        width/2,
                        height/2,
                        Graphics.VCENTER | Graphics.HCENTER);
                this.midlet.splashScreenPainted();
            }
        }  catch (Exception e) {
            midlet.fatalError("SplashScreen:paint " + e.toString());
        }
    }
    
    private Image getSplashImage(int maxWidth, int maxHeight) {
        if (midlet.splashImage == null) {
            //splash.png is suitable for all screen sizes.
            midlet.splashImage = this.midlet.createImage("/res/splash.png");
            //load_screen.jpg designed by John - only really suitable for N95 screen size.
            //midlet.splashImage = this.midlet.createImage("/res/load_screen.jpg");            
        }
        return midlet.splashImage;
    }
    
    
    public void keyPressed(int keyCode) {
        try {
            dismiss();
        }  catch (Exception e) {
            midlet.fatalError("SplashScreen:keyPressed " + e.toString());
        }
    }
    
    private synchronized void dismiss() {
        try {
            if (!this.dismissed) {
                this.dismissed = true;
                this.midlet.splashScreenDone();
            }
        }  catch (Exception e) {
            midlet.fatalError("SplashScreen:dismiss " + e.toString());
        }
    }
}
