/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.value;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: Probe.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class Probe extends Thread {
  
    private File file;
    private int len = 0;

    public Probe() {}

    public Probe(File file) {
      super();
      this.file = file;
    }

    public void run() {
      System.out.println("Thread started "+this.getName());
      try {
        FileInputStream is = new FileInputStream(file);
        while(is.read()>0) {
          len++;
        }
        
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.out.println("Thread finished "+this.getName()+" read: "+len);
    }

    public int getLen() {
      return len;
    }
}
