<template>
  <div id="app">
    <m-header></m-header>
    <m-tab></m-tab>
    <!-- 优化，将dom缓存，切换的时候不会再重新请求数据导致不好的用户体验 -->
    <keep-alive>
      <router-view></router-view>
    </keep-alive>
    
  </div>
</template>

<script>
import MHeader from '@/components/m-header/m-header'
import MTab from '@/components/m-tab/m-tab'
export default {
  name: 'app',
  components: {
    MHeader,
    MTab
  }
}
</script>

<style>
*{
  margin: 0;
  padding:0;
}
html{
  width: 100%;
  height: 100%;
}
body{
  width: 100%;
  min-height: 100%;
  background:rgba(0,0,0,0.8);
  color: #ffffff;
}
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
ul li{
  list-style: none;
}
</style>
