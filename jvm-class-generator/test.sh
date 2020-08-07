src="./src"
bin="./bin"

testRunner="jvm_class_generator.tests.TestRunner"
testRunnerPath="$src/jvm_class_generator/tests/TestRunner.java"

javac -sourcepath $src -d $bin $testRunnerPath
java -classpath $bin $testRunner
