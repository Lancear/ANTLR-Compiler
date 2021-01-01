antlrJar="./lib/antlr.jar"

bin="./bin"
mainClass="compiler.Compiler"

java -classpath $antlrJar:$bin $mainClass "$@"
