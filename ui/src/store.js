import { reactive } from './../dependencies/vue.esm-browser.prod.js';
import configuration from './configuration.js'

export default reactive({
    configuration: JSON.stringify(configuration, null, 2),
    state: {
        simulator: null,
        evse: null,
        config: {
            ws: '',
            cliConfiguration:'',
            wsOptions: {
                production: 'ws://everon.io/ocpp',
                test:'ws://ocpp.test.everon.io/${environment}/ocpp',
                staging:'ws://ocpp.staging.everon.io/ocpp'
            }
        }
    }
})
