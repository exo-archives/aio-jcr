/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public interface WebDavConfig {

  String getDefIdentity();
  
  String getAuthHeader();
  
  String getDefFolderNodeType();
  
  String getDefFileNodeType();
  
  String getDefFileMimeType();
  
  String getUpdatePolicyType();
  
  Vector<String> defSearchNodeTypes();
  
}
