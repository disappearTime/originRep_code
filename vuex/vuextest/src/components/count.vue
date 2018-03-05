<template>
  <div>
      <p>{{ msg }}</p>
      <hr>
      <p>{{ $store.state.count }}--{{ count }}</p>
      <p>
          <button @click="$store.commit('add',10)">+</button>
          <button @click="$store.commit('reduce')">-</button>
          <button @click="add(10)">加</button>
          <button @click="reduce()">减</button>
      </p>
      <p>
          <button @click="addActions()">addActions</button>
          <button @click="reduceActions()">reduceActions</button>
      </p>
      <p>{{ $store.state.a.count }}</p>
  </div>
</template>
<script>
    import store from "@/vuex/store";
    import { mapState , mapMutations , mapGetters,mapActions} from 'vuex'//注意带上花括号
    export default {
        data () {
            return {
                msg : "hello vuex"
            }
        },
        // computed: {
        //     count:function(){
        //         return this.$store.state.count
        //     }
        // },
        // 
        computed: {
            ...mapState(['count']),
            // count () {
            //     return this.$store.getters.count
            // }
            ...mapGetters(["count"])
        },
        methods:{
            ...mapMutations(['add','reduce']),
            ...mapActions(['addActions','reduceActions'])
        },
        store
    }
</script>

