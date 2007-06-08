/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.net.URI;
import java.util.Map;


/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ResourceIdentifier {

  private Map<String, String> parameters = null; 
  private URI uri;
  
  public ResourceIdentifier(String uriString) {
    String str = (uriString.endsWith("/")) ? uriString : (uriString+"/");
    this.uri = URI.create(str);
  }
  
  public URI getURI() {
    return uri;
  }
  
  public Map<String, String> getParameters() throws IllegalStateException {
    if(parameters == null)
      throw new IllegalStateException("Prarameters are not initialized. Call initParameters(pattern) first");
    return parameters;
  }

  public void initParameters(URIPattern pattern) {
    this.parameters = pattern.parse(uri.toASCIIString());
  }
  
  public void relativizeAgainst(String shorterURI) {
    this.uri = URI.create(shorterURI).relativize(uri);
  }

}
