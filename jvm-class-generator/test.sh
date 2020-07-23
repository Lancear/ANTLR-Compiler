src="./src"
bin="./bin"

tester="tests.Tester"
testerPath="$src/tests/Tester.java"

javac -sourcepath $src -d $bin $testerPath
java -classpath $bin $tester
