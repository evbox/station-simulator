const {contextBridge, ipcMain, ipcRenderer} = require('electron')
const path = require('path')
const {spawn} = require('child_process');

let sim = undefined
let state = ''


contextBridge.exposeInMainWorld(
    'api', {
        start: (data) => {
            console.log('START', data)
            startSim()
        },
        stop: (data) => {
            console.log('STOP', data)
            sim.kill('SIGINT');
        },
        state: () => {
            console.log('state')
            state = ''
            sim.stdin.write('stat\r\n');
        },
        onStateData:(cb) => {
            ipcRenderer.on('state-updated', (event, customData) => cb(customData));
        },
        receiveState: (state) => {
            console.log('Got state',state)
        }
    }
);

function startSim() {
    const command = './gradlew'
    const args = ['run', '-Parguments="ws://everon.io/ocpp --configurationFile ./../configuration.yml"']
    const dir = path.join(__dirname, '../../')
    console.log(dir)
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
        writeStateToView('Stopped')
    });
}


function writeStateToView(state){
    const element = document.getElementById('state-data')
    if (element) element.innerText = state
}


window.addEventListener('DOMContentLoaded', () => {
    const replaceText = (selector, text) => {
        const element = document.getElementById(selector)
        if (element) element.innerText = text
    }

    for (const dependency of ['chrome', 'node', 'electron']) {
        replaceText(`${dependency}-version`, process.versions[dependency])
    }
})








