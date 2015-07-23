# hara 

[![Join the chat at https://gitter.im/zcaudate/hara](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/zcaudate/hara?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/zcaudate/hara.png?branch=master)](https://travis-ci.org/zcaudate/hara)

Code patterns and utilities. Please see [finding a middle ground](http://z.caudate.me/finding-a-middle-ground/) for motivations and reasoning.

## Installation

Add to `project.clj`:

```clojure
[im.chit/hara "2.2.3"]

or

[im.chit/hara.<PACKAGE> "2.2.3"]

or

[im.chit/hara.<PACKAGE>.<NAMESPACE> "2.2.3"]
```

Where `PACKAGE` and `NAMESPACE` can be seen from the [website](http://docs.caudate.me/hara/). Please see documentation for usage.

## Versions

#### 2.2.3
- new packages: `hara.io.scheduler`, `hara.event`, `hara.object`
- brand new website

#### 2.1.11
- bugfix for `hara.reflect`, added `hara.object` namespace

#### 2.1.10
- Fixed all reflection warnings

#### 2.1.8
- Reworked `hara.reflect` to use only functions, moved helper macros into vinyasa 

#### 2.1.5
- Fix for `hara.component` to work with none record-based components

#### 2.1.4

- Moved [iroh](http://github.com/zcaudate/iroh) to `im.chit/hara.reflect`
- Added initialisers for `hara.component`

## License

Copyright © 2015 Chris Zheng

Distributed under the MIT License
