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

package org.exoplatform.services.jcr.ext.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 * 
 * @since 1.9
 */

public class NodeRepresentationService {

  private Map<String, NodeRepresentationFactory> factories;
  
  private static final Log Log = ExoLogger.getLogger("jcr.ext.resource.NodeRepresentationService");

  public NodeRepresentationService() {
    this.factories = new HashMap<String, NodeRepresentationFactory>();
  }

  /**
   * Add new NodeRepresentationFactory for node type.
   * @param nodeType the node type.
   * @param representationFactoryType the class of NodeRepresentationFactory.
   * @throws Exception if NodeRepresentationFactory can't be instantiated.
   */
  public void addNodeRepresentationFactory(String nodeType, 
      Class<? extends NodeRepresentationFactory> representationFactoryType) throws Exception {
    factories.put(nodeType, representationFactoryType.newInstance());
  }
  
  /**
   * Add new NodeRepresentationFactory for node type.
   * @param nodeType the node type.
   * @param representationFactory the NodeRepresentationFactory.
   */
  public void addNodeRepresentationFactory(String nodeType,
      NodeRepresentationFactory representationFactory) {
    factories.put(nodeType, representationFactory);
  }

  /**
   * Get NodeRepresentation for given node. String mediaTypeHint can be used as external
   * information for representation. By default node will be represented as doc-view.
   * @param node the jcr node.
   * @param mediaTypeHint the mimetype.
   * @return the NodeRepresentation.
   * @throws RepositoryException
   */
  public NodeRepresentation getNodeRepresentation(Node node, String mediaTypeHint)
      throws RepositoryException {
    
    NodeRepresentationFactory factory = factory(node);
    if(factory != null)
      return factory.createNodeRepresentation(node, mediaTypeHint);
    else
      return new DocViewNodeRepresentation(node);
  }
  
  public void addPlugin(ComponentPlugin plugin) {
    if (plugin instanceof NodeRepresentationFactoryLoaderPlugin) {
      Map<String, String> factories =
        ((NodeRepresentationFactoryLoaderPlugin)plugin).getFactories();
      for (String key : factories.keySet()) {
        try {
          Class<? extends NodeRepresentationFactory> factoryType =
            (Class<? extends NodeRepresentationFactory>) Class.forName(factories.get(key));
          addNodeRepresentationFactory(key, factoryType);
          Log.info("Add new NodeRepresentationFactory " + factories.get(key));
        } catch (Exception e) {
          Log.error("Failed add NodeRepresentationFactory " + factories.get(key));
          e.printStackTrace();
        }
      }
    }
  }

  private NodeRepresentationFactory factory(Node node) throws RepositoryException {

    NodeRepresentationFactory f = factories.get(node.getPrimaryNodeType().getName());
    
    if(f == null) {
      for(String nt : factories.keySet()  ) {
        if(node.isNodeType(nt)) {
          f = factories.get(nt);
          break;
        }
      }
    }

    if(f == null) {
      for (NodeType mixin : node.getMixinNodeTypes()) {
        f = factories.get(mixin.getName());
        if(f != null)
          return f;
      }
    } 
    
    return f;
    
  }

  private class DocViewNodeRepresentation implements NodeRepresentation {
    
    private Node node;

    public DocViewNodeRepresentation(Node node) {
      this.node = node;
    }

    public String getContentEncoding() {
      return Constants.DEFAULT_ENCODING;
    }

    public long getContentLenght() throws RepositoryException {
      return -1;
    }

    /* (non-Javadoc)
     * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getMediaType()
     */
    public String getMediaType() throws RepositoryException {
      return "text/xml";
    }
    
    /* (non-Javadoc)
     * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getLastModified()
     */
    public long getLastModified() throws RepositoryException {
      return 0;
    }

    /* (non-Javadoc)
     * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getProperty(java.lang.String)
     */
    public String getProperty(String name) throws RepositoryException {
      return null;
    }

    /* (non-Javadoc)
     * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getPropertyNames()
     */
    public Collection<String> getPropertyNames() throws RepositoryException {
      return new ArrayList<String>();
    }

    public InputStream getInputStream() throws IOException, RepositoryException {
      final PipedInputStream pin = new PipedInputStream();
      final PipedOutputStream pout = new PipedOutputStream(pin);

      try {

        new Thread() {

          /* (non-Javadoc)
           * @see java.lang.Thread#run()
           */
          public void run() {
            try {
              node.getSession().exportDocumentView(node.getPath(), pout, false, false);
            } catch (Exception e) {
              /* Nothing to do.
               * Can give exception if nothing read from stream,
               * this exception generated by XMLStreamWriterImpl#writeStartDocument.
               */
            } finally {
              try {
                pout.flush();
                pout.close();
              } catch (Exception e) {
              }
            }
          }

        }.start();

        return pin;
      } catch (Exception e) {
        e.printStackTrace();
        throw new IOException("can't get input stream");
      }
      
    }
    
    /* (non-Javadoc)
     * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getNode()
     */
    public Node getNode() {
      return node;
    }

  }
  
}
