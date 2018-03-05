<template>
    <div class="recommend">
        <scroll ref='scroll' :data='songlist'>
            <slider v-if="slider.length">
                <li v-for="item in slider">
                    <a :href="item.linkUrl">
                        <img class="needsclick" @load="loadimg" :src="item.picUrl" alt="">
                    </a> 
                </li>
            </slider>
            <h2 class="list_title">推荐歌单列表</h2>
            <loading v-show="!songlist.length"></loading>
            <ul class="songlist">
                <li class="list_item" v-for="item in songlist">
                    <img v-lazy="listimg" alt="">
                    <div>
                        <p>梦回旧景</p>
                        <p>第一次听原唱，我才十几岁</p>
                        <p>播放量</p>
                    </div>
                </li>
            </ul>
        </scroll>
    </div>
</template>
<script>
    import {getRecommend} from '@/api/getRecommend.js'
    import {getDiclist} from '@/api/getRecommend.js'
    import {ERR_OK} from '@/api/config.js'
    import slider from '@/base/slider/slider.vue'
    import Loading from '@/base/loading/loading.vue'
    import scroll from '@/base/scroll/scroll.vue'
    export default {
        data () {
            return {
                checkloaded: false,
                listimg: require('@/common/images/gyy.jpg'),//假数据
                slider:[],
                songlist:[]
            }
        },
        methods: {
            _getRecommend(){//获取推荐页轮播图数据
                var that = this
                getRecommend().then(function(res){
                    if(res.code == ERR_OK){
                        console.log(res.data.slider)
                        that.slider = res.data.slider;
                    }
                })
            },
            _getDiclist(){//获取推荐页歌单列表数据
                var that = this
                getDiclist().then(function(res){
                    console.log(res.data.hotkey)
                    if(res.code == ERR_OK){
                        that.songlist = res.data.hotkey
                    }
                })
            },
            loadimg () {
                if(!this.checkloaded){
                    this.$refs.scroll.refresh()
                    this.checkloaded = true
                }
            }
        },
        created: function(){
            this.$nextTick(() => {//确保定时器被渲染
                this._getRecommend();
                this._getDiclist();
            })
            
        },
        components: {
            slider,
            Loading,
            scroll
        }
    }
</script>
<style scoped>
    .recommend{
        width: 100%;
        position: fixed;
        top: 85px;
        bottom:0;
    }
    ul li{
        list-style: none;
    }
    .list_title{
        text-align: center;
        font-size: 18px;
        color: brown;
        padding: 15px 0;
    }
    .songlist{
        width: 100%;
    }
    .songlist li{
        margin-bottom: 15px;
        display: flex;
        font-size: 14px;
        width: 100%;
        justify-content: space-between;
        align-items: center;
    }
    .songlist li:nth-last-of-type(1){
        margin-bottom: 0;
    }
    .songlist li img{
        margin: 0 10px;
        width: 60px;
        height: 60px;
        flex-shrink: 0;
        flex-grow: 0
    }
    .songlist li>div{
        margin-right: 10px;
        height: 60px;
        flex-shrink: 1;
        flex-grow: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
    }
</style>


