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
package org.exoplatform.services.cifs.smb.server;

import java.util.Enumeration;
import java.util.Hashtable;

import org.exoplatform.services.cifs.server.SrvSession;
import org.exoplatform.services.cifs.server.auth.Client;
import org.exoplatform.services.cifs.server.core.SharedDevice;
import org.exoplatform.services.cifs.server.filesys.JCRNetworkFile;
import org.exoplatform.services.cifs.server.filesys.NetworkFile;
import org.exoplatform.services.cifs.server.filesys.SearchContext;
import org.exoplatform.services.cifs.server.filesys.TooManyConnectionsException;
import org.exoplatform.services.cifs.server.filesys.TreeConnection;

/**
 * Represent virtual circuit that is logical connection between client and
 * server. There are may by many circuits on transport session.
 * <p>
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 */
public class VirtualCircuit {

  // Default maximum of tree connection and Searches on circuit
  private static final int                   DEFAULT_TREE_CONNECTIONS = 4;

  private static final int                   MAX_TREE_CONNECTIONS     = 16;

  private static final int                   DEAFAULT_SEARCHES        = 4;

  private static final int                   MAX_SEARCHES            = 256;

  // Tree ids are 16bit values
  private static final int                   TREE_ID_MASK             = 0x0000FFFF;


  /**
   * Invalid UID value.
   */
  public static final int                    INVALID_UID             = -1;

  /**
   * ID sent the client and allocated the server for identify VC.
   */
  private int                                uId                    = -1;

  /**
   * Number of VirtualsCircuit for internal purposes.
   */
  private int                                vcNum                  = -1;

  /**
   * Active tree connections.
   */
  private Hashtable<Integer, TreeConnection> connections;

  /**
   * Last tree ID.
   */
  private int                                lasttreeId;

  /**
   * List of opened searches.
   */
  private SearchContext[]                    searches;

  /**
   * Opened searches count.
   */
  private int                                srchCount;

  /**
   * Holds info about owner of this circuit.
   */
  private Client                             client;

  /**
   * Constructor.
   * 
   * @param vcNum VirtualCircuit number
   * @param client Client
   */
  public VirtualCircuit(int vcNum, Client client) {
    this.vcNum = vcNum;
    this.client = client;
  }

  /**
   * Set UUID.
   * 
   * @param uid UUID
   */
  public void setUID(int uid) {
    uId = uid;
  }

  /**
   * Add a new connection to this virtual circuit. Return the allocated tree id
   * for the new connection.
   * 
   * @param shrDev SharedDevice
   * @return int Allocated tree id (connection id)
   * @throws TooManyConnectionsException
   */
  public int addTreeConnection(SharedDevice shrDev) throws TooManyConnectionsException {

    // Check if the connection array has been allocated

    if (connections == null)
      connections = new Hashtable<Integer, TreeConnection>(DEFAULT_TREE_CONNECTIONS);

    // Allocate an id for the tree connection

    int treeId = 0;

    synchronized (connections) {

      // Check if the tree connection table is full

      if (connections.size() == MAX_TREE_CONNECTIONS)
        throw new TooManyConnectionsException();

      // Find a free slot in the connection array

      treeId = (lasttreeId++ & TREE_ID_MASK);
      Integer key = new Integer(treeId);

      while (connections.contains(key)) {

        // Try another tree id for the new connection

        treeId = (lasttreeId++ & TREE_ID_MASK);
        key = new Integer(treeId);
      }

      // Store the new tree connection

      connections.put(key, new TreeConnection(shrDev));
    }

    // Return the allocated tree id

    return treeId;
  }

  /**
   * Find tree connection.
   * @param treeId  tree connection ID
   * @return TreeConnection
   */
  public TreeConnection findTreeConnection(int treeId) {
    // Check if the tree id and connection array are valid

    if (connections == null)
      return null;

    // Get the required tree connection details

    return connections.get(new Integer(treeId));
  }

  /**
   * Return the active tree connection count.
   * 
   * @return int
   */
  public final int getConnectionCount() {
    return connections != null ? connections.size() : 0;
  }

