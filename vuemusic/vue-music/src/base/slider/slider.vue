<template>
  <div class="slider" ref="slider">
      <ul class="slider-group" ref="sliderGroup">
          <slot></slot>
      </ul>
      <div class="dots">
          <span class="dot" v-for="(item,index) in dots" :class="{active:currentPageIndex === index}"></span>
      </div>
  </div>
</template>
<script>
    import BScroll from 'better-scroll'
    import {addClass} from '@/common/js/dom.js'
    export default {
        data () {
            return {
                currentPageIndex:0,//当前的页码
                dots: []
            }
        },
        props: {
            autoplay:{//是否自动播放
                type: Boolean,
                default: true
            },
            loop:{//是否是循环轮播
                type: Boolean,
                default: true
            },
            interval: {//滚动时间
                type: Number,
                default:2000
            }
        },
        mounted () {
            console.log('guazai')
            this.$nextTick(() => {//确保定时器被渲染,也可以使用定时器20秒后执行
                this.setWidth();
                this.initDots()
                this.initSlider();
                if(this.autoplay){
                    this.play()
                }
                window.addEventListener('resize',() => {
                    console.log('resize')
                    if(!this.slider){
                        return
                    }
                    this.setWidth(true)
                    setTimeout( () => {
                        this.slider.refresh();
                        if(this.autoplay){
                            this.play();
                        }
                    },20)
                })
            })
        },
        methods: {
            setWidth(isresize){//设置宽度
                let width = 0;
                let sliderWidth = this.$refs.slider.clientWidth;
                this.children = this.$refs.sliderGroup.children;
                console.log(this.children.length)
                for(let i=0;i<this.children.length;i++){
                    let child = this.children[i]; 
                    addClass(child,'slider-item')
                    child.style.width = sliderWidth+'px';
                    width+=sliderWidth;
                }
                if(this.loop && !isresize){
                    width+=2*sliderWidth
                }
                this.$refs.sliderGroup.style.width = width+'px'
            },
            initSlider(){
                console.log(this.loop)
                this.slider = new BScroll(this.$refs.slider,{
                    scrollX:true,//横向滚动
                    scrollY:false,
                    momentum:false,//是否启动量动画，关闭可以提高效率，
                    click: true,
                    snap: {
                        loop: this.loop,
                        threshold: 0.3,
                        speed:400
                    }
                })
                this.slider.on("scrollEnd",() => {
                    let pageIndex = this.slider.getCurrentPage().pageX;
                    this.currentPageIndex = pageIndex;
                    if(this.autoplay){
                        clearTimeout(this.timer)
                        this.play()
                    }
                })
                this.slider.on('beforeScrollStart', () => {
                    if (this.autoplay) {
                        clearTimeout(this.timer)
                    }
                })
            },
            play(){
                //let pageIndex = this.currentPageIndex;
                let pageIndex = this.slider.getCurrentPage().pageX;
                //console.log(this.children.length)
                this.timer = setTimeout(()=>{
                    if(pageIndex == this.children.length-3){
                        //console.log("here")
                        this.slider.next(400)
                    }else{
                        this.slider.goToPage(pageIndex+1,0,400)
                    }
                },this.interval)
            },
            initDots(){
                this.dots = new Array(this.children.length)
            }
        }
    }
</script>
<style scoped>
    .slider{
        width: 100%;
        position: relative;
        overflow: hidden;
    }
    .slider-item{
        float: left;
    }
    img{
        width: 100%;
    }
    .dots{
        position: absolute;
        width: 100%;
        bottom: 10px;
        text-align: center;
        z-index: 10;
    }
    .dot{
        display: inline-block;
        width: 5px;
        height: 5px;
        border-radius: 50%;
        margin: 0 5px;
        background: grey;
    }
    .dot.active{
        background:brown;
    }
</style>


