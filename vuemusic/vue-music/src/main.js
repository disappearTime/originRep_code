import 'babel-polyfill' // 引入babel-polyfill依赖 es6编译补丁
import Vue from 'vue'
import App from './App'
import router from './router'
import fastclick from 'fastclick' // 解决移动端300毫秒延迟,当fastclick与click冲突时，在需要点击的元素上加needsclick类名
import Lazyload from 'vue-lazyload'
import Store from '@/vuex/store'
fastclick.attach(document.body)

Vue.config.productionTip = false
Vue.use(Lazyload,{
  loading:require('@/components/m-header/logo@2x.png'),
  error: require('@/components/m-header/logo@2x.png')
});
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  Store,
  render: h => h(App)
})
