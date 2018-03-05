<template>
    <div v-show="songs.length>0" class="musiclist">
        <div class="imagebox" :style="bgstyle" ref="imagebox">
            <div class="playbtn">
                <span class="play_icon">△</span>
                随机播放全部</div>
        </div>
        <div class="title">
            <span @click="back" ref="back">《</span>
            {{ title }}
        </div>
        <song-list @scalePercent="setScale" v-if="songs.length>0" :songs="songs"></song-list>
        <loading v-show="!songs.length"></loading>
    </div>
</template>
<script>
import loading from '@/base/loading/loading.vue'
import SongList from '@/base/songlist/songlist.vue'
export default {
    data () {
        return {
        }
    },
    props: {
        songs: {
            type: Array,
            default: []
        }
    },
    computed: {
        bgimgurl(){
            return this.songs[0].image;
        },
        bgstyle(){
            return `background-image: url(${this.bgimgurl})`
        },
        title(){
            return this.songs[0].singer;
        }
    },
    mounted () {
        this.$nextTick(function(){
            console.log(this.songs);
            console.log(this.bgimgurl)
        })
    },
    methods: {
        back(){
            this.$router.back();
        },
        setScale(per){//设置imagebox的放大倍数
            this.$refs.imagebox.style.transform = `scaleY(${per})`
            this.$refs.back.style.transform = `scaleY(1)`
        }
    },
    components: {
        SongList,
        loading
    }
}
</script>
<style scoped>
    .musiclist{
        position: fixed;
        top: 0;
        bottom:0;
        left: 0;
        right: 0;
        z-index: 1000;
        background: #333;
    }
    .imagebox{
        width: 100%;
        height: 0;
        padding-top: 70%;
        background-size: cover;  
    }
    .title{
        width: 100%;
        padding: 10px 0;
        font-size: 15px;
        text-align: center;
        position: absolute;
        top: 0;
        left: 0;
    }
    .title span{
        display: inline-block;
        color: yellow;
        transform: scale(2,1.2)!important;
        position: absolute;
        left: 5px;  
    }
    .playbtn{
        position: absolute;
        top:200px;
        left:50%;
        margin-left: -82px;
        width: 164px;
        text-align: center;
        color: yellow;
        padding: 10px 0;
        border: 1px solid;
        border-radius: 20px;
    }
    .play_icon{
        display: inline-block;
        border: 2px solid;
        width: 23px;
        box-sizing: border-box;
        height: 23px;
        line-height: 21px;
        font-size: 20px;
        border-radius: 50%;
        transform: rotate(-30deg) translateY(1px);
    }
</style>


