var http = require('http');
var router = require('./common/router.js')
http.createServer(function(req,res){
    //res.writeHead(200,{'Content-Type':'text/html;charset=utf-8'})响应头文件
    if(req.url !== 'favicon.ico'){
        var path = req.url.split('?')[0].replace(/\//,'');
        console.log(path)
        try{
            router[path](res,req)
        }catch(e){
            router['home'](res,req)
        }
        
    }
}).listen(8000)