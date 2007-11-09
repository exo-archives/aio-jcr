/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin;

import com.sun.star.awt.XToolkit;
import com.sun.star.frame.XFrame;
import com.sun.star.uno.XComponentContext;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class AboutDialog extends PlugInDialog {
  
  private static final String NAME = "_AboutDialog";
  
  public AboutDialog(WebDavConfig config, XComponentContext xComponentContext, XFrame xFrame, XToolkit xToolkit) {
    super(config, xComponentContext, xFrame, xToolkit);
    dialogName = NAME;
  }  
  
}
