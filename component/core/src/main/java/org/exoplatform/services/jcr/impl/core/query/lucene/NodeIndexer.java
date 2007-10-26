/*
 * Copyright 2004-2005 The Apache Software Foundation or its licensors,
 *                     as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exoplatform.services.jcr.impl.core.query.lucene;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.exoplatform.services.document.DocumentReader;
import org.exoplatform.services.document.DocumentReaderService;
import org.exoplatform.services.document.HandlerNotFoundException;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.jcr.impl.util.JCRDateFormat;
import org.exoplatform.services.log.ExoLogger;

/**
 * Creates a lucene <code>Document</code> object from a {@link javax.jcr.Node}.
 */
public class NodeIndexer {

  /**
   * The logger instance for this class.
   */
  private static Log                       log                   = ExoLogger.getLogger("jcr.NodeIndexer");

  /**
   * The <code>NodeState</code> of the node to index
   */
  protected final NodeData                 node;

  protected final WorkspacePersistentDataManager dataManager;

  /**
   * The variable for the stripping text from files with different formats.
   */
  private DocumentReaderService            documentReaderService = null;

  protected final LocationFactory                sysLocationFactory;

  /**
   * Creates a new node indexer.
   * 
   * @param node the node state to index.
   * @param sysLocationFactory sysLocationFactory.
   */
  protected NodeIndexer(NodeData node, LocationFactory sysLocationFactory, DocumentReaderService ds,
      WorkspacePersistentDataManager dataManager) {
    this.node = node;
    this.sysLocationFactory = sysLocationFactory;
    this.documentReaderService = ds;
    this.dataManager = dataManager;
  }

  /**
   * 
   * DON'T USE THE METHOD
   * 
   * Creates a lucene Document from a node.
   * 
   * @param node the node state to index.
   * @param sysLocationFactory sysLocationFactory.
   * @param ds the document reader service for the stripping text from files
   *          with different formats.
   * @return the lucene Document.
   * @throws RepositoryException if an error occurs while reading property
   *           values from the <code>ItemStateProvider</code>.
   */
  @Deprecated
  public static Document createDocument(final NodeData node, final LocationFactory sysLocationFactory, final DocumentReaderService ds,
      final WorkspacePersistentDataManager dataManager) throws RepositoryException {

    if (node != null) {
      //NodeIndexer indexer = new NodeIndexer(node, sysLocationFactory, ds, dataManager);
      //Document doc = indexer.createDoc();
      return new NodeIndexer(node, sysLocationFactory, ds, dataManager).createDoc();
    } else
      return null;
  }

  /**
   * Creates a lucene Document.
   * 
   * @return the lucene Document with the index layout.
   * @throws RepositoryException if an error occurs while reading property
   *           values from the <code>ItemStateProvider</code>.
   */
  protected Document createDoc() throws RepositoryException {
    Document doc = new Document();

    doc.add(new Field(FieldNames.UUID, node.getIdentifier(), true, true, false));

    String parentIdentifier = node.getParentIdentifier();

    if (parentIdentifier != null) {
      doc.add(new Field(FieldNames.PARENT, parentIdentifier, true, true, false));
      String label = sysLocationFactory.createJCRName(node.getQPath().getName()).getAsString();
      doc.add(new Field(FieldNames.LABEL, label, false, true, false));
    } else { // root
      doc.add(new Field(FieldNames.PARENT, "", true, true, false));
      doc.add(new Field(FieldNames.LABEL, "", false, true, false));
    }

    for (PropertyData prop: dataManager.listChildPropertiesData(node)) {
      // test the case and switch to 
      addValues(doc, prop);
      
//      String fieldName = sysLocationFactory.createJCRName(prop.getQPath().getName()).getAsString();
//      List<ValueData> values = prop.getValues();
//
//      if (values == null)
//        log.warn("null value found at property " + prop.getQPath().getAsString());
//
//      for (int i = 0; i < values.size(); i++) {
//        if (log.isDebugEnabled())
//          try {
//            log.debug("Inside NodeIndexer property value " + fieldName + " [" + i + "], type: "
//                + PropertyType.nameFromValue(prop.getType()));
//          } catch (IllegalArgumentException e) {
//            if (e.getMessage().indexOf("unknown type") >= 0)
//              log.debug("Inside NodeIndexer property value " + fieldName + " [" + i + "], type: " + prop.getType());
//            else
//              log.warn("Error of debug log, inside NodeIndexer property " + fieldName + ", [" + i + "]");
//          }
//        addValue(doc, values.get(i), fieldName, prop.getType());
//      }
//
//      if (values.size() > 1) {
//        // real multi-valued
//        doc.add(new Field(FieldNames.MVP, fieldName, false, true, false));
//      }
    }
    return doc;
  }

