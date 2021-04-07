# gfm-advanced

![](https://raw.githubusercontent.com/Hinaser/gfm-advanced/master/docs/gfmA-demo.gif)

Another GitHub Flavored Markdown plugin for intellij platform.

This plugin is almost a re-work for [gfm-plugin](https://github.com/ShyykoSerhiy/gfm-plugin) by Shyyko Serhiy.

Actually I was creating a pull request to the gfm-plugin which stopped working recent intellij IDEs.  
In the work I found out that the problem of the gfm-plugin comes from JxBrowser which is very difficult to make a patch.  
I need to re-organize the whole code to replace JxBrowser to another browser component, so I decided to create an original repository which can be managed by my own.

## Feature
- Preview markdown as github flavored markdown by GitHub Markdown API.
- If internet connection is not available, use offline markdown parser instead.
- Using embedded Chromium browser to display preview.

## GitHub Markdown API rate limit
[GitHub Markdown API](https://developer.github.com/v3/markdown/)  
[GitHub API rate limit](https://developer.github.com/v3/#rate-limiting)

GitHub API restricts request rate without authentication token.  
\* As of 2020-07-06, 60 requests per hour without auth token while 5000 req/hour with auth token. 

So I strongly recommend to set auth token which can be generated at GitHub page header menu "Settings" -> "Developer settings" -> "Personal access tokens".
\* You don't have to provide any permission to the token for gfm-advanced plugin.

In case you don't have a GitHub account and/or internet connection is not available, markdown parser will
fall back to `flexmark-java` markdown parser.

## Dependencies
- [gfm-plugin](https://github.com/ShyykoSerhiy/gfm-plugin) - \[MIT License]  
  Copyright (c) 2015 shyyko.serhiy  
  
  A lot of thanks to the author of this plugin. I was helped by the plugin for a long time.
  
- [java-cef](https://bitbucket.org/chromiumembedded/java-cef/src/master/) - \[BSD 3-Clause License]  
  Copyright (c) 2008-2013 Marshall A. Greenblatt. Portions  
  Copyright (c) 2006-2009 Google Inc.
  
  Chromium embedded framework for Java. Working good with this plugin so far.
  
- [flexmark-java](https://github.com/vsch/flexmark-java) - \[BSD 2-Clause License]  
  Copyright (c) 2015-2016, Atlassian Pty Ltd  
  Copyright (c) 2016-2018, Vladimir Schneider,
  
  Used as an offline markdown parser.
  
- [highlightjs](https://github.com/highlightjs/highlight.js/) - \[BSD 3-Clause License]  
  Copyright (c) 2006, Ivan Sagalaev.
  
  Syntax highlighter.
