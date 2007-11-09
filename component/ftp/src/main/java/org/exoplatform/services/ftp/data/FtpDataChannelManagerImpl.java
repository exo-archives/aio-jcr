/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.data;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.ftp.client.FtpClientSession;
import org.exoplatform.services.ftp.config.FtpConfig;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpDataChannelManagerImpl implements FtpDataChannelManager {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "FtpDataChannelManagerImpl");
  
  private FtpConfig configuration;
  
  private int dataChannels;
  
  //private Random random;
  
  protected FtpDataTransiver []channels; 
  
  public FtpDataChannelManagerImpl(FtpConfig configuration) {    
    this.configuration = configuration;    
    dataChannels = configuration.getDataMaxPort() - configuration.getDataMinPort() + 1;
    
    channels = new FtpDataTransiver[dataChannels];
  }

  public FtpDataTransiver getDataTransiver(FtpClientSession clientSession) {
    synchronized (this) {      
      for (int i = 0; i < channels.length; i++) {
        if (channels[i] == null) {
          try {
            FtpDataTransiver transiver = new FtpDataTransiverImpl(this, configuration.getDataMinPort() + i, configuration, clientSession);
            channels[i] = transiver;
            return transiver;
          } catch (Exception exc) {
            log.info("Unhandled exception. " + exc.getMessage(), exc);
          }
        }
      }
    }
    return null;
  }
  
  public void freeDataTransiver(FtpDataTransiver dataTransiver) {
    synchronized (this) {
      int dataPort = dataTransiver.getDataPort();
      int index = dataPort - configuration.getDataMinPort();
      channels[index] = null;
    }
  }
  
//  public int getFreeDataPort() {
//    int curRandomNum = random.nextInt(dataChannels);
//    //channels[resultChannel - dataMinPort] = 1;
//    return -1;
//  }
//  
//  public void releaseDataPort(int dataPort) {
//    
//  }  
  
}
