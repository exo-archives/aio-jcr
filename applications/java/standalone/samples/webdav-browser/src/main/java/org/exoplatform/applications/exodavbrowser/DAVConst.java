/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.applications.exodavbrowser;

import java.io.InputStream;

import javax.swing.ImageIcon;



/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}
 */

public class DAVConst{

  public class Info{
    public static final String sTitle = "eXoDavBrowser Version: 1.6";
    public static final String sVersion = "Copyright 2001-2006 The eXo Platform SARL. All rights reserved.";
    public static final String sEXOSite = "http://www.exoplatform.org";
    public static final String sCopyright = "Visit " + sEXOSite;

  }

  public class Type{
    public static final int iCOPY = 1;
    public static final int iMOVE = 2;
  }

  public class SatusDialog{
    public static final int iOK = 1;
    public static final int iCANCEL = -1;
  }

  public class ButtonTitle{
    public static final String sGetFile = "Get File";
    public static final String sWriteFile = "Write File";
    public static final String sWriteFolder = "Write Folder";
    public static final String sExclisiveLock = "Exclusive Lock";
    public static final String sUnLock = "UnLock";
    public static final String sCreateFolder = "Create Folder";
    public static final String sExit = "Exit";
    public static final String sEdit = "Edit";
    public static final String sCut = "Cut";
    public static final String sCopy = "Copy";
    public static final String sPaste = "Paste";
    public static final String sDelete = "Delete";
    public static final String sVersion = "Version";
    public static final String sPutUnderVersionControl = "Put Under Version Control";
    public static final String sVersionReport= "Version Report";
    public static final String sCheckOut= "Check Out";
    public static final String sUncheckOut= "Uncheck Out";
    public static final String sCheckIn= "Check In";
    public static final String sView= "View";
    public static final String sRefresh= "Refresh";
    public static final String sHelp= "Help";
    public static final String sAboutDAVeXplorer= "About DAVeXplorer";
    public static final String sGo = "GO!                                  ";
  }

  public class TableColumn{
    public static final int ICON = 0;
    public static final int LOCK = 1;
    public static final int VERSION = 2;
    public static final int DISPLAYNAME = 3;
    public static final int TYPE = 4;
    public static final int SIZE = 5;
    public static final int DATE = 6;

  }
  public class Image{
    public static final String IMAGEPATH = "/org/exoplatform/applications/exodavbrowser/image/";
    public static final String sDirIcon = IMAGEPATH + "jtree_open.gif";
    public static final String sFileIcon = IMAGEPATH + "file.gif";
    public static final String sParentDirIcon = IMAGEPATH + "parent_dir.gif";
    public static final String sLockIcon = IMAGEPATH + "lock.png";
    public static final String sUnLockIcon = IMAGEPATH + "unlock.png";
    public static final String sVersionIcon = IMAGEPATH + "version.png";
    public static final String sGo = IMAGEPATH + "go.png";

    public static final String sStop = IMAGEPATH + "stop.png";
    public static final String sStopFocus = IMAGEPATH + "stopFocus.png";

    public static final String sMsgIcon = IMAGEPATH + "mess.png";
    public static final String sExologo = IMAGEPATH + "exo1.png";

    /*TOOLBAR ICONS*/
    public static final String sToolGetIcon = IMAGEPATH + "toolGet.png";
    public static final String sToolWriteIcon = IMAGEPATH + "toolWrite.png";

    public static final String sToolCopyIcon = IMAGEPATH + "toolCopy.png";
    public static final String sToolPasteIcon = IMAGEPATH + "toolPaste.png";
    public static final String sToolDeleteIcon = IMAGEPATH + "toolDelete.png";

    public static final String sToolLockIcon = IMAGEPATH + "toolLock.png";
    public static final String sToolUnLockIcon = IMAGEPATH + "toolUnLock.png";

    public static final String sToolPutVersionIcon = IMAGEPATH + "toolPutVersion.png";
    public static final String sToolCheckOutIcon = IMAGEPATH + "toolCheckOut.png";
    public static final String sToolUnCheckOutIcon = IMAGEPATH + "toolUnCheckOut.png";
    public static final String sToolCheckInIcon = IMAGEPATH + "toolCheckIn.png";
    public static final String sToolVersionReportIcon = IMAGEPATH + "toolReport.png";
  }

  public class Config{
    public static final String sConfigFileName = "eXoDavBrowser.eXo";
    public static final String sTampFileName = "eXoDavBrowser.eXo.tmp";
  }

  public ImageIcon getImage(String res){
    //InputStream inS = getClass().getResourceAsStream(res);
    ImageIcon bufferedImage = new ImageIcon();
    try {
      bufferedImage = new ImageIcon(getClass().getResource(res));
    } catch (Exception exc) {
      exc.printStackTrace();
    }
    return bufferedImage;
  }
}