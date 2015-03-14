Mongo Explorer
==============

Cross-platform database management tool for MongoDB.

Requirements
------------

- Java runtime environment 6 or later
- 5 MB of disk space

Download
--------

[mongoexplorer-0.1.1.tar.gz](https://github.com/tutikka/MongoExplorer/raw/master/releases/mongoexplorer-0.1.1.tar.gz)
[mongoexplorer-0.1.0.tar.gz](https://github.com/tutikka/MongoExplorer/raw/master/releases/mongoexplorer-0.1.0.tar.gz)

Installation
------------

Note! Please replace version ``0.1.0`` below accordingly.

MacOS and Linux:

```
$ tar zfxv mongoexplorer-0.1.0.tar.gz
$ cd mongoexplorer-0.1.0
$ sh ./mongoexplorer.sh 
```

Windows:

1. Unzip mongoexplorer-0.1.0.zip (for example using WinZip, WinRar, etc.)
2. Go to the created ``mongoexplorer-0.1.0`` folder and double-click ``mongoexplorer.cmd`` 


Screenshots
-----------

Mongo Explorer running on Mac OS X.

![ScreenShot](/screenshots/ss_1.png)

Mongo Explorer running on Linux Mint.

![ScreenShot](/screenshots/ss_3.png)

Mongo Explorer running on Windows 8.1.

![ScreenShot](/screenshots/ss_2.png)

Building from Source
--------------------

Make sure you have ``git``, ``java`` and ``ant`` available, and set up in your path.

1. Clone the repository
2. In the project base directory, run the Ant build script
3. In the created dist directory, run one of the startup scripts

Example (MacOS, Unix, Linux)

```
$ git clone https://github.com/tutikka/MongoExplorer.git
$ cd MongoExplorer
$ ant
$ cd dist
$ sh ./mongo-explorer.sh &
```
