Summary

    * Status: Problem when renaming a large folder containing multiple files in webdav (Windows)
    * CCP Issue: CCP-838, Product Jira Issue: JCR-1599.
    * Complexity: High

The Proposal
Problem description

What is the problem to fix?
* Some problems occur when renaming a large folder containing multiple files in WebDAV (Windows):
  - Error popup
  - Name might not be updated immediately
  - Error of Java heap space appears in the console.

Fix description

How is the problem fixed?
* Skip event triggering for descendant items on rename operation. 
  In this case change log will be small, rename will be fast. 
  Such feature can be enabled via workspace container's configuration by adding "trigger-events-for-descendents-on-rename" property (default value is "true").

Patch file: JCR-1599.patch

Tests to perform

Reproduction test
* Steps to reproduce:
      1. Create a web folder ToRename pointing to http://localhost:8080/rest/private/jcr/repository/collaboration/
      2. Copy the cygwin folder (near of 215 MB, ~ 14250 items) and the test folder which is a part of cygwin (74.7 MB, ~ 2390 items) in this web folder. There might some errors when copying but the copying finishes well.
      3. Rename the test folder => OK
      4. Rename the ToRename folder which contains the cygwin => KO
      5. Rename the test folder again => KO, although we managed to rename it at step 3/
      6. A new folder appears in the File Explorer having the new name of test (it isn't the case for the folder ToRename)
      7. An error of Java heap space appears in the console.

Tests performed at DevLevel
* Functional tests in jcr.core. manual testing with Tomcat AS under Windows OS

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* Add "trigger-events-for-descendents-on-rename" property to the workspace where you have placed the large folder to rename in the repository-configuration.xml file. Example:
      ...
       <workspaces>
         <workspace name="system">
           <!-- for system storage -->
             <container class="org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer">
               <properties>
                ....
                 <property name="trigger-events-for-descendents-on-rename" value="false"/>
               </properties>
        ...

Value "true" means default behavior, value "false" adds possibility to not trigger events for descendant items on rename which improves performance.

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* With disabled "trigger-events-for-descendents-on-rename", observation manager will not notify about events for descendant items.

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch approved

Support Comment
* Patch validated

QA Feedbacks
*
