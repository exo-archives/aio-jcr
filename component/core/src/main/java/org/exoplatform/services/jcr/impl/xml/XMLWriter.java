/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.xml;

import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.JCRName;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.util.ISO9075;

/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: XMLWriter.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class XMLWriter {

  private boolean nsWritten;
  private SessionImpl session;
  private StringBuffer buffer;
  private Stack nodes;

  public XMLWriter(SessionImpl session) {
    this.buffer = new StringBuffer();
    this.session = session;
    this.nodes = new Stack();
    this.nsWritten = false;
    String enc = System.getProperty("file.encoding");
   //this.buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    this.buffer.append("<?xml version=\"1.0\" encoding=\""+enc+"\"?>");
  }

  public void startElement(JCRName jcrName, Properties attrs) throws RepositoryException {
    Iterator keys;
  
    // encode node name
    InternalQName internalNodeName = ISO9075.encode(jcrName.getInternalName());
    String nodeName = session.getLocationFactory().
           createJCRName(internalNodeName).getAsString();
    
    if (nodeName.length() <= 0)
      nodeName = "jcr:root";
    
    buffer.append("<" + nodeName);

    if (!nsWritten)
      writeNamespaces();

    if (attrs != null) {
      keys = attrs.keySet().iterator();
      while (keys.hasNext()) {
        String key = (String) keys.next();
        String propName = ISO9075.encode(key);
        String value = (String) attrs.get(key);
        writeAttribute(propName, value);
      }
    }
    if (!nodes.empty()) {
      ((Context) nodes.peek()).isOpen = false;
    }
    nodes.push(new Context(nodeName));
    buffer.append(">");
  }

  public void endElement() {
    Context curNode;
    if (!nodes.empty())
      curNode = (Context) nodes.pop();
    else
      throw new RuntimeException("Unexpected Empty Stack at End element !!");

    buffer.append("</" + curNode.nodeName + ">");
  }

  private void writeAttribute(String qname, String value) {
    buffer.append(" " + qname + "=\"" + value + "\"");
  }

  private void writeNamespaces() throws RepositoryException {
    String[] keys = session.getAllNamespacePrefixes();
    for(int i=0; i<keys.length; i++)
    	if(keys[i].length() > 0){
        if (keys[i].equals(Constants.NS_XML_PREFIX)) continue;
        writeAttribute("xmlns:" + keys[i], session.getNamespaceURIByPrefix(keys[i]));
      }
    nsWritten = true;
  }

  public void writeText(String text) {
    buffer.append(text);
    
    /*
    if (!nodes.empty()) {
      if (text.length() > 0)
        ((Context) nodes.peek()).isOpen = false;
    } else
      throw new RuntimeException("Unexpected Empty Stack at Text '" + text + "' !!!");
    buffer.append(text);
    */
  }

  public byte[] getBytes() { //throws RepositoryException     
    return buffer.toString().getBytes();
//    try {
//      return buffer.toString().getBytes(BaseValue.DEFAULT_ENCODING);
//    } catch(UnsupportedEncodingException e) {
//      throw new RepositoryException(BaseValue.DEFAULT_ENCODING + " not supported on this platform", e);
//    }
  }

  public String toString() {
    return buffer.toString();
  }

  private class Context {
    private Context(String nodeName) {
      this.nodeName = nodeName;
      this.isOpen = true;
    }

    private String nodeName;
    private boolean isOpen;
  }
}
