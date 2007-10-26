/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin.config;

import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class FilterType extends XmlConfig {

  public static final String XML_LOCALIZEDNAME = "localized-name";
  public static final String XML_APINAME = "api-name";
  public static final String XML_FILEEXTENSION = "file-extension";
  public static final String XML_MIMETYPE = "mime-type";
  
  private String documentName;  
  
  private String localizedName;
  private String apiName;
  private String fileExtension;
  private String mimeType;
  
  public FilterType(Node filterNode, String documentName) {    
    this.documentName = documentName;

    localizedName = getChildNode(filterNode, XML_LOCALIZEDNAME).getTextContent();
    apiName = getChildNode(filterNode, XML_APINAME).getTextContent();
    fileExtension = getChildNode(filterNode, XML_FILEEXTENSION).getTextContent();
    mimeType = getChildNode(filterNode, XML_MIMETYPE).getTextContent();
  }
  
  public String getDocumentName() {
    return documentName;
  }
  
  public String getLocalizedName() {
    return localizedName;
  }
  
  public String getApiName() {
    return apiName;
  }
  
  public String getFileExtension() {
    return fileExtension;
  }
  
  public String getMimeType() {
    return mimeType;
  }

}
