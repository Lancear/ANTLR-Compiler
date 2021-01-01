./scripts/clean.sh
./scripts/generate-parser.sh

echo Building...

antlrJar="./lib/antlr.jar"

src="./src"
bin="./bin"
mainFile="compiler/Compiler.java"

javac -sourcepath $src -classpath $antlrJar -d $bin $src/$mainFile

echo Build completed successfully!
