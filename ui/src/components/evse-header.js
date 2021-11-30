import {getSimState, startSim, stopSim} from './../simulator.js';
import store from './../store.js';
import packageJson from '../package.json' assert {type: 'json'};

//language=HTML
const template = `
    <header class="app-header">
        <div class="title">
            <h1>EVSE Simulator UI
                <small>v{{version}}</small>
            </h1>

            <div class="controls">
                <button @click="start(store.state.config.ws, store.state.config.adhoc)"
                        v-if="!store.state.simulator.started"
                        type="button"
                        class="btn-icon"
                        title="Start"
                >
                    <svg class="icon">
                        <use class="fill-neutral-1" href="#icon-energy"></use>
                    </svg>
                </button>

                <button @click="getSimState()" v-if="store.state.simulator.started"
                        type="button"
                        class="btn-icon"
                        title="Update state"
                >
                    <svg class="icon">
                        <use class="fill-neutral-1" href="#icon-sync"></use>
                    </svg>
                </button>

                <button @click="stopSim()" v-if="store.state.simulator.started"
                        type="button"
                        class="btn-icon"
                        title="Stop"
                >
                    <svg class="icon">
                        <use class="fill-neutral-1" href="#icon-energy-disabled"></use>
                    </svg>
                </button>
            </div>
        </div>

        <div class="tip">
            <svg class="icon">
                <use class="fill-neutral-1" href="#icon-info"></use>
            </svg>
            <span v-if="store.state.simulator.started === false">Check the config and then click the flash to start</span>
            <span v-if="store.state.simulator.started  === 'starting'">Simulator starting, connecting to {{adhoc || store.state.config.ws}}</span>
            <span v-if="store.state.simulator.started === 'started'">Simulator started, connected to {{ adhoc || store.state.config.ws}}</span>
        </div>
    </header>
`

const evseHeader = {
    name: 'Evse-header',
    setup() {


        function start(ws, adhoc) {
            // https://myevbox.atlassian.net/wiki/spaces/EV/pages/2053931047/Configure+station
            if (ws.includes('${adhoc}')) {
                console.log(ws)
                store.state.config.ws = ws.replace('${adhoc}', adhoc)
            }

            // the simulator only accepts single quoted JSON ¯\_(ツ)_/¯
            startSim(ws, JSON.stringify(JSON.parse(store.configuration)).replaceAll('\"', '\''))
            console.log('Connecting to:', ws, adhoc, store.state.config.ws)
        }

        const version = packageJson.version

        store.state.config.ws = store.state.config.wsOptions['production']
        return {start, store, getSimState, stopSim, version}
    },
    template: template
};

export default evseHeader
