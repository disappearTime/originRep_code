<template>
  <div class="wrapper" ref="wrapper">
      <div class="content">
          <slot></slot>
      </div>
  </div>
</template>
<script>
import Bscroll from 'better-scroll'
export default {
    props: {//允许外部组件传递属性
        probeType:{//参数说明见better-scroll的文档，https://ustbhuangyi.github.io/better-scroll/doc/zh-hans/options.html#probetype
            type:Number,
            default:0//为0时不派发scroll事件
        },
        click:{
            type:Boolean,
            default:true
        },
        data:{
            type:Array,
            default:null
        },
        listenScroll: {//是否监听滚动事件
            type: Boolean,
            default: false
        },
        scrollbar: {
            type: Object,
            default:function(){
                return {
                    fade:true,
                    interactive:false
                }
            }
        }
    },
    methods: {
        initScroll(){
            this.scroll = new Bscroll(this.$refs.wrapper,{
                probeType:this.probeType,
                click:this.click,
                scrollbar: this.scrollbar
            })
            console.log(this.listenScroll+'.......')
            if(this.listenScroll){
                let me = this
                this.scroll.on("scroll",(pos) => {//pos参数为滚动的实时目标，具体时机取决于probeType
                    //console.log('滚动')
                    me.$emit("scroll",pos);//$emit派发一个事件，附加参数会传给监听这个事件的函数。
                })
            }
        },
        disable() {
            this.scroll && this.scroll.disable()
        },
        enable() {
            this.scroll && this.scroll.enable()
        },
        refresh() {
            this.scroll && this.scroll.refresh()
        },
        scrollTo(){
            this.scroll && this.scroll.scrollTo.apply(this.scroll,arguments)
        },
        scrollToElement(){
            this.scroll && this.scroll.scrollToElement.apply(this.scroll,arguments)
        }
    },
    mounted () {
        this.$nextTick(()=>{
            this.initScroll()
        })
    },
    watch: {
        data:function(newval,oldval){
            this.scroll && this.scroll.refresh();
        }
    }
}
</script>
<style scoped>
    .wrapper{
        width: 100%;
        height: 100%;
        overflow: hidden;
    }
    .content{
        width: 100%;
    }
</style>


