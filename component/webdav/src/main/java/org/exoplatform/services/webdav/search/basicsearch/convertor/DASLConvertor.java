package org.exoplatform.services.webdav.search.basicsearch.convertor;

import org.exoplatform.services.webdav.search.basicsearch.convertor.dasl.DASLDocument;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Stack;
import java.io.FileInputStream;
import java.io.File;

/**
 * Created by The eXo Platform SARL
 * Author : Zagrebin Victor <zagrebin_v@mail.ru>
 * @version $Id: DASLConvertor.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class DASLConvertor
{
  private String text_tag_type="text";
  private String start_tag_type="start_tag";
  private String end_tag_type="end_tag";
  private String empty_tag_type="empty_tag";
  private String nl_tag_type="NL";

  private String dasl_prefix;
  private String jcr_path="jcr:path";

  private String select_name="select";
  private String from_name="from";
  private String where_name="where";
  private String orderby_name="orderby";
  private String order_name="order";
  private String ascending_name="ascending";
  private String descending_name="descending";

  //allowable names for SELECT block
  private String prop_name="prop";
  private String allprop_name="allprop";

  //allowable names for FROM block
  private String scope_name="href";
  private String href_name="href";

  //allowable names for WHERE block
  private String lt_name="lt";
  private String lte_name="lte";
  private String gt_name="gt";
  private String gte_name="gte";
  private String eq_name="eq";
  private String or_name="or";
  private String and_name="and";
  private String not_name="not";
  private String like_name="like";
  private String contains_name="contains";
  private String literal_name="literal";

  private String sql_star_name="*";
  private String sql_comma_name=",";
  private String sql_orderby_name="order by";
  private String sql_ascending_name="asc";
  private String sql_descending_name="desc";
  private String sql_lt_name="<";
  private String sql_lte_name="<=";
  private String sql_gt_name=">";
  private String sql_gte_name=">=";
  private String sql_eq_name="=";
  private String sql_and_name="and";
  private String sql_or_name="or";
  private String sql_not_name="not";
  private String sql_like_name="like";
  private String sql_contains_name="contains";
  private String sql_where_name="where";
  private String sql_from_name="from";

  private DASLDocument select_doc;
  private DASLDocument from_doc;
  private DASLDocument where_doc;
  private DASLDocument orderby_doc;
  private String select_converted = "";
  private String from_to_from_converted = "";
  private String from_to_where_converted = "";
  private String where_converted = "";
  private String orderby_converted = "";

  private Vector<String> from_elements;

  public DASLConvertor(Vector<String> els)
  {
          setFromElements(els);
  }

  /**
   * Sets a DASL prefix
   * @param prefix DASL prefix
   */
  public void setDASLPrefix(String prefix)
  {
    dasl_prefix = prefix;
  }

  /**
   * This method use in case you need to specify a types in FROM block
   * in JCR SQL converted query.
   * These types never be specified
   * in dav:basicsearch block. But these types can be
   * transfer to configuration file or source code. Than you will have
   * a correct convertation DASL -> JCR SQL query
   *
   * @param els the names of types you want to specify in FROM block
   * of JCR SQL converted query.
   */
  public void setFromElements(Vector<String> els)
  {
    from_elements = els;
  }

  /**
   * Returns the names of types which is specified for FROM block
   * of JCR SQL converted query.
   *
   * @return The names of types which is specified for FROM block
   * of JCR SQL converted query.
   */
  public Vector<String> getFromElements()
  {
    return from_elements;
  }

  /**
   * Converts dav:basicsearch query into jcr SQL query
   *
   * @param doc a DASL document with nodes from DAV:basicsearch query.
   * @return A string which contains jcr SQL query
   */
  public String convertQuery(DASLDocument doc)
  {
   select_doc = prepareBlock(doc, select_name);
   from_doc = prepareBlock(doc, from_name);
   where_doc = prepareBlock(doc, where_name);
   orderby_doc = prepareBlock(doc, orderby_name);

   if (select_doc != null) select_doc = filterSelectBlock(select_doc);
   if (from_doc != null) from_doc = filterFromBlock(from_doc);
   if (where_doc != null) where_doc = filterOrderbyBlock(where_doc);
   if (orderby_doc != null) orderby_doc = filterWhereBlock(orderby_doc);

   if (select_doc != null) select_converted = convertSelect(select_doc);
   if (from_doc != null) from_to_where_converted = convertFromToWhere(from_doc);
   if (from_doc != null) from_to_from_converted = convertFromToFrom(from_doc);
   if (where_doc != null) where_converted = convertWhere(where_doc);
   if (orderby_doc != null) orderby_converted = convertOrderby(orderby_doc);

   String result_query =  select_converted
                        + from_to_from_converted;
   if(!where_converted.equals(""))
   {
           result_query += where_converted;
           result_query += from_to_where_converted;
   }
   else
   {
        result_query += sql_where_name.toUpperCase()
                     + " "
                     + from_to_where_converted.substring(3,from_to_where_converted.length());
   }
   result_query += orderby_converted;
   return result_query;
  }

  /**
   * Prepares document which consist of only elements from specified block.
   *
   * @param doc DASLDocument with all blocks
   * @param block_name the name of block (select, from, where, orderby)
   * @return DASLDocument which contains only a specified block
   */
  public DASLDocument prepareBlock(DASLDocument doc, String block_name)
  {
   boolean status = false;
   boolean non_empty_status = false;
   DASLDocument.ElementSequence s = new DASLDocument.ElementSequence();
   for (Enumeration e = doc.getElements().elements(); e.hasMoreElements(); )
   {
     Object o = e.nextElement();
     String type = ((DASLDocument.DASLElement)o).getType();
     String name = ((DASLDocument.DASLElement)o).getName();
     if(   type.equalsIgnoreCase(start_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+block_name))
     {
      status = true;
     }
     if(   type.equalsIgnoreCase(end_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+block_name)
        && status)
     {
      return new DASLDocument(s);
     }
     if(    status
        && !(name.equalsIgnoreCase(dasl_prefix+block_name)) )
     {
      s.addElement((DASLDocument.DASLElement)o);
      non_empty_status = true;
     }
   }
   if(non_empty_status) return new DASLDocument(s);
   return null;
  }

  /**
   * Filter document which consist of only elements from SELECT block.
   * The filter keeps an elements admitted to the jcr SQL.
   *
   * @param dd DASLDocument with elements from SELECT block only.
   * @return DASLDocument with elements admitted to the jcr SQL.
   */
  public DASLDocument filterSelectBlock(DASLDocument dd)
  {
    boolean status = false;
        DASLDocument.ElementSequence s = new DASLDocument.ElementSequence();
    for (Enumeration e = dd.getElements().elements(); e.hasMoreElements(); )
    {
     Object o = e.nextElement();
     String type = ((DASLDocument.DASLElement)o).getType();
     String name = ((DASLDocument.DASLElement)o).getName();
     if ( type.equalsIgnoreCase(empty_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+allprop_name))
     {
      s = new DASLDocument.ElementSequence();
      s.addElement((DASLDocument.DASLElement)o);
      return new DASLDocument(s);
     }
     if(!(type.equalsIgnoreCase(empty_tag_type) && name.startsWith(dasl_prefix)))
            s.addElement((DASLDocument.DASLElement)o);
    }
    return new DASLDocument(s);
  }

  /**
   * Filters document which consist of only elements from FROM block.
   * The filter keeps an elements admitted to the jcr SQL.
   *
   * @param dd DASLDocument with elements from FROM block only.
   * @return DASLDocument with elements admitted to the jcr SQL.
   */
  public DASLDocument filterFromBlock(DASLDocument dd)
  {
    /*
    boolean status = false;
    DASLDocument.ElementSequence s = new DASLDocument.ElementSequence();
    for (Enumeration e = dd.getElements().elements(); e.hasMoreElements(); )
    {
     Object o = e.nextElement();
     String type = ((DASLDocument.DASLElement)o).getType();
     String name = ((DASLDocument.DASLElement)o).getName();
     if ( type.equalsIgnoreCase(start_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+href_name))
     {
      s.addElement((DASLDocument.DASLElement)o);
      status = true;
     }
     if (   type.equalsIgnoreCase(end_tag_type)
         && name.equalsIgnoreCase(dasl_prefix+href_name))
     {
      s.addElement((DASLDocument.DASLElement)o);
      status = false;
     }
     if(status&&type.equalsIgnoreCase(text_tag_type))
     {
      s.addElement((DASLDocument.DASLElement)o);
     }
    }
    return new DASLDocument(s);
    */
    return dd;
  }

  /**
   * Filters document which consist of only elements from WHERE block.
   * (Reserved for the changes in future).
   *
   * @param dd DASLDocument with elements from WHERE block only.
   * @return a DASLDocument that has the same contents as this DASLDocument
   */
  public DASLDocument filterWhereBlock(DASLDocument dd)
  {
   return dd;
  }


  /**
   * Filters document which consist of only elements from ORDERBY block.
   * (Reserved for the changes in future).
   *
   * @param dd DASLDocument with elements from ORDERBY block only.
   * @return a DASLDocument that has the same contents as this DASLDocument
   */
  public DASLDocument filterOrderbyBlock(DASLDocument dd)
  {
   return dd;
  }

  /**
   * Converts DASLDocument with SELECT block into jcr SQL query
   *
   * @param doc the DASLDocument with SELECT block
   * @return the jcr SQL query string due to SELECT block
   */
  public String convertSelect(DASLDocument doc)
  {
    boolean status = false;
    StringBuffer query_block = new StringBuffer(select_name.toUpperCase()+" ");
    for (Enumeration e = doc.getElements().elements(); e.hasMoreElements(); )
    {
     Object o = e.nextElement();
     String type = ((DASLDocument.DASLElement)o).getType();
     String name = ((DASLDocument.DASLElement)o).getName();

     if(   type.equalsIgnoreCase(start_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+prop_name)) status = true;
     if(   type.equalsIgnoreCase(end_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+prop_name)) status = false;


     if(   type.equalsIgnoreCase(empty_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+allprop_name))
     {
      query_block.append(sql_star_name+" ");
      return query_block.toString();
     }

     if(   type.equalsIgnoreCase(empty_tag_type)
         && status )
     {
      query_block.append(name+sql_comma_name+" ");
      continue;
     }
    }
    if(!query_block.toString().equalsIgnoreCase(select_name.toUpperCase()+" "))
    {
     query_block.delete((query_block.length() - sql_comma_name.length() - 1),query_block.length()-1);
     return query_block.toString();
    }
    return new String(select_name.toUpperCase()+" "+sql_star_name+" ");
  }

  /**
   * Converts a part of DASLDocument with FROM block (due to path)
   * into jcr SQL query.
   *
   * @param doc the DASLDocument with FROM block
   * @return the jcr SQL query string due to FROM block
   */
  public String convertFromToFrom(DASLDocument doc)
  {
   StringBuffer query_block = new StringBuffer(from_name.toUpperCase()+" ");
   for (Enumeration e = from_elements.elements(); e.hasMoreElements(); )
   {
    Object o = e.nextElement();
    String name = ((String)o);
    query_block.append(name+", ");
   }
   if(query_block.toString().equalsIgnoreCase(from_name.toUpperCase()+" ")) return "";
   else if(query_block.toString().endsWith(", ")) query_block.delete(query_block.length()-2,query_block.length()-1);
   return query_block.toString();
  }

  /**
   * Converts a part of DASLDocument with FROM block (due to path)
   * into jcr SQL query.
   *
   * @param doc the DASLDocument with FROM block
   * @return the jcr SQL query string due to FROM block
   */
  public String convertFromToWhere(DASLDocument doc)
  {
    boolean scope_status = false;
    boolean status = false;
    StringBuffer query_block = new StringBuffer("");
    for (Enumeration e = doc.getElements().elements(); e.hasMoreElements(); )
    {
     Object o = e.nextElement();
     String type = ((DASLDocument.DASLElement)o).getType();
     String name = ((DASLDocument.DASLElement)o).getName();
     if(   type.equalsIgnoreCase(start_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+scope_name) ) scope_status = true;
     if(   type.equalsIgnoreCase(start_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+href_name) ) status = true;
     if(   type.equalsIgnoreCase(end_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+href_name) ) status = false;
     if(   type.equalsIgnoreCase(end_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+scope_name) ) scope_status = false;
     if((o instanceof DASLDocument.Text) && status && scope_status)
     {

      query_block.append(  sql_and_name.toUpperCase()
                                 + " "
                                 + jcr_path
                                 + " "
                                 + sql_like_name.toUpperCase()
                                 + " '"
                                 + ((DASLDocument.Text)o).getContent()+"' ");
      continue;
     }
    }
    if(query_block.toString().equalsIgnoreCase("")) return "";
    else return query_block.toString();
  }

  /**
   * Converts DASLDocument with WHERE block into jcr SQL query
   *
   * @param doc the DASLDocument with WHERE block
   * @return the jcr SQL query string due to WHERE block
   */
  public String convertWhere(DASLDocument doc)
  {
    StringBuffer query_block = new StringBuffer(sql_where_name.toUpperCase()+" ");
    boolean status = false;
    boolean prop_status = false;
    boolean value_status = false;
    int condition_status = 0;
    //boolean place_condition_status = true;
    String status_tag_name = "";
    Stack<String> condition_name = new Stack<String>();

    for (Enumeration e = doc.getElements().elements(); e.hasMoreElements(); )
    {
     Object o = e.nextElement();
     String type = ((DASLDocument.DASLElement)o).getType();
     String name = ((DASLDocument.DASLElement)o).getName();
     if(   type.equalsIgnoreCase(start_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+not_name) )
     {
      if(name.equalsIgnoreCase(dasl_prefix+not_name)) query_block.append(sql_not_name.toUpperCase()+" ");
     }

     if(   type.equalsIgnoreCase(start_tag_type)
        && (   name.equalsIgnoreCase(dasl_prefix+or_name)
            || name.equalsIgnoreCase(dasl_prefix+and_name)
           )
       )
     {
      condition_status++;
      //place_condition_status = true;
      if(name.equalsIgnoreCase(dasl_prefix+or_name)) condition_name.push(sql_or_name);
      if(name.equalsIgnoreCase(dasl_prefix+and_name)) condition_name.push(sql_and_name);
     }


     if(  type.equalsIgnoreCase(start_tag_type) && !status)
     {
      if(   name.equalsIgnoreCase(dasl_prefix+lt_name)
         || name.equalsIgnoreCase(dasl_prefix+lte_name)
         || name.equalsIgnoreCase(dasl_prefix+gt_name)
         || name.equalsIgnoreCase(dasl_prefix+gte_name)
         || name.equalsIgnoreCase(dasl_prefix+eq_name)
         || name.equalsIgnoreCase(dasl_prefix+like_name)
         || name.equalsIgnoreCase(dasl_prefix+contains_name)
         )
      {
       status = true;
       status_tag_name = name;
      }
     }
     if(  type.equalsIgnoreCase(end_tag_type) && status)
     {
      if(   name.equalsIgnoreCase(dasl_prefix+lt_name)
         || name.equalsIgnoreCase(dasl_prefix+lte_name)
         || name.equalsIgnoreCase(dasl_prefix+gt_name)
         || name.equalsIgnoreCase(dasl_prefix+gte_name)
         || name.equalsIgnoreCase(dasl_prefix+eq_name)
         || name.equalsIgnoreCase(dasl_prefix+like_name)
         || name.equalsIgnoreCase(dasl_prefix+contains_name)
         )
      {
       status = false;
       status_tag_name = "";
       if(condition_status != 0)
                       //&& place_condition_status)
       {
        query_block.append(condition_name.pop().toUpperCase()+" ");
        condition_status--;
        //place_condition_status = false;
       }
      }
     }
     if(   type.equalsIgnoreCase(start_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+prop_name)
        && status && !prop_status )
     {
      prop_status = true;
     }

     if(   type.equalsIgnoreCase(end_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+prop_name)
        && status && prop_status)
     {
      prop_status = false;
     }

     if (   type.equalsIgnoreCase(start_tag_type)
             && name.equalsIgnoreCase(dasl_prefix+literal_name)
             && status && !value_status )
             {
               value_status = true;
             }

     if (   type.equalsIgnoreCase(end_tag_type)
             && name.equalsIgnoreCase(dasl_prefix+literal_name)
             && status && value_status)
             {
               value_status = false;
             }

      if(   type.equalsIgnoreCase(empty_tag_type)
                && status
             && prop_status)
      {
             query_block.append(name+" ");
             continue;
      }

     if((o instanceof DASLDocument.Text) && status && !prop_status && value_status)
     {
      String operation = null;
      if(status_tag_name.equalsIgnoreCase(dasl_prefix+lt_name)) operation = sql_lt_name.toUpperCase();
      else if(status_tag_name.equalsIgnoreCase(dasl_prefix+lte_name)) operation = sql_lte_name.toUpperCase();
      else if(status_tag_name.equalsIgnoreCase(dasl_prefix+gt_name)) operation = sql_gt_name.toUpperCase();
      else if(status_tag_name.equalsIgnoreCase(dasl_prefix+gte_name)) operation = sql_gte_name.toUpperCase();
      else if(status_tag_name.equalsIgnoreCase(dasl_prefix+eq_name)) operation = sql_eq_name.toUpperCase();
      else if(status_tag_name.equalsIgnoreCase(dasl_prefix+like_name))
       query_block.append(sql_like_name.toUpperCase()+" '"+((DASLDocument.Text)o).getContent()+"' ");
      if(operation != null)
      query_block.append(operation+" '"+((DASLDocument.Text)o).getContent()+"' ");
     }
     if((o instanceof DASLDocument.Text) && status)
     {
      if(status_tag_name.equalsIgnoreCase(dasl_prefix+contains_name))
      {
       query_block.append(sql_contains_name.toUpperCase()+"(*, '"+((DASLDocument.Text)o).getContent()+"') ");
      }
     }
    }
    return query_block.toString();
  }

  /**
   * Converts DASLDocument with ORDER BY block into jcr SQL query
   *
   * @param doc the DASLDocument with ORDER BY block
   * @return the jcr SQL query string due to ORDER BY block
   */
  public String convertOrderby(DASLDocument doc)
  {
    boolean status = false;
    boolean prop_status = false;
    StringBuffer query_block = new StringBuffer(sql_orderby_name.toUpperCase()+" ");
    for (Enumeration e = doc.getElements().elements(); e.hasMoreElements(); )
    {
     Object o = e.nextElement();
     String type = ((DASLDocument.DASLElement)o).getType();
     String name = ((DASLDocument.DASLElement)o).getName();

     if(  type.equalsIgnoreCase(start_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+order_name))
     {
      status = true;
     }

     if(  type.equalsIgnoreCase(end_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+order_name))
     {
      status = false;
     }

     if(  type.equalsIgnoreCase(start_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+prop_name)
        && status)
     {
      prop_status = true;
     }

     if(  type.equalsIgnoreCase(end_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+prop_name)
        && status)
     {
      prop_status = false;
     }

     if( type.equalsIgnoreCase(empty_tag_type) && status && prop_status)
     {
      query_block.append(name+" ");
      continue;
     }

     if(  type.equalsIgnoreCase(empty_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+ascending_name)
        && status)
     {
      query_block.append(sql_ascending_name.toUpperCase()+sql_comma_name+" ");
     }
     if(  type.equalsIgnoreCase(empty_tag_type)
        && name.equalsIgnoreCase(dasl_prefix+descending_name)
        && status)
     {
      query_block.append(sql_descending_name.toUpperCase()+sql_comma_name+" ");
     }
    }
    if(!query_block.toString().equalsIgnoreCase(sql_orderby_name.toUpperCase()+" "))
    {
     query_block.delete((query_block.length() - sql_comma_name.length() - 1),query_block.length()-1);
     return query_block.toString();
    }
    return "";
  }
}

