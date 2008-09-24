/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.core.value;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.datamodel.Identifier;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.JCRName;
import org.exoplatform.services.jcr.impl.core.JCRPath;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.JCRDateFormat;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.WorkspaceFileCleanerHolder;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;

/**
 * Created by The eXo Platform SAS.<br/> ValueFactory implementation
 * 
 * @author Gennady Azarenkov
 * @version $Id: ValueFactoryImpl.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class ValueFactoryImpl implements ValueFactory {

  private LocationFactory locationFactory;

  private FileCleaner     fileCleaner;

  private File            tempDirectory;

  private int             maxBufferSize;

  public ValueFactoryImpl(LocationFactory locationFactory,
                          WorkspaceEntry workspaceConfig,
                          WorkspaceFileCleanerHolder cleanerHolder) {

    this.locationFactory = locationFactory;
    this.fileCleaner = cleanerHolder.getFileCleaner();

    this.tempDirectory = new File(System.getProperty("java.io.tmpdir"));

    // TODO we use WorkspaceDataContainer constants but is it ok?
    this.maxBufferSize = workspaceConfig.getContainer()
                                        .getParameterInteger(WorkspaceDataContainer.MAXBUFFERSIZE,
                                                             WorkspaceDataContainer.DEF_MAXBUFFERSIZE);
  }

  public ValueFactoryImpl(LocationFactory locationFactory) {

    this.locationFactory = locationFactory;
    this.tempDirectory = new File(System.getProperty("java.io.tmpdir"));
    this.maxBufferSize = WorkspaceDataContainer.DEF_MAXBUFFERSIZE;
  }

  /*
   * @see javax.jcr.ValueFactory#createValue(java.lang.String, int)
   */
  public Value createValue(String value, int type) throws ValueFormatException {
    if (value == null)
      return null;

    try {
      switch (type) {
      case PropertyType.STRING:
        return createValue(new String(value));
      case PropertyType.BINARY:
        try {
          return createValue(new ByteArrayInputStream(value.getBytes(Constants.DEFAULT_ENCODING)));
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException("FATAL ERROR Charset " + Constants.DEFAULT_ENCODING
              + " is not supported!");
        }
      case PropertyType.BOOLEAN:
        return createValue(Boolean.parseBoolean(value));
      case PropertyType.LONG:
        return createValue(Long.parseLong(value));
      case PropertyType.DOUBLE:
        return createValue(Double.parseDouble(value));
      case PropertyType.DATE:
        Calendar cal = JCRDateFormat.parse(value);
        return createValue(cal);
      case PropertyType.PATH:
        try {
          JCRPath path;
          if (value.startsWith("/"))
            path = locationFactory.parseAbsPath(value);
          else
            path = locationFactory.parseRelPath(value);
          return createValue(path);
        } catch (RepositoryException e) {
          throw new ValueFormatException("Path '" + value + "' is invalid");
        }
      case PropertyType.NAME:
        try {
          JCRName name = locationFactory.parseJCRName(value);
          return createValue(name);
        } catch (RepositoryException e) {
          throw new ValueFormatException("Name '" + value + "' is invalid", e);
        }
      case PropertyType.REFERENCE:
        return createValue(new Identifier(value));
      case ExtendedPropertyType.PERMISSION:
        try {
          return PermissionValue.parseValue(value);
        } catch (IOException e) {
          new ValueFormatException("Cant create PermissionValue " + e);
        }
      default:
        throw new ValueFormatException("unknown type " + type);
      }
    } catch (IllegalArgumentException e) { // NumberFormatException
      throw new ValueFormatException("Cant create value from string '" + value + "' for type "
          + PropertyType.nameFromValue(type));
    }
  }

  /*
   * @see javax.jcr.ValueFactory#createValue(java.lang.String)
   */
  public Value createValue(String value) {
    if (value == null)
      return null;
    try {
      return new StringValue(value);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

  }

  /*
   * @see javax.jcr.ValueFactory#createValue(long)
   */
  public Value createValue(long value) {
    try {
      return new LongValue(value);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

  }

  /*
   * @see javax.jcr.ValueFactory#createValue(double)
   */
  public Value createValue(double value) {
    try {
      return new DoubleValue(value);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

  }

  /*
   * @see javax.jcr.ValueFactory#createValue(boolean)
   */
  public Value createValue(boolean value) {
    try {
      return new BooleanValue(value);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

  }

  /*
   * @see javax.jcr.ValueFactory#createValue(java.util.Calendar)
   */
  public Value createValue(Calendar value) {
    if (value == null)
      return null;
    try {
      return new DateValue(value);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /*
   * @see javax.jcr.ValueFactory#createValue(java.io.InputStream)
   */
  public Value createValue(InputStream value) {
    if (value == null)
      return null;
    try {
      return new BinaryValue(value, fileCleaner, tempDirectory, maxBufferSize);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /*
   * @see javax.jcr.ValueFactory#createValue(javax.jcr.Node)
   */
  public Value createValue(Node value) throws RepositoryException {
    if (value == null)
      return null;
    if (!value.isNodeType("mix:referenceable"))
      throw new ValueFormatException("Node " + value.getPath() + " is not referenceable");
    try {
      if (value instanceof NodeImpl) {
        String jcrUuid = ((NodeImpl) value).getInternalIdentifier();
        return new ReferenceValue(new TransientValueData(jcrUuid));
      } else {
        throw new RepositoryException("Its need a NodeImpl instance of Node");
      }
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  // /////////////////////////

  /**
   * @param value
   * @return new NameValue
   * @throws RepositoryException
   */
  public Value createValue(JCRName value) throws RepositoryException {
    if (value == null)
      return null;
    try {
      return new NameValue(value.getInternalName(), locationFactory);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * @param value
   * @return new PathValue
   * @throws RepositoryException
   */
  public Value createValue(JCRPath value) throws RepositoryException {
    if (value == null)
      return null;
    try {
      return new PathValue(value.getInternalPath(), locationFactory);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * @param value
   * @return NEW ReferenceValue
   */
  public Value createValue(Identifier value) {
    if (value == null)
      return null;
    try {
      return new ReferenceValue(value);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

  }

  /**
   * Creates new Value object using ValueData
   * 
   * @param data
   * @param type
   * @return new Value
   * @throws RepositoryException
   */
  public Value loadValue(TransientValueData data, int type) throws RepositoryException {

    try {
      switch (type) {
      case PropertyType.STRING:
        return new StringValue(data);
      case PropertyType.BINARY:
        return new BinaryValue(data, fileCleaner, tempDirectory, maxBufferSize);
      case PropertyType.BOOLEAN:
        return new BooleanValue(data);
      case PropertyType.LONG:
        return new LongValue(data);
      case PropertyType.DOUBLE:
        return new DoubleValue(data);
      case PropertyType.DATE:
        return new DateValue(data);
      case PropertyType.PATH:
        return new PathValue(data, locationFactory);
      case PropertyType.NAME:
        return new NameValue(data, locationFactory);
      case PropertyType.REFERENCE:
        return new ReferenceValue(data);
      case PropertyType.UNDEFINED:
        return null;
      case ExtendedPropertyType.PERMISSION:
        return new PermissionValue(data);
      default:
        throw new ValueFormatException("unknown type " + type);
      }
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

  public FileCleaner getFileCleaner() {
    return fileCleaner;
  }

  public int getMaxBufferSize() {
    return maxBufferSize;
  }

  public File getTempDirectory() {
    return tempDirectory;
  }

}
