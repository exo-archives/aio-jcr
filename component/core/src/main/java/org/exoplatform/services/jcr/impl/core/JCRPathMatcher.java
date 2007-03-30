/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import org.exoplatform.services.jcr.datamodel.InternalQPath;

/**
 * Created by The eXo Platform SARL
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 19.09.2006
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: JCRPathMatcher.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class JCRPathMatcher {
  
  private InternalQPath knownPath = null;
  private boolean forDescendants = false;
  private boolean forAncestors = false;
  
  public JCRPathMatcher(InternalQPath knownPath, boolean forDescendants, boolean forAncestors) {
    this.knownPath = knownPath;
    this.forDescendants = forDescendants;
    this.forAncestors = forAncestors;
  }
  
  public boolean match(InternalQPath path) {
    
    // any, e.g. * 
    if (forDescendants && forAncestors && knownPath == null)
      return true;
    
    // descendants, e.g. /item/*
    if (forDescendants && knownPath != null) {
      return path.isDescendantOf(knownPath, false);
    }
    
    // ancestors, e.g. */item/
    if (forDescendants && knownPath != null) {
      return knownPath.isDescendantOf(path, false);
    }

//    if (forAncestors && knownPath != null) {
//      return path.isAncestorOf(knownPath, false);
//    }
    
    // descendants or ancestors, e.g. */item/*
    if (forDescendants && forAncestors && knownPath != null) {
      return path.isDescendantOf(knownPath, false) && knownPath.isDescendantOf(path, false);
    }

//    if (forDescendants && forAncestors && knownPath != null) {
//      return path.isDescendantOf(knownPath, false) && path.isAncestorOf(knownPath, false);
//    }
    
    return knownPath.equals(path);
  }

}
