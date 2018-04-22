//async的三种异步调用
var async = require('async');
//串行无关联
// console.time('test');//v8引擎的一个测试时间的方法
// async.series([
//     function(callback){
//         //todo 你的代码程序
//         setTimeout(function(){
//             callback(null,'one');//没有错误，第一个参数为空，第二个参数就是把结果传给最终执行的那个函数
//         },1000)
//     },
//     function(callback){
//         //todo
//         setTimeout(function(){
//             callback(null,'two')
//         },2000)
//     }
// ],function(err,result){
//     console.log(result);//['one','two']
//     console.timeEnd('test');//时间是3s多说明是串行的
// })
// console.time('test')
// async.series({
//     'one': function(callback){
//         //todo
//         callback(null,'1');
//     },
//     'two': function(callback){
//         //todo
//         callback(null,'2');
//     }
// },function(err,result){
//     console.log(result);//{ one: '1', two: '2' }
//     console.timeEnd('test')
// })

//并行无关联
// console.time('test')
// async.parallel({
//     'a': function(callback){
//         //todo
//         setTimeout(function(){
//             callback(null,'one');
//         },2000)
//     },
//     'b': function(callback){
//         //todo
//         setTimeout(function(){
//             callback(null,'two')
//         },1000)
//     }
// },function(err,result){
//     console.log(result)//{b: 'two',a:'one'}//时间为2s多，说明是并行的，那个先执行先执行哪个，另一个传数组写法就不赘述了
//     console.timeEnd('test')
// })

//串行有关联
async.waterfall([
    function(callback){
        //todo
        callback(null,'one','two');//把结果传个下一步
    },
    function(arg1,arg2,callback){//arg1,arg2接收第一步传过来的参数
        //todo
        callback(null,arg1,arg2,'three')
    },
    function(arg1,arg2,arg3,callback){
        callback(null,[arg1,arg2,arg3,'four'])//不放在数组里result结果为one
    }
],function(err,result){
    console.log(result)//['one','two','three','four']
})