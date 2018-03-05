import Vue from 'vue'
import Vuex from 'vuex'
Vue.use(Vuex)
const state = {
    singerDetail:{}
}
const getters = {
    getsinger:function(state){
        return state.singerDetail;
    }
}
const mutations = {
    setsinger(state,singer){
        state.singerDetail = singer
    }
}
const actions = {
    
}
export default new Vuex.Store({
    state,
    mutations,
    getters,
    actions
})