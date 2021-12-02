import * as Vue from './../dependencies/vue.esm-browser.prod.js';
import store from './store.js'
import evseConfig from './components/evse-config.js'
import evseState from './components/evse-state.js'
import evseHeader from './components/evse-header.js'

//language=HTML
const template = `
    <evseHeader></evseHeader>
    <evseConfig></evseConfig>
    <evseState></evseState>
`

const app = {
    name: 'App',
    components: {
        evseConfig,
        evseState,
        evseHeader
    },
    setup() {
        return {store}
    },
    template: template
};


const {createApp} = Vue;
createApp(app).mount('#app');
