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
 * classname: XMLParseException
 *
 * desc: XMLParseException provides bespoke exception for XML readers / writers.
 * Exceptions raised can then be handled differently.
 */

package xml;

public class XMLParseException extends Exception {
    public final static short MINOR = 0;    
    public final static short IMPORTANT = 1;
    
    public short severity;

    public XMLParseException(String errorString, short severity) {
        super(errorString);
        this.severity = severity;
    } 
}