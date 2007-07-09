/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.value;

import java.io.Serializable;

import org.exoplatform.services.jcr.core.value.ExtendedValue;

/**
 * The <code>StatefullValue</code> interface defines the API used for the
 * state classes used by the
 * {@link org.exoplatform.services.jcr.rmi.api.value.SerialValue} class.
 * <p>
 * This is a marker interface with two purposes; it separates the value state
 * classes from the more general value classes, and it forces the state classes
 * to be serializable. This interface is used only internally by the State
 * pattern implementation of the
 * {@link org.exoplatform.services.jcr.rmi.api.value.SerialValue} class.
 * <p>
 * This interface is not intended to be implemented by clients. Rather any of
 * the concrete implementations of this class should be used or overwritten as
 * appropriate.
 * 
 * @see org.exoplatform.services.jcr.rmi.api.value.SerialValue
 */
public interface StatefulValue extends ExtendedValue, Serializable {
}
