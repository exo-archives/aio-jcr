Summary

    * Status: NullPointerException at restoring workspace that contains mix:referenceable or mix:versionable node
    * CCP Issue: N/A, Product Jira Issue: JCR-1479.
    * Complexity: Medium

The Proposal
Problem description

What is the problem to fix?

    * There is a side effect found with TestLoadBackup
    * NullPointerException at restore workspace with mix:referenceble nodes

Fix description

How is the problem fixed?

* Check if parent identifier is null or not before updating data

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: JCR-1479.patch

Tests to perform

Reproduction test
  * TestLoadBackup.java

Tests performed at DevLevel
    * functional tests in core and ext projects

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
* Patch approved by the PM

Support Comment
* Support review: patch validated

QA Feedbacks
*

