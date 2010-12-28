Summary

    * Status: Impossible to retrieve the lock on a node even by the root
    * CCP Issue: CCP-587, Product Jira Issue: JCR-1476.
    * Needed for: ECM-5520
    * Complexity: low

The Proposal
Problem description

What is the problem to fix?

    * Step to reproduce:

   1. Upload a Word document
   2. Access to this document in webdav
   3. Close Word by killing the process in the windows task manager, or by shutting down the connection of the user who opened the document.
   4. The document seems as if it's locked. If we want to retrieve the lock, the message " You don't have permission to unlock this node. Please contact with administrator to get correct right" was appeared.

Fix description

How is the problem fixed?

    * Correct the lock holder checking procedure. Now if userID of a session equals system userID (SystemIdentity.SYSTEM) isLockHolder method works correctly and returns true.

Patch information:
Patch files: JCR-1476.patch

Tests to perform

Reproduction test

    * Cf. above

Tests performed at DevLevel

    * Functional tests at jcr (TestLock.java)

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:

    * None

Configuration changes

Configuration changes:

    * None

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
*

Support Comment
* Support validated

QA Feedbacks
*

