import originJsonp from 'jsonp';
export default function jsonp(url,data,options){
    url += (url.indexOf('?')<0? '?' : '&') + params(data);
    return new Promise( (resolve,reject) => {
        originJsonp(url,options,(err,data) => {
            if(!err){
                resolve(data)
            }else{
                //reject(err)
            }
        } )
    } )

}
function params(data){
    let url = '';
    for(let k in data){
        url+=`&${k}=${data[k]}`
    }
    return url?url.substring(1):''
}