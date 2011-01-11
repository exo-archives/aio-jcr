Summary

    * Status: Problems in JCR content after being imported
    * CCP Issue: CCP-722, Product Jira Issue: JCR-1549.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
After importing a customer site and its version histories in WCM-1.2.7, we got problems with some contents:

    * publication:navigationNodeURIs which should be multiple is set as single in those contents.
    * Some mixin types are not found like publication:webpagesPublication inspite that their properties are on the node.
    * We can neither restore old versions of documents nor change the values of properties.

Fix description

How is the problem fixed?

    * Fix customer backup files.
    * Check whether frozen node or child contains jcr:uuid and ignoring it. Version history nodes must have unique Id, and Id must not be assigned from jcr:uuid property.

Patch file: JCR-1549.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
* TestImportVersionHistory that reproduces problem with restore old versions.

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
* Client's backup file is updated - there is a risk that some corrupted nodes are not fixed. Only publication:webpagesPublication nodes are fixed - issue mentions about some "other" mixins.

Is there a performance risk/cost?
* Import from XML may be a little bit slower

Validation (PM/Support/QA)

PM Comment
* Patch approved by the PM

Support Comment
* Support Team Review: patch validated

QA Feedbacks
*
