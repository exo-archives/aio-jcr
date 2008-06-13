/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.io.Reader;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.jcr.NamespaceException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.exoplatform.services.document.DocumentReader;
import org.exoplatform.services.document.DocumentReaderService;
import org.exoplatform.services.document.HandlerNotFoundException;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.core.value.ExtendedValue;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.AbstractValueData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Creates a lucene <code>Document</code> object from a {@link javax.jcr.Node}.
 */
public class NodeIndexer {

    /**
     * The logger instance for this class.
     */
    private static final Log log = ExoLogger.getLogger(NodeIndexer.class);

    /**
     * The default boost for a lucene field: 1.0f.
     */
    protected static final float DEFAULT_BOOST = 1.0f;

    
    /**
     * The <code>NodeState</code> of the node to index
     */
    protected final NodeData node; 
    
    /**
     * The persistent item state provider
     */
    protected final ItemDataConsumer stateProvider;

    /**
     * Namespace mappings to use for indexing. This is the internal
     * namespace mapping.
     */
    protected final NamespaceMappings mappings;

    /**
     * Name and Path resolver.
     */
    protected final LocationFactory resolver; 

    /**
     * Content extractor.
     */
    protected final DocumentReaderService extractor;

    /**
     * The indexing configuration or <code>null</code> if none is available.
     */
    protected IndexingConfiguration indexingConfig;

    /**
     * If set to <code>true</code> the fulltext field is stored and and a term
     * vector is created with offset information.
     */
    protected boolean supportHighlighting = false;

    /**
     * Indicates index format for this node indexer.
     */
    protected IndexFormatVersion indexFormatVersion = IndexFormatVersion.V1;

    private ValueFactoryImpl vFactory;
   
    /**
     * Creates a new node indexer.
     *
     * @param node          the node state to index.
     * @param stateProvider the persistent item state manager to retrieve properties. //StateManager
     * @param mappings      internal namespace mappings.
     * @param extractor     content extractor
     */
    public NodeIndexer(NodeData node,
                       ItemDataConsumer stateProvider,
                       NamespaceMappings mappings,
                       DocumentReaderService extractor) {
        this.node = node;
        this.stateProvider = stateProvider;
        this.mappings = mappings;
        this.resolver = new LocationFactory(mappings);
        this.extractor = extractor;
        this.vFactory  = new ValueFactoryImpl(this.resolver);
    }

    /**
     * Returns the <code>NodeId</code> of the indexed node.
     * @return the <code>NodeId</code> of the indexed node.
     */
    public String getNodeId() {
        return node.getIdentifier();
    }

    /**
     * If set to <code>true</code> additional information is stored in the index
     * to support highlighting using the exo:excerpt pseudo property.
     *
     * @param b <code>true</code> to enable highlighting support.
     */
    public void setSupportHighlighting(boolean b) {
        supportHighlighting = b;
    }

    /**
     * Sets the index format version
     *
     * @param indexFormatVersion the index format version
     */
    public void setIndexFormatVersion(IndexFormatVersion indexFormatVersion) {
        this.indexFormatVersion = indexFormatVersion;
    }
    
    /**
     * Sets the indexing configuration for this node indexer.
     *
     * @param config the indexing configuration.
     */
    public void setIndexingConfiguration(IndexingConfiguration config) {
        this.indexingConfig = config;
    }

