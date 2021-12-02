# Contributing to YacGuide App

Anyone is welcome to contribute to the app. Either by just reporting a
bug or making a feature request or by actually contributing code.
This document explains the practical process and guideline for
contribution.


## Code Contribution Workflow

To contribute a patch, the workflow is as follows:

1. Clone the repository 
2. `git checkout master; git pull`
3. [Create a topic branch](#create-a-topic-branch)
4. [Commit patches](#committing-patches)
5. Push commits to your fork
6. [Open a pull request](#creating-a-pull-request)

For more details about the development process have a look to the
[developer notes](docs/developer-notes.md).


## Create a Topic Branch

For a better overview, follow the branch naming scheme
`<feature|bug>/<issue_id>_<short_description>`.
E.g. `bug/123_app_crash_during_export`


## Committing Patches

A general rule is that commits should be atomic (squash commits if
necessary) and diffs should be easy to read. For this reason, do not
mix any formatting fixes or code moves with actual code changes.

Commit messages should consist of a short subject line of max 50
chars, a blank line and a detailed explanation of the commit, unless
the subject line alone is self-explanatory (like "Fix typo in
CONTRIBUTING.md") in which case the subject line is sufficient. For a
more detailed explanation about this topic, see [this post][git-commit].

If a commit references an issue, please add the reference. For
example: `refs #1234` or `fixes #4321`. Using the `fixes` or `closes`
keywords will cause the corresponding issue to be closed when the pull
request is merged.


## Creating a Pull Request

The description of the pull request should contain sufficient
information of what the patch does. You should include references to
any discussions (for example, other issues or mailing list
discussions).


### Address Feedback

At this point you should expect comments and feedback from other
contributors. You can add more commits to your pull request by
committing them locally and pushing to your fork until you have
satisfied all feedback.


### Squashing/Splitting Commits

If your pull request contains fix-up commits or commits which address
too many things at once, you shall either
[squash or split][git-rewrite-history] these commits.


## Copyright

By contributing to this repository, you agree to license your work
under the [GPL-3.0 license](LICENSE).


[fork-a-repo]: https://help.github.com/en/articles/fork-a-repo
[git-commit]: https://chris.beams.io/posts/git-commit/
[git-rewrite-history]: https://git-scm.com/book/en/v2/Git-Tools-Rewriting-History
