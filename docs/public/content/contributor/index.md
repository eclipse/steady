# Contribute

Our aim is to build a lively community, hence, we welcome any exchange and collaboration with individuals and organizations interested in the use, support and extension of @@PROJECT_NAME@@.

To contribute, read on to learn about your options:

 * [Contribute to the Vulnerability Knowledge Base](#contribute-to-the-vulnerability-knowledge-base)
 * [Help Others](#help-others) on Stack Overflow
 * [Report Bugs](#report-bugs) as GitHub issues
 * [Analyze Bugs](#analyze-bugs)
 * [Contribute Code](#contribute-code) (fixes and features)

## Contribute to the Vulnerability Knowledge Base

Please refer to the [project "KB" documentation](https://sap.github.io/project-kb/contributing/).


## Help Others

You can help by helping others who use @@PROJECT_NAME@@ and need support. Find them on [Stack Overflow](https://stackoverflow.com/questions/tagged/vulas).

## Report Bugs

If you find a bug - a behavior of the code contradicting its specification - you are welcome to report it.
We can only handle well-reported, actual bugs, so please follow the guidelines below and use forums like [Stack Overflow](https://stackoverflow.com/questions/tagged/vulas) for support questions or when in doubt whether the issue is an actual bug.

Once you have familiarized with the guidelines, you can go to the [GitHub issue tracker](@@PROJECT_URL@@/issues/new?template=bug_report.md) to report the issue.

### Quick Checklist for Bug Reports

Issue report checklist:

 * Real, current bug
 * No duplicate
 * Reproducible
 * Good summary
 * Well-documented
 * Minimal example
 * Use the [template](@@PROJECT_URL@@/issues/new?template=bug_report.md)

### Requirements for a bug report

These eight requirements are the mandatory base of a good bug report:

1. **Only real bugs**: please do your best to make sure to only report real bugs! Do not report:
    * issues caused by application code or any code outside @@PROJECT_NAME@@.
    * something that behaves just different from what you expected. A bug is when something behaves different than specified. When in doubt, ask in a forum.
    * something you do not get to work properly. Use a support forum like Stack Overflow to request help.
    * feature requests. Well, this is arguable: critical or easy-to-do enhancement suggestions are welcome, but we do not want to use the issue tracker as wishlist.
2. No duplicate: you have searched the issue tracker to make sure the bug has not yet been reported
3. Good summary: the summary should be specific to the issue
4. Current bug: the bug can be reproduced in the most current version (state the tested version!)
5. Reproducible bug: there are clear steps to reproduce given. This includes, where possible:
    * a URL to access the example
    * any required user/password information (do not reveal any credentials that could be mis-used!)
    * detailed and complete step-by-step instructions to reproduce the bug
6. Precise description:
    * precisely state the expected and the actual behavior
    * give information about the used browser/device and its version, if possible also the behavior in other browsers/devices
    * if the bug is about wrong UI appearance, attach a screenshot and mark what is wrong
    * generally give as much additional information as possible. (But find the right balance: do not invest hours for a very obvious and easy to solve issue. When in doubt, give more information.)
7. Minimal example: it is highly encouraged to provide a minimal example to reproduce in e.g. jsbin: isolate the application code which triggers the issue and strip it down as much as possible as long as the issue still occurs. If several files are required, you can create a gist. This may not always be possible and sometimes be overkill, but it always helps analyzing a bug.
8. Only one bug per report: open different tickets for different issues

You are encouraged to use [this template](@@PROJECT_URL@@/issues/new?template=bug_report.md).

Please report bugs in English, so all users can understand them.

If the bug appears to be a regression introduced in a new version of @@PROJECT_NAME@@, try to find the closest versions between which it was introduced.<!-- and take special care to make sure the issue is not caused by your application's usage of any internal method which changed its behavior.-->

### Issue handling process

When an issue is reported, a committer will look at it and either confirm it as a real issue (by giving the "approved" label), close it if it is not an issue, or ask for more details. Approved issues are then either assigned to a committer in GitHub, reported in our internal issue handling system, or left open as "contribution welcome" for easy or not urgent fixes.

An issue that is about a real bug is closed as soon as the fix is committed. The closing comment explains which patch version(s) will contain the fix.

### Reporting Security Issues

If you find a security issue, please act responsibly and do NOT report it in the public issue tracker.

Instead, [open a ticket](https://bugs.eclipse.org/bugs/enter_bug.cgi?product=Community&component=Vulnerability+Reports&keywords=security&groups=Security_Advisories) in the Eclipse Bugzilla instance.

More information about Eclipse's vulnerability reporting guidelines can be found in the [Eclipse Project Handbook](https://www.eclipse.org/projects/handbook/#vulnerability-reporting).

### Usage of Labels

Github offers labels to categorize issues. We defined the following labels so far:

Labels for issue categories:

 * bug: this issue is a bug in the code
 * documentation: this issue is about wrong documentation
 * enhancement: this is not a bug report, but an enhancement request

Status of open issues:

 * unconfirmed: this report needs confirmation whether it is really a bug (no label; this is the default status)
 * approved: this issue is confirmed to be a bug
 * author action: the author is required to provide information
 * contribution welcome: this fix/enhancement is approved and you are invited to contribute it

Status/resolution of closed issues:

 * fixed: a fix for the issue was provided
 * duplicate: the issue is also reported in a different ticket and is handled there
 * invalid: for some reason or another this issue report will not be handled further (maybe lack of information or issue does not apply anymore)
 * works: not reproducible or working as expected
 * wontfix: while acknowledged to be an issue, a fix cannot or will not be provided

The labels can only be set and modified by committers.

### Issue Reporting Disclaimer

We want to improve the quality of @@PROJECT_NAME@@ and good bug reports are welcome! But our capacity is limited, so we cannot handle questions or consultation requests and we cannot afford to ask for required details. So we reserve the right to close or to not process insufficient bug reports in favor of those which are very cleanly documented and easy to reproduce. Even though we would like to solve each well-documented issue, there is always the chance that it won't happen - remember: @@PROJECT_NAME@@ is Open Source and comes without warranty.

## Analyze Bugs

Analyzing issue reports can be a lot of effort. Any help is welcome! Go to [the Github issue tracker](@@PROJECT_URL@@/issues?state=open) and find an open issue which needs additional work or a bugfix.

Additional work may be further information, or a minimized jsbin example or gist, or it might be a hint that helps understanding the issue. Maybe you can even find and [contribute](#contribute-code) a bugfix?

## Contribute Code

You are welcome to contribute code in order to fix bugs or to implement new features.

There are three important things to know:

1. You must be aware of the Apache License (which describes contributions) and **agree to the Eclipse Contributor Agreement** (see below).
2. There are **several requirements regarding code style, quality, and product standards** which need to be met (we also have to follow them). The respective section below gives more details on the coding guidelines.
3. **Not all proposed contributions can be accepted**. Some features may e.g. just fit a third-party add-on better. The code must fit the overall direction of the open-source vulnerability assessment tool and really improve it, so there should be some "bang for the byte". For most bug fixes this is a given, but major feature implementation first need to be discussed with one of the @@PROJECT_NAME@@ committers <!--(the top 20 or more of the [Contributors List](https://github.com/SAP/openui5/graphs/contributors))-->, possibly one who touched the related code recently. The more effort you invest, the better you should clarify in advance whether the contribution fits: the best way would be to just open an enhancement ticket in the issue tracker to discuss the feature you plan to implement (make it clear you intend to contribute). We will then forward the proposal to the respective code owner, this avoids disappointment.

<!--### Development Conventions and Guidelines

To keep the code readable and maintainable, please follow these rules, even if you find them violated somewhere. Note that this list is not complete. When a file is consistently not following these rules and adhering to the rules would make the code worse, follow the local style.

**TODO: Complete guidelines, see [here](https://github.com/SAP/openui5/blob/master/docs/guidelines.md) for a comprehensive example**
-->

### Eclipse Contributor Agreement (ECA)

Due to legal reasons, contributors will be asked to accept the ECA before they submit the first pull request to this projects, this happens in an automated fashion during the submission process. Please see the [Eclipse Contributor Agreement](https://www.eclipse.org/legal/ECA.php) for more information.

### Contribution Content Guidelines

Contributed content can be accepted if it:

1. is useful to improve @@PROJECT_NAME@@ (explained above)
2. follows the applicable guidelines and standards

The second requirement could be described in entire books and would still lack a 100%-clear definition, so you will get a committer's feedback if something is not right.
<!--Extensive conventions and guidelines documentation is [available here](docs/guidelines.md).-->

These are some of the most important rules to give you an initial impression:

- Check your Java code with [Spotbugs](https://spotbugs.github.io/) by running `mvn -Dspotbugs.excludeFilterFile=findbugs-exclude.xml -Dspotbugs.includeFilterFile=findbugs-include.xml -Dspotbugs.failOnError=true clean compile com.github.spotbugs:spotbugs-maven-plugin:4.0.4:check`. Code with MEDIUM or HIGH priority findings in one of the following categories will be rejected (see `findbugs-include.xml`): PERFORMANCE, SECURITY, CORRECTNESS, MALICIOUS_CODE, MT_CORRECTNESS
- Strictly follow [Google's Java Style Guide](https://google.github.io/styleguide/javaguide.html), e.g., by installing the respective plugin for your IDE or by using the script `.travis/check_code_style.sh`. Contributions will be automatically checked using [google-java-format](https://github.com/google/google-java-format), and rejected if they do not comply.
- Use variable naming conventions like in the other files you are seeing (e.g. hungarian notation)
- No `System.out.println`
- Only access public APIs of other entities (there are exceptions, but this is the rule)
- Comment your code where it gets non-trivial
- Keep an eye on performance and memory consumption
- Write a unit test
- Do not do any incompatible changes, especially do not modify the name or behavior of public API methods or properties
- Always consider the developer who USES your control/code!
    - Think about what code and how much code he/she will need to write to use your feature
    - Think about what she/he expects your control/feature to do

If this list sounds lengthy and hard to achieve - well, that's what WE have to comply with as well, and it's by far not complete…

### How to contribute - the Process

1. Make sure the change would be welcome (e.g. a bugfix or a useful feature); best do so by proposing it in a GitHub issue
2. Create a branch forking @@PROJECT_NAME@@ repository and do your change
3. Commit and push your changes on that branch
    - When you have several commits, squash them into one (see [this explanation](http://davidwalsh.name/squash-commits-git)) - this also needs to be done when additional changes are required after the code review

4. Provide a meaningful commit message incl. links to the respective issue <!--In the commit message follow the [commit message guidelines](docs/guidelines.md#git-guidelines)-->
5. If your change fixes an issue reported at GitHub, add the following line to the commit message:
    - `Fixes @@PROJECT_URL@@/issues/(issueNumber)`
    - Do NOT add a colon after "Fixes" - this prevents automatic closing.
	- When your pull request number is known (e.g. because you enhance a pull request after a code review), you can also add the line `Closes @@PROJECT_URL@@/pull/(pullRequestNumber)`
6. Create a Pull Request to @@PROJECT_URL@@
7. If necessary, sign the Eclipse Contributor Agreement
8. Wait for our code review and approval, possibly enhancing your change on request
    - Note that @@PROJECT_NAME@@ developers also have their regular duties, so depending on the required effort for reviewing, testing and clarification this may take a while

9. Once the change has been approved we will inform you in a comment
10. Your pull request cannot be merged directly into the branch (internal SAP processes), but will be merged internally and immediately appear in the public repository as well. Pull requests for non-code branches (like "gh-pages" for the website) can be directly merged.
11. We will close the pull request, feel free to delete the now obsolete branch

