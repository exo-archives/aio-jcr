Summary

    * Status: Determine property type from import file
    * CCP Issue: CCP-529, Product Jira Issue: JCR-1429. Needed for JCR-1421. Fixes also JCR-1437.
    * Complexity: HIGH

The Proposal
Problem description

What is the problem to fix?
* Cf. CCP-529 JCR-1421 ECM-5489: Some problems occur after importing a document with version history and restoring an old version.

Steps to reproduce:

   1. Create a document (exo:webcontent, exo:article, Sample node).
   2. Activate versioning on this node.
   3. Create different version of this document via checkin/checkout.
   4. Export this node and its version history in Document mode.
   5. Import it with version history, UUID behavior "remove existing"
   6. Possible to view node properties, to publish its content.
   7. Restore one old version: 2 ways
         1. use Manage Version
         2. use Manage Publication
   8. Now it is impossible to view the node properties, or to publish its content.

Fix description

How is the problem fixed?

    * Determine if a property is multi or single value from the node type definition in import file.
    * Determine property type from nodetype definition in DocumentViewImport for version history.

Patch information:
Patch files:
JCR-1429.patch

Tests to perform

Reproduction test

    * cf. above

Tests performed at DevLevel

    * Functional and TCK tests in JCR project
    * Manual testing import/export via WCM

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:

    * No

Configuration changes

Configuration changes:

    * No

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Approved by the PM

Support Comment

    * Support review: approved

QA Feedbacks
*

