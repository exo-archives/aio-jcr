package org.exoplatform.services.webdav.search.basicsearch.convertor.dasl;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by The eXo Platform SARL
 * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
 * @version $Id: DASLDocument.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class DASLDocument
{
 ElementSequence elements;

 public DASLDocument(ElementSequence s) { elements = s; }

 public ElementSequence getElements() { return elements; }

 public void accept(DASLVisitor v) { v.visit(this); }

  /**
   * Abstract class for DASL elements.  Enforces support for Visitors.
   *
   * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
   */
  public static abstract class DASLElement {
    public abstract void accept(DASLVisitor v);
    public abstract String getType();
    public abstract String getName();
  };

  /**
   * DASL Plain text tag node.
   *
   * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
   */
  public static class Text extends DASLElement {
    private String type = "text";
    private String tag_name = "";
    private String content;

    public Text(String s) { content = s; }

    public String getType() { return type; }
    public String getName() { return tag_name; }
    public String getContent() { return content; }
    public void setContent(String s) { content = s; }
    public void accept(DASLVisitor v) { v.visit(this); }
    public int getLength() { return content.length(); }
    public String toString() { return content; }
  }

  /**
   * DASL start tag node.  Stores the tag name and a list of tag attributes.
   *
   * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
   */
  public static class StartTag extends DASLElement {
    private String type = "start_tag";
    private String tag_name;
    public AttributeList attributeList;

    public StartTag(String t) { tag_name = t; }
    public StartTag(String t, AttributeList a) { tag_name = t; attributeList = a; }
    public String getName() { return tag_name; }
    public void setName(String t) { tag_name = t; }
    public String getType() { return type; }

    public void accept(DASLVisitor v) { v.visit(this); }
    public int getLength() {
      int length = 0;
      for (Enumeration ae=attributeList.attributes.elements();
           ae.hasMoreElements(); )
        length += 1 + ((Attribute) ae.nextElement()).getLength();
      return length + tag_name.length() + 2;
    }

    public String toString() {
      String s = "<" + tag_name;
      for (Enumeration ae=attributeList.attributes.elements();
           ae.hasMoreElements(); )
        s += " " + ((Attribute) ae.nextElement()).toString();
      s += ">";
      return s;
    }
  }

  /**
   * DASL end tag.  Stores only the tag name.
   *
   * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
   */
  public static class EndTag extends DASLElement
  {
    private String type = "end_tag";
    private String tag_name;

    public EndTag(String t) { tag_name = t; }
    public String getName() { return tag_name; }
    public void setName(String t) { tag_name = t; }
    public String getType() { return type; }
    public void accept(DASLVisitor v) { v.visit(this); }
    public int getLength() { return 3 + tag_name.length(); }
    public String toString() { return "</" + tag_name + ">"; }
  }

  /**
   * DASL empty tag node.  Stores the tag name and a list of tag attributes.
   *
   * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
   */
  public static class EmptyTag extends DASLElement {
    private String type = "empty_tag";
    private String tag_name;

    public EmptyTag(String t) { tag_name = t; }
    public String getName() { return tag_name; }
    public void setName(String t) { tag_name = t; }
    public String getType() { return type; }
    public void accept(DASLVisitor v) { v.visit(this); }
    public int getLength() { return 3 + tag_name.length(); }
    public String toString() { return "<" + tag_name + "/>"; }
  }

  /**
   * End of line tag node.
   *
   * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
   */
  public static class NewLine extends DASLElement {
    public String type = "NL";
    private String tag_name = "";
    public static final String NL = System.getProperty("line.separator");

    public NewLine() {}
    public String getType() { return type; }
    public String getName() { return tag_name; }
    public void accept(DASLVisitor v) { v.visit(this); }
    public int getLength() { return NL.length(); }
    public String toString() { return NL; }
  }

  /**
   * The attribute.
   *
   * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
   */
  public static class Attribute
  {
    public String name, value;
    public boolean hasValue;

    public Attribute(String n) { name = n; hasValue = false; }
    public Attribute(String n, String v)
    {
      name = n;
      value = v;
      hasValue = true;
    }

    public int getLength()
    {
      return (hasValue? name.length() + 1 + value.length() : name.length());
    }
    public String toString() { return (hasValue ? name+"="+value : name); }
  }

  public static class AttributeList
  {
    public Vector<Attribute> attributes;

    public AttributeList() { attributes = new Vector<Attribute>(); }
    public void addAttribute(Attribute a) { attributes.addElement(a); }
  }

  /**
   * A sequence of DASL elements.
   *
   * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
   */
  public static class ElementSequence
  {
    protected Vector<DASLElement> elements;

    public ElementSequence(int n) { elements = new Vector<DASLElement>(n); }
    public ElementSequence()      { elements = new Vector<DASLElement>(); }

    public void addElement(DASLElement o) { elements.addElement(o); }
    public Enumeration elements() { return elements.elements(); }
  }
}