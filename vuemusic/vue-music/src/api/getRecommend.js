import jsonp from '@/common/js/jsonp.js'
import {commonParams,options} from '@/api/config.js'
import axios from 'axios'
export function getRecommend(){
    let url = 'https://c.y.qq.com/musichall/fcgi-bin/fcg_yqqhomepagerecommend.fcg';
    let data = Object.assign({},commonParams,{
        platform: 'h5',
        uin: 0,
        needNewCode: 1
    })
    return jsonp(url,data,options);//注意这里不要少了return，才能将promise对象返回到函数外部
}
export function getDiclist(){//获取推荐页的推荐歌单
    let url = 'https://c.y.qq.com/splcloud/fcgi-bin/gethotkey.fcg';
    let data = Object.assign({},commonParams,{
        loginUin:0,
        hostUin:0,
        format:'jsonp',
        platform:'yqq',
        needNewCode:0
    })
    return jsonp(url,data,options);
}
export function getDiclist_dl(){//通过代理的方式取数据 代理服务在webpack.dev.conf.js 最新的vue-webpack-template 中已经去掉了dev-server.js 改用webpack-dev-server 代替
    let url = 'api/getdiclist';
    let data = Object.assign({},commonParams,{
        loginUin:0,
        hostUin:0,
        format:'jsonp',
        platform:'yqq',
        needNewCode:0
    })
    return axios.get(url,{
        params:data
    }).then((res) => {
        return Promise.resolve(res);
    })
}