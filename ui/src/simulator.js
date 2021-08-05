const path = require('path')
const {spawn} = require('child_process')
import store from './store.js'

let sim = undefined
let state = ''

function writeStateToStore(state) {
    const stateString = state.match(/START EVSE state([\s\S]*)END EVSE state/);
    console.log('stateString', stateString)

    const stationsString = state.match(/List of stations:([\s\S]*)Select another/);
    console.log('stationsString', stationsString)

    if (stationsString?.length) {
        store.state.simulator = stationsString[1]
    }

    if (stateString?.length) {
        store.state.evse = JSON.parse(stateString[1])
    }

}

function startSim(ws, configuration) {
    const command = './gradlew'
    const args = ['run', `-Parguments="${ws} --configuration ${configuration}"`]
    const dir = path.join(__dirname, '../')
    sim = spawn(command, args, {cwd: dir, shell: true});
    store.state.simulator = 'starting'

    function stdoutWatcher() {
        return setTimeout(() => {
            console.log('stdOutTimer', state)
            writeStateToStore(state)
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

function getSimState() {
    state = ''
    sim.stdin.write('stat\r\n');
}

function stopSim() {
    console.log('STOP')
    sim.kill('SIGINT');
    store.state.simulator = false
    store.state.evse = false
}

function plug(evse, connector){
    sim.stdin.write(`plug ${evse} ${connector}\r\n`);
    getSimState()
}

function unplug(evse, connector){
    sim.stdin.write(`unplug ${evse} ${connector}\r\n`);
    getSimState()
}


export {startSim, getSimState, stopSim , plug, unplug}








