import store from './../store.js'

//language=HTML
const template = `
    <div class="config">
        
        <div class="tip">
            <svg class="icon">
                <use class="fill-neutral-1" href="#icon-info"></use>
            </svg>
            <span v-if="store.state.simulator.started === false">Check the config and then click the flash to start</span>
            <span v-if="store.state.simulator.started  === 'starting'">Simulator starting connected to {{store.state.config.ws}}</span>
            <span v-if="store.state.simulator.started === 'started'">Simulator running connected to {{store.state.config.ws}}</span>
        </div>
        
        <div class="config-ws" v-if="store.state.simulator.started === false">
            <label for="wsUrl">
                <svg class="icon">
                    <use class="fill-neutral-5" href="#icon-upload"></use>
                </svg>
            </label>
            <select v-model="store.state.config.ws" id="wsUrl" name="wsUrl">
                <option v-for="(value, name) in store.state.config.wsOptions" :key="name" :value="value">
                    {{name}}
                </option>
            </select>
            <svg class="icon">
                <use class="fill-neutral-5" href="#icon-right"></use>
            </svg>

            <span v-if="!adhoc.url"> {{store.state.config.ws}}</span>
            <span v-if="adhoc.url"> {{adhoc.url}}
                <input type="text" v-model="store.state.config.adhoc" placeholder="adxxxxx" style="width: 5em">
                {{adhoc.path}}
            </span>

        </div>
        <div class="config-simulator" v-if="store.state.simulator.started === false">
            <label for="configuration">JSON configuration</label>
            <textarea name=configuration""
                      id=""
                      v-model="store.configuration"></textarea>
        </div>
    </div>
    </div>

`

const evseConfig = {
    name: 'Evse-config',
    setup() {
        return {store}
    },
    computed: {
        adhoc() {
            const adhocPlaceholder = '${adhoc}'
            const adhoc = {
                url: null,
                id: null,
                path: null
            }

            if (store.state.config.ws.includes(adhocPlaceholder)) {
                const wsParts = store.state.config.ws.split(adhocPlaceholder)
                adhoc.url = wsParts[0]
                adhoc.path = wsParts[1]
            }

            return adhoc;
        }
    },
    template: template
};

export default evseConfig
