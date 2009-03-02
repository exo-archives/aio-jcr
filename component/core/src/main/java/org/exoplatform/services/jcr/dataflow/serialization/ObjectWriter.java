/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.dataflow.serialization;

import java.io.IOException;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 13.02.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: JCRObjectOutput.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public interface ObjectWriter {

  /**
   * Writes an array of bytes. This method will block until the bytes
   * are actually written.
   * @param b the data to be written
   * @exception IOException If an I/O error has occurred.
   */
  public void write(byte b[]) throws IOException;
  
  /**
   * Writes a sub array of bytes.
   * @param b the data to be written
   * @param off the start offset in the data
   * @param len the number of bytes that are written
   * @exception IOException If an I/O error has occurred.
   */
  public void write(byte b[], int off, int len) throws IOException;

  /**
   * Flushes the stream. This will write any buffered
   * output bytes.
   * @exception IOException If an I/O error has occurred.
   */
  public void flush() throws IOException;

  /**
   * Closes the stream. This method must be called
   * to release any resources associated with the
   * stream.
   * @exception IOException If an I/O error has occurred.
   */
  public void close() throws IOException;
  
  /**
   * Writes a <code>boolean</code> value to this output stream.
   * If the argument <code>v</code>
   * is <code>true</code>, the value <code>(byte)1</code>
   * is written; if <code>v</code> is <code>false</code>,
   * the  value <code>(byte)0</code> is written.
   * The byte written by this method may
   * be read by the <code>readBoolean</code>
   * method of interface <code>DataInput</code>,
   * which will then return a <code>boolean</code>
   * equal to <code>v</code>.
   *
   * @param      v   the boolean to be written.
   * @exception  IOException  if an I/O error occurs.
   */
  void writeBoolean(boolean v) throws IOException;
  
  /**
   * Writes an <code>int</code> value, which is
   * comprised of four bytes, to the output stream.
   * The byte values to be written, in the  order
   * shown, are:
   * <p><pre><code>
   * (byte)(0xff &amp; (v &gt;&gt; 24))
   * (byte)(0xff &amp; (v &gt;&gt; 16))
   * (byte)(0xff &amp; (v &gt;&gt; &#32; &#32;8))
   * (byte)(0xff &amp; v)
   * </code></pre><p>
   * The bytes written by this method may be read
   * by the <code>readInt</code> method of interface
   * <code>DataInput</code> , which will then
   * return an <code>int</code> equal to <code>v</code>.
   *
   * @param      v   the <code>int</code> value to be written.
   * @exception  IOException  if an I/O error occurs.
   */
  void writeInt(int v) throws IOException;
  
  /**
   * Writes a <code>long</code> value, which is
   * comprised of eight bytes, to the output stream.
   * The byte values to be written, in the  order
   * shown, are:
   * <p><pre><code>
   * (byte)(0xff &amp; (v &gt;&gt; 56))
   * (byte)(0xff &amp; (v &gt;&gt; 48))
   * (byte)(0xff &amp; (v &gt;&gt; 40))
   * (byte)(0xff &amp; (v &gt;&gt; 32))
   * (byte)(0xff &amp; (v &gt;&gt; 24))
   * (byte)(0xff &amp; (v &gt;&gt; 16))
   * (byte)(0xff &amp; (v &gt;&gt;  8))
   * (byte)(0xff &amp; v)
   * </code></pre><p>
   * The bytes written by this method may be
   * read by the <code>readLong</code> method
   * of interface <code>DataInput</code> , which
   * will then return a <code>long</code> equal
   * to <code>v</code>.
   *
   * @param      v   the <code>long</code> value to be written.
   * @exception  IOException  if an I/O error occurs.
   */
  void writeLong(long v) throws IOException;
  
}
