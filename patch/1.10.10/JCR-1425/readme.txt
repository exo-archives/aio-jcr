Summary

    * Status: Impossible to retrieve node property from JCR using RMI connection
    * CCP Issue: CCP-555, Product Jira Issue: JCR-1425.
    * Complexity: LOW

The Proposal
Problem description

What is the problem to fix?

    * Fix retrieving "exo:permissions" property from the node
    * To reproduce this issue:
         1. Use an RMI connection to a JCR (http://wiki.exoplatform.org/xwiki/bin/view/JCR/RMI)
         2. Use the class SomeClass.java to
                o Retrieve a node (ok)
                o Retrieve the property "exo:permissions" (ok)
                o Retrieve the values contained in this property. "exo:property" is multi-value (ko)

We have an exception (see exception.txt at JCR-1425)
Fix description

How is the problem fixed?

    * Fixed parsing permission identity on the server side before sending to the client

Patch information:
Patch files:
JCR-1425.patch

Tests to perform

Reproduction test

    * cf. above

Tests performed at DevLevel

    * Functional and TCK tests in JCR project

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

    * Patch validated by Support

QA Feedbacks
*

