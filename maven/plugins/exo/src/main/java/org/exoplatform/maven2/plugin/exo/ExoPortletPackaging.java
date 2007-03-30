/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.exoplatform.maven2.plugin.Utils;
import org.exoplatform.tools.xml.webapp.v23.ModifyWebXMLOperation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
/** 
 * Created by The eXo Platform SARL
 * Author : Phung Hai Nam
 *          phunghainam@gmail.com
 * Nov 18, 2005
 */
/**
 * @goal portletWar
 * 
 * @requiresDependencyResolution runtime
 * @description mvn exo:portlet 
 */
public class ExoPortletPackaging extends ExoPackaging {
  /**
   * Single directory for extra files to include in the WAR.
   *
   * @parameter expression="${basedir}/src/webapp"
   * @required
   */
  private File warSourceDirectory;
  /**
   * The name of the generated war.
   *
   * @parameter expression="${project.build.finalName}"
   * @required
   */
  private String warName;
  
  public void execute() throws MojoExecutionException {
    try {
      File webappDest = new File(outputDirectory + "/" + warName) ;
      Utils.copyDirectoryStructure(warSourceDirectory, webappDest, Utils.getDefaultIgnoreFiles());
      //copy classes 
      File classesSrc = new File(outputDirectory +  "/classes") ;
      if(classesSrc.exists()) {
        File webappClassDest = new File(outputDirectory + "/" + warName + "/WEB-INF/classes") ;
        if(!webappClassDest.exists()) webappClassDest.mkdir() ;
        FileUtils.copyDirectoryStructure(classesSrc, webappClassDest);
      }
      String webxml  = outputDirectory + "/" + warName +  "/WEB-INF/web.xml" ;
      modifyWeb(webxml,webxml, warName) ; 
      File warFile = new File( outputDirectory, warName + ".war" );
      performPackaging(warFile, webappDest);
    } catch(Exception ex) {
      throw new MojoExecutionException("Error", ex) ;
    }
  }  
  
  public void modifyWeb(String inputURL, String outputURL, String warName) throws Exception {
    //MavenJellyContext jcontext = (MavenJellyContext)getContext() ;
    ClassLoader old = Thread.currentThread().getContextClassLoader() ;
    Thread.currentThread().setContextClassLoader(ModifyWebXMLOperation.class.getClassLoader()) ;
    ModifyWebXMLOperation op = new ModifyWebXMLOperation(warName);
    File inputFile = new File(inputURL) ;
    File outputFile = new File(outputURL) ;
    try {
      op.modifyWebXML(inputFile, outputFile);
    } finally {
      Thread.currentThread().setContextClassLoader(old) ;
    }
  }
  
  
  public void modifyWithDOM(InputStream is) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setIgnoringComments(true);
    factory.setCoalescing(true);
    factory.setNamespaceAware(false);
    factory.setValidating(false);
    DocumentBuilder parser = factory.newDocumentBuilder();
    Document document = parser.parse(is);
    
    String encoding = "UTF-8" ;
    Transformer serializer = TransformerFactory.newInstance().newTransformer();
    serializer.setOutputProperty(OutputKeys.ENCODING, encoding);
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    serializer.transform(new DOMSource(document), new StreamResult(bout));
    System.out.println(bout.toString(encoding)) ;
  }
  
  private Node addExoPortletDeployer(Document pDoc) {
    Node listener = pDoc.createElement("listener");
    Node listenerClass = pDoc.createElement("listener-class");
    listenerClass.appendChild(pDoc.createTextNode("test"));
    listener.appendChild(listenerClass);
    return listener;
  }
  
  protected Node getWebAppNode(Document pDoc) throws Exception {
    Node result = pDoc.getElementsByTagName("web-app").item(0) ;
    return result;
  }
}
