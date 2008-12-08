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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
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

  private static final Log           LOG                       = ExoLogger.getLogger(GroovyScript2RestLoaderPlugin.class.getName());

  private static final QName         GROOVY_RESOURCE_CONTAINER = new QName("groovy-resource-container");

  private static final QName         PATH                      = new QName("path");

  private static final QName         NAME_ATTRIBUTE            = new QName("name");

  private static final QName         AUTOLOAD_ATTRIBUTE        = new QName("autoload");

  private List<XMLGroovyScript2Rest> l                         = new ArrayList<XMLGroovyScript2Rest>();

  private Schema                     schema;

  private Enumeration<java.net.URL>  configFiles;

  private String                     repository;

  private String                     workspace;

  private String                     node;

  public GroovyScript2RestLoaderPlugin(InitParams params) {
    repository = params.getValueParam("repository").getValue();
    workspace = params.getValueParam("workspace").getValue();
    node = params.getValueParam("node").getValue();
    init();
    while (configFiles.hasMoreElements()) {
      String cf = configFiles.nextElement().toString();
      try {
        processConfig(cf);
      } catch (Exception e) {
        LOG.error("Failed process config file " + cf, e);
      }
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

  private void init() {
    try {
      schema = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
                            .newSchema(new StreamSource(Thread.currentThread()
                                                              .getContextClassLoader()
                                                              .getResourceAsStream("dtd/groovy-resource-container.xsd")));
    } catch (SAXException e) {
      LOG.error("Can't parse xml schema. ", e);
      throw new RuntimeException(e);
    }
    try {
      configFiles = Thread.currentThread()
                          .getContextClassLoader()
                          .getResources("conf/groovy2rest.xml");
    } catch (IOException e) {
      LOG.error("Error occurs when try to get configuration files. ", e);
      throw new RuntimeException(e);
    }
  }

  private void processConfig(String configFile) throws Exception {
    LOG.info("Load configuration from " + configFile);
    StreamSource source = new StreamSource(configFile);
    schema.newValidator().validate(source);
    XMLInputFactory xFactory = XMLInputFactory.newInstance();
    XMLEventReader r = xFactory.createXMLEventReader(source);
    XMLGroovyScript2Rest xg = new XMLGroovyScript2Rest();
    while (r.hasNext()) {
      XMLEvent event = r.nextEvent();
      if (event.isStartElement()) {
        StartElement se = event.asStartElement();
        if (se.getName().equals(GROOVY_RESOURCE_CONTAINER)) {
          xg.setName(se.getAttributeByName(NAME_ATTRIBUTE).getValue());
          Attribute a = se.getAttributeByName(AUTOLOAD_ATTRIBUTE);
          xg.setAutoload(a != null ? Boolean.valueOf(a.getValue()) : false);
        } else if (se.getName().equals(PATH)) {
          xg.setPath(r.getElementText());
        }
      }
      if (event.isEndElement()) {
        if (event.asEndElement().getName().equals(GROOVY_RESOURCE_CONTAINER)) {
          l.add(xg);
          xg = new XMLGroovyScript2Rest();
        }
      }
    }
    r.close();
  }

}
