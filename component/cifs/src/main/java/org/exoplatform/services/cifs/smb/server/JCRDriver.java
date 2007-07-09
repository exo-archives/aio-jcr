/**
 * 
 */
package org.exoplatform.services.cifs.smb.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.cifs.server.filesys.AccessMode;
import org.exoplatform.services.cifs.server.filesys.FileAttribute;
import org.exoplatform.services.cifs.server.filesys.FileExistsException;
import org.exoplatform.services.cifs.server.filesys.FileInfo;
import org.exoplatform.services.cifs.server.filesys.FileOpenParams;
import org.exoplatform.services.cifs.server.filesys.NameCoder;
import org.exoplatform.services.cifs.server.filesys.NetworkFile;
import org.exoplatform.services.cifs.server.filesys.SearchContext;
import org.exoplatform.services.cifs.server.filesys.TreeConnection;
import org.exoplatform.services.cifs.smb.server.JCRNetworkFile;
import org.exoplatform.services.cifs.smb.server.SMBSrvSession;
import org.exoplatform.services.cifs.util.WildCard;
import org.exoplatform.services.jcr.impl.core.JCRPath;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.JCRPath.PathElement;
import org.exoplatform.services.log.ExoLogger;

/**
 * This class consist basic and almoust used operation with JCR
 * 
 * @author Karpenko
 * 
 */
public class JCRDriver {
  // Debug logging

  private static final Log logger = ExoLogger
      .getLogger("org.exoplatform.services.cifs.JCRDriver");

  /**
   * Create file or directory by recieved params
   * 
   * @param conn
   * @param params
   *          FileOpenParams
   * @return
   * @throws FileExistsException
   * @throws LockException
   * @throws RepositoryException
   */
  public static NetworkFile createFile(TreeConnection conn,
      FileOpenParams params) throws FileExistsException, LockException,
      RepositoryException {
    // logger.debug(" createFile params ["+params+"] TEMPLATE");

    String path = params.getPath();

    if ((conn.getSession().itemExists(path)) && (params.isOverwrite() == false)) {
      throw new FileExistsException();
    }
    // Create it - the path will be created, if necessary

    Session session = conn.getSession();
    Node nodeRef = createNode(session, path, !params.isDirectory());

    // TODO here must be setting file priperties like creatin date and other

    // create the network file
    NetworkFile netFile = new JCRNetworkFile();
    ((JCRNetworkFile) netFile).setNodeRef(nodeRef);
    // Add a file state for the new file/folder

    // done
    if (logger.isDebugEnabled()) {
      logger.debug("Created file: \n" + "   path: " + path + "\n"
          + "   file open parameters: " + params + "\n" + "   node: " + nodeRef
          + "\n" + "   network file: " + netFile);
    }

    return netFile;

  }

