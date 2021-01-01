echo Generating parser...

antlrJar="./lib/antlr.jar"
antlrMainclass="org.antlr.v4.Tool"

grammarFile="./src/parser/Yapl.g4"
targetPackage="parser"
targetDir="./src/$targetPackage"

# generate parser
java -cp $antlrJar $antlrMainclass $grammarFile -package $targetPackage -listener -visitor

# cleanup
rm -rf $targetDir/*.tokens
rm -rf $targetDir/*.interp

echo Parser generated successfully!
