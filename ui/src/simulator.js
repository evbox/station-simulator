const path = require('path')
const {spawn} = require('child_process')
import store from './store.js'


// java -jar packr-all-4.0.0.jar --platform mac --jdk OpenJDK11U-jre_x64_mac_openj9_11.0.11_9_openj9-0.26.0.tar.gz --classpath dependencies/simulator.jar --output out-mac --executable simulator --mainclass com.evbox.everon.ocpp.simulator
// https://github.com/AdoptOpenJDK/openjdk11-binaries/releases?after=jdk11u-2019-01-22-14-45
// https://github.com/libgdx/packr/releases

// issue https://github.com/eclipse-openj9/openj9/issues/3795



let sim = undefined
let state = ''

function writeStateToStore(state) {
    const stateString = state.match(/START EVSE state([\s\S]*)END EVSE state/);
    console.log('stateString', stateString)

    const stationsString = state.match(/List of stations:([\s\S]*?)Select another/);
    console.log('stationsString', stationsString)

    const pluggedString = state.match(/evse([\s\S]*?)plugged/);
    console.log('pluggedString', pluggedString)

    const unpluggedString = state.match(/evse([\s\S]*?)unplugged/);
    console.log('unpluggedString', unpluggedString)

    if (stationsString?.length > 0) {
        store.state.simulator.evses = stationsString[1].trim().split('\n').map(evse => {
            console.log('evseLine', evse)
            const evseModel = evse.split(':')
            return {
                id: evseModel[1],
                selected: evseModel[0].includes('SELECTED')
            }
        })


        if(store.state.simulator.started !== 'started') {
            store.state.simulator.started = 'started'
            getSimState()
        }

    }

    if (stateString?.length) {
        store.state.evse = JSON.parse(stateString[1])
    }

    if (pluggedString || unpluggedString){
        getSimState()
    }

}

function startSim(ws, configuration) {
    console.log('dir',__dirname)
    const command = './simulator'
    const args = [`${ws} --configuration "${configuration}"`]
    const dir = path.join(__dirname, '../out-mac/Contents/MacOS')
    sim = spawn(command, args, {cwd: dir, shell: true});
    store.state.simulator.started = 'starting'

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
    store.state.simulator.started = false
    store.state.simulator.evses = null
    store.state.evse = null
}

function plug(evse, connector) {
    console.log('Pluging', evse, connector)
    state = ''
    sim.stdin.write(`plug ${evse} ${connector}\r\n`);
}

function unplug(evse, connector) {
    console.log('Unpluging', evse, connector)
    state = ''
    sim.stdin.write(`unplug ${evse} ${connector}\r\n`);
}

function selectEvse(evseIndex) {
    state = ''
    sim.stdin.write(`${evseIndex}\r\n`);
    getSimState()
}

function auth(tokenId, evseIndex) {
    state = ''
    sim.stdin.write(`auth ${tokenId} ${evseIndex}\r\n`);
}

export {startSim, getSimState, stopSim, plug, unplug, selectEvse, auth}








