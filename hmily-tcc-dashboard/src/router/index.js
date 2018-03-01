import Vue from 'vue'
import Router from 'vue-router'
const explain = r => require.ensure([], () => r(require('../page/explain')), 'explain')
const adminSet = r => require.ensure([], () => r(require('../page/adminSet')), 'adminSet')
const home = r => require.ensure([], () => r(require('../page/home')), 'home')
const login = r => require.ensure([], () => r(require('../page/login')), 'login')
const manage = r => require.ensure([], () => r(require('../page/manage')), 'manage')
const transactionRecoveryInfo = r => require.ensure([], () => r(require('../page/transactionRecoveryInfo')), 'transactionRecoveryInfo')

Vue.use(Router)

export default new Router({
    routes: [
        {
            path: '/',
            component: login
        },
        {
            path: '/manage',
            component: manage,
            name: '',
            children: [{
                path: '',
                component: home,
                meta: [],
            }, {
                path: '/transactionRecoveryInfo',
                component: transactionRecoveryInfo,
                meta: ['事务补偿管理', '事务补偿信息列表'],
            }, {
                path: '/adminSet',
                component: adminSet,
                meta: ['设置', '管理员设置'],
            },  {
                path: '/explain',
                component: explain,
                meta: ['说明', '说明'],
            }]
        }
    ]
})
