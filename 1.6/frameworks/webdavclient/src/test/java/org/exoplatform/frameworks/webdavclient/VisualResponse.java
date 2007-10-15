/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.properties.CheckedInProp;
import org.exoplatform.frameworks.webdavclient.properties.CheckedOutProp;
import org.exoplatform.frameworks.webdavclient.properties.ContentLengthProp;
import org.exoplatform.frameworks.webdavclient.properties.ContentTypeProp;
import org.exoplatform.frameworks.webdavclient.properties.CreatorDisplayNameProp;
import org.exoplatform.frameworks.webdavclient.properties.DisplayNameProp;
import org.exoplatform.frameworks.webdavclient.properties.LastModifiedProp;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;
import org.exoplatform.frameworks.webdavclient.properties.ResourceTypeProp;
import org.exoplatform.frameworks.webdavclient.properties.VersionNameProp;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class VisualResponse {

  private static Log logTable = ExoLogger.getLogger("jcr.T");  

  public static void visualResponse(ResponseDoc response) {
    String borderTop =       "+---------------+---------------------------------------------------------------------------------------\\ ";
    String borderBottom =   " \\-------------/ \\--------------------------------------------------------------------------------------/ ";
    
    int availLength = borderTop.length() - 20;
    
    String href = response.getHref();
    String displayName = "";
    String isCollection = "";
    String contLength = "";
    String contType = "";
    String lastModified = "";
    String checkedIn = "";
    String checkedOut = "";
    String versionName = "";    
    String creatorDisplayName = "";
    
    PropApi displayNameProp = response.getProperty(Const.DavProp.DISPLAYNAME);
    if (displayNameProp != null) {
      displayName = ((DisplayNameProp)displayNameProp).getDisplayName();
    }

    PropApi resTypeProp = response.getProperty(Const.DavProp.RESOURCETYPE);
    if (resTypeProp != null) {
      boolean collection = ((ResourceTypeProp)resTypeProp).isCollection();
      isCollection = (collection)? "true" : "false";
    }

    PropApi contentLength = response.getProperty(Const.DavProp.GETCONTENTLENGTH);
    if (contentLength != null) {
      long contLen = ((ContentLengthProp)contentLength).getContentLength();
      contLength = String.format("%s", contLen);
    }

    PropApi contentType = response.getProperty(Const.DavProp.GETCONTENTTYPE);
    if (contentType != null) {
      contType = ((ContentTypeProp)contentType).getContentType();
    }
    
    PropApi chIn = response.getProperty(Const.DavProp.CHECKEDIN);
    if (chIn != null) {
      boolean isCheckedIn = ((CheckedInProp)chIn).isCheckedIn();
      checkedIn = (isCheckedIn) ? "true" : "false";
    }
    
    PropApi chOut = response.getProperty(Const.DavProp.CHECKEDOUT);
    if (chOut != null) {
      boolean isCheckedOut = ((CheckedOutProp)chOut).isCheckedOut();
      checkedOut = (isCheckedOut) ? "true" : "false";
    }

    PropApi verName = response.getProperty(Const.DavProp.VERSIONNAME);
    if (versionName != null) {
      versionName = ((VersionNameProp)verName).getVersionName();
    }
    
    PropApi lModified = response.getProperty(Const.DavProp.GETLASTMODIFIED);
    if (lModified != null) {
      lastModified = ((LastModifiedProp)lModified).getLastModified();
    }
    
    PropApi lCreatorDisplayName = response.getProperty(Const.DavProp.CREATORDISPLAYNAME);
    if (lCreatorDisplayName != null) {
      creatorDisplayName = ((CreatorDisplayNameProp)lCreatorDisplayName).getCreatorDisplayName();
    }
    
    href = getTruncated(response.getHref(), availLength);
    displayName = getTruncated("<" + displayName + ">", availLength);
    isCollection = getTruncated(isCollection, availLength);
    contLength = getTruncated(contLength, availLength);
    contType = getTruncated(contType, availLength);
    lastModified = getTruncated(lastModified, availLength);
    checkedIn = getTruncated(checkedIn, availLength);
    checkedOut = getTruncated(checkedOut, availLength);
    versionName = getTruncated(versionName, availLength);
    creatorDisplayName = getTruncated(creatorDisplayName, availLength);
    
    logTable.info(borderTop);    
    logTable.info("| HREF          | " + href +               "|");
    logTable.info("| DISPLAYNAME   | " + displayName +        "|");
    logTable.info("| ISCOLLECTION  | " + isCollection +       "|");
    logTable.info("| CONTENTLENGTH | " + contLength +         "|");
    logTable.info("| CONTENTTYPE   | " + contType +           "|");
    logTable.info("| LASTMODIFIED  | " + lastModified +       "|");
    logTable.info("| CHECKED-IN    | " + checkedIn +          "|");
    logTable.info("| CHECKED-OUT   | " + checkedOut +         "|");    
    logTable.info("| VERSION-NAME  | " + versionName +        "|");
    logTable.info("| CR-DISPL-NAME | " + creatorDisplayName + "|");
    logTable.info(borderBottom);
    
  }
  
  protected static String getTruncated(String param, int maxValue) {
    try {
      String datka = "";
      for (int i = 0; i < maxValue; i++) {
        if (i >= param.length()) {
          datka += " ";
        } else {
          datka += param.charAt(i);
        }
      }
      return datka;
    } catch (Exception exc) {
      logTable.info("Unhandled exception. " + exc.getMessage());
      exc.printStackTrace();
    }
    return "";
  }
  
  
}
