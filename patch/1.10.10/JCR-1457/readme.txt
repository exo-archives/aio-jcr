Summary

    * Status: TestBackupManager error
    * CCP Issue: N/A, Product Jira Issue: JCR-1457.
    * Complexity: Low

The Proposal
Problem description

What is the problem to fix?
    * NPE during importing data using WorkspaceContentImporter

Fix description

How is the problem fixed?
    * Put in mapNodePropertiesInfo the value which will be asked then

Patch information:
    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files: JCR-1457.patch

Tests to perform

Reproduction test

    * TestBackupManager.java in ext project

Tests performed at DevLevel

    * Functional tests in JCR project

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

    * Patch approved

QA Feedbacks
*

