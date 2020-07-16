src="./src"
bin="./bin"
output="./output"
main="app.Main"
classFileName="TestMethodStructure.class"

javac -sourcepath $src -d $bin $src/app/Main.java
java -classpath $bin $main
echo
echo
javap -v $output/$classFileName