  /**
   * Adds a value to the lucene Document.
   * 
   * @param doc the document.
   * @param fieldName fieldName.
   * @param propType propType.
   */
  @Deprecated
  private void addValue(Document doc, ValueData internalValue, String fieldName, int propType)
      throws RepositoryException {
    switch (propType) {
    case PropertyType.BINARY:
      addBinaryValue(doc, fieldName, internalValue);
      break;
    case PropertyType.BOOLEAN:
      addBooleanValue(doc, fieldName, internalValue);
      break;
    case PropertyType.DATE:
      addCalendarValue(doc, fieldName, internalValue);
      break;
    case PropertyType.DOUBLE:
      addDoubleValue(doc, fieldName, internalValue);
      break;
    case PropertyType.LONG:
      addLongValue(doc, fieldName, internalValue);
      break;
    case PropertyType.REFERENCE:
      addReferenceValue(doc, fieldName, internalValue);
      break;
    case PropertyType.PATH:
      addPathValue(doc, fieldName, internalValue);
      break;
    case PropertyType.STRING:
      addStringValue(doc, fieldName, internalValue);
      break;
    case PropertyType.NAME:
      addNameValue(doc, fieldName, internalValue);
      break;
    case ExtendedPropertyType.PERMISSION:
      addPermissionValue(doc, fieldName, internalValue);
      break;
    default:
      throw new IllegalArgumentException("illegal internal value type " + propType);
    }
  }
  
  /**
   * For concurrency usage case all internal vars were made a final. Need to be rechecked. 
   * */
  private void addValues(final Document doc, final PropertyData prop)
      throws RepositoryException {
    
    int propType = prop.getType();
    String fieldName = sysLocationFactory.createJCRName(prop.getQPath().getName()).getAsString();
    
    if (propType == PropertyType.BINARY) {
      List<ValueData> data = null;
      if (node.getQPath().getName().equals(Constants.JCR_CONTENT)) {
        // seems nt:file found, try for nt:resource props
        PropertyData pmime = (PropertyData) dataManager.getItemData(node, new QPathEntry(Constants.JCR_MIMETYPE, 0));
        if (pmime != null) {
          // index if have jcr:mimeType sibling for this binary property only
          try {
            DocumentReader dreader = documentReaderService.getDocumentReader(new String(pmime.getValues().get(0).getAsByteArray()));
            
            // ok, have a reader
            // if the prop obtainer from cache it will contains a values, otherwise read prop with values from DM
            data = prop.getValues().size() > 0 ? prop.getValues() :
                ((PropertyData) dataManager.getItemData(node, new QPathEntry(Constants.JCR_DATA, 0))).getValues();
            if (data == null)
              log.warn("null value found at property " + prop.getQPath().getAsString());
            
            for (ValueData pvd: data) {
              InputStream is = null; 
              try {
                doc.add(new Field(FieldNames.FULLTEXT, dreader.getContentAsText(is = pvd.getAsStream()), false, true, true));
              } finally {
                try {
                  is.close();
                } catch (Throwable e) { }
              }  
            }
          } catch(HandlerNotFoundException e) {
            // no handler - no index
            if (log.isDebugEnabled())
              log.error("Binary value indexer handler error " + e, e);
          } catch(IOException e) {
            // no data - no index
            if (log.isDebugEnabled())
              log.error("Binary value indexer IO error " + e, e);
          } catch(Exception e) {
            //if (log.isDebugEnabled())
            log.error("Binary value indexer error " + e, e);
          }
        }
      }
      
      if (data == null)
        doc.add(new Field(FieldNames.FULLTEXT, "", false, true, true));
      else if (data.size() > 1)
        // real multi-valued
        doc.add(new Field(FieldNames.MVP, fieldName, false, true, false));
    } else {
      try {
        // if the prop obtainer from cache it will contains a values, otherwise read prop with values from DM
        QPathEntry[] path = prop.getQPath().getEntries();
        List<ValueData> data = prop.getValues().size() > 0 ? prop.getValues() :
          ((PropertyData) dataManager.getItemData(node, path[path.length - 1])).getValues();
        
        if (data == null)
          log.warn("null value found at property " + prop.getQPath().getAsString());
        
        for (ValueData pvd: data) {
          switch (propType) {
          case PropertyType.BOOLEAN:
            addBooleanValue(doc, fieldName, pvd);
            break;
          case PropertyType.DATE:
            addCalendarValue(doc, fieldName, pvd);
            break;
          case PropertyType.DOUBLE:
            addDoubleValue(doc, fieldName, pvd);
            break;
          case PropertyType.LONG:
            addLongValue(doc, fieldName, pvd);
            break;
          case PropertyType.REFERENCE:
            addReferenceValue(doc, fieldName, pvd);
            break;
          case PropertyType.PATH:
            addPathValue(doc, fieldName, pvd);
            break;
          case PropertyType.STRING:
            addStringValue(doc, fieldName, pvd);
            break;
          case PropertyType.NAME:
            addNameValue(doc, fieldName, pvd);
            break;
          case ExtendedPropertyType.PERMISSION:
            addPermissionValue(doc, fieldName, pvd);
            break;
          default:
            throw new IllegalArgumentException("illegal internal value type " + propType);
          }        
        }
        if (data.size() > 1)
          // real multi-valued
          doc.add(new Field(FieldNames.MVP, fieldName, false, true, false));
      } catch(RepositoryException e) {
        throw new RepositoryException("Index of property value error. " + prop.getQPath().getAsString() + ". " + e, e);
      }
    }
  }

