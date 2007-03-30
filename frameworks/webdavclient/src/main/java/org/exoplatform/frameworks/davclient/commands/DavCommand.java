/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient.commands;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.HttpClient;
import org.exoplatform.frameworks.davclient.ServerLocation;
import org.exoplatform.frameworks.davclient.Const.HttpHeaders;
import org.exoplatform.services.log.ExoLogger;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class DavCommand {

  private static Log log = ExoLogger.getLogger("jcr.DavCommand");

  public static final String AUTH_BASIC = "Basic";

  protected Document xmlDoc = null;
  protected String commandName;

  protected ServerLocation location = null;

  protected String resourcePath;
  protected int depth = 1;

  private int rangeStart = -1;
  private int rangeEnd = -1;

  protected HttpClient client;
  protected byte []requestDataBytes = null;
  protected byte []responseDataBytes = null;

  private boolean enableXml = true;

  public DavCommand(ServerLocation location) throws Exception {
    this.location = location;

    client = new HttpClient(location.getHost(), location.getPort());

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

  public int execute() throws Exception {
    if (enableXml) {
      xmlDoc = toXml(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
      if (xmlDoc != null) {
        serializeXml(xmlDoc);
      }
    }

    if (requestDataBytes != null) {
      client.setRequestBody(requestDataBytes);
    }

    client.setHttpCommand(commandName);

    String path = location.getServletPath();
    if (resourcePath != null) {
      path += resourcePath;
    }

    client.setRequestPath(path);

    if (location.getUserId() != null) {
      String userId = location.getUserId();
      String userPass = location.getUserPass();

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


//    log.info("STATUS: " + status);
//    ArrayList<String> headers = client.getResponseHeadersNames();
//    for (int i = 0; i < headers.size(); i++) {
//      String curHeader = headers.get(i);
//      String curHeaderValue = client.getResponseHeader(curHeader);
//      log.info("HEADER: [" + curHeader + "] = [" + curHeaderValue + "]");
//    }
//
//    log.info("REPLY: " + new String(client.getResponseBody()));


    finalExecute();
    return status;
  }

  public abstract void finalExecute();

  public abstract Document toXml(Document xmlDocument);

  public void serializeXml(Document xmlDocument) throws Exception {
    DOMImplementation impl = xmlDocument.getImplementation();
    DOMImplementationLS implLS =  (DOMImplementationLS) impl.getFeature("LS","3.0");
    LSSerializer writer = implLS.createLSSerializer();

    Output out = new Output();
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    out.setByteStream(outStream);

    writer.write(xmlDocument, out);
    requestDataBytes = outStream.toByteArray();

//    String xml = new String(requestDataBytes);
//    log.info("-----------------------\r\n" + xml + "\r\n");
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
