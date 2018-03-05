<template>
    <transition name="slide">
        <music-list v-if="songs.length>0" :songs="songs"></music-list>
    </transition>
    
</template>
<script>
import {mapGetters} from 'vuex'
import { getsongdetail } from '@/api/getSinger.js'
import { ERR_OK } from '@/api/config.js'
import { createSong } from '@/common/js/song.js'
import musicList from '@/components/m-musiclist/musiclist.vue'
export default {
    data(){
        return {
            songs: []
        }
    },
    computed: {
        ...mapGetters(['getsinger'])//相当于这个构造器里多了getsinger属性
    },
    created () {
        console.log(this.getsinger)
    },
    mounted () {
        this.$nextTick(() => {
            this._getsongdetail();
        })
    },
    methods: {
        _getsongdetail(){
            let me = this;
            if(!this.getsinger.id){
                this.$router.push({
                    path: '/singer'
                })
            }else{
                getsongdetail(this.getsinger.id).then(function(res){
                    //console.log(res);
                    if(res.code == ERR_OK){
                        //console.log(res.data.list)
                        me.songs = me.handlersongdetail(res.data.list);
                        console.log(me.songs);
                    }
                })
            }
        },
        handlersongdetail(list){//处理歌手详情数据
            let ret = []
            list.forEach((item) => {
                let {musicData} = item;
                if(musicData.songid && musicData.albummid){
                    ret.push(createSong(musicData))
                }
            });
            return ret;
        }
    },
    components: {
        musicList
    }
}
</script>
<style scoped>
    .slide-enter-active, .slide-leave-active{
        transition: all 0.3s;
    }
    .slide-enter, .slide-leave-active{
        transform: translate3d(100%,0,0);
    }
</style>