  /**
   * Remove the specified tree connection from the active connection list.
   * 
   * @param treeId int
   * @param sess SrvSession
   */
  protected void removeConnection(int treeId, SrvSession sess) {

    // Check if the tree id is valid

    if (connections == null)
      return;

    // Close the connection and remove from the connection list

    synchronized (connections) {

      // Get the connection

      Integer key = new Integer(treeId);
      TreeConnection tree = connections.get(key);

      // Close the connection, release resources

      if (tree != null) {

        // Close the connection

        tree.closeConnection(sess);

        // Remove the connection from the connection list

        connections.remove(key);
      }
    }
  }

  /**
   * Store the seach context in the specified slot.
   * 
   * @param slot Slot to store the search context.
   * @param srch com.starla.smbsrv.SearchContext
   */
  public final void setSearchContext(int slot, SearchContext srch) {

    // Check if the search slot id is valid

    if (searches == null || slot > searches.length)
      return;

    // Store the context

    searches[slot] = srch;
  }

  /**
   * Return the search context for the specified search id.
   * 
   * @return SearchContext
   * @param srchId int
   */
  public final SearchContext getSearchContext(int srchId) {

    // Check if the search array is valid and the search index is valid

    if (searches == null || srchId >= searches.length)
      return null;

    // Return the required search context

    return searches[srchId];
  }

  /**
   * Allocate a slot in the active searches list for a new search.
   * 
   * @return int Search slot index, or -1 if there are no more search slots
   *         available.
   */
  public final int allocateSearchSlot() {

    // Check if the search array has been allocated

    if (searches == null)
      searches = new SearchContext[DEAFAULT_SEARCHES];

    // Find a free slot for the new search

    int idx = 0;

    while (idx < searches.length && searches[idx] != null)
      idx++;

    // Check if we found a free slot

    if (idx == searches.length) {

      // The search array needs to be extended, check if we reached the
      // limit.

      if (searches.length >= MAX_SEARCHES)
        return -1;

      // Extend the search array

      SearchContext[] newSearch = new SearchContext[searches.length * 2];
      System.arraycopy(searches, 0, newSearch, 0, searches.length);
      searches = newSearch;
    }

    // Return the allocated search slot index

    srchCount++;
    return idx;
  }

  /**
   * Deallocate the specified search context/slot.
   * 
   * @param ctxId int
   */
  public final void deallocateSearchSlot(int ctxId) {

    // Check if the search array has been allocated and that the index is
    // valid

    if (searches == null || ctxId >= searches.length)
      return;

    // Close the search
    // There may be actions with search context befor closing

    // Free the specified search context slot

    srchCount--;
    searches[ctxId] = null;
  }

  /**
   * Close the virtual circuit, close active tree connections.
   * 
   * @param sess SrvSession
   */
  public final void closeCircuit(SrvSession sess) {

    // Check if there are any active searches

    if (searches != null) {

      // Close all active searches

      for (int idx = 0; idx < searches.length; idx++) {

        // Check if the current search slot is active

        if (searches[idx] != null)
          deallocateSearchSlot(idx);
      }

      // Release the search context list, clear the search count

      searches = null;
      srchCount = 0;
    }

    // Check if there are open tree connections

    if (connections != null) {

      synchronized (connections) {

        // Close all active tree connections

        Enumeration<TreeConnection> enm = connections.elements();

        while (enm.hasMoreElements()) {

          // Get the current tree connection

          TreeConnection tree = (TreeConnection) enm.nextElement();

          // Check if there are open files on the share

          if (tree.openFileCount() > 0) {

            // Close the open files, release locks

            for (int i = 0; i < tree.getFileTableLength(); i++) {

              // Get an open file

              NetworkFile curFile = tree.findFile(i);
              if (curFile != null && curFile instanceof JCRNetworkFile) {

                try {

                  // ((JCRNetworkFile) curFile).flush();
                  // ((JCRNetworkFile) curFile).saveChanges();

                } catch (Exception ex) {
                  ex.printStackTrace();
                }
              }
              // Remove the file from the tree connection list

              tree.removeFile(i, sess);

            }
          }
        }
      }
      // Clear the tree connection list

      connections.clear();

    }
  }

  /**
   * Get client info.
   * 
   * @return Client
   */
  public Client getClientInfo() {
    return client;
  }

}
