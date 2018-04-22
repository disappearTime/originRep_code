var http = require('http');//请求协议
var file = require('./common/file.js')
http.createServer( function(request,response){
    response.writeHead(200,{'Content-Type':'text/html;charset=utf-8'})
    if(request.url !== '/favicon.ico'){
        //file.readfilesync(response)
        //response.end();异步读取是这个要写在回调函数里，否则因为是异步的，end可能会先执行
        file.readfile('./templete/main.html',response)
    }
} ).listen(8000)