Summary

    * Status: ConcurrentModificationException on org.exoplatform.services.jcr.ext.hierarchy.impl.NewUserListener.processUserStructure
    * CCP Issue: CCP-492, Product Jira Issue: JCR-1411
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * ConcurrentModificationException during save in LinkedWorkspaceStorageCacheImpl.onSaveItems()

Fix description

How is the problem fixed?

    * Cache traversing in LinkedWorkspaceStorageCacheImpl.onSaveItems(ItemStateChangesLog) protected thanks to the writeLock as below:

      writeLock.lock();
      try {
          ...
      } finally {
          writeLock.unlock();
      }

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
There are currently no attachments on this page.
Tests to perform

Which test should have detect the issue?
*

Is a test missing in the TestCase file?
*

Added UnitTest?
*

Recommended Performance test?
*
Documentation changes

Where is the documentation for this feature?
*

Changes Needed:
*
Configuration changes

Is this bug changing the product configuration?
*

Describe configuration changes:
*

Previous configuration will continue to work?
*
Risks and impacts

Is there a risk applying this bug fix?
*

Can this bug fix have an impact on current client projects?
*

Is there a performance risk/cost?
*
Validation By PM & Support

PM Comment
*

Support Comment
* Validated by Support
QA Feedbacks

Performed Tests
*

