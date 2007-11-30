/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.value;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by The eXo Platform SAS.
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
