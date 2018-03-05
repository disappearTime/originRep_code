export default class Song {
    constructor({id,mid,singer,name,ablum,duration,image,url}){
        this.id = id;
        this.mid = mid;
        this.singer = singer;
        this.name = name;
        this.ablum = ablum;
        this.duration = duration;
        this.image = image;
        this.url = url
    }
}
export function createSong(musicData){//创建类的实例 工厂模式
    return new Song({
        id: musicData.songid,
        mid: musicData.songmid,
        singer: filterSinger(musicData.singer),
        name: musicData.songname,
        album: musicData.albumname,
        duration: musicData.interval,
        image: `https://y.gtimg.cn/music/photo_new/T002R300x300M000${musicData.albummid}.jpg?max_age=2592000`,
        //url: `http://ws.stream.qqmusic.qq.com/${musicData.songid}.m4a?fromtag=46`//已禁用
        url: 'http://dl.stream.qqmusic.qq.com/C400001Qu4I30eVFYb.m4a?vkey=194DE7EC19562D35B7DE0ACCFC45A46A27E8F11609CA045A3B3E5A3DBAEC063DC68813B097B913E59888B4811CC70FAE3A985DE7757BCFB3&guid=5089166735&uin=0&fromtag=66'//写死
    })
}
function filterSinger(singer) {
    let ret = []
    if (!singer) {
      return ''
    }
    singer.forEach((s) => {
      ret.push(s.name)
    })
    return ret.join('/')
}