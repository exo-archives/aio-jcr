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

package org.exoplatform.services.jcr.ext.script.groovy;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.logging.Log;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class GroovyScript2RestLoaderPlugin extends BaseComponentPlugin {

  /**
   * Logger.
   */
  private static final Log           LOG                       = ExoLogger.getLogger(GroovyScript2RestLoaderPlugin.class.getName());

  /**
   * QName for 'groovy-resource-container' tag.
   */
  private static final QName         GROOVY_RESOURCE_CONTAINER = new QName("groovy-resource-container");

  /**
   * QName for 'path' tag.
   */
  private static final QName         PATH                      = new QName("path");

  /**
   * QName for 'name' attribute in 'groovy-resource-container' tag.
   */
  private static final QName         NAME_ATTRIBUTE            = new QName("name");

  /**
   * QName for 'autoload' attribute in 'groovy-resource-container' tag.
   */
  private static final QName         AUTOLOAD_ATTRIBUTE        = new QName("autoload");

  /**
   * Configurations for scripts what were got from XML.
   */
  private List<XMLGroovyScript2Rest> l                         = new ArrayList<XMLGroovyScript2Rest>();

  /**
   * Schema for validation XML document.
   */
  // dtd/groovy-resource-container.xsd
  private Schema                     schema;

  /**
   * Configuration URLs.
   */
  // conf/groovy2rest.xml
  private Enumeration<java.net.URL>  configFiles;

  /**
   * Repository.
   */
  private String                     repository;

  /**
   * Workspace.
   */
  private String                     workspace;

  /**
   * Root node for scripts. If it does not exist new one will be created.
   */
  private String                     node;

  /**
   * Context Class-Loader.
   */
  private ClassLoader                classLoader;

  public GroovyScript2RestLoaderPlugin(InitParams params) {
    repository = params.getValueParam("repository").getValue();
    workspace = params.getValueParam("workspace").getValue();
    node = params.getValueParam("node").getValue();
    init();
    while (configFiles.hasMoreElements()) {
      String cf = configFiles.nextElement().toString();
      processConfig(cf);
    }
  }

  public List<XMLGroovyScript2Rest> getXMLConfigs() {
    return l;
  }

  /**
   * @return the repository
   */
  public String getRepository() {
    return repository;
  }

  /**
   * @return the workspace
   */
  public String getWorkspace() {
    return workspace;
  }

  /**
   * @return the node
   */
  public String getNode() {
    return node;
  }

  /**
   * Initialization.
   */
  protected void init() {
    classLoader = Thread.currentThread().getContextClassLoader();
    try {
      schema = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
                            .newSchema(new StreamSource(classLoader.getResourceAsStream("dtd/groovy-resource-container.xsd")));
    } catch (SAXException e) {
      LOG.error("Can't parse xml schema. ", e);
      throw new RuntimeException(e);
    }
    try {
      configFiles = classLoader.getResources("conf/groovy2rest.xml");
    } catch (IOException e) {
      LOG.error("Error occurs when try to get configuration files. ", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Process configuration file.
   * 
   * @param configFile configuration file.
   */
  protected void processConfig(String configFile) {
    XMLEventReader eventReader = null;
    try {
      LOG.info("Load configuration from " + configFile);
      
      // XML validation
      StreamSource source = new StreamSource(configFile);
      schema.newValidator().validate(source);
      
      int sep = configFile.lastIndexOf('!');
      // get jar file path where configuration was loaded
      String jarFile = configFile.substring(0, sep);
      
      // process XML events
      eventReader = XMLInputFactory.newInstance().createXMLEventReader(source);
      XMLGroovyScript2Rest xg = new XMLGroovyScript2Rest();
      while (eventReader.hasNext()) {
        XMLEvent event = eventReader.nextEvent();
        if (event.isStartElement()) {
          StartElement se = event.asStartElement();
          if (se.getName().equals(GROOVY_RESOURCE_CONTAINER)) {
            // start 'groovy-resource-container' tag 
            xg.setName(se.getAttributeByName(NAME_ATTRIBUTE).getValue());
            Attribute a = se.getAttributeByName(AUTOLOAD_ATTRIBUTE);
            xg.setAutoload(a != null ? Boolean.valueOf(a.getValue()) : false);
          } else if (se.getName().equals(PATH)) {
            // start 'path' tag 
            String p = eventReader.getElementText();
            // script path should be relative to jar file where configuration was loaded
            URL resource = new URL(jarFile + "!" + ((p.charAt(0) != '/') ? "/" + p : p));
            xg.setPath(resource);
          }
        }
        if (event.isEndElement()) {
          // end 'groovy-resource-container' tag.
          // One item of configuration was read save it in list.
          if (event.asEndElement().getName().equals(GROOVY_RESOURCE_CONTAINER)) {
            l.add(xg);
            //
            if (eventReader.hasNext())
              xg = new XMLGroovyScript2Rest();
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (eventReader != null) {
        try {
          eventReader.close();
        } catch (XMLStreamException e) {
          LOG.error("Error occurs when try to close XMLEventReader. ", e);
        }
      }
    }
  }
  
}
