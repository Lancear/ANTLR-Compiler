src="./src"
bin="./bin"
output="./output"
main="impl.Backend"
mainPath="impl/Backend"
outputClass="TestA"

javac -sourcepath $src -d $bin $src/$mainPath.java
java -classpath $bin $main
echo
echo
javap -classpath $output -v $outputClass
echo
echo
echo --- Executing ---------------
java -classpath $output $outputClass