    /**
     * Creates a lucene Document.
     *
     * @return the lucene Document with the index layout.
     * @throws RepositoryException if an error occurs while reading property
     *                             values from the <code>ItemStateProvider</code>.
     */
    protected Document createDoc() throws RepositoryException {
        Document doc = new Document();

        doc.setBoost(getNodeBoost());

        // special fields UUID
        doc.add(new Field(FieldNames.UUID, node.getIdentifier(), Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
        try {
            // parent UUID
            if (node.getParentIdentifier()== null) {
                // root node
                doc.add(new Field(FieldNames.PARENT, "", Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
                doc.add(new Field(FieldNames.LABEL, "", Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
            } else {
                doc.add(new Field(FieldNames.PARENT, node.getParentIdentifier(), Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
              
                String name = resolver.createJCRName(node.getQPath().getName()).getAsString();
                doc.add(new Field(FieldNames.LABEL, name, Field.Store.YES, Field.Index.NO_NORMS, Field.TermVector.NO));
            }
        } catch (NamespaceException e) {
            // will never happen, because this.mappings will dynamically add
            // unknown uri<->prefix mappings
        }

        List<PropertyData> props = this.stateProvider.getChildPropertiesData(node); 
          
        for (Iterator<PropertyData> it = props.iterator(); it.hasNext();) {
          PropertyData propState = (PropertyData)it.next();
                // add each property to the _PROPERTIES_SET for searching beginning with V2
                if (indexFormatVersion.getVersion()
                        >= IndexFormatVersion.V2.getVersion()) {
                    addPropertyName(doc, propState.getQPath().getName());
                }
                
                List<ValueData> values = propState.getValues();
                for (int i = 0; i < values.size(); i++) {
                    
                    addValue(doc, values.get(i), propState.getQPath().getName(),propState.getType());
                }
                if (values.size() > 1) {
                    // real multi-valued
                    addMVPName(doc, propState.getQPath().getName());
                }
        }
        return doc;
    }

    /**
     * Adds a {@link FieldNames#MVP} field to <code>doc</code> with the resolved
     * <code>name</code> using the internal search index namespace mapping.
     *
     * @param doc  the lucene document.
     * @param name the name of the multi-value property.
     */
    private void addMVPName(Document doc, InternalQName name) throws RepositoryException{
        String propName = resolver.createJCRName(name).getAsString();
        doc.add(new Field(FieldNames.MVP, propName, Field.Store.NO, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
    }

    /**
     * Adds a value to the lucene Document.
     *
     * @param doc   the document.
     * @param value the internal jackrabbit value.
     * @param name  the name of the property.
     * @param  
     * @throws RepositoryException 
     * @throws IllegalStateException 
     * @throws ValueFormatException 
     */
    private void addValue(Document doc, ValueData value, InternalQName name, int propertyType) throws ValueFormatException, IllegalStateException, RepositoryException {
    String fieldName = resolver.createJCRName(name).getAsString();
    ExtendedValue val = null;
    if (PropertyType.BINARY != propertyType)
      val = (ExtendedValue) vFactory.loadValue(((AbstractValueData) value).createTransientCopy(),
                                               propertyType);
        switch (propertyType) { 
            case PropertyType.BINARY:
                if (isIndexed(name)) {
                    addBinaryValue(doc, fieldName, value);
                }
                break;
            case PropertyType.BOOLEAN:
                if (isIndexed(name)) {
                    
                    addBooleanValue(doc, fieldName, Boolean.valueOf(val.getBoolean()));
                }
                break;
            case PropertyType.DATE:
                if (isIndexed(name)) {
                    addCalendarValue(doc, fieldName, val.getDate());
                }
                break;
            case PropertyType.DOUBLE:
                if (isIndexed(name)) {
                    addDoubleValue(doc, fieldName, new Double(val.getDouble()));
                }
                break;
            case PropertyType.LONG:
                if (isIndexed(name)) {
                    addLongValue(doc, fieldName, new Long(val.getLong()));
                }
                break;
            case PropertyType.REFERENCE:
                if (isIndexed(name)) {
                    addReferenceValue(doc, fieldName, val.getString());
                }
                break;
            case PropertyType.PATH:
                if (isIndexed(name)) {
                    addPathValue(doc, fieldName, val.getString());
                }
                break;
            case PropertyType.STRING:
                if (isIndexed(name)) {
                    // never fulltext index jcr:uuid String
                    if (name.equals(Constants.JCR_UUID)) {
                        addStringValue(doc, fieldName, val.getString(),
                                false, false, DEFAULT_BOOST);
                    } else {
                        addStringValue(doc, fieldName, val.getString(),
                                true, isIncludedInNodeIndex(name),
                                getPropertyBoost(name));
                    }
                }
                break;
            case PropertyType.NAME:
                // jcr:primaryType and jcr:mixinTypes are required for correct
                // node type resolution in queries
                if (isIndexed(name) ||
                        name.equals(Constants.JCR_PRIMARYTYPE) ||
                        name.equals(Constants.JCR_MIXINTYPES)) {
                    addNameValue(doc, fieldName, val.getString()); 
                }
                break;
            case ExtendedPropertyType.PERMISSION:
                  break;
            default:
                throw new IllegalArgumentException("illegal internal value type:"+ExtendedPropertyType.nameFromValue(propertyType));
        }
    }

    /**
     * Adds the property name to the lucene _:PROPERTIES_SET field.
     *
     * @param doc  the document.
     * @param name the name of the property.
     */
    private void addPropertyName(Document doc, InternalQName name) throws RepositoryException {
        String fieldName = resolver.createJCRName(name).getAsString();
        doc.add(new Field(FieldNames.PROPERTIES_SET, fieldName, Field.Store.YES, Field.Index.NO_NORMS));
    }
    
    /**
     * Adds the binary value to the document as the named field.
     * <p/>
     * This implementation checks if this {@link #node} is of type nt:resource
     * and if that is the case, tries to extract text from the binary property
     * using the {@link #extractor}.
     *
     * @param doc           The document to which to add the field
     * @param fieldName     The name of the field to add
     * @param internalValue The value for the field to add to the document.
     * @throws RepositoryException 
     */
    protected void addBinaryValue(Document doc,
                                  String fieldName,
                                  ValueData internalValue) throws RepositoryException {
        
      String text = null;
      
      if (node.getQPath().getName().equals(Constants.JCR_CONTENT)) {
        PropertyData prop =(PropertyData)stateProvider.getItemData(node, new QPathEntry(Constants.JCR_MIMETYPE,0));
        try {
          List<ValueData> values = prop.getValues();
          ValueData mimeValue = values.get(0);
          String mime = new String(mimeValue.getAsByteArray());

          DocumentReader dreader = extractor.getDocumentReader(mime);

          InputStream is = null;
          
          //check the jcr:encoding property
          PropertyData encProp = (PropertyData)stateProvider.getItemData(node, new QPathEntry(new InternalQName(Constants.NS_JCR_URI, "encoding"),0));
          
          String encoding = null;
          if(encProp!=null){
            ValueData encValue = encProp.getValues().get(0);
            encoding = new String(encValue.getAsByteArray());
          }
          
          try {
            
            is = internalValue.getAsStream();
            
            if(encoding==null){
              text = dreader.getContentAsText(is);
            }else{
              text = dreader.getContentAsText(is,encoding);
            }
          } finally {
            try {
              is.close();
            } catch (Throwable e) {}  
          }
        } catch(HandlerNotFoundException e) {
          // no handler - no index
          log.warn("This content is not readable " + e);
        } catch(IOException e) {
          // no data - no index
          log.error("Binary value indexer IO error " + e, e);
        } catch(Exception e) {
          log.error("Binary value indexer error " + e, e);
        }
        
      }
      
      if (text != null) {
       doc.add(createFulltextField(text));
      }else{
      //  log.warn("Properties binary value is not extracted");
      }
    }

    /**
     * Adds the string representation of the boolean value to the document as
     * the named field.
     *
     * @param doc           The document to which to add the field
     * @param fieldName     The name of the field to add
     * @param internalValue The value for the field to add to the document.
     */
    protected void addBooleanValue(Document doc, String fieldName, Object internalValue) {
        doc.add(createFieldWithoutNorms(fieldName, internalValue.toString(), false));
    }

    /**
     * Creates a field of name <code>fieldName</code> with the value of <code>
     * internalValue</code>. The created field is indexed without norms.
     * 
     * @param fieldName     The name of the field to add
     * @param internalValue The value for the field to add to the document.
     * @param store         <code>true</code> if the value should be stored, 
     *                      <code>false</code> otherwise
     */
    protected Field createFieldWithoutNorms(String fieldName,
            String internalValue, boolean store) {
        Field field = new Field(FieldNames.PROPERTIES,
                FieldNames.createNamedValue(fieldName, internalValue),
                true ? Field.Store.YES : Field.Store.NO, Field.Index.NO_NORMS,
                Field.TermVector.NO);
        return field;
    }

    /**
     * Adds the calendar value to the document as the named field. The calendar
     * value is converted to an indexable string value using the
     * {@link DateField} class.
     * 
     * @param doc
     *            The document to which to add the field
     * @param fieldName
     *            The name of the field to add
     * @param internalValue
     *            The value for the field to add to the document.
     */
    protected void addCalendarValue(Document doc, String fieldName, Object internalValue) {
        Calendar value = (Calendar) internalValue;
        long millis = value.getTimeInMillis();
        doc.add(createFieldWithoutNorms(fieldName, DateField.timeToString(millis), false));
    }

    /**
     * Adds the double value to the document as the named field. The double
     * value is converted to an indexable string value using the
     * {@link DoubleField} class.
     *
     * @param doc           The document to which to add the field
     * @param fieldName     The name of the field to add
     * @param internalValue The value for the field to add to the document.
     */
    protected void addDoubleValue(Document doc, String fieldName, Object internalValue) {
        double doubleVal = ((Double) internalValue).doubleValue();
        doc.add(createFieldWithoutNorms(fieldName, DoubleField.doubleToString(doubleVal), false));
    }

    /**
     * Adds the long value to the document as the named field. The long
     * value is converted to an indexable string value using the {@link LongField}
     * class.
     *
     * @param doc           The document to which to add the field
     * @param fieldName     The name of the field to add
     * @param internalValue The value for the field to add to the document.
     */
    protected void addLongValue(Document doc, String fieldName, Object internalValue) {
        long longVal = ((Long) internalValue).longValue();
        doc.add(createFieldWithoutNorms(fieldName, LongField.longToString(longVal), false));
    }

    /**
     * Adds the reference value to the document as the named field. The value's
     * string representation is added as the reference data. Additionally the
     * reference data is stored in the index.
     *
     * @param doc           The document to which to add the field
     * @param fieldName     The name of the field to add
     * @param internalValue The value for the field to add to the document.
     */
    protected void addReferenceValue(Document doc, String fieldName, Object internalValue) {
        doc.add(createFieldWithoutNorms(fieldName, internalValue.toString(), true));
    }

    /**
     * Adds the path value to the document as the named field. The path
     * value is converted to an indexable string value using the name space
     * mappings with which this class has been created.
     *
     * @param doc           The document to which to add the field
     * @param fieldName     The name of the field to add
     * @param internalValue The value for the field to add to the document.
     */
    protected void addPathValue(Document doc, String fieldName, Object internalValue) {
        doc.add(createFieldWithoutNorms(fieldName, internalValue.toString(), false)); 
    }

    /**
     * Adds the string value to the document both as the named field and
     * optionally for full text indexing if <code>tokenized</code> is
     * <code>true</code>.
     *
     * @param doc           The document to which to add the field
     * @param fieldName     The name of the field to add
     * @param internalValue The value for the field to add to the document.
     * @param tokenized     If <code>true</code> the string is also tokenized
     *                      and fulltext indexed.
     */
    protected void addStringValue(Document doc, String fieldName,
                                  Object internalValue, boolean tokenized) {
        addStringValue(doc, fieldName, internalValue, tokenized, true, DEFAULT_BOOST);
    }

    /**
     * Adds the string value to the document both as the named field and
     * optionally for full text indexing if <code>tokenized</code> is
     * <code>true</code>.
     *
     * @param doc                The document to which to add the field
     * @param fieldName          The name of the field to add
     * @param internalValue      The value for the field to add to the
     *                           document.
     * @param tokenized          If <code>true</code> the string is also
     *                           tokenized and fulltext indexed.
     * @param includeInNodeIndex If <code>true</code> the string is also
     *                           tokenized and added to the node scope fulltext
     *                           index.
     * @param boost              the boost value for this string field.
     */
    protected void addStringValue(Document doc, String fieldName,
                                  Object internalValue, boolean tokenized,
                                  boolean includeInNodeIndex, float boost) {
        // simple String
        String stringValue = (String) internalValue;
        doc.add(createFieldWithoutNorms(fieldName, stringValue, false));
        if (tokenized) {
            if (stringValue.length() == 0) {
                return;
            }
            // create fulltext index on property
            Field f = new Field(FieldNames.createFullTextFieldName(fieldName), stringValue,
                    Field.Store.NO,
                    Field.Index.TOKENIZED,
                    Field.TermVector.NO);
            f.setBoost(boost);
            doc.add(f);

            if (includeInNodeIndex) {
                // also create fulltext index of this value
                doc.add(createFulltextField(stringValue));
            }
        }
    }

    /**
     * Adds the name value to the document as the named field. The name
     * value is converted to an indexable string treating the internal value
     * as a qualified name and mapping the name space using the name space
     * mappings with which this class has been created.
     *
     * @param doc           The document to which to add the field
     * @param fieldName     The name of the field to add
     * @param internalValue The value for the field to add to the document.
     */
    protected void addNameValue(Document doc, String fieldName, Object internalValue) {
       doc.add(createFieldWithoutNorms(fieldName, internalValue.toString(), false));
    }

    /**
     * Creates a fulltext field for the string <code>value</code>.
     *
     * @param value the string value.
     * @return a lucene field.
     */
    protected Field createFulltextField(String value) {
        if (supportHighlighting) {
            // store field compressed if greater than 16kR
            Field.Store stored;
            if (value.length() > 0x4000) {
                stored = Field.Store.COMPRESS;
            } else {
                stored = Field.Store.YES;
            }
            
            return new Field(FieldNames.FULLTEXT, value, stored,
                    Field.Index.TOKENIZED, Field.TermVector.WITH_OFFSETS);
        } else {
        
            return new Field(FieldNames.FULLTEXT, value,
                    Field.Store.NO, Field.Index.TOKENIZED); 
        }
    }

    /**
     * Creates a fulltext field for the reader <code>value</code>.
     *
     * @param value the reader value.
     * @return a lucene field.
     */
    protected Field createFulltextField(Reader value) {
        if (supportHighlighting) {
            // need to create a string value
            StringBuffer textExtract = new StringBuffer();
            char[] buffer = new char[1024];
            int len;
            try {
                while ((len = value.read(buffer)) > -1) {
                    textExtract.append(buffer, 0, len);
                }
            } catch (IOException e) {
                log.warn("Exception reading value for fulltext field: " +
                        e.getMessage());
                log.debug("Dump:", e);
            } finally {
                try {
                    value.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            return createFulltextField(textExtract.toString());
        } else {
            return new Field(FieldNames.FULLTEXT, value);
        }
    }

    /**
     * Returns <code>true</code> if the property with the given name should be
     * indexed.
     *
     * @param propertyName name of a property.
     * @return <code>true</code> if the property should be fulltext indexed;
     *         <code>false</code> otherwise.
     */
    protected boolean isIndexed(InternalQName propertyName) {
        if (indexingConfig == null) {
            return true;
        } else {
            return indexingConfig.isIndexed(node, propertyName);
        }
    }

    /**
     * Returns <code>true</code> if the property with the given name should also
     * be added to the node scope index.
     *
     * @param propertyName the name of a property.
     * @return <code>true</code> if it should be added to the node scope index;
     *         <code>false</code> otherwise.
     */
    protected boolean isIncludedInNodeIndex(InternalQName propertyName) {
        if (indexingConfig == null) {
            return true;
        } else {
            return indexingConfig.isIncludedInNodeScopeIndex(node, propertyName);
        }
    }

    /**
     * Returns the boost value for the given property name.
     *
     * @param propertyName the name of a property.
     * @return the boost value for the given property name.
     */
    protected float getPropertyBoost(InternalQName propertyName) {
        if (indexingConfig == null) {
            return DEFAULT_BOOST;
        } else {
            return indexingConfig.getPropertyBoost(node, propertyName);
        }
    }

    /**
     * @return the boost value for this {@link #node} state.
     */
    protected float getNodeBoost() {
        if (indexingConfig == null) {
            return DEFAULT_BOOST;
        } else {
            return indexingConfig.getNodeBoost(node);
        }
    }
}
