  //基于commonJs规范的模块化开发
var fs = require('fs');
module.exports = {
    readfilesync(res){//同步读取
        var data = fs.readFileSync('./templete/main.html','utf-8');//这个路径是相对于fs.js的，因为这个方法是在fs.js中调用的
        res.write(data);//将数据写到浏览器中
    },
    readfile(file,res,req){
        fs.readFile(file,'utf-8',function(err,data){
            res.write(data);
            res.end()
        })
    },
    readimgfile(file,res){
        fs.readFile(file,'binary',function(err,data){
            res.write(data,'binary');
            res.end()
        })
    },
    writefile(file){//写入文件到本地
        fs.writeFile(file,'我是被写入到本地的',(err) => {
            if(err) throw err;
        })
    }
}