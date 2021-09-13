import {getSimState, stopSim, plug, unplug, selectEvse, auth} from './../simulator.js'
import store from './../store.js'

//language=HTML
const template = `
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

const evseState = {
    name: 'Evse-state',
    setup() {

        return {store, getSimState, stopSim, plug, unplug, selectEvse, auth}
    },
    template: template
};

export default evseState
