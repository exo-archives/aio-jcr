/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.importing;

import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class BackupContentImporter extends SystemViewImporter {
  /**
   * Class logger.
   */
  private final Log log                   = ExoLogger.getLogger("jcr.BackupContentImporter");

  /**
   * The flag indicates whether a verified that the first element is the root.
   */
  private boolean   isFirstElementChecked = false;

  /**
   * Class used to import content of workspace, using "System View XML Mapping",
   * while restoring data from backup.
   * 
   * @param parent
   * @param uuidBehavior
   * @param saveType
   * @param context
   */
  public BackupContentImporter(NodeImpl parent,
                               int uuidBehavior,
                               XmlSaveType saveType,
                               InvocationContext context) {
    super(parent, uuidBehavior, saveType, context);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.SystemViewImporter#startElement(java.lang.String,
   *      java.lang.String, java.lang.String, java.util.Map)
   */
  @Override
  public void startElement(String namespaceURI,
                           String localName,
                           String name,
                           Map<String, String> atts) throws RepositoryException {
    InternalQName elementName = locationFactory.parseJCRName(name).getInternalName();

    if (Constants.SV_NODE.equals(elementName)) {
      // sv:node element

      // node name (value of sv:name attribute)
      String svName = getAttribute(atts, Constants.SV_NAME);
      if (svName == null) {
        throw new RepositoryException("Missing mandatory sv:name attribute of element sv:node");
      }
      ImportNodeData newNodeData = null;
      InternalQName currentNodeName = null;
      int nodeIndex = 1;
      NodeData parentData = tree.peek();
      if (!isFirstElementChecked) {
        if (!ROOT_NODE_NAME.equals(svName))
          throw new RepositoryException("The first element must be root. But found '" + svName
              + "'");
        isFirstElementChecked = true;
      }

      if (ROOT_NODE_NAME.equals(svName)) {
        currentNodeName = Constants.ROOT_PATH.getName();
        // remove the wrong root from the stack
        tree.pop();
        newNodeData = new ImportNodeData(Constants.ROOT_PATH,
                                         Constants.ROOT_UUID,
                                         -1,
                                         Constants.NT_UNSTRUCTURED,
                                         new InternalQName[0],
                                         0,
                                         null,
                                         new AccessControlList());
        // Not persistent state. Root created during the creation workspace.
        changesLog.add(new ItemState(newNodeData,
                                     ItemState.ADDED,
                                     true,
                                     parentData.getQPath(),
                                     false,
                                     false));

      } else {

        currentNodeName = locationFactory.parseJCRName(svName).getInternalName();
        nodeIndex = getNodeIndex(parentData, currentNodeName, null);
        newNodeData = new ImportNodeData(parentData, currentNodeName, nodeIndex);
        newNodeData.setOrderNumber(getNextChildOrderNum(parentData));
        newNodeData.setIdentifier(IdGenerator.generate());
        changesLog.add(new ItemState(newNodeData, ItemState.ADDED, true, parentData.getQPath()));
      }
      tree.push(newNodeData);
    } else {
      super.startElement(namespaceURI, localName, name, atts);
    }
  }

}
