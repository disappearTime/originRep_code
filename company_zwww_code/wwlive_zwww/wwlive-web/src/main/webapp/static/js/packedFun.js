/**
 * Created by Zhuweiwei on 2017/6/26 0026.
 */
//根据参数值，获取路径中相应的参数值。
function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
    var r = window.location.search.substr(1).match(reg);  //匹配目标参数
    //alert(r);
    if (r != null) return unescape(r[2]); return null; //返回参数值
}
//最大输入长度为num的文本域,并将当前输入的字数返回。
function checkLength(which,num) {
    var maxChars = num;
    var nowChars = which.value.length;
    if (nowChars > maxChars){
        nowChars = maxChars;
        which.value = which.value.substring(0, maxChars);
    }
    return nowChars;
}
//将点号分割的字符串数字转成数字
function toNum(str){
    var str1 = str;
    var arr = str1.split(".");
    var newStr = arr.join("");
    var newNum = Number(newStr);
    return newNum;
}
//判断出手机的机型
function getMobileType() {
    var mobileType = "";
    var u = navigator.userAgent;
    if (u.indexOf('Android') > -1 || u.indexOf('Linux') > -1) {//安卓手机
        mobileType = "android"
    } else if (u.indexOf('iPhone') > -1) {//苹果手机
        mobileType ="iphone";
    } else if (u.indexOf('Windows Phone') > -1) {//winphone手机
        mobileType = "winphone";
    }
    return mobileType;
}
//跳转直播间
function toLive(videoId, chatroomId, anchorId, type, cover,ext,videoName){
    /*console.log(decodeURIComponent(ext));*/
    var needUpdateVersion=220;
    var appN = "dl";
    var appName = getUrlParam("app");
    var version = $("#version").val();//注意引入的页面要有这个控件
    var verS = toNum(version);

    var obj = new Object();
    obj.videoId = videoId;
    obj.chatroomId = chatroomId;
    obj.anchorId = anchorId;
    var startTime=new Date().getTime();
    obj.startTime = startTime;
    var live = new Object();
    live.fun = "live";

    var mobileType = getMobileType();
    if(mobileType == "android"){
        obj.type = type;
        obj.cover = cover;
        if(ext!=null && ext!="" && ext!=undefined){
            obj.ext = ext;
        }
        live.data = obj;
        if(appName==appN){
            window.stub.jsClient(JSON.stringify(live));
        }else{
            if(verS<needUpdateVersion){
                updataVersion("live",obj);
            }else{
                window.stub.jsClient(JSON.stringify(live));
            }
        }
    }else if(mobileType=="iphone"){
        obj.videoName = videoName;
        obj.formatType = type;
        obj.coverImg = cover;
        if(ext!=null && ext!="" && ext!=undefined){
            var oUrl = JSON.parse(decodeURIComponent(ext));
            /*console.log(oUrl.standURL);*/
            obj.standURL = oUrl.standURL;
            obj.heighURL = oUrl.heighURL;
            obj.fullHeighURL = oUrl.fullHeighURL;
        }
        live.data = obj;
        window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(live));
    }else{

    }
}
//更新版本
function updataVersion(livefun,livedata){
//         alert("--callUpdate-29--");
    var obj = new Object();
    var path = $("#web").val();//注意引入页面要有相应的控件
    var cnid = getUrlParam("cnid");
    var appType = getUrlParam("app");
    $.ajax({
        url: path+"/external/android/check?version=2.0.0&cnid="+cnid+"&app="+appType,
        type:"GET",
        dataType:"json",
        success: function(data){
            obj.updateMsg=data.updateMsg;
            obj.md5=data.md5;
            obj.errorMsg=data.errorMsg;
            obj.code= data.code;
            obj.url=data.url;
            obj.apkSize=data.apkSize;
            obj.version=data.version;
            obj.isupdate= 2;
            var live = new Object();
            live.fun = livefun;//"live";
            live.data = livedata;
            live.callUpdate=obj;
            live.callRefresh="";
//                alert(JSON.stringify(live));
            window.stub.jsClient(JSON.stringify(live));
        }
    });
}
//跳转主播个人页
function toPersonalPage(anchorId){
    var needUpdateVersion=220;
    var appN = "dl";
    var appName = getUrlParam("app");
    var version = $("#version").val();//引入的页面需要有相应的控件（input）
    var verS = toNum(version);
    var obj = new Object();
    obj.anchorId = anchorId + "";
    var live = new Object();
    live.fun = "anchorCenter";
    live.data = obj;
    if(appName==appN){
        var mobileType = getMobileType();
        if(mobileType == "android"){
            window.stub.jsClient(JSON.stringify(live));
        }else if(mobileType=="iphone"){
            window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(live));
        }else{

        }
    }else{
        if(verS<needUpdateVersion){
            updataVersion("anchorCenter",obj);
        }else{
            var mobileType = getMobileType();
            if(mobileType == "android"){
                window.stub.jsClient(JSON.stringify(live));
            }else if(mobileType=="iphone"){
                window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(live));
            }else{
            }
        }
    }
    event.stopPropagation();
}
//去掉字符串头尾的空格
function trimStr(str){
    return str.replace(/(^\s*)|(\s*$)/g,"");
}
//确保多次点击时只有最后一次点击的请求能够成功
function stopPreAjax() {
    var pendingRequests = {};
    $.ajaxPrefilter(function( options, originalOptions, jqXHR ) {
        var key = options.url;
        if (!pendingRequests[key]) {
            pendingRequests[key] = jqXHR;
        }else{
            //jqXHR.abort(); //放弃后触发的提交
            pendingRequests[key].abort(); // 放弃先触发的提交
        }
        var complete = options.complete;
        options.complete = function(jqXHR, textStatus) {
            pendingRequests[key] = null;
            if ($.isFunction(complete)) {
                complete.apply(this, arguments);
            }
        };
    });
}
//与客户端交互，调用原生功能，上传图片
function uploadTxImg(){
    var obj = new Object();
    var operation = new Object();
    operation.fun = "takePhoto";
    operation.data = obj;
    var mobileType = getMobileType();
    if(mobileType=="android"){
        window.stub.jsClient(JSON.stringify(operation));
    }else if(mobileType=="iphone"){
        window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(operation));
    }else {

    }
}
//截取url问号后面所有的参数。
function getAllParam() {
    var url = window.location.href;
    /*console.log(url);*/
    var backStr = "";
    var arr = url.split("?");
    var arr1 = [];
    for(var i=0;i<arr.length;i++){
        if(i>0){
            arr1.push(arr[i]);
        }
    }
    backStr = arr1.join("?");
    return backStr;
}
//删除url指定名称的参数
function UrlParamDel(url ,name ){
    var reg=new RegExp("\\\? | &"+name+"= ([^&]+)(&|&)","i");
    return url.replace(reg,"");
}
//删除url指定名称参数
function funcUrlDel(name){
    var loca = window.location;
    var baseUrl = loca.origin + loca.pathname + "?";
    var query = loca.search.substr(1);
    if (query.indexOf(name)>-1) {
        var obj = {}
        var arr = query.split("&");
        for (var i = 0; i < arr.length; i++) {
            arr[i] = arr[i].split("=");
            obj[arr[i][0]] = arr[i][1];
        };
        delete obj[name];
        var url = baseUrl + JSON.stringify(obj).replace(/[\"\{\}]/g,"").replace(/\:/g,"=").replace(/\,/g,"&");
        return url
    };
}
//移动端复制内容
function Copy(str){
    var save = function(e){
        e.clipboardData.setData('text/plain', str);
        e.preventDefault();
    }
    document.addEventListener('copy', save);
    document.execCommand('copy');
    document.removeEventListener('copy',save);
}
/*用正则表达式实现html解码*/
function decode(str){
    var s = "";
    if(str.length == 0) return "";
    s = str.replace(/&amp;/g,"&");
    s = s.replace(/&lt;/g,"<");
    s = s.replace(/&gt;/g,">");
    s = s.replace(/&nbsp;/g," ");
    s = s.replace(/&#39;/g,"\'");
    s = s.replace(/&quot;/g,"\"");
    return s;
}
//关注页推荐关注页的人数更新，注意这个函数没有通用性，是根据id名来更新人数的。
function updateFollowPageMan(videoId,chatroomId,anchorId,number) {
    if(number>0){
        var id = "#view_" + anchorId;
        newP = number - 1;
        people = "&nbsp;&nbsp;" + newP;
        $(id).html(people);
    }
}
//关注页推荐关注页下滑的时候，与客户端交互去掉点击的时候的灰色背景。
function clearGreybg(){
    if($('.follow-list li') != undefined && $('.follow-list li') != "" && $('.follow-list li') !=null && $('.follow-list li').length>0 ){
        $('.follow-list li').removeClass("v3_cur");
    }else {
        return;
    }
}
