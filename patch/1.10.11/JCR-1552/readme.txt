Summary

    * Status: Corrupted data if the server is stopped while document is locked
    * CCP Issue: CCP-587, Product Jira Issue: JCR-1552.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
1. Lock a node (node A)
2. Right click into A: see the unlock icon: OK
3. Stop and restart server
4. Right click into A: see the lock icon ---> KO (should be unlock icon)
5. Click the lock icon.
Error in server console:

[ERROR] UIJcrExplorerContainer - an unexpected error occurs while locking the node <javax.jcr.ItemExistsException: [collaboration] ADD PROPERTY. Item already exists. Condition: parent ID, name, index. []:1[]Documents:1[]Live:1[]weq:1http://www.jcp.org/jcr/1.0lockOwner:1, ID: eededf897f00010101f7cff1710532f6, ParentID: ed5ee2117f000101008b6cca53d5c790. Cause >>>> Violation of unique index JCR_IDX_SITEM_PARENT: duplicate value(s) for column(s) CONTAINER_NAME,PARENT_ID,NAME,I_INDEX,I_CLASS,VERSION in statement [insert into JCR_SITEM(ID, PARENT_ID, NAME, CONTAINER_NAME, VERSION, I_CLASS, I_INDEX, P_TYPE, P_MULTIVALUED) VALUES(?,?,?,?,?,2,?,?,?)]: Violation of unique index JCR_IDX_SITEM_PARENT: duplicate value(s) for column(s) CONTAINER_NAME,PARENT_ID,NAME,I_INDEX,I_CLASS,VERSION in statement [insert into JCR_SITEM(ID, PARENT_ID, NAME, CONTAINER_NAME, VERSION, I_CLASS, I_INDEX, P_TYPE, P_MULTIVALUED) VALUES(?,?,?,?,?,2,?,?,?)]: Violation of unique index JCR_IDX_SITEM_PARENT: duplicate value(s) for column(s) CONTAINER_NAME,PARENT_ID,NAME,I_INDEX,I_CLASS,VERSION in statement [insert into JCR_SITEM(ID, PARENT_ID, NAME, CONTAINER_NAME, VERSION, I_CLASS, I_INDEX, P_TYPE, P_MULTIVALUED) VALUES(?,?,?,?,?,2,?,?,?)]>javax.jcr.ItemExistsException: [collaboration] ADD PROPERTY. Item already exists. Condition: parent ID, name, index. []:1[]Documents:1[]Live:1[]weq:1http://www.jcp.org/jcr/1.0lockOwner:1, ID: eededf897f00010101f7cff1710532f6, ParentID: ed5ee2117f000101008b6cca53d5c790. Cause >>>> Violation of unique index JCR_IDX_SITEM_PARENT: duplicate value(s) for column(s) CONTAINER_NAME,PARENT_ID,NAME,I_INDEX,I_CLASS,VERSION in statement [insert into JCR_SITEM(ID, PARENT_ID, NAME, CONTAINER_NAME, VERSION, I_CLASS, I_INDEX, P_TYPE, P_MULTIVALUED) VALUES(?,?,?,?,?,2,?,?,?)]: Violation of unique index JCR_IDX_SITEM_PARENT: duplicate value(s) for column(s) CONTAINER_NAME,PARENT_ID,NAME,I_INDEX,I_CLASS,VERSION in statement [insert into JCR_SITEM(ID, PARENT_ID, NAME, CONTAINER_NAME, VERSION, I_CLASS, I_INDEX, P_TYPE, P_MULTIVALUED) VALUES(?,?,?,?,?,2,?,?,?)]: Violation of unique index JCR_IDX_SITEM_PARENT: duplicate value(s) for column(s) CONTAINER_NAME,PARENT_ID,NAME,I_INDEX,I_CLASS,VERSION in statement [insert into JCR_SITEM(ID, PARENT_ID, NAME, CONTAINER_NAME, VERSION, I_CLASS, I_INDEX, P_TYPE, P_MULTIVALUED) VALUES(?,?,?,?,?,2,?,?,?)]
at org.exoplatform.services.jcr.impl.storage.jdbc.SQLExceptionHandler.handleAddException(SQLExceptionHandler.java:115)
at org.exoplatform.services.jcr.impl.storage.jdbc.JDBCStorageConnection.add(JDBCStorageConnection.java:534)
at org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager.doAdd(WorkspacePersistentDataManager.java:421)
at org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager.save(WorkspacePersistentDataManager.java:159)
at org.exoplatform.services.jcr.impl.dataflow.persistent.ACLInheritanceSupportedWorkspaceDataManager.save(ACLInheritanceSupportedWorkspaceDataManager.java:153)
......

Fix description

How is the problem fixed?

    * There is a component in JCR that is responsible for storing the list of locked nodes (FileSystemLockPersister). When the server starts or stops, this component performs sweeping of lock, so unlocking the nodes. 
      When JVM exits or stop, the latest snapshot of HSQL Database not flushed on file system. It is the particularity of this DB Engine. It does asynchronous and delayed flush, so offering great performance while making changes in-memory.
      In this case, FileSystemLockPersister on stop event unlocks all the nodes, and so removes jcr:lockOwner and jcr:lockIsDeep properties of locked nodes. But latest snapshot of DB is not flushed, so properties exist after restart.
      Fix allows FileSystemLockPersister to remove locks only on startup, but not the stop. So the data is removed from DB not before 	it's shutdown, but in ordinary mode. So Db is in actual state.

Patch file: JCR-1552.patch

Tests to perform

Reproduction test

    * Cf. above

Tests performed at DevLevel

    * Manual test by following the steps described in issue.

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:

    * none

Configuration changes

Configuration changes:

    * none

Will previous configuration continue to work?

    *

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?

    * In highly loaded systems, server's shutdown can be a bit faster, but startup will be a bit slower.

Validation (PM/Support/QA)

PM Comment
* Patch Approved by the PM

Support Comment
* Support review: patch tested and validated

QA Feedbacks
*

