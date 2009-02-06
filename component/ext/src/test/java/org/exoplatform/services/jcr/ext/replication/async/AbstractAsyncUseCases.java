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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.storage.ResourcesHolder;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 20.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AbstractMergeUseCases.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public abstract class AbstractAsyncUseCases extends BaseStandaloneTest {

  private static final Log  log       = ExoLogger.getLogger("ext.AbstractMergeUseCases");

  protected ResourcesHolder resHolder = new ResourcesHolder();

  protected class TesterRandomChangesFile
                                         extends
                                         org.exoplatform.services.jcr.ext.replication.async.storage.RandomChangesFile {

    static final String PREFIX = "ChangesFile";

    static final String SUFIX  = "SUFIX";

    public TesterRandomChangesFile(String crc, long id) throws IOException {
      super(File.createTempFile(PREFIX, SUFIX), crc, id, resHolder);
    }

    public TesterRandomChangesFile(File f, String crc, long id) throws IOException {
      super(f, crc, id, resHolder);
    }
  }

  protected abstract class BaseMergeUseCase {
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
   * Complex UseCase1 1 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Add node1, rename node to node2 on server 1
   * 
   * 2. Add node1, rename node to node2 on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   */
  public class ComplexUseCase1 extends BaseMergeUseCase {
    public ComplexUseCase1(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.addMixin("mix:referenceable");
      sessionHighPriority.move("/item1", "/item2");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      Node node = sessionLowPriority.getRootNode().addNode("item1");
      node.addMixin("mix:referenceable");
      sessionLowPriority.move("/item1", "/item2");
      sessionLowPriority.save();
    }
  }

  /**
   * Complex UseCase1 1 (server 1 - high priority, server 2 -low priority)
   * 
   * Update property with size = 200kb
   */
  public class ComplexUseCase2 extends BaseMergeUseCase {
    public ComplexUseCase2(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      StringBuilder str = new StringBuilder();
      for (int i = 0; i < 20000; i++)
        str.append("aaaaaaaaaa");

      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("prop1", new ByteArrayInputStream(str.toString().getBytes()));
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
    }
  }

  /**
   * Complex UseCase1 3 (server 1 - high priority, server 2 -low priority)
   */
  public class ComplexUseCase3 extends BaseMergeUseCase {
    public ComplexUseCase3(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item2");
      node.addNode("item21");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      Node node = sessionLowPriority.getRootNode().addNode("item1");
      Node node11 = node.addNode("item11");
      Node node111 = node11.addNode("item111");
      node111.remove();
      node11.addNode("item112");
      sessionLowPriority.move("/item1", "/item2");
      sessionLowPriority.save();
    }
  }

  /**
   * Complex UseCase4 (server 1 - high priority, server 2 -low priority)
   * 
   * With complex node type (nt:file + mixin dc:elementSet)
   */
  public class ComplexUseCase4 extends BaseMergeUseCase {
    public ComplexUseCase4(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node test = sessionHighPriority.getRootNode().addNode("cms1");
      Node cool = test.addNode("nnn", "nt:file");
      Node contentNode = cool.addNode("jcr:content", "nt:resource");
      contentNode.setProperty("jcr:encoding", "UTF-8");
      contentNode.setProperty("jcr:data", new FileInputStream(createBLOBTempFile(5225)));
      contentNode.setProperty("jcr:mimeType", "application/octet-stream");
      contentNode.setProperty("jcr:lastModified",
                              sessionHighPriority.getValueFactory()
                                                 .createValue(Calendar.getInstance()));

      cool.addMixin("dc:elementSet");

      cool.setProperty("dc:creator", new String[] { "Creator 1", "Creator 2", "Creator 3" });

      ValueFactory vf = cool.getSession().getValueFactory();
      cool.setProperty("dc:date", new Value[] { vf.createValue(Calendar.getInstance()),
          vf.createValue(Calendar.getInstance()), vf.createValue(Calendar.getInstance()) });

      cool.setProperty("dc:source", new String[] { "Source 1", "Source 2", "Source 3" });
      cool.setProperty("dc:description", new String[] { "description 1", "description 2",
          "description 3", "description 4" });
      cool.setProperty("dc:publisher", new String[] { "publisher 1", "publisher 2", "publisher 3" });
      cool.setProperty("dc:language", new String[] { "language 1", "language 2", "language3",
          "language 4", "language5" });

      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
      Node test = sessionLowPriority.getRootNode().addNode("cms1");
      Node cool = test.addNode("nnn", "nt:file");
      Node contentNode = cool.addNode("jcr:content", "nt:resource");
      contentNode.setProperty("jcr:encoding", "UTF-8");
      contentNode.setProperty("jcr:data", new FileInputStream(createBLOBTempFile(3521)));
      contentNode.setProperty("jcr:mimeType", "application/octet-stream");
      contentNode.setProperty("jcr:lastModified",
                              sessionLowPriority.getValueFactory()
                                                .createValue(Calendar.getInstance()));

      cool.addMixin("dc:elementSet");

      cool.setProperty("dc:creator", new String[] { "Creator 1", "Creator 2", "Creator 3" });

      ValueFactory vf = cool.getSession().getValueFactory();
      cool.setProperty("dc:date", new Value[] { vf.createValue(Calendar.getInstance()),
          vf.createValue(Calendar.getInstance()), vf.createValue(Calendar.getInstance()) });

      cool.setProperty("dc:source", new String[] { "Source 1", "Source 2", "Source 3" });
      cool.setProperty("dc:description", new String[] { "description 1", "description 2",
          "description 3", "description 4" });
      cool.setProperty("dc:publisher", new String[] { "publisher 1", "publisher 2", "publisher 3" });
      cool.setProperty("dc:language", new String[] { "language 1", "language 2", "language3",
          "language 4", "language5" });

      sessionLowPriority.save();
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      Node contentNode = sessionHighPriority.getRootNode()
                                            .getNode("cms1")
                                            .getNode("nnn")
                                            .getNode("jcr:content");
      contentNode.setProperty("jcr:data", new FileInputStream(createBLOBTempFile(1521)));
      contentNode.setProperty("jcr:lastModified",
                              sessionHighPriority.getValueFactory()
                                                 .createValue(Calendar.getInstance()));

      Node cool = contentNode.getParent();
      cool.setProperty("dc:source", new String[] { "Source H 1", "Source h 2" });

      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      Node contentNode = sessionLowPriority.getRootNode()
                                           .getNode("cms1")
                                           .getNode("nnn")
                                           .getNode("jcr:content");
      contentNode.setProperty("jcr:data", new FileInputStream(createBLOBTempFile(2521)));
      contentNode.setProperty("jcr:lastModified",
                              sessionLowPriority.getValueFactory()
                                                .createValue(Calendar.getInstance()));

      Node cool = contentNode.getParent();
      cool.setProperty("dc:source", new String[] { "Source l 1", "Source L 2" });
      cool.setProperty("dc:identifier", new String[] { "Id L 1", "Ident l 2", "Ident_L3" });

      sessionLowPriority.save();
    }

  }

  /**
   * Complex UseCase5 (server 1 - high priority, server 2 -low priority)
   * 
   * With complex node type (nt:file + mixin dc:elementSet)
   */
  public class ComplexUseCase5 extends BaseMergeUseCase {
    public ComplexUseCase5(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      Node node1 = sessionHighPriority.getRootNode().addNode("node_in_db1", "nt:unstructured");
      for (int j = 0; j < 1; j++) {
        for (int i = 0; i < 1; i++)
          node1.addNode("testNode_" + j + "_" + i, "nt:unstructured");
      }
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      Node node2 = sessionLowPriority.getRootNode().addNode("node_in_db2", "nt:unstructured");
      for (int j = 0; j < 1; j++) {
        for (int i = 0; i < 1; i++)
          node2.addNode("testNode_" + j + "_" + i, "nt:unstructured");
      }
      sessionLowPriority.save();
    }

  }

  /**
   * Complex UseCase6 Add node and twice move it
   * 
   */
  public class ComplexUseCase6 extends BaseMergeUseCase {
    public ComplexUseCase6(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.getRootNode().addNode("item1");
      sessionLowPriority.move("/item1", "/item2");
      sessionLowPriority.move("/item2", "/item3");
      sessionLowPriority.save();
    }

  }

  public class ComplexUseCase9 extends BaseMergeUseCase {
    public ComplexUseCase9(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("file");
      node = node.addNode("fileA");
      node.setProperty("data", "value");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.getRootNode().getNode("file").remove();
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.getRootNode().getNode("file").remove();

      Node node = sessionLowPriority.getRootNode().addNode("file");
      node = node.addNode("fileA");
      node.setProperty("data", "newValue");
      sessionLowPriority.save();
    }
  }

  public class ComplexUseCase10 extends BaseMergeUseCase {
    public ComplexUseCase10(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("file");
      node = node.addNode("fileA");
      node.setProperty("data", "value");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.getRootNode().getNode("file").remove();

      Node node = sessionHighPriority.getRootNode().addNode("file");
      node = node.addNode("fileA");
      node.setProperty("data", "newValue");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.move("/file", "/newFile");
      sessionLowPriority.save();
    }
  }

  public class ComplexUseCase11 extends BaseMergeUseCase {
    public ComplexUseCase11(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("folder2");
      node = sessionHighPriority.getRootNode().addNode("folder1");
      node = node.addNode("fileA");
      node.setProperty("data", "value");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      sessionHighPriority.move("/folder1/fileA", "/folder2/fileA");
      sessionHighPriority.move("/folder2/fileA", "/folder2/fileZZ");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      sessionLowPriority.move("/folder1/fileA", "/folder2/fileA");
      sessionLowPriority.move("/folder2/fileA", "/folder2/fileAA");
      sessionLowPriority.save();
    }
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
      sessionHighPriority.getRootNode().getNode("item1").setProperty("fileA", "dataLow");
      ;
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
   * Add tree of nodes item on low priority, already added on high priority.
   */
  public class AddSameTreeUseCase extends BaseMergeUseCase {
    public AddSameTreeUseCase(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.setProperty("prop1", "value1_b");
      node = node.addNode("item11");
      node.setProperty("prop11", "value11_b");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
      Node node = sessionLowPriority.getRootNode().addNode("item1");
      node.setProperty("prop1", "value1_a");
      node = node.addNode("item11");
      node.setProperty("prop11", "value11_a");
      sessionLowPriority.save();
    }

    @Override
    public void useCaseHighPriority() throws Exception {
    }

    @Override
    public void useCaseLowPriority() throws Exception {

    }
  }

  /**
   * Add tree of nodes item on low priority, already added on high priority.
   */
  public class AddDiffTreeUseCase extends BaseMergeUseCase {
    public AddDiffTreeUseCase(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item2");
      node.setProperty("prop1", "value1");
      node = node.addNode("item21");
      node.setProperty("prop11", "value11");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
      Node node = sessionLowPriority.getRootNode().addNode("item1");
      node.setProperty("prop1", "value1");
      node = node.addNode("item11");
      node.setProperty("prop11", "value11");
      sessionLowPriority.save();
    }

    @Override
    public void useCaseHighPriority() throws Exception {
    }

    @Override
    public void useCaseLowPriority() throws Exception {

    }
  }

  /**
   * 1. Add item on low priority, no high priority changes.
   */
  public class Add1_1_UseCase extends BaseMergeUseCase {
    public Add1_1_UseCase(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
    }

    @Override
    public void initDataLowPriority() throws Exception {
      Node node = sessionLowPriority.getRootNode().addNode("item1");
      node.addMixin("mix:referenceable");
      node.setProperty("prop1", "value3");
      sessionLowPriority.save();
    }

    @Override
    public void useCaseHighPriority() throws Exception {
    }

    @Override
    public void useCaseLowPriority() throws Exception {

    }
  }

  /**
   * 1. Add item on high priority, no low priority changes.
   */
  public class Add1_2_UseCase extends BaseMergeUseCase {
    public Add1_2_UseCase(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.addMixin("mix:referenceable");
      node.setProperty("prop1", "value4");
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

    }
  }

  /**
   * 2. Add item on low priority, already added on high priority.
   */
  public class Add2_x_UseCase extends BaseMergeUseCase {
    public Add2_x_UseCase(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.addMixin("mix:referenceable");
      node.setProperty("prop1", "value4");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
      Node node = sessionLowPriority.getRootNode().addNode("item1");
      node.addMixin("mix:referenceable");
      node.setProperty("prop1", "value3");
      sessionLowPriority.save();
    }

    @Override
    public void useCaseHighPriority() throws Exception {
    }

    @Override
    public void useCaseLowPriority() throws Exception {

    }
  }

  /**
   * 3. Add item on low priority already added and deleted on high priority.
   */
  public class Add3_1_UseCase extends BaseMergeUseCase {
    public Add3_1_UseCase(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      node.addMixin("mix:referenceable");
      node.remove();
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
      Node node = sessionLowPriority.getRootNode().addNode("item1");
      node.addMixin("mix:referenceable");
      sessionLowPriority.save();
    }

    @Override
    public void useCaseHighPriority() throws Exception {
    }

    @Override
    public void useCaseLowPriority() throws Exception {

    }
  }

  /**
   * 3. Add item on high priority already added and deleted on low priority.
   */
  public class Add3_2_UseCase extends BaseMergeUseCase {
    public Add3_2_UseCase(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().addNode("item1");
      sessionHighPriority.save();
    }

    @Override
    public void initDataLowPriority() throws Exception {
      Node node = sessionLowPriority.getRootNode().addNode("item1");
      node.remove();
      sessionLowPriority.save();
    }

    @Override
    public void useCaseHighPriority() throws Exception {
    }

    @Override
    public void useCaseLowPriority() throws Exception {

    }
  }

  /**
   * 4. Add Item on high priority to a deleted parent on low priority (conflict)
   */
  public class Add4_1_UseCase extends BaseMergeUseCase {
    public Add4_1_UseCase(SessionImpl sessionLowPriority, SessionImpl sessionHighPriority) {
      super(sessionLowPriority, sessionHighPriority);
    }

    @Override
    public void initDataHighPriority() throws Exception {
    }

    @Override
    public void initDataLowPriority() throws Exception {
      Node node = sessionLowPriority.getRootNode().addNode("item1");
      node.addMixin("mix:referenceable");
      sessionLowPriority.save();
    }

    @Override
    public void useCaseHighPriority() throws Exception {
      Node node = sessionHighPriority.getRootNode().getNode("item1").addNode("item11");
      sessionHighPriority.save();
    }

    @Override
    public void useCaseLowPriority() throws Exception {
      Node node = sessionLowPriority.getRootNode().getNode("item1");
      node.remove();
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
          if (srcValues[i] instanceof BinaryValue) {
            try {
              compareStream(srcValues[i].getStream(), dstValues[i].getStream());
              continue;
            } catch (Exception e) {
              return false;
            }
          }

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

    if (srcNodes.getSize() != dstNodes.getSize()) {
      log.error("Invalid child nodes count: " + src.getName());
      return false;
    }

    boolean res1 = true;
    while (srcNodes.hasNext()) {
      Node srcChildNode = srcNodes.nextNode();
      Node dstChildNode = dst.getNode(srcChildNode.getName());

      if (!isNodesEquals(srcChildNode, dstChildNode)) {
        res1 = false;
      }
    }

    srcNodes = src.getNodes();
    dstNodes = dst.getNodes();

    boolean res2 = true;
    while (srcNodes.hasNext()) {
      if (!isNodesEquals(srcNodes.nextNode(), dstNodes.nextNode())) {
        res2 = false;
      }
    }

    return res1 || res2;
  }

  /**
   * Compare two nodes.
   * 
   * @param src
   * @param dst
   * @return
   */
  protected boolean isNodesEquals(Node src, Node dst, SessionImpl sessionSrc, SessionImpl sessionDst) throws Exception {

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
          if (srcValues[i] instanceof BinaryValue) {
            try {
              compareStream(srcValues[i].getStream(), dstValues[i].getStream());
              continue;
            } catch (Exception e) {
              return false;
            }
          }

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

    if (srcNodes.getSize() != dstNodes.getSize()) {
      log.error("Invalid child nodes count: " + src.getName());
      return false;
    }

    while (srcNodes.hasNext()) {
      Node srcChildNode = srcNodes.nextNode();
      Node dstChildNode = sessionDst.getNodeByUUID(srcChildNode.getUUID());

      if (!isNodesEquals(srcChildNode, dstChildNode, sessionSrc, sessionDst)) {
        return false;
      }
    }

    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void tearDown() throws Exception {
    resHolder.close();

    super.tearDown();
  }

}
