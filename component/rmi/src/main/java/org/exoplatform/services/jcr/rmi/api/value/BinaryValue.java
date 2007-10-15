/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.value;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * The <code>BinaryValue</code> class implements the committed value state for
 * Binary values as a part of the State design pattern (Gof) used by this
 * package.
 * <p>
 * NOTE: This class forwards the <code>InputStream</code> from which it was
 * created through the {@link #getStream()} method but does not close the
 * stream. It is the sole responsibility of the user of this value to close the
 * stream if not needed anymore to prevent memory loss.
 * <p>
 * This class implements {@link #readObject(ObjectInputStream)} and
 * {@link #writeObject(ObjectOutputStream)} methods to (de-)serialize the data.
 * 
 * @since 0.16.4.1
 * @see org.exoplatform.services.jcr.rmi.api.value.SerialValue
 */
public class BinaryValue /*implements Serializable, StatefulValue*/ extends BaseNonStreamValue {

  /** The serial version UID */
  private static final long serialVersionUID = -2410070522924274051L;

  /** The <code>InputStream</code> providing the value */
  private InputStream       stream;
  

  
  /**
   * Creates an instance on the given <code>InputStream</code>. This exact
   * stream will be provided by the {@link #getStream()}, thus care must be
   * taken to not inadvertendly read or close the stream.
   * 
   * @param stream The <code>InputStream</code> providing the value.
   */
  protected BinaryValue(InputStream stream) {
    this.stream = stream;
  }

  /**
   * Creates an instance providing the UTF-8 representation of the given string
   * value.
   * 
   * @param value The string whose UTF-8 representation is provided as the value
   *          of this instance.
   * @throws ValueFormatException If the platform does not support UTF-8
   *           encoding (which is unlikely as UTF-8 is required to be available
   *           on all platforms).
   */
  protected BinaryValue(String value) throws ValueFormatException {
    this(toStream(value));
  }

  /**
   * Helper method to convert a string value into an <code>InputStream</code>
   * from which the UTF-8 representation can be read.
   * 
   * @param value The string value to be made available through a stream.
   * @return The <code>InputStream</code> from which the UTF-8 representation
   *         of the <code>value</code> may be read.
   * @throws ValueFormatException If the platform does not support UTF-8
   *           encoding (which is unlikely as UTF-8 is required to be available
   *           on all platforms).
   */
  protected static InputStream toStream(String value) throws ValueFormatException {
    try {
      return new ByteArrayInputStream(value.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new ValueFormatException("Invalid string value encoding", e);
    }
  }

  /**
   * Returns the <code>InputStream</code> from which this instance has been
   * created.
   * 
   * @return value stream
   */
  public InputStream getStream() {
    return stream;
  }

  /**
   * Returns <code>PropertyType.BINARY</code>.
   * 
   * @return property type
   */
  public int getType() {
    return PropertyType.BINARY;
  }

  /**
   * Always throws <code>IllegalStateException</code> because only an
   * <code>InputStream</code> is available from this implementation.
   * 
   * @return nothing
   * @throws IllegalStateException as defined above.
   */
  public String getString() throws IllegalStateException {
    throw new IllegalStateException("Stream already retrieved");
  }

  /**
   * Always throws <code>IllegalStateException</code> because only an
   * <code>InputStream</code> is available from this implementation.
   * 
   * @return nothing
   * @throws IllegalStateException as defined above.
   */
  public long getLong() throws IllegalStateException {
    throw new IllegalStateException("Stream already retrieved");
  }

  /**
   * Always throws <code>IllegalStateException</code> because only an
   * <code>InputStream</code> is available from this implementation.
   * 
   * @return nothing
   * @throws IllegalStateException as defined above.
   */
  public double getDouble() throws IllegalStateException {
    throw new IllegalStateException("Stream already retrieved");
  }

  /**
   * Always throws <code>IllegalStateException</code> because only an
   * <code>InputStream</code> is available from this implementation.
   * 
   * @return nothing
   * @throws IllegalStateException as defined above.
   */
  public Calendar getDate() throws IllegalStateException {
    throw new IllegalStateException("Stream already retrieved");
  }

  /**
   * Always throws <code>IllegalStateException</code> because only an
   * <code>InputStream</code> is available from this implementation.
   * 
   * @return nothing
   * @throws IllegalStateException as defined above.
   */
  public boolean getBoolean() throws IllegalStateException {
    throw new IllegalStateException("Stream already retrieved");
  }

  /**
   * Writes the contents of the underlying stream to the
   * <code>ObjectOutputStream</code>.
   * 
   * @param out The <code>ObjectOutputStream</code> to where the binary data
   *          is copied.
   * @throws IOException If an error occurrs writing the binary data.
   */
  private void writeObject(ObjectOutputStream out) throws IOException {
    byte[] buffer = new byte[4096];
    int bytes = 0;
    while ((bytes = stream.read(buffer)) >= 0) {
      if (bytes > 0) {
        // just to ensure that no 0 is written
        out.writeInt(bytes);
        out.write(buffer, 0, bytes);
      }
    }
    // Write the end of stream marker
    out.writeInt(0);
    // close stream
    stream.close();
  }

  /**
   * Reads the binary data from the <code>ObjectInputStream</code> into a
   * temporary file that is used to back up the binary stream contents of the
   * constructed value instance. The temporary file gets deleted when the binary
   * stream is closed or garbage collected.
   * 
   * @param in The <code>ObjectInputStream</code> from where to get the binary
   *          data.
   * @throws IOException If an error occurrs reading the binary data.
   */
  private void readObject(ObjectInputStream in) throws IOException {
    final File file = File.createTempFile("jcr-value", "bin");

    OutputStream out = new FileOutputStream(file);
    byte[] buffer = new byte[4096];
    for (int bytes = in.readInt(); bytes > 0; bytes = in.readInt()) {
      if (buffer.length < bytes) {
        buffer = new byte[bytes];
      }
      in.readFully(buffer, 0, bytes);
      out.write(buffer, 0, bytes);
    }
    out.close();

    stream = new FileInputStream(file) {

      private boolean closed = false;

      @Override
      public void close() throws IOException {
        super.close();
        closed = true;
        file.delete();
      }

      @Override
      protected void finalize() throws IOException {
        try {
          if (!closed) {
            file.delete();
          }
        } finally {
          super.finalize();
        }
      }
    };
  }

  public long getLength() {
    try {
      return stream.available();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0;
  }

  public int getOrderNumber() {
    // TODO Auto-generated method stub
    return 0;
  }

  public String getReference() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    // TODO Auto-generated method stub
    byte[] buff = new byte[IdGenerator.IDENTIFIER_LENGTH];
    try {
      stream.read(buff);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return buff.toString();
  }

  public void setOrderNumber(int arg0) {
    // TODO Auto-generated method stub
    
  }


}
