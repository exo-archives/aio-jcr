Summary

    * Status: Don't Allow adding Actions into Contents other than Folder nodetypes
    * CCP Issue: CCP-480, Product Jira Issues: JCR-1595
    * Fixes also: ECM-5477.
    * Needed also: ECM-5591.
    * Complexity: Low

The Proposal
Problem description

What is the problem to fix?

    * To reproduce the problem:
         1. Create/Open an Article node
         2. Click on Manage Actions and add a new action.
         3. Try to edit this node.
            -> The node could not be edited .

Fix description

Problem analysis

    * Problem of getting node definition.

How is the problem fixed?

    * Determine more suitable node definition based on node type.

Patch file: JCR-1595.patch

Tests to perform

Reproduction test

    * TestNodeDefinition.java

Tests performed at DevLevel

    * Functional testing in jcr.core project

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
* N/A

Is there a performance risk/cost?
* No, patch tested by QA TESTUKR-220

Validation (PM/Support/QA)

PM Comment
* Patch approved

Support Comment
* Patch validated

QA Feedbacks
*
