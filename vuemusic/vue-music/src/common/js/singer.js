export default class Singer{
    constructor({id,name}){//对象解构赋值
        this.id = id;
        this.name = name;
        this.avatar = `https://y.gtimg.cn/music/photo_new/T001R150x150M000${id}.jpg?max_age=2592000`
    }
}