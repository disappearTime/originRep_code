/**
 * Created by Zhuweiwei on 2017/7/14 0014.
 */
var path = $("#web").val();
//跳转到法律法规页
function toLawPage() {
    window.location.href = path + "/static/html/law.html?target=blank";
}
//获取缓存数据
function getCacheNum(num) {
    // alert(num);
    if(num!="undefiend" && num!=null){
        $(".cache").html(num);
    }
}
//清空缓存
function emptyCache() {
    $(".cache").html("0B");
    var obj = new Object();
    var empty = new Object();
    empty.fun = "emptyCache";
    empty.data = obj;
    var mobileType = getMobileType();
    if(mobileType=="android"){
        window.stub.jsClient(JSON.stringify(empty));
    }else if(mobileType=="iphone"){
        window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(empty));
    }else {

    }
}
//检查更新
function checkUpdate() {
    var obj = new Object();
    var update = new Object();
    update.fun = "update";
    update.data = obj;
    var mobileType = getMobileType();
    if(mobileType=="android"){
        window.stub.jsClient(JSON.stringify(update));
    }else if(mobileType=="iphone"){
        window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(update));
    }else {

    }
}
