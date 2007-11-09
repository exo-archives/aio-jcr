/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lnkproducer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class LinkGenerator {
  
  // MICROSOFT *.LNK FILE HEADER
  public static int []linkHeader = {
    // 0h 1 dword Always 0000004Ch �L�
      0x4C, 0x00, 0x00, 0x00,
    
    // 4h 16 bytes GUID of shortcut files    
      0x01, 0x14/*0x04*/, 0x02, 0x00,    
      0x00, 0x00, 0x00, 0x00,
      0xC0, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x46,
    
    // 14h 1 dword Flags
      0x81/*0x00*/, 0x00, 0x00, 0x00,
    
    // 18h 1 dword File attributes
      0x00, 0x00, 0x00, 0x00,
    
    // 1Ch 1 qword Time 1
      0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00,
    
    // 24h 1 qword Time 2
      0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00,
    
    // 2Ch 1 qword Time 3
      0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00,
    
    // 34h 1 dword File length
      0x00, 0x00, 0x00, 0x00,
    
    // 38h 1 dword Icon number
      0x00, 0x00, 0x00, 0x00,
    
    // 3Ch 1 dword ShowWnd value
      0x01, 0x00, 0x00, 0x00,

    // 40h 1 dword Hot key
      0x00, 0x00, 0x00, 0x00,
    
    // 44h 2 dwords Unknown, always zero
      0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00
  };
  
  private String hostName;
  private String servletPath;
  private String targetPath;
  
  public LinkGenerator(String hostName, String servletPath, String targetPath) {
    this.hostName = hostName;
    this.servletPath = servletPath;
    this.targetPath = targetPath;
  }
  
  public byte []generateLinkContent() throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    
    // LINK HEADER
    for (int i = 0; i < linkHeader.length; i++) {
      byte curByteValue = (byte)linkHeader[i];
      outStream.write(curByteValue);
    }
    
    // LINK BODY
    byte []linkContent = getLinkContent();    
    writeInt(linkContent.length + 2, outStream);
    outStream.write(linkContent);
    
    // WRITE END LINK FILE
    for (int i = 0; i < 6; i++) {
      outStream.write(0);
    }
    
    return outStream.toByteArray();
  }
  
  private byte []getLinkContent() throws IOException {    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    byte []firstItem = getFirstItem();
    writeInt(firstItem.length + 2, outStream);
    writeBytes(firstItem, outStream);
    
    byte []lastItem = getLastItem();
    writeInt(lastItem.length + 2, outStream);
    writeBytes(lastItem, outStream);
    
    String []pathes = servletPath.split("/");
    String root = pathes[pathes.length - 1];

    byte []rootItem = getRootItem(root, servletPath);    
    writeInt(rootItem.length + 2, outStream);
    writeBytes(rootItem, outStream);
    
    pathes = targetPath.split("/");
    String curHref = servletPath;
      
    for (int i= 0; i < pathes.length; i++) {
      if ("".equals(pathes[i])) {
        continue;
      }
      
      String curName = pathes[i];
      curHref += "/" + curName;
      
      if (i < pathes.length - 1) {
        byte []linkItem = getHreffedFolder(curName, curHref);
        writeInt(linkItem.length + 2, outStream);
        writeBytes(linkItem, outStream);
      } else {
        byte []linkFile = getHreffedFile(curName, curHref);
        writeInt(linkFile.length + 2, outStream);
        writeBytes(linkFile, outStream);            
      }
      
    }
    
    return outStream.toByteArray();
  }
  
  private byte []getFirstItem() throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    int []firstItem = {        
        0x1F, 0x50, 0xE0, 0x4F,
        0xD0, 0x20, 0xEA, 0x3A,
        0x69, 0x10, 0xA2, 0xD8,
        0x08, 0x00, 0x2B, 0x30,
        0x30, 0x9D,
    };
    
    writeInts(firstItem, outStream);
    
    return outStream.toByteArray();
  }

  private byte []getLastItem() throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    
    int []lastItem = {        
        0x2E, 0x80, 0x00, 0xDF,
        0xEA, 0xBD, 0x65, 0xC2,
        0xD0, 0x11, 0xBC, 0xED,
        0x00, 0xA0, 0xC9, 0x0A,
        0xB5, 0x0F          
    };
    
    writeInts(lastItem, outStream);
    
    return outStream.toByteArray();
  }
  
  private byte []getRootItem(String rootName, String servlet) throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    
    byte []rootHead = getRootHeader();
    writeBytes(rootHead, outStream);
    
    byte []rootValue = getRootValue(rootName);
    writeInt(rootValue.length / 2, outStream);
    writeBytes(rootValue, outStream);
    
    outStream.write(0);
    outStream.write(0);
    
    writeSizedString(servlet, outStream);
    
    writeZeroQWord(outStream);
    
    return outStream.toByteArray();
  }
  
  private byte []getRootHeader() throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    int []rootHeader = {
        0x4C, 0x50, 0x00, 0x01, 0x42, 0x57, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
        0x00, 0x00
    };
    writeInts(rootHeader, outStream);
    
    return outStream.toByteArray();
  }
  
  private byte []getRootValue(String rootName) throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    
    simpleWriteString(rootName, outStream);
    int []rootVal = {
        0x20, 0x00, 0x3D, 0x04, 0x30, 0x04, 0x20, 0x00  
    };
    writeInts(rootVal, outStream);

    simpleWriteString(hostName, outStream);
    
    return outStream.toByteArray();
  }
  
  private byte []getHreffedFolder(String itemName, String itemHref) throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    
    int []linkHrefHeader = {
        0x4C, 0x50, 0x00, 0x22,
        0x42, 0x57, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x10, 0x00,
        0x00, 0x40
    };
    writeInts(linkHrefHeader, outStream);
    
    writeSizedString(itemName, outStream);
    writeSizedString(itemHref, outStream);

    writeZeroQWord(outStream);
        
    return outStream.toByteArray();
  }
  
  private byte []getHreffedFile(String itemName, String itemHref) throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    
    int []attrs = {
        0x4C, 0x50, 0x00, 0x22,
        0x42, 0x57, 0xF0, 0x43,
        0x1C, 0x29, 0xB6, 0x5C,
        0xC7, 0x01, 0x00, 0x7C,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x80, 0x00,
        0x00, 0x00
    };
    writeInts(attrs, outStream);
    
    writeSizedString(itemName, outStream);
    writeSizedString(itemHref, outStream);
    
    writeZeroQWord(outStream);
    
    return outStream.toByteArray();
  }
  
  private void simpleWriteString(String outString, OutputStream outStream) throws IOException {
    for (int i = 0; i < outString.length(); i++) {
      char curChar = outString.charAt(i);
      outStream.write((byte)curChar);
      outStream.write(0);
    }
  }
  
  private void writeZeroString(String outString, OutputStream outStream) throws IOException {
    simpleWriteString(outString, outStream);
    outStream.write(0);
    outStream.write(0);
  }
  
  private void writeSizedString(String outString, OutputStream outStream) throws IOException {    
    int stringLength = outString.length();
    writeInt(stringLength, outStream);
    writeZeroString(outString, outStream);
  }
  
  private void writeInt(int intValue, OutputStream outStream) throws IOException {
    int lowByte = intValue & 0xFF;
    int highByte = (intValue & 0xFF00) >> 8;
    outStream.write((byte)lowByte);
    outStream.write((byte)highByte);    
  }
  
  private void writeInts(int []bytes, OutputStream outStream) throws IOException {
    for (int i = 0; i < bytes.length; i++) {
      byte curByte = (byte)bytes[i];
      outStream.write(curByte);
    }        
  }

  private void writeBytes(byte []bytes, OutputStream outStream) throws IOException {
    for (int i = 0; i < bytes.length; i++) {
      byte curByte = bytes[i];
      outStream.write(curByte);
    }        
  }  
  
  private void writeZeroQWord(OutputStream outStream) throws IOException {
    int []zeroQWord = {0x00, 0x00, 0x00, 0x00};
    writeInts(zeroQWord, outStream);        
  }
  
}