  public static NetworkFile openFile(TreeConnection conn, FileOpenParams params)
      throws PathNotFoundException, AccessDeniedException, RepositoryException {
    try {
      logger.debug("openFile");

      String path = params.getPath();
      Session session = conn.getSession();
      Node nodeRef = (Node) session.getItem(path);

      // Check permissions on the file/folder node
      // Check for read access

      if (params.hasAccessMode(AccessMode.NTRead))
        try {
          session.checkPermission(path, "read");
        } catch (java.security.AccessControlException e) {
          throw new AccessDeniedException("No read access to "
              + params.getFullPath());
        }

      // Check for write access

      if (params.hasAccessMode(AccessMode.NTWrite))
        try {
          session.checkPermission(path, "add_node");
          session.checkPermission(path, "set_property");
        } catch (java.security.AccessControlException e) {
          throw new AccessDeniedException("No write access to "
              + params.getFullPath());
        }

      // Check for delete access

      if (params.hasAccessMode(AccessMode.NTDelete))
        try {
          session.checkPermission(path, "remove");
        } catch (java.security.AccessControlException e) {
          throw new AccessDeniedException("No delete access to "
              + params.getFullPath());
        }

      // Check if the file has a lock
      if ((params.hasAccessMode(AccessMode.NTWrite)) && (nodeRef.isLocked()))
        throw new AccessDeniedException("File is locked, no write access to "
            + params.getFullPath());

      // TODO: Check access writes and compare to write requirements

      // Create the network file

      NetworkFile netFile = new JCRNetworkFile();

      // set relevant parameters
      if (params.isReadOnlyAccess()) {
        netFile.setGrantedAccess(NetworkFile.READONLY);
      } else {
        netFile.setGrantedAccess(NetworkFile.READWRITE);
      }

      // check the type
      FileInfo fileInfo = getFileInformation(nodeRef);

      ((JCRNetworkFile) netFile).setNodeRef(nodeRef);

      if (fileInfo.isDirectory()) {
        netFile.setAttributes(FileAttribute.Directory);
      } else {
        // Set the current size
        netFile.setFileSize(fileInfo.getSize());
      }

      // Set the file timestamps

      if (fileInfo.hasCreationDateTime())
        netFile.setCreationDate(fileInfo.getCreationDateTime());

      if (fileInfo.hasModifyDateTime())
        netFile.setModifyDate(fileInfo.getModifyDateTime());

      if (fileInfo.hasAccessDateTime())
        netFile.setAccessDate(fileInfo.getAccessDateTime());

      // Set the file attributes

      netFile.setAttributes(fileInfo.getFileAttributes());

      // If the file is read-only then only allow read access

      if (netFile.isReadOnly())
        netFile.setGrantedAccess(NetworkFile.READONLY);

      // done
      if (logger.isDebugEnabled()) {
        logger.debug("Created network file: \n" + "   node: " + nodeRef + "\n"
            + "   param: " + params + "\n" + "   netfile: " + netFile);
      }

      // If the file has been opened for overwrite then truncate the file
      // to zero length, this will
      // also prevent the existing content data from being copied to the
      // new version of the file

      if (params.isOverwrite() && netFile != null) {
        // Truncate the file to zero length
        ((JCRNetworkFile) netFile).truncFile(params.getAllocationSize());

      }

      // Debug

      if (logger.isDebugEnabled()) {
        logger.debug("Opened network file: \n" + "   path: " + params.getPath()
            + "\n" + "   file open parameters: " + params + "\n"
            + "   network file: " + netFile);
      }

      // Return the network file

      return netFile;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RepositoryException(e.getMessage());
    }

  }

  /**
   * Helper method to extract file info from a received node.
   * <p>
   * This method goes direct to the repo for all information and no data is
   * cached here.
   * 
   * @param nodeRef
   *          the node which file information we looking for
   * @return Returns the file information pertinent to the node
   * @throws RepositoryException
   * 
   */

  public static FileInfo getFileInformation(Node nodeRef)
      throws RepositoryException {
    try {
      // retrieve required properties and create file info
      FileInfo fileInfo = new FileInfo();

      // unset all attribute flags
      int fileAttributes = 0;
      fileInfo.setFileAttributes(fileAttributes);

      // if node is directory (all nodes that is'nt file type)
      if (!(nodeRef.isNodeType("nt:file"))) {
        // add directory attribute
        fileAttributes |= FileAttribute.Directory;
        fileInfo.setFileAttributes(fileAttributes);
      } else {
        // Get the file size from the content

        Node contentNode = nodeRef.getNode("jcr:content");
        Property dataProp = contentNode.getProperty("jcr:data");
        
        long size = 0L;
        if (dataProp != null) {
          size = dataProp.getLength();
        }

        fileInfo.setSize(size);

        // Set the allocation size by rounding up the size to a 512 byte
        // block boundary

        if (size > 0)
          fileInfo.setAllocationSize((size + 512L) & 0xFFFFFFFFFFFFFE00L);

        // Check the lock status of the file

        boolean lock = nodeRef.isLocked();

        if (lock == true) {
          // File is locked so mark it as read-only

          int attr = fileInfo.getFileAttributes();

          if ((attr & FileAttribute.ReadOnly) == 0)
            attr += FileAttribute.ReadOnly;

          // if (true)//setLocketFilesAsOffline
          // attr += FileAttribute.NTOffline;

          fileInfo.setFileAttributes(attr);
        }
      }

      // TODO: make the correct date setup
      fileInfo.setCreationDateTime(0);
      fileInfo.setModifyDateTime(0);
      fileInfo.setAccessDateTime(0);

      // name setup
      String name = nodeRef.getName();

      // TODO here is a sense to transfer name encoding directly to
      // responsemaker
      // (like Trans2FindFirst2, Trans2FindNext2) and encode name at last
      // possible
      // momment befor send response paket;

      if (name != null) {
        // check is file is single
        String nname = name;
        if (nodeRef.getSession().getRootNode() != nodeRef) {
          Node parent = nodeRef.getParent();
          NodeIterator ni = parent.getNodes(name);
          int i = 0;
          int el = 0;

          while (ni.hasNext()) {
            if (nodeRef == ni.nextNode())
              el = i + 1;

            i++;
          }
          if (i > 1)
            nname = name + "[" + el + "]";
        }
        fileInfo.setFileName(NameCoder.EncodeName(nname));
      }

      // Read/write access TODO bind this shit with JCR permissions, lock or
      // somthing else

      boolean deniedPermission = false;
      boolean isReadOnly = false;
      if (isReadOnly || deniedPermission) {
        int attr = fileInfo.getFileAttributes();
        if ((attr & FileAttribute.ReadOnly) == 0) {
          attr += FileAttribute.ReadOnly;
          fileInfo.setFileAttributes(attr);
        }
      }

      // Set the normal file attribute if no other attributes are set

      if (fileInfo.getFileAttributes() == 0)
        fileInfo.setFileAttributes(FileAttribute.NTNormal);

      // Debug

      if (logger.isDebugEnabled()) {
        logger.debug("\n Fetched file info: \n" + "   info: " + fileInfo);
      }

      // Return the file information

      return fileInfo;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RepositoryException(e.getMessage());
    }
  }

