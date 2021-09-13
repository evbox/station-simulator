import {startSim, getSimState, stopSim, plug, unplug, selectEvse, auth} from './../simulator.js'
import store from './../store.js'

//language=HTML
const template = `
    <div v-if="!store.state.simulator.started">
        <label for="wsUrl">Target</label>
        <select v-model="store.state.config.ws" id="wsUrl" name="wsUrl">
            <option v-for="(value, name) in store.state.config.wsOptions" :key="name" :value="value">
                {{name}}
            </option>
        </select>
        <div v-if="store.state.config.ws.includes('test')">
            <label for="adhoc">adHoc</label>
            <input type="text"
                   name="adhoc"
                   v-model="adhoc">
        </div>
        <p> WebSocket:{{store.state.config.ws}}</p>
        <div>
            <label for="configuration">Configuration</label>
            <textarea name=configuration"" id="" cols="30" rows="10"
                      v-model="store.configuration"></textarea>
        </div>
    </div>

    <button @click="start(store.state.config.ws, adhoc)" v-if="!store.state.simulator.started">Start</button>
    <div v-if="store.state.simulator.started">
        <button @click="getSimState()">Update state</button>
        <button @click="stopSim()">Stop</button>
    </div>
`

const evseConfig= {
    name: 'Evse-config',
    setup() {
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

export default evseConfig
