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
 * classname: FileBrowser
 *
 * desc: FileBrowser class - code is based upon the FileConnection demo from the Sony Ericson developer site
 */

package gpsjake;

import javax.microedition.lcdui.*;
import java.util.*;

abstract class FileBrowser extends List implements CommandListener, FileActionInvoker {
    
    private Command backCommand;
    
    private final static String FILE_SEPARATOR = "/";
    private final static String UPPER_DIR = "..";
    private final static String CURRENT_DIR = ".";
    private Vector fileTypes;
    private boolean showDirsOnly = false;
    
    private String currentDir="/";
    private String tryDir;
    
    // This vector contains the directories and files in thecurrent directory
    private Vector dirList = new Vector();
    
    private Image folderImage;
    private Image fileImage;
    
    // The fileAction object which we use to retrieve the directory contents
    protected FileAction fileAction= null;
    
    public GPSJakeMIDlet midlet;
    
    private String selectedFile;
    private String selectedDir;
    
    public FileBrowser(GPSJakeMIDlet midlet, Vector fileTypes, String title) {
        super("GPSjake", List.IMPLICIT);  
        try {
            this.midlet = midlet;
            this.fileTypes = fileTypes;
            setTitle(title);

            fileAction = new FileAction(this);
            currentDir=FILE_SEPARATOR;        

            // Load the images we are using
            createImages();

            backCommand = new Command("Back", Command.BACK, 1);
            addCommand(backCommand);
            setCommandListener(this);

            //Add an empty element to prevent "no data" appearing
            append("", null);        
            //Set the zero length string item as the selected item
            //but don't select it (prevent it being highlighted.
            this.setSelectedIndex(0, false);

            // Get the content of the root ("/") directory
            new Thread(new Runnable() {
                public void run() {
                    fileAction.getDirContent(currentDir);
                }
            }).start();
        } catch (Exception e) {
            midlet.fatalError("FileBrowser:FileBrowser " + e.toString());
        }          
    }
    
    public FileBrowser(GPSJakeMIDlet midlet, String title) {
        super("GPSjake", List.IMPLICIT);
        try {
            this.midlet = midlet;
            setTitle(title);        

            showDirsOnly = true;
            fileAction = new FileAction(this);
            currentDir=FILE_SEPARATOR;        

            // Load the images we are using
            createImages();

            backCommand = new Command("Back", Command.BACK, 1);
            addCommand(backCommand);
            setCommandListener(this);

            //Add an empty element to prevent "no data" appearing
            append("", null);        
            //Set the zero length string item as the selected item
            //but don't select it (prevent it being highlighted.
            this.setSelectedIndex(0, false);

            // Get the content of the root ("/") directory
            new Thread(new Runnable() {
                public void run() {
                    fileAction.getDirContent(currentDir);
                }
            }).start();
        } catch (Exception e) {
            midlet.fatalError("FileBrowser:FileBrowser " + e.toString());
        }            
    }
    
    public void commandAction(Command c, Displayable d) {
        try {
            if (c == backCommand) {
                back();
            } else if (c == List.SELECT_COMMAND) {
                int index = getSelectedIndex();
                String selectedItem = dirList.elementAt(index).toString();
                if (showDirsOnly && selectedItem.equals(CURRENT_DIR)) {
                    selectedDir = "file://" + currentDir;
                    fileSelected();
                } else if (!showDirsOnly && isFileType(selectedItem)){
                    selectedDir = "file://" + currentDir;
                    selectedFile = "file://" + currentDir + (String)dirList.elementAt(index);
                    fileSelected();
                } else {
                    traverse(index);
                }
            }
        } catch (Exception e) {
            midlet.fatalError("FileBrowser:commandAction " + e.toString());
        }              
    }
    
    private void appendDirsAndFiles(int indexOfLastDir) {
        String dirName;
        try {
            this.deleteAll();
            for (int i = 0; i < dirList.size(); i++) {
                if (i < indexOfLastDir) {
                    dirName = dirList.elementAt(i).toString();
                    //Trim the seperator from the directory name.
                    if (dirName.charAt(dirName.length() - 1) == '/') {
                        dirName = dirName.substring(0, dirName.length() - 1);
                    }
                    append(dirName, folderImage);
                } else {
                    append(dirList.elementAt(i).toString(), fileImage);                
                }
            }
            this.setSelectedIndex(0, true);
        } catch (Exception e) {
            midlet.fatalError("FileBrowser:appendDirsAndFiles " + e.toString());
        }               
    }
    