  /**
   * Adds the binary value to the document as the named field. <p/> This
   * implementation checks if this {@link #node} is of type nt:resource and if
   * that is the case, tries to extract text from the data atom using
   * {@link TextFilterService}add a {@link FieldNames#FULLTEXT} field .
   * 
   * @param doc The document to which to add the field
   * @param fieldName The name of the field to add
   * @param internalValue The value for the field to add to the document.
   */
  @Deprecated
  protected void addBinaryValue(Document doc, String fieldName, ValueData internalValue) throws RepositoryException {

    String text = "";
    if (node.getQPath().getName().equals(Constants.JCR_CONTENT)) {
      List<PropertyData> cprops = dataManager.getChildPropertiesData(node);
      for (int i = 0; i < cprops.size(); i++) {
        PropertyData prop = (PropertyData) cprops.get(i);
        if (prop.getQPath().getName().equals(Constants.JCR_MIMETYPE)) {
          try {
            List<ValueData> values = prop.getValues();
            ValueData mimeValue = values.get(0);
            String mime = new String(mimeValue.getAsByteArray());

            DocumentReader dreader = documentReaderService.getDocumentReader(mime);

            InputStream is = null; 
            try {
              text = dreader.getContentAsText(is = internalValue.getAsStream());
            } finally {
              try {
                is.close();
              } catch (Throwable e) {}  
            }
          } catch (HandlerNotFoundException e) {
            // ok,
          } catch (Exception e) {
            log.error("Error of add binary value " + e, e);
          }
        }
      }
    }
    if (text != null) {
      doc.add(new Field(FieldNames.FULLTEXT, text, false, true, true));
    }
  }

  /**
   * Adds the string representation of the boolean value to the document as the
   * named field.
   * 
   * @param doc The document to which to add the field
   * @param fieldName The name of the field to add
   * @param internalValue The value for the field to add to the document.
   */
  protected void addBooleanValue(Document doc, String fieldName, ValueData internalValue) throws RepositoryException {
    try {
      String strValue = new String(internalValue.getAsByteArray());
      doc.add(new Field(FieldNames.PROPERTIES, FieldNames.createNamedValue(fieldName, strValue), false, true, false));
    } catch (IOException e) {
      log.error("Error of add boolean value: " + e.getMessage(), e);
    }
  }

  /**
   * Adds the calendar value to the document as the named field. The calendar
   * value is converted to an indexable string value using the {@link DateField}
   * class.
   * 
   * @param doc The document to which to add the field
   * @param fieldName The name of the field to add
   * @param internalValue The value for the field to add to the document.
   */
  protected void addCalendarValue(Document doc, String fieldName, ValueData internalValue) throws RepositoryException {

    try {
      Calendar cal = new JCRDateFormat().deserialize(new String(internalValue.getAsByteArray(),
          Constants.DEFAULT_ENCODING));
      String strValue = DateField.dateToString(cal.getTime());

      doc.add(new Field(FieldNames.PROPERTIES, FieldNames.createNamedValue(fieldName, strValue), false, true, false));
    } catch (IOException e) {
      log.error("Error of add calendar value: " + e.getMessage(), e);
    }
  }

  /**
   * Adds the double value to the document as the named field. The double value
   * is converted to an indexable string value using the {@link DoubleField}
   * class.
   * 
   * @param doc The document to which to add the field
   * @param fieldName The name of the field to add
   * @param internalValue The value for the field to add to the document.
   */
  protected void addDoubleValue(Document doc, String fieldName, ValueData internalValue) throws RepositoryException {

    try {
      String strValue = DoubleField.doubleToString(Double.parseDouble(new String(internalValue.getAsByteArray())));
      doc.add(new Field(FieldNames.PROPERTIES, FieldNames.createNamedValue(fieldName, strValue), false, true, false));
    } catch (IOException e) {
      log.error("Error of add double value: " + e.getMessage(), e);
    }
  }