  public static SearchContext startSearch(TreeConnection conn, String srchPath,
      int srchAttr) throws FileNotFoundException, RepositoryException {

    logger.debug("startSearch path [" + srchPath + "]");
    try {
      Session jcrSess = conn.getSession();

      List<Node> results;

      // if this contains no wildcards, then we can fasttrack it
      if (!WildCard.containsWildcards(srchPath)) {
        // a specific name is required
        Node foundNodeRef = (Node) jcrSess.getItem(srchPath);
        if (foundNodeRef == null) {
          results = Collections.emptyList();
        } else {
          results = Collections.singletonList(foundNodeRef);
        }
      } else {

        int i = srchPath.lastIndexOf("/");
        String path = srchPath.substring(0, i);
        // String wildcard =srchPath.substring(i, srchPath.length());
        if (path.equals(""))
          path = "/";

        Node rootNode = (Node) jcrSess.getItem(path);
        NodeIterator ni;
        // if(wildcard.equalsIgnoreCase("*.*")){
        ni = rootNode.getNodes("*");
        // }
        // else{
        // ni = rootNode.getNodes(wildcard);
        // }

        results = new ArrayList<Node>((int) ni.getSize());
        int count = 0;
        while (ni.hasNext()) {
          results.add(ni.nextNode());
          count++;
        }
        logger.debug("files found " + count);
      }

      SearchContext ctx = new SearchContext(results, srchPath);

      return ctx;
    } catch (PathNotFoundException ex) {
      // Debug

      if (logger.isDebugEnabled())
        logger.debug("Start search - access denied, " + srchPath);

      // Convert to a file not found status

      throw new FileNotFoundException("Start search " + srchPath);

    }

  }

