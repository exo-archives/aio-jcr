Summary

    * Status: Problem of replication of the search index in a cluster
    * CCP Issue: CCP-679, Product Jira Issue: JCR-1517.
    * Complexity: middle

The Proposal
Problem description

What is the problem to fix?
* The search of the customer is personalized and based on the frozen nodes. However, it seems that there is a problem with them when using a cluster.
  When publishing a content on a node 1, the advanced search allows to retrieve the content and its associated frozen nodes.
  When we repeat the same search on node 2, the content is returned but not its associated frozen nodes.

Fix description

How is the problem fixed?
* SystemSearchManager was added as listener for changes in WorkspaceDataManagerProxy.

Patch file: JCR-1517.patch

Tests to perform

Reproduction test

    * Install a cluster AIO 1.6.6, 2 nodes / values in a database
    * Publish a content on the first node containing the word 'test' for example
    * In the advanced search, run the following query: SELECT * FROM nt: base WHERE jcr: path LIKE '/%' AND (CONTAINS (*, 'test'))
      -> The content is returned by the search and its associated frozen node
    * Repeat the same search on node 2
      -> Return the content, but not its frozen node

Tests performed at DevLevel
* Functional tests in core project
* Test which reproduces issue

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
* NO

Validation (PM/Support/QA)

PM Comment
* Patch validated by the PM

Support Comment
* Support review: Patch validated

QA Feedbacks
*

