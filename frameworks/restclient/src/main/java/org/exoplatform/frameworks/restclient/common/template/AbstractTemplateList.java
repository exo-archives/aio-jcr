/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.restclient.common.template;

import java.util.ArrayList;

import org.exoplatform.frameworks.httpclient.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class AbstractTemplateList implements TemplateList {
  
  private String xmlListName;
  
  private ArrayList<Template> templates = new ArrayList<Template>();
  
  private Element templateListElement;
  
  public AbstractTemplateList(String xmlListName) {
    Log.info("public AbstractListTemplate()");
    this.xmlListName = xmlListName;
  }
  
  public void addTemplate(Template template) {
    templates.add(template);
  }
  
  public ArrayList<Template> getTemplateList() {
    return (ArrayList<Template>)templates.clone();
  }

  public Element serialize(Document xmlDocument) {
    Log.info("public Element serialize(Document xmlDocument, boolean isRoot)");
    
    Log.info("XMLLISTNAME: " + xmlListName);
    
    templateListElement = xmlDocument.createElementNS(EXO_HREF, EXO_PREFIX + xmlListName);
    templateListElement.setAttribute(XMLNS_LINK, EXO_XLINK);
    
    for (int i = 0; i < templates.size(); i++) {
      Template curTemplate = templates.get(i);
      Element templateElement = curTemplate.serialize(xmlDocument);
      templateListElement.appendChild(templateElement);
    }
    
    Log.info("Try to serialize...........");
    return templateListElement;
  }
  
  public boolean parse(Node templateNode) throws Exception {
    Log.info("AbstractTemplateList : public boolean parse(Node templateNode) throws Exception");
    return false;
  }

}
