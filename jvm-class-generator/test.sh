src="./src"
bin="./bin"

testRunner="tests.TestRunner"
testRunnerPath="$src/tests/TestRunner.java"

javac -sourcepath $src -d $bin $testRunnerPath
java -classpath $bin $testRunner
