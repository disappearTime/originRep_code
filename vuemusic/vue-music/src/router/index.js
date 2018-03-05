import Vue from 'vue'
import Router from 'vue-router'
import Recommend from '@/components/m-recommend/recommend'
import Rank from '@/components/m-rank/rank'
import Singer from '@/components/m-singer/singer'
import Research from '@/components/m-research/research'
import Singerdetail from '@/components/m-singerdetail/singerdetail'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      redirect: '/recommend'
    },
    {
      path: '/recommend',
      component: Recommend
    },
    {
      path: '/singer',
      component: Singer,
      children:[
        {
          path:'/singer/:id',
          component: Singerdetail
        }
      ]
    },
    {
      path: '/rank',
      component: Rank
    },
    {
      path: '/research',
      component: Research
    }
  ]
})
