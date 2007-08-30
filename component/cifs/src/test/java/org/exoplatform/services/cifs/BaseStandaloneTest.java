/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cifs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 *  
 */

public abstract class BaseStandaloneTest extends TestCase {
  protected static Log logger = ExoLogger.getLogger("jcr.JCRTest");

  // jcr repository configuration file for cifs server test purposes!
  public static String confURL = "conf/standalone/cifs-configuration.xml";

  // JAAS auth file
  public static String confAuth = "login.conf";

  // Standalone container has CIFSserver inside.

  public static RepositoryService repositoryService = null; 

  public static CIFSServiceImpl serv = null;

  public void setUp() throws Exception {

    // check if container not already run

    if ((repositoryService == null) || (serv == null)) {
      
      logger.debug("container == null");
      URL configurationURL = Thread.currentThread().getContextClassLoader()
          .getResource(confURL);
      if (configurationURL == null)
        throw new Exception("No configuration found. Check that \"" + confURL
            + "\" exists !");

      StandaloneContainer.addConfigurationURL(configurationURL.toString());

      // obtain standalone container
      StandaloneContainer container = StandaloneContainer.getInstance();

      // set JAAS auth config

      URL loginURL = Thread.currentThread().getContextClassLoader()
          .getResource(confAuth);

      if (loginURL == null)
        throw new Exception("No login config found. Check that resource "
            + confAuth + " exists !");

      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", loginURL
            .toString());

      serv = (CIFSServiceImpl) container
          .getComponentInstanceOfType(CIFSServiceImpl.class);
      repositoryService = (RepositoryService) container
          .getComponentInstanceOfType(RepositoryService.class);
    }
  }

  protected void compareStream(InputStream etalon, InputStream data, long etalonPos, long dataPos, long length) throws IOException, Exception {

    int index = 0;
    
    byte[] ebuff = new byte[64 * 1024];
    int eread = 0;
    ByteArrayOutputStream buff = new ByteArrayOutputStream();
    
    skipStream(etalon, etalonPos);
//    if (etalonPos > 0) {
//      long pos = etalonPos; 
//      long sk = 0;
//      long sks = 0;
//      while (sks < etalonPos && (sk = etalon.skip(etalonPos)) > 0) {
//        sks += sk;
//      };
//      if (sk <0)
//        fail("Can not read the etalon (skip bytes)");
//      if (sks < dataPos)
//        fail("Can not skip bytes from the etalon (" + etalonPos + " bytes)");
//    }
    
    skipStream(data, dataPos);
//    if (dataPos > 0) {
//      long sk = 0;
//      long sks = 0;
//      while (sks < dataPos && (sk = data.skip(dataPos)) > 0) {
//        sks += sk;
//      };
//      if (sk <0)
//        fail("Can not read the data (skip bytes)");
//      if (sks < dataPos)
//        fail("Can not skip bytes from the data (" + dataPos + " bytes)");
//    }
    
    while ((eread = etalon.read(ebuff)) > 0) {

      byte[] dbuff = new byte[eread];
      while (buff.size() < eread) {
        int dread = -1;
        try {
          dread = data.read(dbuff);
        } catch(IOException e) {
          throw new Exception("Streams is not equals by length or data stream is unreadable. Cause: " + e.getMessage());
        }
        buff.write(dbuff, 0, dread);
      }

      dbuff = buff.toByteArray();

      for (int i=0; i<eread; i++) {
        byte eb = ebuff[i];
        byte db = dbuff[i];
        if (eb != db)
          throw new Exception(
              "Streams is not equals. Wrong byte stored at position " + index + " of data stream. Expected 0x" + 
              Integer.toHexString(eb) + " '" + new String(new byte[] {eb}) + 
              "' but found 0x" + Integer.toHexString(db) + " '" + new String(new byte[] {db}) + "'");
        
        index++;
        if (length > 0 && index >= length)
          return; // tested length reached
      }

      buff = new ByteArrayOutputStream();
      if (dbuff.length > eread) {
        buff.write(dbuff, eread, dbuff.length);
      }
    }

    if (buff.size() > 0 || data.available() > 0)
      throw new Exception("Streams is not equals by length. Readed " + index);
  }  
  
  
  protected void skipStream(InputStream stream, long pos) throws IOException {
    long curPos = pos; 
    long sk = 0;
    while ((sk = stream.skip(curPos)) > 0) {
      curPos -= sk; 
    };
    if (sk <0)
      fail("Can not read the stream (skip bytes)");
    if (curPos != 0)
      fail("Can not skip bytes from the stream (" + pos + " bytes)");
  }

  
}