    public void updateDirList(Enumeration e, boolean isRoot, String returnedDir) {
        
        // This is a callback method used by the FileAction to send us
        // the directoryContent.
        
        int indexOfLastDir = 0;
        
        try {
            // if e is null somtheing went wrong and we don't do anything
            // throw a FileActionException exception.
            if (e == null) {
                throw new FileActionException("FileActionException");
            }
            //If everything went well set the current directory to the directory
            //that we successfully traversed to.
            currentDir = returnedDir;
            
            // remove all elements in the dirList vector
            dirList.removeAllElements();
            
            if (showDirsOnly && !isRoot) {
                dirList.addElement(CURRENT_DIR);
                indexOfLastDir++;
            }
            
            // If we are not in the root add the .. directory for going back in the filesystem
            if (!isRoot) {
                dirList.addElement(UPPER_DIR);
                indexOfLastDir++;                
            }
            
            Vector files = new Vector();
            
            // Add all the directories to the dirList vector
            while (e.hasMoreElements()) {
                String s =(String)e.nextElement();
                if (s.endsWith(FILE_SEPARATOR)) {// If it is a directory
                    //dirList.insertElementAt(s,lastDirIndex);
                    dirList.addElement(s);
                    indexOfLastDir++;                    
                }
                
                //Only add the files if we're displaying them. i.e. not in directory
                //browsing mode.
                if (!showDirsOnly) {
                    //Note fileTypes is null if showDirs == true
                    //as it won't have been initiated.
                    if (isFileType(s)) {
                        files.addElement(s);
                    }
                }
            }
            
            //Add all the files to the dirList
            for (int i = 0; i < files.size(); i++) {
                dirList.addElement(files.elementAt(i));
            }            
            
            //Add directory contents to list
            appendDirsAndFiles(indexOfLastDir);
        } catch (Exception ex) {
            midlet.importantError(ex.toString(), "FileBrowser:Can't browse selected folder.");
        }
    }
    
    // This method is called when the user presses fire button
    // to display the content in the marked directory
    private void traverse(int index) {
        try {
            String chosenDir = (String)dirList.elementAt(index);

            // If the marked directory is .. traverse back in the filesystem
            if (chosenDir.equals(UPPER_DIR)) {
                //Use FILE_SEPARATOR
                int i = currentDir.lastIndexOf('/', currentDir.length()-2);
                if (i != -1) {
                    tryDir = currentDir.substring(0, i+1);
                } else {
                    tryDir = FILE_SEPARATOR;
                }
            } else {
                // Else traverse into the marked Directory
                tryDir = currentDir + chosenDir;
            }

            // Start the thread to get the content of the chosenDir directory
            // the fileaction object will call the updateDirList method when it has retrieveed all the content.
            new Thread(new Runnable() {
                public void run() {
                    fileAction.getDirContent(tryDir);
                }
            }).start();
        } catch (Exception e) {
            midlet.fatalError("FileBrowser:traverse " + e.toString());
        }        
    }
    
    private boolean isFileType(String file) {
        try {
            for (Enumeration types = fileTypes.elements(); types.hasMoreElements();) {
                String fileType = (String) types.nextElement();
                if (file.toUpperCase().endsWith("." + fileType.toUpperCase())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            midlet.fatalError("FileBrowser:isFileType " + e.toString());
            return false;
        }
    }
   
    private void createImages() {
        try {
            folderImage = Image.createImage("/res/folder.PNG");
            fileImage = Image.createImage("/res/file.PNG");
        } catch (Exception e) {
            midlet.importantErrorLogOnly("FileBrowser:createImages " + e.toString());
        }
    }
    
    public String getSelectedFile() {
        return selectedFile;
    }
    
    public String getSelectedDir() {
        return selectedDir;
    }
    
    abstract void back();
    
    abstract void fileSelected();
    
}


