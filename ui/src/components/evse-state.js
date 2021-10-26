import {getSimState, stopSim, plug, unplug, selectEvse, auth} from './../simulator.js'
import store from './../store.js'

//language=HTML
const template = `
    <div class="state">
        <div v-if="store.state.evse"
             v-for="evse in store.state.evse.evses"
             :key="evse.id">
            <div class="state-status">
                <div class="state-status-simulator" v-for="(simEvse, index) in store.state.simulator.evses"
                     :key="evse.id">
                    <div v-if="simEvse.selected" class="state-status-simulator__active">
                        <svg class="icon">
                            <use class="fill-neutral-1" href="#icon-evse"></use>
                        </svg>
                        <div><span v-if="evse.evseStatus == 'AVAILABLE'"> {{simEvse.id}} Available</span>
                            <span class="state-status-separator"> / </span>
                            <span v-if="evse.charging == false">Not charging</span></div>
                    </div>
                    <button class="state-status-simulator__inactive btn"
                            v-if="!simEvse.selected"
                            @click="selectEvse(index)">Select {{simEvse.id}}
                    </button>
                </div>
            </div>

            <ul class="state-control">
                <li class="state-connector"
                    v-for="connector in evse.connectors"
                    :key="connector.id">

                    <div class="state-connector-info">
                        <svg class="icon">
                            <use :class="{ connecting: isConnecting,  connected: connector.cableStatus === 'UNPLUGGED', connected: connector.cableStatus === 'PLUGGED'}"
                                 href="#icon-iec-62196-t1"></use>
                        </svg>
                        {{connector.id}} - {{connector.cableStatus}} - {{connector.connectorStatus}}
                    </div>


                    <div class="state-connector-control">
                        
                        <button class="btn"
                                :disabled="connector.cableStatus === 'PLUGGING' || connector.cableStatus === 'UNPLUGGING'"
                                @click="cable(evse.id, connector)">
                            <span v-show="connector.cableStatus === 'UNPLUGGED'">Plug cable</span>
                            <span v-show="connector.cableStatus === 'PLUGGED'">Unplug cable</span>
                            <span v-show="connector.cableStatus === 'PLUGGING'">Plugging..</span>
                            <span v-show="connector.cableStatus === 'UNPLUGGING'">Unplugging..</span>
                        </button>
                        
                        <div class="state-connector-control-start">
                            <input type="text"
                                   name="rfid"
                                   placeholder="RFID"
                                   v-model="connector.rfid">
                            <button class="btn"
                                    :disabled="!connector.rfid"
                                    @click="auth(connector.rfid, evse.id)">Start Charging
                            </button>
                        </div>
                    </div>

                </li>
            </ul>

            <button class="btn-link" @click="showRawState = !showRawState">Toggle raw state</button>
            <div v-if="showRawState">
                <pre> sim:{{store.state.simulator}}</pre>
                <pre> evse:{{store.state.evse}}</pre>
            </div>

        </div>
    </div>
`

const mock = model => {
    model.state.simulator.started = true
    model.state.evse = {
        "heartbeatInterval": 86400,
        "evConnectionTimeOut": 120,
        "evses": [
            {
                "id": 1,
                "connectors": [
                    {
                        "id": 1,
                        "cableStatus": "UNPLUGGED",
                        "connectorStatus": "Available"
                    },
                    {
                        "id": 2,
                        "cableStatus": "UNPLUGGED",
                        "connectorStatus": "Available"
                    }
                ],
                "tokenId": null,
                "charging": false,
                "seqNo": 0,
                "evseStatus": "AVAILABLE",
                "transaction": {
                    "transactionId": null,
                    "status": "NONE"
                },
                "scheduledNewEvseStatus": null,
                "totalConsumedWattHours": 0
            }
        ],
        "currentTime": "2021-09-22 08:19:54"
    }


    model.state.simulator.evses = [
        {
            "id": " EVB-X000001",
            "selected": true
        },
        {
            "id": " EVB-X000002",
            "selected": false
        }
    ]
}

const evseState = {
    name: 'Evse-state',
    data() {
        return {showRawState: false}
    },
    setup() {

        // Mock state simulator started
        // mock(store)

        function cable(evseId, connector) {

            if (connector.cableStatus === 'UNPLUGGED'){
                connector.cableStatus = 'PLUGGING'
                plug(evseId, connector.id)
            }

            if (connector.cableStatus === 'PLUGGED'){
                connector.cableStatus = 'UNPLUGGING'
                unplug(evseId, connector.id)
            }

        }

        return {store, getSimState, stopSim, cable, selectEvse, auth}
    },
    template: template
};

export default evseState
