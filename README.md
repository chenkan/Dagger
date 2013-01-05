## Dagger is a light, robust Web UI autotest framework

Dagger is a light, robust Web UI autotest framework based on [Selenium](http://seleniumhq.org/) and [TestNg](http://testng.org/doc/index.html).

Dagger is an automation framework first, it provides APIs to control browsers; 
Dagger is a test framework then, it uses TestNg to organize testcases and TestNg's assertions are embedded in APIs above; 
Dagger is a design style at last: the framework and the testcases based on it both should be light and straightforward.

* Wiki: <https://github.com/chenkan/Dagger/wiki>
* Issues: <https://github.com/chenkan/Dagger/issues>
* Tags: Selenium, TestNg, autotest

## Features

* Easy to learn while only less then 20 APIs altogether, see <https://github.com/chenkan/Dagger/wiki>.
* Providing a quickstart, see <https://github.com/chenkan/Dagger/wiki>.
* High speed with parallel mode which is indeed TestNg's feature.
* High stability with a trick on TestNg to retry failed/skipped testcases automatically, see <http://qa.blog.163.com/blog/static/1901470022012611722547>.
* Using Chrome as default browser which is much more quick and stable than Firefox and IE. 
* Firefox and IE are also supported.

## Coming soon

* Integrating Flex/Flash automation.

## How to use Dagger

Dagger is quite suitable for a small group to kick off Web UI autotest, for this case, just checkout Dagger with Eclipse and write testcases within it.

Already have an autotest framework? Please build Dagger into a .jar file to use. However, no build script now :-( 

## Contributors
* NetEase, Inc.