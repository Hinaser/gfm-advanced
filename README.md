# gfm-advanced

Another Github Flavored Markdown plugin for intellij platform.

This plugin is almost a re-work for [gfm-plugin](https://github.com/ShyykoSerhiy/gfm-plugin) by Shyyko Serhiy.

Actually I was creating a pull request to the gfm-plugin which stopped working recent intellij IDEs.  
In the work I found out that the problem of the gfm-plugin comes from JxBrowser which is very difficult to make a patch.  
I need to re-organize the whole code to replace JxBrowser to another browser component, so I decided to create an original repository which can be managed by my own.

## Feature
- Preview markdown as github flavored markdown by Github Markdown API.
- If internet connection is not available, use offline markdown parser instead.
- Using embedded Chromium browser to display preview.

## Github Markdown API rate limit
[Github Markdown API](https://developer.github.com/v3/markdown/)  
[Github API rate limit](https://developer.github.com/v3/#rate-limiting)

Github API restricts request rate without authentication token.  
\* As of 2020-07-06, 60 requests per hour without auth token while 5000 req/hour with auth token. 

So I strongly recommend to set auth token which can be generated at Github page header menu "Settings" -> "Developer settings" -> "Personal access tokens".
\* You don't have to provide any permission to the token for gfm-advanced plugin.

In case you don't have a Github account and/or internet connection is not available, markdown parser will
fall back to `flexmark-java` markdown parser.

## Special Thanks
- [gfm-plugin](https://github.com/ShyykoSerhiy/gfm-plugin)  
  A lot of thanks to the author of this plugin. I was helped by the plugin for a long time.
- [java-cef](https://bitbucket.org/chromiumembedded/java-cef/src/master/)  
  Chromium embedded framework for Java. Working good with this plugin so far.
- [flexmark-java](https://github.com/vsch/flexmark-java)  
  Offline markdown parser.
- [highlightjs](https://github.com/highlightjs/highlight.js/)  
  Syntax highlighter.