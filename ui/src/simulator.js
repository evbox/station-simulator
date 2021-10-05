const path = require('path')
const {spawn} = require('child_process')
import store from './store.js'

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

// $ ./gradlew run -Parguments="ws://everon.io/ocpp --configuration {'stations':[{'id':'EVB-P17390866','evse-state.js':{'count':1,'connectors':1}}]}"

// ./gradlew run -Parguments="ws://ocpp.test.everon.io/ad3136a1/ocpp --configuration {'stations':[{'id':'EVB-P17390866','evse':{'count':1,'connectors':1}}]}"


function startSim(ws, configuration) {
    console.log('dir',__dirname)
    const command = '../dependencies/jre/bin/java -jar simulator.jar'
    const args = [`${ws} --configuration "${configuration}"`]
    const dir = path.join(__dirname, '../dependencies')
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
    console.log('Starting transaction', tokenId, evseIndex)
    state = ''
    sim.stdin.write(`auth ${tokenId} ${evseIndex}\r\n`);
}

export {startSim, getSimState, stopSim, plug, unplug, selectEvse, auth}








