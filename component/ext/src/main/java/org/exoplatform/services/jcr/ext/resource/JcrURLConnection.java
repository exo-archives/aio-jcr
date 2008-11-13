/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.services.jcr.ext.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class JcrURLConnection extends URLConnection {

  /**
   * Logger.
   */
  private static final Log          LOG    = ExoLogger.getLogger(JcrURLConnection.class.getName());

  /**
   * JCR session.
   */
  private Session                   session;

  /**
   * See {@link NodeRepresentationService}.
   */
  private NodeRepresentationService nodeRepresentationService;

  /**
   * See {@link UnifiedNodeReference}.
   */
  private UnifiedNodeReference      nodeReference;

  /**
   * Representation for node.
   */
  private NodeRepresentation        nodeRepresentation;

  /**
   * @param nodeReference node reference
   * @param session jcr session
   * @param nodeRepresentationService node representation service
   * @throws MalformedURLException if URL syntax incorrect
   */
  public JcrURLConnection(UnifiedNodeReference nodeReference,
                          Session session,
                          NodeRepresentationService nodeRepresentationService) throws MalformedURLException {
    super(nodeReference.getURL());
    this.session = session;
    this.nodeReference = nodeReference;
    this.nodeRepresentationService = nodeRepresentationService;

    doOutput = false;
    allowUserInteraction = false;
    useCaches = false;
    ifModifiedSince = 0;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void connect() throws IOException {
    if (connected)
      return;

    try {
      
      Node node = null;
      if (nodeReference.isPath())
        node = session.getRootNode().getNode(nodeReference.getPath().substring(1));
      else if (nodeReference.isIdentitifier())
        node = session.getNodeByUUID(nodeReference.getIdentitifier().getString());
      else
        throw new IllegalArgumentException("Absolute path or Identifier was not found!");

      nodeRepresentation = nodeRepresentationService.getNodeRepresentation(node, "text/xml");

      connected = true;
    } catch (Exception e) {
      // if can't get node representation then close session no sense to continue 
      session.logout();
      LOG.error("connection refused.", e);
      throw new IOException();
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void finalize() {
    disconnect();
  }

  /**
   * Close JCR session. 
   */
  public void disconnect() {
    session.logout();
    connected = false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InputStream getInputStream() throws IOException {
    if (!connected)
      connect();

    try {
      return nodeRepresentation.getInputStream();
    } catch (Exception e) {
      LOG.error("can't get input stream.", e);
      throw new IOException();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getContent() throws IOException {
    if (!connected)
      connect();

    return nodeRepresentation.getNode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getContent(Class[] classes) throws IOException {
    throw new UnsupportedOperationException("protocol support only "
        + "javax.jcr.Node as content, use method getContent() instead this.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getContentType() {
    try {
      if (!connected)
        connect();

      return nodeRepresentation.getMediaType();
    } catch (Exception e) {
      LOG.error("can't get media type.", e);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getContentLength() {
    try {
      if (!connected)
        connect();

      return (int) nodeRepresentation.getContentLenght();
    } catch (Exception e) {
      LOG.error("can't get content length.", e);
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDoOutput(boolean dooutput) {
    if (dooutput)
      throw new UnsupportedOperationException("protocol doesn't support output!");
    super.setDoOutput(dooutput);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getContentEncoding() {
    try {
      if (!connected)
        connect();

      return nodeRepresentation.getContentEncoding();
    } catch (Exception e) {
      LOG.error("can't get content encoding.", e);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getLastModified() {
    try {
      return nodeRepresentation.getLastModified();
    } catch (Exception e) {
      LOG.error("can't get content las modified.", e);
    }
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAllowUserInteraction(boolean allowuserinteraction) {
    if (allowuserinteraction)
      throw new UnsupportedOperationException("protocol doesn't support user interaction!");
    super.setAllowUserInteraction(allowuserinteraction);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setUseCaches(boolean usecaches) {
    if (usecaches)
      throw new UnsupportedOperationException("protocol doesn't support caches!");
    super.setUseCaches(usecaches);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setIfModifiedSince(long ifmodifiedsince) {
    if (ifmodifiedsince > 0)
      throw new UnsupportedOperationException("protocol doesn't support this feature!");
    super.setIfModifiedSince(ifmodifiedsince);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addRequestProperty(String key, String value) {
    throw new UnsupportedOperationException("protocol doesn't support request properties!");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRequestProperty(String key, String value) {
    throw new UnsupportedOperationException("protocol doesn't support request properties!");
  }

}
