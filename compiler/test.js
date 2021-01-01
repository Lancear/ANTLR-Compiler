Array.prototype.last = function() { return this[this.length - 1]; };

const system = require('child_process');
const fs = require('fs');

testFolder('parser');
testFolder('symbolcheck');
testFolder('typecheck');
testFolder('codegen', true);

function testFolder(folder, doRun = false) {
  fs.readdirSync(`./testfiles/${folder}`).filter(f => f.endsWith('.yapl')).map(f => `${folder}/${f.replace('.yapl', '')}`).forEach(f => test(f, doRun));
}

function test(testfile, doRun = false) {
  console.log("[TESTING]", testfile)
  compile(testfile);

  if (doRun) {
    const result = run(testfile);
    const expected = fs.readFileSync(`./testfiles/${testfile}.true`, 'utf8').split('\r').join('');
    console.log("[RUN TEST RESULT]", (result === expected) ? "TRUE" : "FALSE");
  }

  console.log();
  console.log();
}

function compile(testfile) {
  try {
    const stdout = system.execSync(`cd ${__dirname} & ./scripts/run.sh ./testfiles/${testfile}.yapl ./output/${testfile}`).toString();
    console.log(stdout);
  }
  catch (err) {
    // stderr is logged automatically 
  }
}

function run(testfile) {
  try {
    const classFile = testfile.split('/').last();
    const className = classFile.charAt(0).toUpperCase() + classFile.substring(1);
    const stdout = system.execSync(`cd ${__dirname} & java -cp ./output/${testfile} ${className}`).toString();
    // console.log(stdout);
    return stdout;
  }
  catch (err) {
    // stderr is logged automatically 
    return err;
  }
}
