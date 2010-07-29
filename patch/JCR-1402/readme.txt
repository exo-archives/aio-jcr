Summary

    * Status: Provide a mechanism to manage Cache-Control header value for different mime-types from server configuration
    * CCP Issue: CCP-411, Product Jira Issues : JCR-1402 depends on WS-249
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Provide a mechanism to manage Cache-Control header value for different mime-types from server configuration.
    * In WS-249, Create a MediaTypeHelper class to be able to sort and check for matching for mymetypes.
      MimeTypes must be sorted using the following rule:
      "type / subtype"
      "type / *"
      "* / *"

Fix description

How the problem is fixed?

    * You Can use "cache-control" configuration parameter to specify what types of documents must be cached and for how long.
      The value of this parameter must contain colon-separated pairs "MediaType:cache-control value".
      For example if you need to cache all text/xml and text/plain files for 5 minutes (300 sec.) and other text/* files for 10 minutes (600 sec.) use the next configuration:

      <component>
        <type>org.exoplatform.services.jcr.webdav.WebDavServiceImpl</type>
        <init-params>
          <value-param>
            <name>cache-control</name>
            <value>text/xml,text/plain:max-age=300;text/*:max-age=600;</value>
          </value-param>
        <init-params>
      <component>

    * You can use "If-Modidfied-Since" header to validate is the file was changed. (see Hypertext Transfer Protocol - Header Field Definitions for more information about header usage)
    * For WS-249, Add MediaTypeHelper classes.

Patch informations:

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
* You need to add "cache-control" parameter to the configuration of WebDavServiceImpl

Describe configuration changes:

<component>
  <type>org.exoplatform.services.jcr.webdav.WebDavServiceImpl</type>
  <init-params>
    <value-param>
      <name>cache-control</name>
      <value>text/xml,text/plain:max-age=300;text/*:max-age=600;</value>
    </value-param>
  <init-params>
<component>

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
*Patch validated by Support Team , this includes JCR-1402 and WS-249
QA Feedbacks

Performed Tests
*

