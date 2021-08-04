const path = require('path')
const {spawn} = require('child_process')

const startBtn = document.getElementById('start');
const stopBtn = document.getElementById('stop');
const stateBtn = document.getElementById('state');
const stateContainer = document.getElementById('state-data')
const stationsContainer = document.getElementById('stations-data')

let sim = undefined
let state = ''


startBtn.addEventListener('click', (e) => {
    e.preventDefault();
    startSim()
});

stopBtn.addEventListener('click', (e) => {
    e.preventDefault();
    console.log('STOP', data)
    sim.kill('SIGINT');
    stateContainer.innerText = 'Stopped'
});

stateBtn.addEventListener('click', (e) => {
    e.preventDefault();
    state = ''
    sim.stdin.write('stat\r\n');
});

function writeStateToView(state){
    const stateString = state.match(/START EVSE state([\s\S]*)END EVSE state/);
    console.log('stateString',stateString)

    const stationsString = state.match(/List of stations:([\s\S]*)Select another/);
    console.log('stationsString',stationsString)

    if (stationsString?.length){
        stationsContainer.innerText = stationsString[1]
    }

    if (stateString?.length){
        const stateObject = JSON.parse(stateString[1])
        stateContainer.innerText = JSON.stringify(stateObject, null, '\t')
    }

}

function startSim() {
    const command = './gradlew'
    const args = ['run', '-Parguments="ws://everon.io/ocpp --configurationFile ./../configuration.yml"']
    const dir = path.join(__dirname, '../')

    sim = spawn(command, args, {cwd: dir, shell: true});

    function stdoutWatcher(){
        return setTimeout(() => {
            console.log('stdOutTimer',state)
            writeStateToView(state)
        }, 5000);
    }

    let stdOutTimer = stdoutWatcher()

    sim.stdout.on('data', (data) => {
        clearTimeout(stdOutTimer)
        const dataString = data.toString()
        state += dataString
        stdOutTimer = stdoutWatcher()
    });

    sim.stdout.on('end', (data) => {
        console.log(`stdoutEnded!!!!!!!!!!!`);
    });

    sim.stderr.on('data', (data) => {
        console.error(`stderr: ${data}`);
    });

    sim.on('close', (code) => {
        console.log(`child process exited with code ${code}`);
        // writeStateToView('Stopped')
    });
}












