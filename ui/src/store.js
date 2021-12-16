import {reactive} from './../dependencies/vue.esm-browser.prod.js';
import configuration from './configuration.js'

export default reactive({
    configuration: JSON.stringify(configuration, null, 2),
    state: {
        simulator: {
            started: false,
            evses: null
        },
        evse: null,
        config: {
            ws: '',
            cliConfiguration: '',
            adhoc:'',
            wsOptions: {
                production: 'ws://everon.io/ocpp',
                test: 'ws://ocpp-legacy.test.everon.io/${adhoc}/ocpp',
                staging: 'ws://ocpp.staging.everon.io/ocpp'
            }
        }
    }
})
