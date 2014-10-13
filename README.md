MongoExplorer
=============

Cross-platform database management tool for MongoDB

Building from Source
--------------------

Make sure you have git, java and ant available, and set up in your path.

1. Clone the repository
2. In the project base directory, run the Ant build script
3. In the created dist directory, run one of the startup scripts

Example (MacOS, Unix, Linux)

```
git clone https://github.com/tutikka/MongoExplorer.git
cd MongoExplorer
ant build.xml
cd dist
sh ./mongo-explorer.sh &
```