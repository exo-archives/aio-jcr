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
package org.exoplatform.services.jcr.impl.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.value.StringValue;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.AbstractValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * Created by The eXo Platform SAS 15.05.2006
 * 
 * PropertyData bulk reader.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: PropertyDataReader.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class PropertyDataReaderOld extends ItemDataReaderOld {

  private HashMap<InternalQName, PropertyInfo> propeties = new HashMap<InternalQName, PropertyInfo>();

  private class PropertyInfo {
    private InternalQName   propertyName = null;

    private boolean         multiValued  = false;

    private List<ValueData> mValueData   = null;

    private List<Value>     mValue       = null;

    private ValueData       valueData    = null;

    private Value           value        = null;

    private int             type         = PropertyType.STRING;

    PropertyInfo(InternalQName propertyName, int type) {
      this.propertyName = propertyName;
      this.type = type;
    }

    public InternalQName getPropertyName() {
      return propertyName;
    }

    public boolean isMultiValued() {
      return multiValued;
    }

    public void setMultiValued(boolean multiValued) {
      this.multiValued = multiValued;
    }

    public List<Value> getValues() throws ValueFormatException,
                                  PathNotFoundException,
                                  RepositoryException {
      if (mValue == null) {
        List<ValueData> vds = getValueDatas();
        List<Value> vs = new ArrayList<Value>();
        for (ValueData vd : vds) {
          vs.add(makeValue(vd, getType()));
        }
        mValue = vs;
      }
      return mValue;
    }

    public List<ValueData> getValueDatas() throws ValueFormatException, PathNotFoundException {
      if (multiValued) {
        if (mValueData != null) {
          return mValueData;
        }
      } else if (valueData != null) {
        throw new ValueFormatException("Property " + parent.getQPath().getAsString()
            + propertyName.getAsString() + " is multi-valued");
      }
      throw new PathNotFoundException("Property " + parent.getQPath().getAsString()
          + propertyName.getAsString() + " not found (multi-valued)");
    }

    public void setValueDatas(List<ValueData> mValue) {
      this.mValueData = mValue;
      this.multiValued = true;
    }

    public Value getValue() throws ValueFormatException, PathNotFoundException, RepositoryException {
      if (value == null) {
        value = makeValue(getValueData(), getType());
      }
      return value;
    }

    public ValueData getValueData() throws ValueFormatException, PathNotFoundException {
      if (!multiValued) {
        if (valueData != null) {
          return valueData;
        }
      } else if (mValueData != null) {
        throw new ValueFormatException("Property " + parent.getQPath().getAsString()
            + propertyName.getAsString() + " is single-valued");
      }
      throw new PathNotFoundException("Property " + parent.getQPath().getAsString()
          + propertyName.getAsString() + " not found (single-valued)");
    }

    public void setValueData(ValueData value) {
      this.valueData = value;
      this.multiValued = false;
    }

    public int getType() {
      return type;
    }
  }

  public PropertyDataReaderOld(NodeData parent, DataManager dataManager, ValueFactoryImpl valueFactory) {
    super(parent, dataManager, valueFactory);
  }

  public PropertyDataReaderOld forProperty(InternalQName name, int type) {
    propeties.put(name, new PropertyInfo(name, type));
    return this;
  }

  public List<ValueData> getPropertyValueDatas(InternalQName name) throws ValueFormatException,
                                                                  PathNotFoundException {
    return propeties.get(name).getValueDatas();
  }

  public List<Value> getPropertyValues(InternalQName name) throws ValueFormatException,
                                                          PathNotFoundException,
                                                          RepositoryException {
    return propeties.get(name).getValues();
  }

  public Value getPropertyValue(InternalQName name) throws ValueFormatException,
                                                   PathNotFoundException,
                                                   RepositoryException {
    return propeties.get(name).getValue();
  }

  public ValueData getPropertyValueData(InternalQName name) throws ValueFormatException,
                                                           PathNotFoundException {
    return propeties.get(name).getValueData();
  }

  public void read() throws RepositoryException {
    List<PropertyData> ndProps = dataManager.getChildPropertiesData(parent);
    for (PropertyData prop : ndProps) {
      PropertyInfo propInfo = propeties.get(prop.getQPath().getName());
      if (propInfo != null) {
        List<ValueData> valueDataList = prop.getValues();
        if (prop.isMultiValued()) {
          propInfo.setValueDatas(valueDataList);
        } else {
          if (valueDataList.size() > 0)
            propInfo.setValueData(valueDataList.get(0));
        }
      }
    }
  }

  private Value makeValue(ValueData valueData, int type) throws RepositoryException {
    if (valueFactory != null) {
      TransientValueData tvd = ((AbstractValueData) valueData).createTransientCopy();
      return valueFactory.loadValue(tvd, type);
    }
    try {
      return new StringValue(new String(valueData.getAsByteArray(), Constants.DEFAULT_ENCODING));
    } catch (UnsupportedEncodingException e) {
      try {
        return new StringValue(new String(valueData.getAsByteArray()));
      } catch (IOException e1) {
        throw new RepositoryException("Can't make value from value data: " + e1.getMessage(), e1);
      }
    } catch (IOException e) {
      throw new RepositoryException("Can't make value from value data: " + e.getMessage(), e);
    }
  }

}
