/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 13.01.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: ChangesSubscriberTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class ChangesSubscriberTest extends AbstractTrasportTest {
  
  private static final String CH_NAME     = "AsyncRepCh_Test";

  private static final String bindAddress = "127.0.0.1";

  public void testOnStartLocalEvent() throws Exception {
    List<String> repositoryNames = new ArrayList<String>();
    repositoryNames.add(repository.getName());
    
    int priority = 50;
    int waitAllMemberTimeout = 60; // 60 seconds.
    
    File storage = new File("../target/temp/storage/" + System.currentTimeMillis());
    storage.mkdirs();
    
    List<Integer> otherParticipantsPriority = new ArrayList<Integer>();
    otherParticipantsPriority.add(100);
    
    AsyncReplication asyncReplication = new AsyncReplication(repositoryService,
                                                             repositoryNames,
                                                             priority,
                                                             bindAddress,
                                                             CH_CONFIG, 
                                                             CH_NAME,
                                                             waitAllMemberTimeout,
                                                             storage.getAbsolutePath(),
                                                             otherParticipantsPriority);
    
    asyncReplication.start();
    
    asyncReplication.synchronize();
  }
}
