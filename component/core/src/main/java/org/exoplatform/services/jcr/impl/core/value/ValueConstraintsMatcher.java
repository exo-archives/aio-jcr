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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.JCRPath;
import org.exoplatform.services.jcr.impl.core.JCRPathMatcher;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.JCRDateFormat;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 13.09.2006
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ValueConstraintsMatcher.java 13463 2007-03-16 09:17:29Z geaz $
 */
public class ValueConstraintsMatcher {

  protected static Log log = ExoLogger.getLogger("jcr.ValueConstraintsMatcher");
  
  protected final static String DEFAULT_THRESHOLD = "";
  
  public class ConstraintRange {
    
    private final String value;
    private final boolean exclusive;
    
    public ConstraintRange(String value) {
      this.value = value;
      this.exclusive = false;
    }
    
    public ConstraintRange(String value, boolean exclusive) {
      this.value = value;
      this.exclusive = exclusive;
    }

    protected boolean isExclusive() {
      return exclusive;
    }

    protected String getThreshold() {
      return value;
    }    
  }
  
  public class MinMaxConstraint {
    
    private final ConstraintRange minValue;
    private final ConstraintRange maxValue;
    private final ConstraintRange singleValue;
    
    public MinMaxConstraint(ConstraintRange minValue, ConstraintRange maxValue) {
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.singleValue = null;
    }

    protected ConstraintRange getMin() {
      return minValue;
    }

    protected ConstraintRange getMax() {
      return maxValue;
    }    
    
    public ConstraintRange getSingleValue() {
      return singleValue;
    }
  }
  
  private final String[] constraints; 
  
  private final SessionImpl session;
    
  public ValueConstraintsMatcher(String[] constraints, SessionImpl session) {
    this.constraints = constraints;
    this.session = session;
  }
  
  public ValueConstraintsMatcher(String constraint, SessionImpl session) {
    this.constraints = new String[] {constraint};
    this.session = session;
  }
  
  protected MinMaxConstraint parseAsMinMax(String constraint) throws ConstraintViolationException {
    
    // constraint as min,max range:
    // value constraints in the form of inclusive or exclusive ranges: 
    // i.e., "[min, max]", "(min, max)", "(min, max]" or "[min, max)". 
    // Where "[" and "]" indicate "inclusive", while "(" and ")" indicate "exclusive". 
    // A missing min or max value indicates no bound in that direction
    
    String[] parts = constraint.split(",");
      
    if (parts.length != 2)
      throw new ConstraintViolationException("Value constraint '" + constraint + "' is invalid accrding the JSR-170 spec.");
    
    boolean exclusive = false;
    
    if (parts[0].startsWith("(")) 
      exclusive = true;
    else if (parts[0].startsWith("["))
      exclusive = false;
    else
      throw new ConstraintViolationException("Value constraint '" + constraint + "' min exclusion rule is unefined accrding the JSR-170 spec.");
    
    ConstraintRange minValue = new ConstraintRange(
        parts[0].length() > 1 ? parts[0].substring(1) : DEFAULT_THRESHOLD,
        exclusive    
    );

    if (parts[1].endsWith(")")) 
      exclusive = true;
    else if (parts[1].endsWith("]"))
      exclusive = false;
    else
      throw new ConstraintViolationException("Value constraint '" + constraint + "' max exclusion rule is unefined accrding the JSR-170 spec.");
    
    ConstraintRange maxValue = new ConstraintRange(
        parts[1].length() > 1 ? parts[1].substring(0, parts[1].length() - 1) : DEFAULT_THRESHOLD,
        exclusive    
    );
    
    return new MinMaxConstraint(minValue, maxValue);
  }
  
