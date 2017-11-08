import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-default/index.css'
import VueResource from 'vue-resource'



Vue.config.productionTip = true

Vue.use(VueResource);
Vue.use(ElementUI)

var vm = new Vue({
    el: '#app',
    router: router,
    components: {App}
})
