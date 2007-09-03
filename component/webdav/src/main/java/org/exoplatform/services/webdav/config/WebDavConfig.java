/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public interface WebDavConfig {

  String getDefIdentity();
  
  String getAuthHeader();
  
  boolean isAutoMixLockable();
  
  String getDefFolderNodeType();
  
  String getDefFileNodeType();
  
  String getDefFileMimeType();
  
  String getCataogName();
  
  String getRepositoryName();
  
  String getUpdatePolicyType();
  
  Vector<String> defSearchNodeTypes();
  
  ArrayList<HashMap<String, String>> getRequestDocuments();
  
  PropertyFactory getPropertyFactory();
  
}