  public boolean match(ValueData value, int type) throws ConstraintViolationException, IllegalStateException, RepositoryException {
    
    if (constraints == null || constraints.length <= 0)
      return true;
    
    boolean invalid = true;
    
    // do not use getString because of string consuming
    TransientValueData valueData = (TransientValueData)value;
    if (type == PropertyType.STRING) {
      try {
        String strVal = new String(valueData.getAsByteArray(), Constants.DEFAULT_ENCODING);
        
        for (int i=0; invalid && i < constraints.length; i++) {
          String constrString = constraints[i]; 
          if (strVal.matches(constrString)) {
            invalid = false;
          }
        }  
      } catch(UnsupportedEncodingException e) {
        throw new RuntimeException("FATAL ERROR Charset " + Constants.DEFAULT_ENCODING + " is not supported!");
      } catch(IOException e) {
        throw new RepositoryException("FATAL ERROR Value data stream reading error " + e.getMessage(), e);
      }
      
    } else if (type == PropertyType.NAME) {
      
      LocationFactory locator = session.getLocationFactory();
      NameValue nameVal;
      try {
        nameVal = new NameValue(valueData, locator);
      } catch (IOException e) {
        throw new RepositoryException(e);
      }
      for (int i=0; invalid && i < constraints.length; i++) {
        String constrString = constraints[i];
        InternalQName constrName = locator.parseJCRName(constrString).getInternalName();
        if (nameVal.getQName().equals(constrName)) {
          invalid = false;
        }
      }
      
    } else if (type == PropertyType.PATH) {
      
      LocationFactory locator = session.getLocationFactory();
      PathValue pathVal;
      try {
        pathVal = new PathValue(valueData, locator);
      } catch (IOException e) {
        throw new RepositoryException(e);
      } 
      for (int i=0; invalid && i < constraints.length; i++) {
        String constrString = constraints[i];
        
        JCRPathMatcher constrPath = parsePathMatcher(locator, constrString);
        if (constrPath.match(pathVal.getQPath())) {
          invalid = false;
        }
      }
      
    } else if (type == PropertyType.REFERENCE) {
      
      try {
        ReferenceValue refVal = new ReferenceValue(valueData);
        NodeImpl refNode = (NodeImpl) session.getNodeByUUID(refVal.getIdentifier().getString());
        for (int i=0; invalid && i < constraints.length; i++) {
          String constrString = constraints[i];
          InternalQName constrName = session.getLocationFactory().parseJCRName(constrString).getInternalName();
          if (refNode.isNodeType(constrName)) {
            invalid = false;
          }
        }
      } catch(ItemNotFoundException e) {
        if (log.isDebugEnabled())
          log.debug("Reference constraint node is not found: " + e.getMessage());
        // But if it's a versionHisroy ref property for add mix:versionable... 
        // we haven't a versionHisroy created until save method will be called on this session/item... 
        // it's transient state here.   
        invalid = false; // so, value is matched, we hope...
      } catch(RepositoryException e) {
        log.error("Reference constraint error: " + e.getMessage(), e);
        // [PN] Posible trouble is session.getNodeByUUID() call result, 
        // till bug can be found in version restore operation. 
        invalid = true; 
      } catch (IOException e) {
        log.error("Reference constraint error: " + e.getMessage(), e);
        invalid = true; 
      }
      
    } else if (type == PropertyType.BINARY) {
      
      long valueLength = valueData.getLength();
      for (int i=0; invalid && i < constraints.length; i++) {
        String constrString = constraints[i]; 
        
        boolean minInvalid = true;
        boolean maxInvalid = true;
        
        MinMaxConstraint constraint = parseAsMinMax(constrString);
        
        long min = constraint.getMin().getThreshold().length() > 0 ? 
            new Long(constraint.getMin().getThreshold()) : 
              Long.MIN_VALUE;
        if (constraint.getMin().isExclusive()) {
          if (valueLength > min)
            minInvalid = false;
        } else {
          if (valueLength >= min)
            minInvalid = false;
        }
        
        long max = constraint.getMax().getThreshold().length() > 0 ? 
            new Long(constraint.getMax().getThreshold()) : 
              Long.MAX_VALUE;
        if (constraint.getMax().isExclusive()) {
          if (valueLength < max)
            maxInvalid = false;
        } else {
          if (valueLength <= max)
            maxInvalid = false;
        }
        invalid = maxInvalid | minInvalid;
      }
      
    } else if (type == PropertyType.DATE) {
      
      Calendar valueCalendar;
      try {
        valueCalendar = new DateValue(valueData).getDate();
      } catch (IOException e) {
        throw new RepositoryException(e);
      }
      for (int i=0; invalid && i < constraints.length; i++) {
        String constrString = constraints[i]; 
        
        boolean minInvalid = true;
        boolean maxInvalid = true;
        
        MinMaxConstraint constraint = parseAsMinMax(constrString);
        
        try {
          if (constraint.getMin().getThreshold().length() > 0) {
            Calendar min = JCRDateFormat.parse(constraint.getMin().getThreshold());
            if (constraint.getMin().isExclusive()) {
              if (valueCalendar.compareTo(min) > 0)
                minInvalid = false;
            } else {
              if (valueCalendar.compareTo(min) >= 0)
                minInvalid = false;
            }
          } else 
            minInvalid = false;
        } catch(ValueFormatException e) {
          minInvalid = false;
        }
        
        try {
          if (constraint.getMax().getThreshold().length() > 0) {
            Calendar max = JCRDateFormat.parse(constraint.getMax().getThreshold());
            if (constraint.getMax().isExclusive()) {
              if (valueCalendar.compareTo(max) < 0)
                maxInvalid = false;
            } else {
              if (valueCalendar.compareTo(max) <= 0)
                maxInvalid = false;
            }
          } else
            maxInvalid = false;
        } catch(ValueFormatException e) {
          maxInvalid = false;
        }
        
        invalid = maxInvalid | minInvalid;
      }
      
    } else if (type == PropertyType.LONG || type == PropertyType.DOUBLE) {
      
      // will be compared as double in any case
      Number valueNumber;
      try {
        valueNumber = new DoubleValue(valueData).getDouble();
      } catch (IOException e) {
        throw new RepositoryException(e);
      }
      for (int i=0; invalid && i < constraints.length; i++) {
        String constrString = constraints[i]; 
        
        boolean minInvalid = true;
        boolean maxInvalid = true;
        
        MinMaxConstraint constraint = parseAsMinMax(constrString);
        
        Number min = constraint.getMin().getThreshold().length() > 0 ? 
            new Double(constraint.getMin().getThreshold()) : 
              Double.MIN_VALUE;
        if (constraint.getMin().isExclusive()) {
          if (valueNumber.doubleValue() > min.doubleValue())
            minInvalid = false;
        } else {
          if (valueNumber.doubleValue() >= min.doubleValue())
            minInvalid = false;
        }
        
        Number max = constraint.getMax().getThreshold().length() > 0 ? 
            new Double(constraint.getMax().getThreshold()) : 
              Double.MAX_VALUE;
        if (constraint.getMax().isExclusive()) {
          if (valueNumber.doubleValue() < max.doubleValue())
            maxInvalid = false;
        } else {
          if (valueNumber.doubleValue() <= max.doubleValue())
            maxInvalid = false;
        }
        invalid = maxInvalid | minInvalid;
      }
    } else if (type == PropertyType.BOOLEAN) {
      // JCR-283, 4.7.17.6 BOOLEAN has no Constraint
      invalid = false;
    }
    
    return !invalid;
  }
  
