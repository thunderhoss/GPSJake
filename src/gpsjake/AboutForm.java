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
* classname: AboutForm
*
* desc: Class creates About form.
*/

package gpsjake;

import javax.microedition.lcdui.*;

public class AboutForm extends Form implements CommandListener {
	private final Command backCommand;
	private GPSJakeMIDlet midlet;   
	private StringItem aboutString;
	
	AboutForm(GPSJakeMIDlet midlet) {
	
	    super("GPSjake");
	    
	    this.midlet = midlet;
	   	    
	    aboutString = new StringItem ("", "GPS Jake\n\n\n© 2013 Mike Glynn\nwww.gt140.co.uk");
	    
	    backCommand = new Command("Back", Command.BACK, 1);
	    
	    try {
	        
	        append(aboutString);
	        addCommand(backCommand);
	
	        setCommandListener(this);
	    } catch (Exception e) {
	        midlet.fatalError("AboutForm:AboutForm " + e.toString());
	    }
	}
	
	public void commandAction(Command c, Displayable d) {
	    try {
	    	if (c == backCommand) {
	            midlet.backMainMenu();                
	        }           
	    } catch (Exception e) {
	        midlet.fatalError("AboutForm:commandAction " + e.toString());
	    }
	}

}
