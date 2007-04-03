/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient.commands;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.HttpClient;
import org.exoplatform.frameworks.webdavclient.Log;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.LSOutput;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class DavCommand {

  public static final String AUTH_BASIC = "Basic";

  //protected Document xmlDoc = null;
  protected String commandName;

  protected WebDavContext context = null;

  protected String resourcePath;
  protected int depth = 1;

  private int rangeStart = -1;
  private int rangeEnd = -1;

  protected HttpClient client;
  protected byte []requestDataBytes = null;
  protected InputStream inStream = null;
  protected byte []responseDataBytes = null;

  private boolean enableXml = true;

  public DavCommand(WebDavContext context) throws Exception {
    this.context = context;

    client = new HttpClient(context.getHost(), context.getPort());

    client.setRequestHeader(Const.HttpHeaders.CONNECTION, Const.HttpHeaders.TE);
    client.setRequestHeader(Const.HttpHeaders.TE, "trailers, deflate, gzip, compress");
    client.setRequestHeader(Const.HttpHeaders.DEPTH, "1");
    client.setRequestHeader(Const.HttpHeaders.TRANSLATE, "f");
    client.setRequestHeader(Const.HttpHeaders.ACCEPTENCODING, "deflate, gzip, x-gzip, compress, x-compress");
    //next string influence for the search by content, should be commented
    //client.setRequestHeader(Const.HttpHeaders.CONTENTTYPE, "text/xml");
  }

  public void setResourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  public void setDepth(int depth) {
    this.depth = depth;
  }

  public void setRange(int rangeStart) {
    this.rangeStart = rangeStart;
  }

  public void setRange(int rangeStart, int rangeEnd) {
    this.rangeStart = rangeStart;
    this.rangeEnd = rangeEnd;
  }

  public void setRequestDataBuffer(byte []requestDataBytes) {
    this.requestDataBytes = requestDataBytes;
  }
  
  public void setRequestInputStream(InputStream inStream, long streamLength) throws Exception {
    this.inStream = inStream;
    client.setRequestHeader(Const.HttpHeaders.CONTENTLENGTH, "" + streamLength);    
  }

  public void setXmlEnabled(boolean enableXml) {
    this.enableXml = enableXml;
  }

  public void setLockToken(String lockToken) throws Exception {
    client.setRequestHeader(Const.HttpHeaders.LOCKTOKEN, "<" + lockToken + ">");
  }

  public String getResponseHeader(String headerName) {
    return client.getResponseHeader(headerName);
  }

  public ArrayList<String> getResponseHeadersNames() {
    return client.getResponseHeadersNames();
  }

  public byte []getResponseDataBuffer() {
    return client.getResponseBytes();
  }

  private static Document getDomDocument() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();    
    return builder.newDocument();
  }  
  
  public int execute() throws Exception {
    if (enableXml) {
      Document xmlDocument = getDomDocument();
      Element rootElement = toXml(xmlDocument);      
      if (rootElement != null) {
        serializeElement(rootElement);
      }
    }

    if (inStream != null) {
      client.setRequestStream(inStream);
    } else {
      if (requestDataBytes != null) {
        client.setRequestBody(requestDataBytes);
      }      
    }

    client.setHttpCommand(commandName);

    String path = context.getServletPath();
    if (resourcePath != null) {
      path += resourcePath;
    }

    client.setRequestPath(path);

    if (context.getUserId() != null) {
      String userId = context.getUserId();
      String userPass = context.getUserPass();

      byte []encoded = Base64.encodeBase64(new String(userId + ":" + userPass).getBytes());
      String encodedAuth = new String(encoded);

      client.setRequestHeader(Const.HttpHeaders.AUTHORIZATION, AUTH_BASIC + " " + encodedAuth);
    }

    client.setRequestHeader(Const.HttpHeaders.DEPTH, String.format("%d", depth));

    if (rangeStart >= 0) {
      String rangeHeader = "bytes=" + rangeStart + "-";
      if (rangeEnd >= 0) {
        rangeHeader += rangeEnd;
      }
      client.setRequestHeader(Const.HttpHeaders.RANGE, rangeHeader);
    }

    client.conect();

    int status = client.execute();

    finalExecute();
    return status;
  }

  public abstract void finalExecute();

  public abstract Element toXml(Document xmlDocument);

  private void serializeElement(Element element) throws Exception {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    
    DOMSource source = new DOMSource(element.getOwnerDocument());
    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    StreamResult resultStream = new StreamResult(outStream);
    
    transformer.transform(source, resultStream);
    
    requestDataBytes = outStream.toByteArray();
    
    Log.info("REQUEST:\r\n" + new String(requestDataBytes));
  }  
  
  static class Output implements LSOutput {

    OutputStream bs;
    Writer cs;
    String sId;
    String enc;

    public Output() {
        bs = null;
        cs = null;
        sId = null;
        enc = "UTF-8";
    }

    public OutputStream getByteStream() {
        return bs;
    }
    public void setByteStream(OutputStream byteStream) {
        bs = byteStream;
    }
    public Writer getCharacterStream() {
        return cs;
    }
    public void setCharacterStream(Writer characterStream) {
        cs = characterStream;
    }
    public String getSystemId() {
        return sId;
    }
    public void setSystemId(String systemId) {
        sId = systemId;
    }
    public String getEncoding() {
        return enc;
    }
    public void setEncoding(String encoding) {
        enc = encoding;
    }
  }

}
