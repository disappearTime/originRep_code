<template>
    <div class="songlist" ref="songlist">
        <scroll :data="songs" 
                :listenScroll="listenScroll" 
                :probeType="probeType"
                :scrollbar="scrollbar"
                @scroll="scroll">
            <ul class="song_list">
                <li v-for="(item,index) in songs">
                    <p>{{ item.name }}</p>
                    <p>{{ item.singer }}</p>
                </li>
            </ul>
        </scroll>
    </div>
</template>
<script>
import scroll from '@/base/scroll/scroll.vue'
export default {
    props: {
        songs:{
            type: Array,
            default: []
        }
    },
    data () {
        return {
            clientWidth:'',
            pos_top:'',
            scrollY: 0,
            probeType: 3,
            scrollbar: {
                fade:false,
                interactive: true
            },
            listenScroll: true
        }
    },
    mounted () {
        this.$nextTick(()=>{
            this.getClientWidth();
            this.setPosTop();
            //console.log(this.songs);
        })  
    },
    methods: {
        getClientWidth(){
            this.clientWidth = this.$refs.songlist.clientWidth
        },
        setPosTop(){//设置songlist的定位高度
            //console.log(typeof this.clientWidth);
            //console.log(this.clientWidth)
            this.pos_top = this.clientWidth*0.7;
            this.$refs.songlist.style.top = this.pos_top+'px';
        },
        scroll(pos){
            //console.log(pos.y);
            this.scrollY = pos.y;
        }
    },
    components: {
        scroll
    },
    watch: {
        scrollY(newY){
            if(newY>0){
                let percent = (this.pos_top+newY)/this.pos_top;
                this.$emit('scalePercent',percent)
            }
        }
    }
}
</script>
<style scoped>
    .songlist{
        width: 100%;
        position: absolute;
        bottom: 0;
    }
    .songlist ul{
        padding: 10px 0 0 20px;
    }
    .songlist li{
        padding-top: 15px; 
    }
    .songlist li p{
        font-size: 14px;
        line-height: 18px;
    }
    .songlist li p:nth-of-type(2){
        color: grey;
    }
</style>


