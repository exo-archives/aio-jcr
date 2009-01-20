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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 20.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AbstractMergeUseCases.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public abstract class AbstractMergeUseCases extends BaseStandaloneTest {

  private static final Log log = ExoLogger.getLogger("ext.AbstractMergeUseCases");

  private abstract class BaseMergeUseCase {
    protected final SessionImpl sessionLowPriority;

    protected final SessionImpl sessionHighPriority;

    public BaseMergeUseCase(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      this.sessionLowPriority = sessionLowPriority;
      this.sessionHighPriority = sessionHighPriority;
    }

    public final boolean checkEquals() throws Exception {
      return isNodesEquals(sessionHighPriority.getRootNode(), sessionLowPriority.getRootNode());
    }

    public abstract void initDataLowPriority() throws Exception;
    
    public abstract void initDataHighPriority() throws Exception;

    public abstract void useCaseLowPriority() throws Exception;
    
    public abstract void useCaseHighPriority() throws Exception;
  }

  /**
   * Demo usecase 1 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Add text file /fileA.txt on server 1
   * 
   * 2. Add text file /fileB.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if files exist, if content of files same as original.
   */
  public class UseCase1 extends BaseMergeUseCase {
    public UseCase1(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.getRootNode().getNode("item1").setProperty("fileA", "dataA");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.getRootNode().getNode("item1").setProperty("fileB", "dataB");
      sessionLowPriority.save();
    }
  }
  
  /**
   * Demo usecase 2 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Add text file /fileA.txt on server 1
   * 
   * 2. Add text file /fileA.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt exists only, if /fileA.txt content equals to
   * server1
   */
  public class UseCase2 extends BaseMergeUseCase {
    public UseCase2(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      sessionHighPriority.getRootNode().addNode("item1");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
      
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.getRootNode().getNode("item1").setProperty("fileA", "dataA");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.getRootNode().getNode("item1").setProperty("fileA", "dataB");
      sessionLowPriority.save();
    }
  }
  
  /**
   * Demo usecase 3 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for files /fileA.txt, /fileB.txt on both servers
   * 
   * 2. Remove file /fileA.txt on server 1
   * 
   * 3. Remove file /fileB.txt on server 2
   * 
   * 4. Initialize synchronization on server 1
   * 
   * 5. Initialize synchronization on server 2
   * 
   * 6. After synchronization ends check if no files exists on both servers
   */
  public class UseCase3 extends BaseMergeUseCase {
    public UseCase3(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("fileA", "dataA");
      node.setProperty("fileB", "dataB");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.getRootNode().getNode("item1").getProperty("fileA").remove();
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.getRootNode().getNode("item1").getProperty("fileB").remove();
      sessionLowPriority.save();
    }
  }
  
  /**
   * Demo usecase 4 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Remove file /fileA.txt on server 1
   * 
   * 3. Edit file /fileA.txt on server 2
   * 
   * 4. Initialize synchronization on server 1
   * 
   * 5. Initialize synchronization on server 2
   * 
   * 6. After synchronization ends check if /fileA.txt deleted both servers
   */
  public class UseCase4 extends BaseMergeUseCase {
    public UseCase4(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("fileA", "dataA");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.getRootNode().getNode("item1").getProperty("fileA").remove();
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.getRootNode().getNode("item1").setProperty("fileB", "dataNew");
      sessionLowPriority.save();
    }
  }
  
  /**
   * Demo usecase 5 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Edit file /fileA.txt on server 1
   * 
   * 3. Remove file /fileA.txt on server 2
   * 
   * 4. Initialize synchronization on server 1
   * 
   * 5. Initialize synchronization on server 2
   * 
   * 6. After synchronization ends check if /fileA.txt exists on both servers, if /fileA.txt content
   * equals to edited on server 1
   */
  public class UseCase5 extends BaseMergeUseCase {
    public UseCase5(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("fileA", "data");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.getRootNode().getNode("item1").setProperty("fileA", "dataNew");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.getRootNode().getNode("item1").getProperty("fileA").remove();
      sessionLowPriority.save();
    }
  }
  
  /**
   * Demo usecase 8 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Rename /fileA.txt to /fileZZ.txt on server 1
   * 
   * 3. Edit /fileA.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if file /fileZZ.txt only exists on both servers with
   * content from server 1
   */
  public class UseCase8 extends BaseMergeUseCase {
    public UseCase8(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("fileA", "data");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.move("/item1", "/item2");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.getRootNode().getNode("item1").setProperty("fileA", "dataNew");
      sessionLowPriority.save();
    }
  }
  
  /**
   * Demo usecase 9 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Edit /fileA.txt on server 1
   * 
   * 3. Rename /fileA.txt to /fileZZ.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if file /fileA.txt only exist son both servers with content
   * from server 1 (edited)
   */
  public class UseCase9 extends BaseMergeUseCase {
    public UseCase9(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("fileA", "data");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.getRootNode().getNode("item1").setProperty("fileA", "dataNew");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.move("/item1", "/item2");
      sessionLowPriority.save();
    }
  }
  
  /**
   * Demo usecase 12 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Edit text file /fileA.txt on server 1
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt content equals to edited to edited on both
   * servers
   */
  public class UseCase12 extends BaseMergeUseCase {
    public UseCase12(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("fileA", "data");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.getRootNode().getNode("item1").setProperty("fileA", "dataNew");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
    }
  }
  
  /**
   * Demo usecase 13 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Edit text file /fileA.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt content equals to edited to edited on both
   * servers
   */
  public class UseCase13 extends BaseMergeUseCase {
    public UseCase13(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("fileA", "data");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.getRootNode().getNode("item1").setProperty("fileA", "dataNew");
      sessionLowPriority.save();
    }
  }
  
  /**
   * Demo usecase 14 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Edit text file /fileA.txt on server 1
   * 
   * 3. Edit text file /fileA.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt content equals to edited on server 1 on both
   * servers
   */
  public class UseCase14 extends BaseMergeUseCase {
    public UseCase14(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("fileA", "data");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.getRootNode().getNode("item1").setProperty("fileA", "dataHigh");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.getRootNode().getNode("item1").setProperty("fileA", "dataLow");
      sessionLowPriority.save();
    }
  }
  
  /**
   * Demo usecase 15 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Delete file /fileA.txt on server 1
   * 
   * 3. Edit text file /fileA.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt not exists on both server
   */
  public class UseCase15 extends BaseMergeUseCase {
    public UseCase15(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("fileA", "data");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.getRootNode().getNode("item1").getProperty("fileA").remove();
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.getRootNode().getNode("item1").setProperty("fileA", "dataLow");
      sessionLowPriority.save();
    }
  }
  
  /**
   * Demo usecase 16 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Delete file /fileA.txt on server 2
   * 
   * 3. Edit text file /fileA.txt on server 1
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt not exists on both server
   */
  public class UseCase16 extends BaseMergeUseCase {
    public UseCase16(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("fileA", "data");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.getRootNode().getNode("item1").setProperty("fileA", "dataLow");;
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.getRootNode().getNode("item1").getProperty("fileA").remove();
      sessionLowPriority.save();
    }
  }
  
  /**
   * Demo usecase 17 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt and folder /folder1 on both servers
   * 
   * 2. Edit text file /fileA.txt on server 1
   * 
   * 3. Move file /fileA.txt to /folder1/fileAA.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt exists on both server and content equals to
   * edited
   */
  public class UseCase17 extends BaseMergeUseCase {
    public UseCase17(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("fileA", "data");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.getRootNode().getNode("item1").setProperty("fileA", "dataLow");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.move("/item1", "/item2");
      sessionLowPriority.save();
    }
  }
  
  /**
   * Demo usecase 18 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt and folder /folder1 on both servers
   * 
   * 2. Edit text file /fileA.txt on server 2
   * 
   * 3. Move file /fileA.txt to /folder1/fileAA.txt on server 1
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt exists on both server and content equals to
   * edited
   */
  public class UseCase18 extends BaseMergeUseCase {
    public UseCase18(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("fileA", "data");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.move("/item1", "/item2");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.getRootNode().getNode("item1").setProperty("fileA", "dataLow");
      sessionLowPriority.save();
    }
  }
  
  /**
   * Compare two nodes.
   * 
   * @param src
   * @param dst
   * @return
   */
  protected boolean isNodesEquals(Node src, Node dst) throws Exception {
    // compare node name and UUID
    if (!src.getName().equals(dst.getName())
        || src.isNodeType("mix:referenceable") != dst.isNodeType("mix:referenceable")
        || (src.isNodeType("mix:referenceable") && dst.isNodeType("mix:referenceable") && !src.getUUID()
                                                                                              .equals(dst.getUUID()))) {
      log.error("Nodes names are not equals: " + src.getName() + " | " + dst.getName());
      return false;
    }

    // compare properties
    PropertyIterator srcProps = src.getProperties();
    PropertyIterator dstProps = dst.getProperties();
    while (srcProps.hasNext()) {
      if (!dstProps.hasNext()) {
        log.error("Second node has no property: " + srcProps.nextProperty().getName());
        return false;
      }

      PropertyImpl srcProp = (PropertyImpl) srcProps.nextProperty();
      PropertyImpl dstProp = (PropertyImpl) dstProps.nextProperty();

      if (!srcProp.getName().equals(dstProp.getName()) || srcProp.getType() != dstProp.getType()) {
        log.error("Properties names are not equals: " + srcProp.getName() + " | "
            + dstProp.getName());
        return false;
      }

      Value srcValues[];
      if (srcProp.isMultiValued()) {
        srcValues = srcProp.getValues();
      } else {
        srcValues = new Value[1];
        srcValues[0] = srcProp.getValue();
      }

      Value dstValues[];
      if (dstProp.isMultiValued()) {
        dstValues = dstProp.getValues();
      } else {
        dstValues = new Value[1];
        dstValues[0] = dstProp.getValue();
      }

      if (srcValues.length != dstValues.length) {
        log.error("Length of properties values are not equals: " + srcProp.getName() + " | "
            + dstProp.getName());
        return false;
      }

      for (int i = 0; i < srcValues.length; i++) {
        if (!srcValues[i].equals(dstValues[i])) {
          log.error("Properties values are not equals: " + srcProp.getName() + "|"
              + dstProp.getName());
          return false;
        }
      }
    }

    if (dstProps.hasNext()) {
      log.error("First node has no property: " + dstProps.nextProperty().getName());
      return false;
    }

    // compare child nodes
    NodeIterator srcNodes = src.getNodes();
    NodeIterator dstNodes = dst.getNodes();
    while (srcNodes.hasNext()) {
      if (!dstNodes.hasNext()) {
        log.error("Second node has no child node: " + srcNodes.nextNode().getName());
        return false;
      }

      if (!isNodesEquals(srcNodes.nextNode(), dstNodes.nextNode())) {
        return false;
      }
    }

    if (dstNodes.hasNext()) {
      log.error("First node has no child node: " + dstNodes.nextNode().getName());
      return false;
    }

    return true;
  }

}
