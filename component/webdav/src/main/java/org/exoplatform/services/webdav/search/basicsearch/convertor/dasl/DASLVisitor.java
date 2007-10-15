package org.exoplatform.services.webdav.search.basicsearch.convertor.dasl;

import java.util.Enumeration;

/**
 * Created by The eXo Platform SARL
 * Author : Zagrebin Victor <zagrebin_v@mail.ru>
 * @version $Id: DASLVisitor.java 12004 2007-01-17 12:03:57Z geaz $
 */


/**
 * Abstract class implementing Visitor pattern for DASLDocument objects.
 *
 * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
 */

public abstract class DASLVisitor {

  public void visit(DASLDocument.StartTag t)         { }
  public void visit(DASLDocument.EndTag t)      { }
  public void visit(DASLDocument.EmptyTag t)    { }
  public void visit(DASLDocument.Text t)        { }
  public void visit(DASLDocument.NewLine n)     { }

  public void visit(DASLDocument.ElementSequence s) {
    for (Enumeration e = s.elements();
         e.hasMoreElements(); )
      ((DASLDocument.DASLElement)e.nextElement()).accept(this);
  }

  public void visit(DASLDocument d) {
    start();
    visit(d.elements);
    finish();
  }

  public void start()  { }
  public void finish() { }
}

