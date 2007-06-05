package org.exoplatform.services.cifs;

import java.util.Calendar;
import java.util.Vector;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Session;

import org.picocontainer.Startable;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cifs.server.NetworkServer;
import org.exoplatform.services.cifs.smb.ShareType;
import org.exoplatform.services.cifs.smb.server.SMBServer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.impl.core.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.impl.ExoLog;
import org.exoplatform.container.StandaloneContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Karpenko Sergey
 * 
 */

public class CIFSServiceImpl implements CIFSService, Startable {
  
	  private Log log = ExoLogger.getLogger("org.exoplatform.services.CIFS.CIFSServiceImpl");
	  
	  // Server configuration
	  private ServerConfiguration config = null;

	  private RepositoryService repositoryService = null;
	  
	  private NetworkServer server;
  
	  public CIFSServiceImpl(InitParams params,
		      RepositoryService rep){
		  
		    repositoryService = rep;
        
        if(params==null){
          config = new ServerConfiguration();
        }
        else{
          config = new ServerConfiguration(params);
        }
	  }

	  public void start(){
		  try{
				log.info("Starting CIFS service");
				server = new SMBServer(config,repositoryService);
				server.startServer();
		  }
		  catch(Exception e){
			  e.printStackTrace();
		  }
	  }
	
	  public void stop(){
		  log.info("Stoping...");
		  try{
			  if (server!=null) server.shutdownServer(false);
        repositoryService = null;
		  }
		  catch(Exception e){
			  e.printStackTrace();
		  }
	  }
	  
	  public ServerConfiguration getConfiguration(){
		  return config;
	  }
}
