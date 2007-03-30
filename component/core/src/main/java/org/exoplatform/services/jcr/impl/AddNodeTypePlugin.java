package org.exoplatform.services.jcr.impl;

import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

public class AddNodeTypePlugin extends BaseComponentPlugin {

  private List nodeTypes_;
  private List nodeTypesFiles;

  public AddNodeTypePlugin(InitParams params) {
    ValuesParam param = params.getValuesParam("nodeTypes");
    if (param != null) {
      nodeTypes_ = param.getValues();
    }
    param = params.getValuesParam("nodeTypesFiles");
    if (param != null) {
      nodeTypesFiles = param.getValues();
    }        
  }

  public List getNodeTypes() {
    return nodeTypes_;
  }
  
  public List getNodeTypesFiles() {
    return nodeTypesFiles;
  }
}
