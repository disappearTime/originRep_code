<template>
  <div class="loading">
      <p class="load"></p>
      <p>正在加载······</p>
  </div>
</template>
<script>
export default {
}
</script>
<style scoped>
.loading{
    text-align: center;
}
.load{
    display: inline-block;
    width: 25px;
    height: 25px;
    border: 3px solid;
    border-radius: 50%;
    border-color: greenyellow transparent transparent transparent;
    margin: 40px 0 15px 0;
    transition: all 0.4s;
    animation: animated 1s infinite; 
}
@keyframes animated {
    from{
        transform: rotate(0deg)
    }
    to{
        transform: rotate(360deg)
    }
}
</style>