  /**
   * Adds the long value to the document as the named field. The long value is
   * converted to an indexable string value using the {@link LongField} class.
   * 
   * @param doc The document to which to add the field
   * @param fieldName The name of the field to add
   * @param internalValue The value for the field to add to the document.
   */
  protected void addLongValue(Document doc, String fieldName, ValueData internalValue) throws RepositoryException {

    try {
      String strValue = LongField.longToString(Long.parseLong(new String(internalValue.getAsByteArray())));

      doc.add(new Field(FieldNames.PROPERTIES, FieldNames.createNamedValue(fieldName, strValue), false, true, false));
    } catch (IOException e) {
      log.error("Error of add permission value: " + e.getMessage(), e);
    }
  }

  /**
   * Adds the reference value to the document as the named field. The value's
   * string representation is added as the reference data. Additionally the
   * reference data is stored in the index.
   * 
   * @param doc The document to which to add the field
   * @param fieldName The name of the field to add
   * @param internalValue The value for the field to add to the document.
   */
  protected void addReferenceValue(Document doc, String fieldName, ValueData internalValue) throws RepositoryException {

    try {
      String strValue = new String(internalValue.getAsByteArray());

      doc.add(new Field(FieldNames.PROPERTIES, FieldNames.createNamedValue(fieldName, strValue), true, true, false));

    } catch (IOException e) {
      log.error("Error of add reference value: " + e.getMessage(), e);
    }
  }

  /**
   * Adds the path value to the document as the named field. The path value is
   * converted to an indexable string value using the name space mappings with
   * which this class has been created.
   * 
   * @param doc The document to which to add the field
   * @param fieldName The name of the field to add
   * @param internalValue The value for the field to add to the document.
   */
  protected void addPathValue(Document doc, String fieldName, ValueData internalValue) throws RepositoryException {

    try {
      String strQpath = new String(internalValue.getAsByteArray());

      String strValue = this.sysLocationFactory.createJCRPath(QPath.parse(strQpath)).getAsString(false);

      doc.add(new Field(FieldNames.PROPERTIES, FieldNames.createNamedValue(fieldName, strValue), false, true, false));
    } catch (IllegalPathException e) {
      throw new RepositoryException(e);
    } catch (IOException e) {
      log.error("Error of add path value: " + e.getMessage(), e);
    }
  }

  /**
   * Adds the string value to the document both as the named field and for full
   * text indexing.
   * 
   * @param doc The document to which to add the field
   * @param fieldName The name of the field to add
   * @param internalValue The value for the field to add to the document.
   */
  protected void addStringValue(Document doc, String fieldName, ValueData internalValue) throws RepositoryException {

    try {
      String stringValue = new String(internalValue.getAsByteArray());

      // simple String
      doc
          .add(new Field(FieldNames.PROPERTIES, FieldNames.createNamedValue(fieldName, stringValue), false, true, false));
      // also create fulltext index of this value
      doc.add(new Field(FieldNames.FULLTEXT, stringValue, false, true, true));
      // create fulltext index on property
      int idx = fieldName.indexOf(':');
      fieldName = fieldName.substring(0, idx + 1) + FieldNames.FULLTEXT_PREFIX + fieldName.substring(idx + 1);
      doc.add(new Field(fieldName, stringValue, false, true, true));
    } catch (IOException e) {
      throw new RepositoryException("Error of add string value: " + e.getMessage(), e);
    }
  }

  /**
   * Adds the name value to the document as the named field. The name value is
   * converted to an indexable string treating the internal value as a qualified
   * name and mapping the name space using the name space mappings with which
   * this class has been created.
   * 
   * @param doc The document to which to add the field
   * @param fieldName The name of the field to add
   * @param internalValue The value for the field to add to the document.
   */
  protected void addNameValue(Document doc, String fieldName, ValueData internalValue) throws RepositoryException {

    try {
      String strQname = new String(internalValue.getAsByteArray());

      String strValue = this.sysLocationFactory.createJCRName(InternalQName.parse(strQname)).getAsString();
      doc.add(new Field(FieldNames.PROPERTIES, FieldNames.createNamedValue(fieldName, strValue), false, true, false));
    } catch (IllegalNameException e) {
      throw new RepositoryException(e);
    } catch (IOException e) {
      log.error("Error of add name value: " + e.getMessage(), e);
    }
  }

  protected void addPermissionValue(Document doc, String fieldName, ValueData internalValue) throws RepositoryException {

    try {
      String strValue = new String(internalValue.getAsByteArray());

      doc.add(new Field(FieldNames.PROPERTIES, FieldNames.createNamedValue(fieldName, strValue), false, true, false));
    } catch (IOException e) {
      log.error("Error of add permission value: " + e.getMessage(), e);
    }
  }

}