package org.exoplatform.services.webdav.search.basicsearch.convertor;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.StringReader;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXParseException;

/**
 * Created by The eXo Platform SARL
 * Author : Zagrebin Victor <zagrebin_v@mail.ru>
 * @version $Id: SaxValidator.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class SaxValidator
{
 private StreamSource schema;
 private File xml_file;
 private String xml;

 private String dasl_prefix;

 private String xmlns = "xmlns:";

 /**
  * Initializes an xml file and schema sources.
  *
  * @param xml a string with dav:basicsearch query
  * @param schema an xsd file for dav:basicsearch validation
  */
 public SaxValidator(String xml)
 {
  this.xml = new String(xml);
  this.xml = this.xml.replaceFirst("DAV:", "DAV:/");
  definePrefixes(this.xml);
  DynamicXSD dxsd = new DynamicXSD(dasl_prefix);
  StringReader xsd_sr = new StringReader(dxsd.xsd);
  this.schema = new StreamSource(xsd_sr);
 }

 /**
  * Initializes an xml file and schema sources.
  *
  * @param xml_file a file with dav:basicsearch query
  * @param schema an xsd file for dav:basicsearch validation
  */
 public SaxValidator(File xml_file, File schema)
 {
  this.schema = new StreamSource(schema);
  this.xml_file = xml_file;
 }

 /**
  * Initiate a validation process
  *
  * @param s an XML string with DAV:searchrequest query
  */
 public void validateSource(String s) throws Exception {   
   //RFC 2396 (URI spec) URI syntax disallows just using DAV: by seniority
   //but RFC 2518 Group dosen't think so (WebDAV spec)
   //Note that in some cases this discrepancy between specifications may cause interrop problems.
   //so the replaceFirst temporary solves this problem for this validator

   try
   {
    SAXParserFactory spf = SAXParserFactory.newInstance();

    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

     Schema schema = schemaFactory.newSchema(this.schema);
     spf.setSchema(schema);
     spf.setNamespaceAware(true);
     spf.setValidating(true);
     Validator v = schema.newValidator();
     StringReader sr = new StringReader(s);
     v.validate(new StreamSource(sr));
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * Defines DASL prefix.
   *
   * @param s an XML string with DAV:searchrequest query
   */
  public void definePrefixes(String s)
  {
   int end = s.indexOf("DAV:/");
   int start = s.lastIndexOf(xmlns,end);
   dasl_prefix = s.substring(start+xmlns.length(), end - 1);
   dasl_prefix = dasl_prefix.replaceAll("="," ");
   dasl_prefix = dasl_prefix.trim() + ":";
  }

  /**
   * Returns DASL prefix
   *
   * @return DASL prefix
   */
  public String getDASLPrefix()
  {
   return dasl_prefix;
  }

 /**
  * Analyses the source XML and initiates a validation.
  *
  * @param file source file
  * @return the string with file content.
  * @throws Exception
  */
 public void validate() throws Exception
 {
  if(xml_file == null) validateSource(xml);
  else validateSource(readStringFromFile(xml_file));
 }

 /**
  * Reads content from specified file into the String.
  *
  * @param file source file
  * @return The string with file content.
  * @throws Exception
  */
 public String readStringFromFile(File file) throws Exception
 {
  int size;
  String s = file.toString();
  InputStream f = new FileInputStream(s);
  size = f.available();
  byte b[] = new byte[size];
  f.read(b);
  f.close();
  s = new String(b);
  return s;
 }

 /**
  * Analyze the type of exeption.
  * If it is a parsing error than outputs a location one,
  * else outputs the message of exception.
  *
  * @param e an exception.
  */
 private void fail(Exception e)
 {
  if (e instanceof SAXParseException)
  {
   SAXParseException spe = (SAXParseException) e;
   System.err.printf("line: %d column: %d message: %s%n",
                     spe.getLineNumber(),
                     spe.getColumnNumber(), spe.getMessage());
  }
  else
  {
   System.err.println(e.getMessage());
  }
  //System.exit(1);
 }
}