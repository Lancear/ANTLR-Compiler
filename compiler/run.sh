antlrPath="./lib/antlr-4.8-complete.jar"
antlrMain="org.antlr.v4.Tool"
package="parser"
packagePath="./src/parser"

java -cp $antlrPath $antlrMain $packagePath/Yapl.g4 -package $package -listener -visitor
rm $packagePath/*.tokens
rm $packagePath/*.interp

src="./src"
bin="./bin"
main="app.Main"
mainPath="app/Main"

javac -sourcepath $src -classpath $antlrPath -d $bin $src/$mainPath.java
java -classpath $antlrPath:$bin $main ./testfiles/quicksort.yapl ./output/quicksort

java -classpath ./output/quicksort Quicksort
