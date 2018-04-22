var file = require('./file.js')
var url = require('url')
var queryString = require('querystring')
module.exports = {
    'home': function(res){
        res.writeHead(200,{'Content-Type':'text/html;charset=utf-8'})
        file.readfile('./templete/main.html',res)
    },
    'login': function(res,req){
        res.writeHead(200,{'Content-Type':'text/html;charset=utf-8'})
        //处理get请求 注意这些操作要在读取文件到浏览器端前执行，
        // var urlObj = url.parse(req.url,true).query;
        // console.log(url);
        // if(urlObj.name && urlObj.password){
        //     console.log(urlObj);
        // }

        //处理post请求
        var post = ''
        req.on('data',function(chunk){
            post+=chunk;
        })
        req.on('end',function(){
            console.log(post)
            var urlObj = queryString.parse(post);
            console.log(urlObj);
        })

        file.readfile('./templete/login.html',res,req)
    },
    'register': function(res){
        res.writeHead(200,{'Content-Type':'text/html;charset=utf-8'})
        file.readfile('./templete/register.html',res)
    },
    'image':function(res){
        res.writeHead(200,{'Content-Type':'image/jpeg'});
        file.readimgfile('./image/0.jpg',res)
    }
    
}