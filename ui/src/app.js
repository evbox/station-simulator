import * as Vue from './../dependencies/vue.esm-browser.prod.js';
import evseConfig from './components/evse-config.js'
import evseState from './components/evse-state.js'

//language=HTML
const template = `
    <evseConfig></evseConfig>
    <evseState></evseState>
`

const app = {
    name: 'App',
    components: {
        evseConfig,
        evseState
    },

    setup() {
        const {onMounted} = Vue;

        // onMounted(() => {
        //     console.log('mounted')
        // })
    },
    template: template
};


const {createApp} = Vue;
createApp(app).mount('#app');
