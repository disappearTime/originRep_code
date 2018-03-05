import jsonp from '@/common/js/jsonp.js';
import { commonParams,options } from './config.js';
export function getSinger(){
    let url = 'https://c.y.qq.com/v8/fcg-bin/v8.fcg';
    let data = Object.assign({},commonParams,{
        channel:'singer',
        page:'list',
        key:'all_all_all',
        pagesize:100,
        pagenum:1,
        g_tk:5381,
        loginUin:0,
        hostUin:0,
        platform:'yqq',
        needNewCode:0
    })
    return jsonp(url,data,options)
}
export function getsongdetail(singerid){//获取歌曲的详细信息
    let url = 'https://c.y.qq.com/v8/fcg-bin/fcg_v8_singer_track_cp.fcg';
    let data = Object.assign({},commonParams,{
        g_tk:5381,
        loginUin:0,
        hostUin:0,
        platform:'yqq',
        needNewCode:0,
        singermid:singerid,
        order:'listen',
        begin:0,
        num:30,
        songstatus:1,
        inCharset:'utf8'
    })
    return jsonp(url,data,options)
}