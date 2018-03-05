import Vue from 'vue';//引入vue
import Vuex from 'vuex';//引入vuex
Vue.use(Vuex);
const state = {
    count:1
}
const mutations={//修改状态
    add (state,n) {
        state.count+=n
    },
    reduce (state){
        state.count--
    }
}
const getters = {//过滤计算器
    count:function(state){
        return state.count+=100
    }
}
const actions = {//异步修改数据
    addActions (context) {//context是上下文的意思，这里是store
        context.commit("add",10);
        setTimeout(function(){
            context.commit("reduce")
        },3000)
        console.log("我比reduce先执行了")
    },
    reduceActions (context){
        context.commit("reduce")
    }
}
const moduleA = {//模块组，如果不是项目特别大，由好几个前端开发，不建议使用模块组
    state,mutations,getters,actions
}
export default new Vuex.Store({
    // state,
    // mutations,
    // getters,
    // actions
    modules:{
        a:moduleA
    }
})