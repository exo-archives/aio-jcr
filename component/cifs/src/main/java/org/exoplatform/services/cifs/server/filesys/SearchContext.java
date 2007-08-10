/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.exoplatform.services.cifs.server.filesys;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.cifs.server.filesys.JCRDriver;

/**
 * <p>
 * The search context represents the state of an active search by a disk
 * interface based class. The context is used to continue a search across
 * multiple requests. Context contains list of founded nodes.
 */
public class SearchContext {

  // Maximum number of files to return per search request.

  private int m_maxFiles;

  // Tree identifier that this search is associated with

  private int m_treeId;

  // Search string

  private String m_searchStr;

  // Flags
  // Bit 0 - close search after this request
  // Bit 1 - close search if end of search reached
  // Bit 2 - return resume keys for each entry found
  // Bit 3 - continue search from previous ending place
  // Bit 4 - find with backup intent
 
  private int m_flags;

  // requesteed nodes
  private List<Node> m_nodes;

  //work as resume Id, and indexes used node if -1 no nodes read.
  private int index = -1;

  /**
   * Default constructor.
   */
  public SearchContext() {
  }

  public SearchContext(List<Node> results, String srchPath) {
    this.m_searchStr = srchPath;
    this.m_nodes = results;
  }

  /**
   * Return the search context flags.
   * 
   * @return int
   */
  public final int getFlags() {
    return m_flags;
  }

  /**
   * Return the maximum number of files that should be returned per search
   * request.
   * 
   * @return int
   */
  public final int getMaximumFiles() {
    return m_maxFiles;
  }

  /**
   * Return the search string, used for resume keys in some SMB dialects.
   * 
   * @return java.lang.String
   */
  public final String getSearchString() {
    return m_searchStr != null ? m_searchStr : "";
  }

  /**
   * Return the tree identifier of the tree connection that this search is
   * associated with.
   * 
   * @return int
   */
  public final int getTreeId() {
    return m_treeId;
  }

  /**
   * Set the search context flags.
   * 
   * @param flg
   *          int
   */
  public final void setFlags(int flg) {
    m_flags = flg;
  }

  /**
   * Set the maximum files to return per request packet.
   * 
   * @param maxFiles
   *          int
   */
  public final void setMaximumFiles(int maxFiles) {
    m_maxFiles = maxFiles;
  }

  /**
   * Set the search string.
   * 
   * @param str
   *          java.lang.String
   */
  public final void setSearchString(String str) {
    m_searchStr = str;
  }

  /**
   * Set the tree connection id that the search is associated with.
   * 
   * @param id
   *          int
   */
  public final void setTreeId(int id) {
    m_treeId = id;
  }

  /**
   * Determine if there are more files for the active search.
   * 
   * @return boolean
   */
  public boolean hasMoreFiles() {
    return index < (m_nodes.size() - 1);
  }

  /**
   * Return file information for the next file in the active search. Returns
   * false if the search is complete.
   * 
   * @param info
   *          FileInfo to return the file information.
   * @return true if the file information is valid, else false
   */
  public boolean nextFileInfo(FileInfo info) throws PathNotFoundException,
      RepositoryException {
    // Check if there is anything else to return

    if (!hasMoreFiles())
      return false;

    // Increment the index

    index++;

    // Get the next file info from the node search

    Node nextNodeRef = m_nodes.get(index);

    // Get the file information and copy across to the callers file info

    FileInfo nextInfo = JCRDriver.getFileInformation(nextNodeRef);
    info.copyFrom(nextInfo);

    // Indicate that the file information is valid

    return true;
  }

  public int getResumeId() {
    return index;
  }

  /**
   * Return the search context as a string.
   * 
   * @return java.lang.String
   */
  public String toString() {
    StringBuffer str = new StringBuffer();
    str.append("[");
    str.append(getSearchString());
    str.append(":");
    str.append(getMaximumFiles());
    str.append(",");
    str.append("0x");
    str.append(Integer.toHexString(getFlags()));
    str.append("]");

    return str.toString();
  }

  /**
   * Rollback index in node list at one position.<p>
   * Its used whan file info cant fit into packet size, and will be send in next packet. 
   * 
   */ 
  public void rollbackAtOnePosition() {
    index--;
  }

}
