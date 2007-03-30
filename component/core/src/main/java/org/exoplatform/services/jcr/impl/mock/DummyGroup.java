/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.
 * Please look at license.txt in info directory for more license detail.
 **/


package org.exoplatform.services.jcr.impl.mock;

import org.exoplatform.services.organization.Group;

public class DummyGroup implements Group{

  private String id  ;
  private String parentId  ;
  private String groupName ;
  private String label ;
  private String desc ;
  

  public DummyGroup(String id, String name) {
    this.groupName = name;
    this.id = id;
  }
  

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  

  public String getParentId() { return parentId ; }
  public void setParentId(String parentId) { this.parentId = parentId; }
  
  public String getGroupName() { return groupName; }
  public void setGroupName(String name) { this.groupName = name; }

  public String getLabel() { return label ; }
  public void   setLabel(String s) { label = s ; }
  
  public String getDescription() { return desc ; }
  public void   setDescription(String s)  { desc = s ; }


  public String toString() {
    return "Group[" + id + "|" + groupName + "]";
  }
}