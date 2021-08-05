import * as Vue from './../dependencies/vue.esm-browser.prod.js';
import {startSim, getSimState, stopSim, plug, unplug} from './simulator.js'
import store from './store.js'

//language=HTML
const Simulator = {
    template: `
        <div>
            component
        </div>
    `
};

//language=HTML
const template = `
    <div  v-if="!store.state.simulator">
        <label for="wsUrl">Target</label>
        <select v-model="store.state.config.ws" id="wsUrl" name="wsUrl">
            <option v-for="(value, name) in store.state.config.wsOptions" :key="name" :value="value">
                {{name}}
            </option>
        </select>
        <p> WebSocket:{{store.state.config.ws}}</p>
    </div>

    <div v-if="store.state.config.ws.includes('test')">
        <label for="adhoc">adHoc</label>
        <input type="text"
               name="adhoc"
               v-model="environment">
    </div>


   


    <button @click="start(store.state.config.ws, environment)" v-if="!store.state.simulator">Start</button>
    <button @click="getSimState()" v-if="store.state.simulator">State</button>
    <button @click="stopSim()" v-if="store.state.simulator">Stop</button>

    <ul v-if="store.state.evse">
        <li v-for="evse in store.state.evse.evses" :key="evse.id">
            status:{{evse.evseStatus}}
            charging:{{evse.charging}}

            <h1>Connectors</h1>
            <ul>
                <li v-for="connector in evse.connectors" :key="connector.id">
                    {{connector.id}} - {{connector.cableStatus}} - {{connector.connectorStatus}}
                    <button @click="plug(evse.id, connector.id)" v-if="connector.cableStatus === 'UNPLUGGED'">Plug
                        cable
                    </button>
                    <button @click="unplug(evse.id, connector.id)" v-if="connector.cableStatus !== 'UNPLUGGED'">Unplug
                        cable
                    </button>
                </li>
            </ul>
        </li>
    </ul>

    <pre> {{store.state.simulator}}</pre>
    <pre> {{store.state.evse}}</pre>
`

const app = {
    name: 'App',
    components: {Simulator},

    setup() {
        const {onMounted} = Vue;

        // onMounted(() => {
        //     console.log('mounted')
        // })

        function start(ws, adhoc) {
            if (ws.includes('${environment}')) {
                ws = ws.replace('${environment}', adhoc)
            }

            startSim(ws)
        }

        store.state.config.ws = store.state.config.wsOptions['production']



        return {start, store, getSimState, stopSim, plug, unplug}
    },
    template: template
};


const {createApp} = Vue;
createApp(app).mount('#app');
