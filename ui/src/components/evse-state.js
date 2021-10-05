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
                    <button class="state-status-simulator__inactive"
                            v-if="!simEvse.selected"
                            @click="selectEvse(index)">Select {{simEvse.id}}
                    </button>
                </div>
            </div>


            <h1>Connectors</h1>
            <ul>
                <li v-for="connector in evse.connectors" :key="connector.id">
                    {{connector.id}} - {{connector.cableStatus}} - {{connector.connectorStatus}}
                    <button @click="plug(evse.id, connector.id)" v-if="connector.cableStatus === 'UNPLUGGED'">Plug
                        cable
                    </button>
                    <button @click="unplug(evse.id, connector.id)" v-if="connector.cableStatus !== 'UNPLUGGED'">
                        Unplug
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

        </div>

        <!--        <p>Evses</p>-->
        <!--        <ul v-if="store.state.simulator.evses">-->
        <!--            <li v-for="(evse, index) in store.state.simulator.evses" :key="evse.id">-->
        <!--                <span>{{evse.id}}</span>-->
        <!--                <span v-if="evse.selected">Selected</span>-->
        <!--                <button v-if="!evse.selected" @click="selectEvse(index)">Select</button>-->
        <!--            </li>-->
        <!--        </ul>-->

        <pre> sim:{{store.state.simulator}}</pre>
        <pre> evse:{{store.state.evse}}</pre>
    </div>

`

const evseState = {
    name: 'Evse-state',
    setup() {

        // store.state.evse = {
        //     "heartbeatInterval": 86400,
        //     "evConnectionTimeOut": 120,
        //     "evses": [
        //         {
        //             "id": 1,
        //             "connectors": [
        //                 {
        //                     "id": 1,
        //                     "cableStatus": "UNPLUGGED",
        //                     "connectorStatus": "Available"
        //                 },
        //                 {
        //                     "id": 2,
        //                     "cableStatus": "UNPLUGGED",
        //                     "connectorStatus": "Available"
        //                 }
        //             ],
        //             "tokenId": null,
        //             "charging": false,
        //             "seqNo": 0,
        //             "evseStatus": "AVAILABLE",
        //             "transaction": {
        //                 "transactionId": null,
        //                 "status": "NONE"
        //             },
        //             "scheduledNewEvseStatus": null,
        //             "totalConsumedWattHours": 0
        //         }
        //     ],
        //     "currentTime": "2021-09-22 08:19:54"
        // }
        //
        //
        // store.state.simulator.evses = [
        //     {
        //         "id": " EVB-X000001",
        //         "selected": true
        //     },
        //     {
        //         "id": " EVB-X000002",
        //         "selected": false
        //     }
        // ]

        return {store, getSimState, stopSim, plug, unplug, selectEvse, auth}
    },
    template: template
};

export default evseState
