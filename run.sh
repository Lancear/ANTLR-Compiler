src="./src"
bin="./bin"
output="./output"
main="app.Main"
outputClass="Main"

javac -sourcepath $src -d $bin $src/app/Main.java
java -classpath $bin $main
echo
echo
javap -classpath $output -v $outputClass
echo
echo
echo --- Executing ---------------
java -classpath $output $outputClass
