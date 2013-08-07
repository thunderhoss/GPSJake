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
 * classname: FileAction
 *
 * desc: FileAction class - code is based upon the FileConnection demo from the Sony Ericson developer site
 */

package gpsjake;

/*****************************************************************************
 *
 * Description: FileAction
 * The FileAction class is used for fileSystem funtionality such as list Directory content,
 * save file, load image and delete file/directory.
 * The object that creates fileAction object has to implement the FileActionInvoker interface since
 * it takes a reference to a FileActionInvoker to its constructor.
 * The fileAction object calls methods of the FileActionInvoker when it is done performing the
 * filesystemfunctionality
 *
 *
 * Created By: Johan Kateby
 *
 * @file        FileAction.java
 *
 * COPYRIGHT All rights reserved Sony Ericsson Mobile Communications AB 2004.
 * The software is the copyrighted work of Sony Ericsson Mobile Communications AB.
 * The use of the software is subject to the terms of the end-user license
 * agreement which accompanies or is included with the software. The software is
 * provided "as is" and Sony Ericsson specifically disclaim any warranty or
 * condition whatsoever regarding merchantability or fitness for a specific
 * purpose, title or non-infringement. No warranty of any kind is made in
 * relation to the condition, suitability, availability, accuracy, reliability,
 * merchantability and/or non-infringement of the software provided herein.
 *
 *****************************************************************************/

import java.util.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;

public class FileAction {
    
    // The object to be notified when a file system operation has finished
    FileActionInvoker browser;
    
    public FileAction(FileActionInvoker browser) {
        this.browser=browser;
    }
    
    // get the content of a directory and send the content to the fileActionInvoker
    // by callig the method browser.updateDirList
    
    public synchronized void getDirContent(String dir) {
  
        Enumeration CurrentDirEnum = null ;
        FileConnection currDirFC = null;
        
        boolean isRoot=false;
        
        try {
            if (dir.equals("/")) {
                CurrentDirEnum = FileSystemRegistry.listRoots();
                isRoot=true;
            } else {
                currDirFC = (FileConnection)Connector.open("file://"+dir);
                if (currDirFC!=null) {
                    CurrentDirEnum = currDirFC.list();
                }
            }
        } catch(SecurityException e) {
                CurrentDirEnum=null;   
        } catch(Exception ex) {
                CurrentDirEnum=null;   
        } finally {
            try {
                if (currDirFC!=null) {
                    currDirFC.close();
                }             
                currDirFC=null;
            } catch(Exception e) {
            }
            browser.updateDirList(CurrentDirEnum, isRoot, dir);
        }
    }
}