    /**
     * Parses JCR path matcher from string
     * 
     * @param path
     * @return 
     * @throws RepositoryException
     */
   private JCRPathMatcher parsePathMatcher(LocationFactory locFactory, String path) throws RepositoryException {
      
      JCRPath knownPath = null;
      boolean forDescendants = false;
      boolean forAncestors = false;
      
      if (path.equals("*") || path.equals(".*")) {
        // any
        forDescendants = true;
        forAncestors = true;
      } else if (path.endsWith("*") && path.startsWith("*")) {
        forDescendants = true;
        forAncestors = true;
        knownPath = parsePath(path.substring(1, path.length()-1), locFactory);
      } else if (path.endsWith("*")) {
        forDescendants = true;
        knownPath = parsePath(path.substring(0, path.length()-1), locFactory);
      } else if (path.startsWith("*")) {
        forAncestors = true;
        knownPath = parsePath(path.substring(1), locFactory);
      } else {
        knownPath = parsePath(path, locFactory);
      }
      
      return new JCRPathMatcher(knownPath.getInternalPath(), forDescendants, forAncestors);

  }
   
   JCRPath parsePath(String path, LocationFactory locFactory)  throws RepositoryException {
     try {
       return locFactory.parseAbsPath(path);
     } catch(RepositoryException e) {
       try {
         return locFactory.parseRelPath(path);
       } catch(RepositoryException e1) {
         throw e;
       }
     }
   }

}
