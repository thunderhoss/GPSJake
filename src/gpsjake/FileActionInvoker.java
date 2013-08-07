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
 * classname: FileActionInvoker
 *
 * desc: FileActionInvoker class - code is based upon the FileConnection demo from the Sony Ericson developer site
 */

package gpsjake;

/*****************************************************************************

 Description: FileActionInvoker interface
* FileAction class take a FileActionInvoker as a argument to its constuctor.
* This is done because the FileAction object will call certain methods in FileActionHandler 
* when excuting it's file system functionalities.
  
 Created By: Johan Kateby
 
 @file        FileActionInvoker.java

 COPYRIGHT All rights reserved Sony Ericsson Mobile Communications AB 2004.
 The software is the copyrighted work of Sony Ericsson Mobile Communications AB.
 The use of the software is subject to the terms of the end-user license 
 agreement which accompanies or is included with the software. The software is 
 provided "as is" and Sony Ericsson specifically disclaim any warranty or 
 condition whatsoever regarding merchantability or fitness for a specific 
 purpose, title or non-infringement. No warranty of any kind is made in 
 relation to the condition, suitability, availability, accuracy, reliability, 
 merchantability and/or non-infringement of the software provided herein.

*****************************************************************************/

import java.util.*;

public interface FileActionInvoker {
    
    // FileAction calls this method from its getDirContent method
    public void updateDirList(Enumeration CurrentDirEnum, boolean isRoot, String dir);
    
}
