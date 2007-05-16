/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.common.template;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class AbstractTemplateList implements TemplateList {
  
  private static Log log = ExoLogger.getLogger("jcr.AbstractTemplateList");
  
  private ArrayList<Template> templates = new ArrayList<Template>();
  
  protected String templateName;

  public AbstractTemplateList(String templateName) {
    this.templateName = templateName;
  }

  public void addTemplate(Template template) {
    templates.add(template);
  }

  public ArrayList<Template> getTemplateList() {
    return (ArrayList<Template>)templates.clone();
  }

  public Element serialize(Document xmlDocument) {
    log.info("public Element serialize(Document xmlDocument)");
    Element templateListEl = xmlDocument.createElement(EXO_PREFIX + templateName);
    
    templateListEl.setAttribute(XMLNS_EXO, EXO_HREF);
    templateListEl.setAttribute(XMLNS_LINK, EXO_XLINK);
    
    for (int i = 0; i < templates.size(); i++) {
      Template template = templates.get(i);
      Element templateElement = template.serialize(xmlDocument);
      templateListEl.appendChild(templateElement);
    }
    
    return templateListEl;
  }
  
  public boolean parse(Node templateNode) {
    log.info("public boolean parse(Node templateNode)");
    return false;
  }
  
  public abstract boolean fillContent() throws Exception;
  
}
