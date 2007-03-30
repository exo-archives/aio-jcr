/***************************************************************************
 * Copyright 2001-2005 The eXo Platform SARL         All rights reserved.  *
 * Please visit http://www.exoplatform.org for more license detail.        *
 **************************************************************************/
package org.exoplatform.tools.xml;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method ;

import org.apache.xerces.dom.CoreDocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.exoplatform.tools.text.TextDocument;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A utility class
 * 
 * @author Hatim
 */
public class XMLUtils
{
  public static Document loadDOMDocument(InputStream pInputStream) throws InterruptedException, SAXException,
      IOException
  {
    try
    {
      DOMParser parser = new DOMParser();
      parser.setFeature("http://xml.org/sax/features/namespaces", true);

      parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
      parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      parser.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
      parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      parser.setFeature("http://xml.org/sax/features/validation", false);
      parser.setFeature("http://apache.org/xml/features/validation/schema", false);
      parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);

      InputSource inputSource = new InputSource(pInputStream);
      parser.parse(inputSource);

      return parser.getDocument();
    }
    finally
    {
      if (pInputStream != null)
      {
        try
        {
          pInputStream.close();
        }
        catch (IOException e)
        {
        }
      }
    }
  }

  public static String getEncoding(Document pDoc)
  {
    // we only can do this because we are using Xerces
    CoreDocumentImpl docImpl = (CoreDocumentImpl) pDoc;
    String encoding = null ;
    try {
      Method method = pDoc.getClass().getMethod("getXmlEncoding", null) ;
      if(method != null) encoding = docImpl.getXmlEncoding() ;
    } catch (Exception ex) {
      encoding = "UTF-8";
    }
    // if no encoding was given in the XML declaration, try the actual one
    if ((encoding == null) || (encoding.length() < 1))
    {
      encoding = docImpl.getInputEncoding();
      if ((encoding == null) || (encoding.length() < 1))
      {
        // use this if everything else failed
        encoding = "UTF-8";
      }
    }

    return encoding;
  }

  public static TextDocument loadFile(InputStream pFile, String pEncoding) throws IOException
  {
    StringBuffer sb = new StringBuffer();
    BufferedReader reader;
    if ((pEncoding == null) || (pEncoding.length() < 1))
    {
      // if no encoding is given then use the default encoding
      reader = new BufferedReader(new InputStreamReader(pFile));
    }
    else
    {
      reader = new BufferedReader(new InputStreamReader(pFile, pEncoding));
    }

    int c;
    char[] carray = new char[1];

    while ((c = reader.read(carray, 0, 1)) != -1)
    {
      sb.append(carray[0]);
    }
    try
    {
      reader.close();
    }
    catch (Exception e)
    {
      //ignore
    }

    return new TextDocument(sb.toString());
  }

  public static byte[] getFormattedDoc(Document pDoc, String pEncoding, InputStream pFile)
      throws UnsupportedEncodingException, IOException
  {
    TextDocument textDoc = loadFile(pFile, pEncoding);

    OutputFormat format = new OutputFormat(pDoc);

    format.setLineSeparator(textDoc.getNewLineString());
    format.setIndenting(true);
    format.setLineWidth(0);
    format.setEncoding(pEncoding);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    XMLSerializer serializer = new XMLSerializer(outputStream, format);
    serializer.serialize(pDoc);

    //InputStream inputStream = new BufferedInputStream(new
    // ByteArrayInputStream(outputStream.toByteArray()));
    return outputStream.toByteArray();
  }

  public static void closeStream(InputStream pStream)
  {
    try
    {
      pStream.close();
    }
    catch (Exception e)
    {

    }
  }

  public static void closeStream(OutputStream pStream)
  {
    try
    {
      pStream.close();
    }
    catch (Exception e)
    {

    }
  }

}