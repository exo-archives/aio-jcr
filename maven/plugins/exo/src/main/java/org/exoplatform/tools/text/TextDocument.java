/***************************************************************************
 * Copyright 2001-2004 The eXo Platform SARL         All rights reserved.  *
 * Please visit http://www.exoplatform.org for more license detail.        *
 **************************************************************************/

package org.exoplatform.tools.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An immutable representation of a text document. -- Line endings of different format
 * text files -- CR(#13),LF(#10) = DOS CR(#13) = MAC LF(#10) = UNIX
 * 
 * @author Hatim Khan
 * @version $Revision: 1.1 $
 */
public class TextDocument
{
  public static final String CLASS_VERSION = "$Id: TextDocument.java,v 1.1 2004/04/19 03:45:49 hatimk Exp $"; //$NON-NLS-1$
  public static final int DOS_FORMAT = 0;
  public static final int UNIX_FORMAT = 1;
  public static final int MAC_FORMAT = 2;
  public static final int DEFAULT_FORMAT = DOS_FORMAT;
  private static final String CR = "\r";
  private static final String LF = "\n";
  private static final String CRLF = "\r\n";
  private int mDocumentFormat = DEFAULT_FORMAT;
  private List mLinesContainer = new ArrayList();

  /**
   * The constructor method.
   * 
   * @param pDocument
   * 
   * @throws IOException
   */
  public TextDocument(String pDocument) throws IOException
  {
    mDocumentFormat = determineFormat(pDocument);
    initDocument(pDocument);
  }

  /**
   * Returns the format of this document. The following formats are recognized: <br>
   * DOS : CR(#13),LF(#10) <br>
   * UNIX : LF(#10) <br>
   * MAC : CR(#13) <br>
   * For this method to work, your document should have at least two lines
   * 
   * @return the format of this document
   */
  public int getDocumentFormat()
  {
    return mDocumentFormat;
  }

  /**
   * A convenient method to return a new line string that matches the format of the
   * document.
   * 
   * @return a new line string that matches the format of the document;
   */
  public String getNewLineString()
  {
    switch (mDocumentFormat)
    {
      case DOS_FORMAT:
        return CRLF;

      case UNIX_FORMAT:
        return LF;

      case MAC_FORMAT:
        return CR;

      default:
        return CRLF;
    }
  }

  /**
   * Returns the document in one string. Please be aware that this might not be exactly
   * the same document that was given to the constructor of this class.
   * 
   * @return the document in one string
   */
  public String getString()
  {
    StringBuffer sb = new StringBuffer();
    Iterator iL = mLinesContainer.iterator();
    while (iL.hasNext())
    {
      Line currentLine = (Line) iL.next();
      sb.append(currentLine.getContent());
    }

    return sb.toString();
  }

  /**
   * Given a 1-based line number and 1-based column number, returns the 0-based offset.
   * Use this offest with getString() to access the string.
   * 
   * @param pLineNumber 1-based line number
   * @param pColumnNumber 1-based column number
   * 
   * @return 0-based offest, -1 if is not within range
   */
  public int getOffset1(int pLineNumber, int pColumnNumber)
  {
    int linenumber = pLineNumber - 1;
    if ((linenumber > (mLinesContainer.size() - 1)) || (linenumber < 0) || (mLinesContainer.size() < 1))
    {
      return -1;
    }

    Line line = (Line) mLinesContainer.get(linenumber);

    int max = line.getOffset() + (line.getContent().length() - 1);
    int offset = line.getOffset() + (pColumnNumber - 1);
    return (offset > max) ? -1 : offset;
  }

  private int determineFormat(String pDocument)
  {
    // the logic is simple, first look for CR if not there then it is unix,
    // otherwise see if there is any CRLF pair if so then it is DOS
    // otherwise it is MAC
    int crLoc = pDocument.indexOf(CR);
    if (crLoc == -1)
    {
      return UNIX_FORMAT;
    }

    int crlfLoc = pDocument.indexOf(CRLF);

    return (crlfLoc == -1) ? MAC_FORMAT : DOS_FORMAT;
  }

  private void initDocument(String mDocument) throws IOException
  {
    String newLine = getNewLineString();
    int newLineLength = newLine.length();
    int offset = 0;
    BufferedReader reader = new BufferedReader(new StringReader(mDocument));
    String oneLine = reader.readLine();
    while (oneLine != null)
    {
      mLinesContainer.add(new Line(oneLine + newLine, offset));
      offset += (oneLine.length() + newLineLength);
      oneLine = reader.readLine();
    }
    reader.close();
  }

  private class Line
  {
    private int mOffset;
    private String mContent;

    public Line(String pContent, int pOffest)
    {
      mContent = pContent;
      mOffset = pOffest;
    }

    /**
     * Get the content of this line
     * 
     * @return the content of this line
     */
    public String getContent()
    {
      return mContent;
    }

    /**
     * Returns the 0-based offset of this line in the document.
     * 
     * @return the 0-based offest of the begining of this line in the document
     */
    public int getOffset()
    {
      return mOffset;
    }
  }
}