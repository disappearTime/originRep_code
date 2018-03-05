<template>
  <div class="tab">
      <router-link @click.native="getCurrentRouter(1)" tag='div' to="/recommend">推荐</router-link>
      <router-link @click.native="getCurrentRouter(2)" tag="div" to="/singer" >歌手</router-link>
      <router-link @click.native="getCurrentRouter(3)" tag="div" to="/rank">排行</router-link>
      <router-link @click.native="getCurrentRouter(4)" tag="div" to="/research">搜索</router-link>
  </div>
</template>
<script>
    export default {
        methods: {
            getCurrentRouter(index){//父组件想在子组件上监听自己的click的话，需要加native修饰符
                console.log(this.$route)//可以通过this.$route.path来判断当前路由，也可以通过传进来的参数判断
                console.log(index);
            }
        }
    }
</script>
<style scoped>
    .tab{
        width: 100%;
        height: 45px;
        display: flex;
        justify-content: space-around;
        align-items: center;
        flex-grow: 1;
        flex-shrink: 1;
        background:#333;
        position: relative;
        z-index: 200;
    }
    .tab div{
        width: 60px;
        height: 100%;
        text-align: center;
        line-height: 45px;
        box-sizing: border-box;
    }
    .tab div.router-link-active{
        border-bottom: 2px solid brown;
    }
</style>


