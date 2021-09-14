import {getSimState, startSim, stopSim} from "./../simulator.js";
import store from "./../store.js";

//language=HTML
const template = `
    <header>
        <h1>EVSE Simulator UI</h1>
        <div>
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

    </header>
`

const evseHeader = {
    name: 'Evse-header',
    setup() {
        function start(ws, adhoc) {
            // https://myevbox.atlassian.net/wiki/spaces/EV/pages/2053931047/Configure+station
            if (ws.includes('${adhoc}')) {
                ws = ws.replace('${adhoc}',  adhoc)
            }

            // the simulator only accepts single quoted JSON ¯\_(ツ)_/¯
            startSim(ws, JSON.stringify(JSON.parse(store.configuration)).replaceAll('\"', '\''))
            console.log('Connecting to:', ws, adhoc)
        }

        store.state.config.ws = store.state.config.wsOptions['production']
        return {start, store, getSimState, stopSim}
    },
    template: template
};

export default evseHeader