  /**
   * Create node and not existed directories
   * 
   * @param sess
   *          jcr-session
   * @param path
   *          full path including directory and filename
   * @param isFile
   * @return Node created node
   * @throws LockException
   * @throws RepositoryException
   */
  public static Node createNode(Session sess, String path, boolean isFile)
      throws LockException, RepositoryException {
    try {

      // split the path up into its constituents
      JCRPath jcrPath = ((SessionImpl) sess).getLocationFactory().parseJCRPath(
          path);
      PathElement[] pathEl = jcrPath.getRelPath(jcrPath.getDepth());

      Node parentFolderNodeRef = sess.getRootNode();

      // ensure that the folder path exists (create if not)
      for (int i = 0; i < jcrPath.getDepth() - 1; i++) {

        Node tempFolderNodeRef = parentFolderNodeRef;

        String nextNodeName = pathEl[i].getAsString();
        if (tempFolderNodeRef.hasNode(nextNodeName)) {
          parentFolderNodeRef = tempFolderNodeRef.getNode(nextNodeName);
        } else {
          parentFolderNodeRef = tempFolderNodeRef.addNode(nextNodeName);
        }
      }

      // add the file or folder

      String type = isFile ? "nt:file" : "nt:folder";
      String name = pathEl[jcrPath.getDepth() - 1].getAsString();

      Node createdNodeRef = parentFolderNodeRef.addNode(name, type);
      if (type == "nt:file") {
        Node dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");

        MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
        mimetypeResolver.setDefaultMimeType("application/zip");
        String mimeType = mimetypeResolver.getMimeType(name);

        dataNode.setProperty("jcr:mimeType", mimeType);
        dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
        dataNode.setProperty("jcr:data", "");
      }
      // done
      sess.save();
      if (logger.isDebugEnabled()) {
        logger.debug("Created node: \n" + "   path: " + path + "\n"
            + "   is file: " + isFile + "\n" + "   new node: "
            + createdNodeRef.getPath());
      }
      return createdNodeRef;

      // Exception classes is chget for simple
    } catch (ConstraintViolationException e) {
      e.printStackTrace();
      throw new RepositoryException(e.getMessage());
    } catch (VersionException e) {
      e.printStackTrace();
      throw new RepositoryException(e.getMessage());
    } catch (ValueFormatException e) {
      e.printStackTrace();
      throw new RepositoryException(e.getMessage());
    } catch (NoSuchNodeTypeException e) {
      e.printStackTrace();
      throw new RepositoryException(e.getMessage());
    }
  }

  public static int writeFile(SMBSrvSession m_sess, TreeConnection conn,
      NetworkFile netFile, byte[] buf, int dataPos, int dataLen, int offset)
      throws Exception {
    return netFile.writeFile(buf, dataPos, dataLen, offset);

  }

  /**
   * Read a block of data from the specified file.
   * 
   * @param sess
   *          Session details
   * @param tree
   *          Tree connection
   * @param file
   *          Network file
   * @param buf
   *          Buffer to return data to
   * @param dataPos
   *          Starting position in the return buffer
   * @param MaxCount
   *          Maximum size of data to return
   * @param offset
   *          File offset to read data
   * 
   * @throws AcccessDeniedException
   * @throws RepositoryException
   * @throws IOException
   * 
   */
  public static int readFile(SMBSrvSession m_sess, TreeConnection conn,
      NetworkFile netFile, byte[] buf, int dataPos, int maxCount, int offset)
      throws AccessDeniedException, RepositoryException, IOException {
    // Check if the file is a directory

    if (netFile.isDirectory())
      throw new AccessDeniedException();

    // Read a block of data from the file

    Node n = ((JCRNetworkFile) netFile).getNodeRef();
    InputStream is = n.getNode("jcr:content").getProperty("jcr:data")
        .getStream();
   
    int count;
    long skip_count = is.skip(offset);
    if ((skip_count < offset) || (skip_count == -1)) {
      count = 0;
    } else {
      byte[] tmpbuf = new byte[maxCount];
      count = is.read(tmpbuf);
      if (count != -1) {
        System.arraycopy(tmpbuf, 0, buf, dataPos, count);
      } else {
        // Read count of -1 indicates a read past the end of file
        count = 0;
      }
    }

    is.close();

    // done
    if (logger.isDebugEnabled()) {
      logger.debug("Read bytes from file: \n" + "   network file: " + netFile
          + "\n" + "   buffer size: " + buf.length + "\n" + "   buffer pos: "
          + dataPos + "\n" + "   size: " + maxCount + "\n" + "   file offset: "
          + offset + "\n" + "   bytes read: " + count);
    }
    return count;
  }

  public static void truncateFile(SMBSrvSession m_sess, TreeConnection conn,
      NetworkFile netFile, long wrtoff) throws Exception {
    // Check if the file is a directory

    if (netFile.isDirectory())
      throw new AccessDeniedException();

    ((JCRNetworkFile) netFile).truncFile(wrtoff);

  }

  /**
   * This method is called by setFileAttributes command
   * 
   */
  public static void setFileInformation(SMBSrvSession m_sess,
      TreeConnection conn, String fileName, FileInfo finfo) throws Exception {
    // TODO impllementation is important

  }

}
