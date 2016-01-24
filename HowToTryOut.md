# How to Try Out #

  1. Install Eclipse SDK 3.4.2 or 3.5.0.
  1. Install SVN Eclipse integration from http://subversion.tigris.org/ to be able to download samples later.
  1. Download the ohl patch jar file (one with matching version) from "Downloads"
  1. Run Eclipse as
```
Eclipse.exe -dev patch_file_name.jar
```
  1. In the running Eclipse import java projects into your workspace from https://ohl.googlecode.com/svn/trunk/samples SVN URL. It should be a sample project and important _ohl\_rt_ library project. One depends on another.
  1. Try to play with it.
  1. Try basic features: [enum-case declaration](LangEnumCase.md), [switch operator](LangSwitch.md), [user types](LangUserType.md) and [tuples](LangTuple.md).
  1. Try advanced features: [enum-case extension](LangEnumCaseExtends.md), [generic enum-cases](LangGenericEnumCase.md), [enum-case as a generic type](LangTypeParam.md), [variance](LangVariance.md), [reflection](LangReflection.md).
  1. Feel free to write me if you have thoughts or there are problems. See also [comments](Discussion.md).