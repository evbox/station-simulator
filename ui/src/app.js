import * as Vue from './../dependencies/vue.esm-browser.prod.js';
import {startSim, getSimState, stopSim, plug, unplug, selectEvse, auth} from './simulator.js'
import store from './store.js'

//language=HTML
const Simulator = {
    //language=HTML
    template: `
        <div>
            component
        </div>
    `
};

//language=HTML
const template = `
    <div v-if="!store.state.simulator.started">
        <label for="wsUrl">Target</label>
        <select v-model="store.state.config.ws" id="wsUrl" name="wsUrl">
            <option v-for="(value, name) in store.state.config.wsOptions" :key="name" :value="value">
                {{name}}
            </option>
        </select>
        <p> WebSocket:{{store.state.config.ws}}</p>
    </div>

    <div>
        <textarea name="" id="" cols="30" rows="10"
                  v-model="store.configuration"></textarea>
    </div>

    <div v-if="store.state.config.ws.includes('test')">
        <label for="adhoc">adHoc</label>
        <input type="text"
               name="adhoc"
               v-model="adhoc">
    </div>

    <button @click="start(store.state.config.ws, adhoc)" v-if="!store.state.simulator.started">Start</button>
    <div v-if="store.state.simulator.started">
        <button @click="getSimState()">Update state</button>
        <button @click="stopSim()">Stop</button>
    </div>

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
                    
                    <div>
                        <label for="rfid">RFID</label>
                        <input type="text"
                               name="rfid"
                               v-model="rfid">
                        <button @click="auth(rfid, evse.id)">Auth</button>
                    </div>
                    
                </li>
            </ul>
        </li>
    </ul>

    <p>Evses</p>
    <ul v-if="store.state.simulator.evses">
        <li v-for="(evse, index) in store.state.simulator.evses" :key="evse.id">
            <span>{{evse.id}}</span>
            <span v-if="evse.selected">Selected</span>
            <button v-if="!evse.selected" @click="selectEvse(index)">Select</button>
        </li>
    </ul>

    <pre> sim:{{store.state.simulator}}</pre>
    <pre> evse:{{store.state.evse}}</pre>
`

const app = {
    name: 'App',
    components: {},

    setup() {
        const {onMounted} = Vue;

        // onMounted(() => {
        //     console.log('mounted')
        // })


        function start(ws, adhoc) {
            // https://myevbox.atlassian.net/wiki/spaces/EV/pages/2053931047/Configure+station
            if (ws.includes('${environment}')) {
                ws = ws.replace('${environment}', adhoc)
            }

            // the simulator only accepts single quoted JSON ¯\_(ツ)_/¯
            startSim(ws, JSON.stringify(JSON.parse(store.configuration)).replaceAll('\"', '\''))
            console.log('Connecting to:', ws)
        }

        store.state.config.ws = store.state.config.wsOptions['production']
        return {start, store, getSimState, stopSim, plug, unplug, selectEvse, auth}
    },
    template: template
};


const {createApp} = Vue;
createApp(app).mount('#app');
