import { reactive } from './../dependencies/vue.esm-browser.prod.js';

export default reactive({
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
