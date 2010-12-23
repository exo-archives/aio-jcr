Summary

    * Status: The If-Modified-Since property in the HTTP header doesn't exist
    * CCP Issue: CCP-628, Product Jira Issue: JCR-1540.
    * Complexity: Low

The Proposal
Problem description

What is the problem to fix?

    * If-Modified-Since element is added to Http request by Browser if the last response for the resource have Last-Modified element.
      In org.exoplatform.services.jcr.webdav.command.GetCommand class of JCR project, the returned response is needed to set value for lastModified element.

Fix description

How is the problem fixed?

    * The problem is fixed by adding Last-modified header to responses.

Patch information:
Patch files: JCR-1540.patch

Tests to perform

Reproduction test

    * Use WebDAV GET on any resource in web browser to test if last-modified header is added to response, use GET one more time to test if if-modified-since is added to request

Tests performed at DevLevel

    * JCR core functional test, manual reproduction of usecase in web browser

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
* Patch approved by the PM.

Support Comment
* Support review: patch validated.

QA Feedbacks
*

