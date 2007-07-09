/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.property;

import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.AbstractDAVProperty;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WorkspaceResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: SupportedQueryGrammarSetProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class SupportedQueryGrammarSetProp extends AbstractDAVProperty {
  
  private static Log log = ExoLogger.getLogger("jcr.SupportedQueryGrammarSetProp");

  private ArrayList<String> queryGrammars = new ArrayList<String>();
  
  public SupportedQueryGrammarSetProp() {
    super(DavProperty.Search.SUPPORTEDQUERYGRAMMARSET);
  }
  
  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {
    if (!(resource instanceof WorkspaceResource)) {
      
      log.info("suppotred query grammar set >>> returned");
      
      return false;
    }
    
    Session session = ((WorkspaceResource)resource).getSession();
    
    if (session == null) {
      return false;
    }
    
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    String []supportedLanguages = queryManager.getSupportedQueryLanguages();
    for (int i = 0; i < supportedLanguages.length; i++) {
      queryGrammars.add(supportedLanguages[i]);
    }

    status = DavStatus.OK;
    return true;
  }  
  
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    super.serialize(rootDoc, parentElement);
    if (status != DavStatus.OK) {
      return;
    }
    
    {
      Element supportedQueryGrammarEl = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.Search.SUPPORTEDQUERYGRAMMAR);
      propertyElement.appendChild(supportedQueryGrammarEl);
      
      Element grammarEl = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.Search.GRAMMAR);
      supportedQueryGrammarEl.appendChild(grammarEl);

      Element basicSearchEl = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.Search.BASICSEARCH);
      grammarEl.appendChild(basicSearchEl);
    }
    
    for (int i = 0; i < queryGrammars.size(); i++) {
      Element supportedQueryGrammarEl = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.Search.SUPPORTEDQUERYGRAMMAR);
      propertyElement.appendChild(supportedQueryGrammarEl);
      
      Element grammarEl = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.Search.GRAMMAR);
      supportedQueryGrammarEl.appendChild(grammarEl);
      
      Element syntaxEl = rootDoc.createElementNS(DavConst.EXO_NAMESPACE, "exo:" + queryGrammars.get(i));
      grammarEl.appendChild(syntaxEl);      
    }
            
  }
  
}
