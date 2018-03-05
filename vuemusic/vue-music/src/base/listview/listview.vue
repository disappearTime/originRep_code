<template>
    <div class="wrap">
        <loading v-show="!data.length"></loading>
        <scroll :data="data" 
                ref="listview"
                @scroll="scroll" 
                :probeType="probeType"
                :listenScroll="listenScroll">
            <ul class="list_view">
                <li v-for="(item,index) in data" :data-index="index" ref="listgroup">
                    <h3 class="singer_topic">{{ item.title }}</h3>
                    <ul class="list">
                        <li class="needsclick" @click="seletitem(val)" v-for="val in item.items">
                            <img v-lazy="val.avatar" alt="">{{ val.name }}
                        </li>
                    </ul>
                </li>
            </ul> 
        </scroll>
        <div class="list_tab" @touchstart="shotcutTouchStart" @touchmove.stop.prevent="shotcutTouchMove">
            <ul>
                <li v-for="(item,index) in singer_topic" 
                :class="{'active': currentIndex===index}"
                :data-index="index">
                {{ item }}</li>
            </ul>
        </div>
        <div class="fixed_title" v-show="fixed_title" ref="fixed">
            <h3>{{ fixed_title }}</h3>
        </div>
    </div>
    
</template>
<script>
import scroll from '@/base/scroll/scroll'
import {getData} from '@/common/js/dom.js'
import loading from '@/base/loading/loading.vue'
let dotHeight = 22
let topic_height = 49
export default {
    props: {
        data: {
            type: Array,
            default:[]
        }
    },
    data(){
        return {
            listenScroll: true,
            probeType: 3,//实时派发scroll事件
            scrollY: -1,
            currentIndex: 0,//当前索引
            listHeight: [],
            diff: -1
        }    
    },
    created(){
        this.touch = {};//用于记录点击移动右侧导航栏的时候鼠标信息，这个数据不放在data里，是因为data里的数据在vue里会被检测从而和dom上的数据进行绑定，而这里不需要检测。
    },
    computed: {
        singer_topic(){
            return this.data.map((items)=>{//数组的map方法，映射
                return items.title.substr(0,1)
            })
        },
        fixed_title(){
            if(this.scrollY >= 0){
                return ''
            }
            return this.data[this.currentIndex]?this.data[this.currentIndex].title:""
        }
    },
    components: {
        scroll,
        loading
    },
    methods: {
        shotcutTouchStart(e){//右侧导航点击切换
            console.log(e);
            let anchorIndex = getData(e.target,'index');
            if(!anchorIndex && parseInt(anchorIndex)!=0){
                return;
            }
            this.currentIndex = parseInt(anchorIndex);
            console.log(this.currentIndex);
            console.log(typeof anchorIndex)
            this.touch.anchorIndex = parseInt(anchorIndex);
            let firsttouch = e.touches[0];
            this.touch.y1 = firsttouch.pageY
            this.$refs.listview.scrollToElement(this.$refs.listgroup[anchorIndex],0)
        },
        shotcutTouchMove(e){//右侧导航的move事件
            let firsttouch = e.touches[0];
            this.touch.y2 = firsttouch.pageY;
            let delta = (this.touch.y2 - this.touch.y1)/dotHeight | 0;
            let anchorIndex = this.touch.anchorIndex + delta;
            //console.log(typeof anchorIndex)
            //console.log(anchorIndex);
            if(anchorIndex<0){
                anchorIndex = 0
            }else if(anchorIndex>=this.$refs.listgroup.length){
                anchorIndex = this.$refs.listgroup.length - 1;
            }
            this.currentIndex = anchorIndex;
            this.$refs.listview.scrollToElement(this.$refs.listgroup[anchorIndex],0)//第二个参数0表示不要缓冲动画
        },
        scroll(pos){
            //console.log('pos='+pos)
            //console.log(pos.y);
            this.scrollY = pos.y;
        },
        computeHeight(){//计算每个模块距离滚动区域顶部的高度
            this.listHeight = [];
            let height = 0;
            this.listHeight.push(height);
            let list = this.$refs.listgroup;
            for(let i=0;i<list.length;i++){
                let item = list[i];
                height+=item.clientHeight;
                this.listHeight.push(height);
            }
            console.log(this.listHeight);
        },
        seletitem(singer){
            this.$emit("select",singer)
        }

    },
    watch: {
        data(){
            let me = this
            this.$nextTick(()=>{
                me.computeHeight()
            })
        },
        scrollY(newY){
            //console.log(-newY);
            let list_hei = this.listHeight;
            //在头部滚动
            //console.log('newY='+newY);
            if(newY >= 0){
                this.currentIndex = 0;
                return;
            }
            //在中间部分滚动
            for(let i=0;i<list_hei.length;i++){
                let toplimit = list_hei[i];
                let floorlimit = list_hei[i+1];
                if(-newY>toplimit && -newY < floorlimit){
                    this.currentIndex = i;
                    //console.log(-newY);
                    //console.log(floorlimit)
                    let d_val = floorlimit+newY
                    this.diff = d_val
                    return;       
                }
            }
            //在底部滚动 循环结束都没有满足条件的，说明滚到最底部
            this.currentIndex = list_hei.length - 2;
        },
        diff(newVal){
            let trans_data = (newVal>=0 && newVal<=topic_height)? newVal-topic_height:0;
            if(this.trans_data === trans_data){
                return;
            }
            this.trans_data = trans_data;
            this.$refs.fixed.style.transform = `translate3d(0,${trans_data}px,0)`
        }
    }
}
</script>
<style scoped>
    .wrap{
        width: 100%;
        height: 100%;
        position: relative;
        z-index: 100;
        overflow: hidden;
    }
    .singer_topic{
        padding: 15px 0;
        font-size: 14px;
        color: #ddd;
        background-color: grey;
        text-indent: 15px;
    }
    .list_view,.list_view li,list{
        width: 100%;
    }
    .list li{
        width: 100%;
        height: 40px;
        padding: 10px 0;
    }
    .list li img{
        width: 40px;
        height: 40px;
        border-radius: 50%;
        vertical-align: middle;
        margin: 0 10px;
    }
    .list_tab{
        position: fixed;
        width: 25px;
        right:0px;
        top:130px;
        padding: 5px 0;
        z-index: 1000;
    }
    .list_tab li{
        width: 22px;
        height: 22px;
        border-radius: 50%;
        background: grey;
        text-align: center;
        line-height: 22px;
        background-color: grey;
        font-size: 14px;
    }
    .active{
        color: yellow;
    }
    .fixed_title{
        width: 100%;
        position: absolute;
        top:0px;
        z-index: 0;
    }
    .fixed_title h3{
        width: 100%;
        padding: 15px 0;
        font-size: 14px;
        color: #ddd;
        background-color: grey;
        text-indent: 15px;
    }
</style>


