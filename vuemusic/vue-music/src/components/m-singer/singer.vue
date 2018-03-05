<template>
  <div class="singer">
      <list-view @select="selectSinger" :data='singer'></list-view>
      <router-view></router-view>
  </div>
</template>
<script>
    import { getSinger } from '@/api/getSinger.js'
    import { ERR_OK } from '@/api/config.js'
    import Singer from '@/common/js/singer.js'
    import ListView from '@/base/listview/listview.vue'
    import store from '@/vuex/store.js'//注意注册一下store 否则可能报commit not defined错误
    import { mapMutations } from 'vuex'
    const HOT_NAME = '热门';
    const HOT_SINGER_LEN = 10
    export default {
      data () {
        return {
          singer: []
        }
      },
      created () {
        setTimeout(()=>{
          this.getSingerList();
        },20)
      },
      methods: {
        getSingerList(){//获取歌手列表
          let that = this;
          getSinger().then(function(res){
            //console.log(res)
            if(res.code === ERR_OK){
              //that.singer = res.data.list;
              //console.log(that.singer)
              let singerData = that.handlerSinger(res.data.list);
              that.singer = singerData;
              console.log(singerData)
            }
          })
        },
        handlerSinger(list){//格式化歌手数据
          let map = {
            hot: {
              title: HOT_NAME,
              items: []
            }
          }
          list.forEach((item,index) => {
            if( index < HOT_SINGER_LEN ){
              map.hot.items.push( new Singer({ 
                id: item.Fsinger_mid,
                name: item.Fsinger_name,
              }) )
            }
            let key = item.Findex;
            if(!map[key]){
              map[key] = {
                title: key,
                items:[]
              }
            }
            map[key].items.push( new Singer({ 
                id: item.Fsinger_mid,
                name: item.Fsinger_name,
              }) )
          });
          //处理map 
          let hot = [];
          let ret = [];
          for( let key in map){
            let val = map[key];
            if( val.title.match(/[a-zA-Z]/) ){
              ret.push(val)
            }else if(val.title == HOT_NAME){
              hot.push(val);
            }
          }
          ret.sort((a,b)=>{
            return a.title.charCodeAt(0) - b.title.charCodeAt(0)
          })
          return hot.concat(ret);
        },
        selectSinger(singer){
          //console.log(singer);
          this.$router.push({
            path:`/singer/${singer.id}`
          })
          this.setsinger(singer);
        },
        ...mapMutations({
          setsinger: 'setsinger'
        }),//映射，相当于这个实例下有了setsinger方法
      },
      components: {
        ListView
      },
      store
    }
</script>
<style scoped>
  .singer{
    width: 100%;
    position: absolute;
    top: 85px;
    bottom: 0;
  }
</style>
