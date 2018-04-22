function livingInfo(videoId, chatroomId, anchorId, formatType, coverImg,version,ext) {
	console.log(decodeURI(ext));
	var obj = new Object();
	obj.videoId = videoId;
	obj.chatroomId = chatroomId;
	obj.anchorId = anchorId;
	obj.type = formatType;
	obj.cover = coverImg;
	//alert("ext:"+ext);
	if(ext!=null && ext!="" && ext!=undefined){
		obj.ext = ext;
		//alert("-startTime--"+startTime);
	}
    var startTime=new Date().getTime();
    obj.startTime = startTime;
	var live = new Object();
	live.fun = "live";
	live.data = obj;
	/*alert(JSON.stringify(live));*/
	/*console.log(JSON.stringify(live))*/
    //判断机型
    var u = navigator.userAgent;
    if (u.indexOf('Android') > -1 || u.indexOf('Linux') > -1) {//安卓手机
        /*alert("安卓手机");*/
        window.stub.jsClient(JSON.stringify(live));
    } else if (u.indexOf('iPhone') > -1) {//苹果手机
        /*alert("苹果手机");*/
        window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(live));
    } else if (u.indexOf('Windows Phone') > -1) {//winphone手机
		/*alert("winphone手机");*/
    }
}
//获取url中的参数
function getUrlParam(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
	var r = window.location.search.substr(1).match(reg);  //匹配目标参数
	if (r != null) return unescape(r[2]); return null; //返回参数值
}
function videoInfo(videoId, anchorId, formatType, coverImg) {
	var obj = new Object();
	obj.videoId = videoId;
	obj.anchorId = anchorId;
	obj.type = formatType;
	obj.cover = coverImg;
	var video = new Object();
	video.fun = "video";
	video.data = obj;
    //判断机型
    var u = navigator.userAgent;
    if (u.indexOf('Android') > -1 || u.indexOf('Linux') > -1) {//安卓手机
		/*alert("安卓手机");*/
        window.stub.jsClient(JSON.stringify(video));
    } else if (u.indexOf('iPhone') > -1) {//苹果手机
		/*alert("苹果手机");*/
        window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(video));
    } else if (u.indexOf('Windows Phone') > -1) {//winphone手机
		/*alert("winphone手机");*/
    }
}

function videoInfo4IOS(videoId) {
	if (videoId == null || videoId == undefined) {
		return;
	}
	videoId = videoId + "";
	var obj = new Object();
	obj.videoId = videoId;
	var video = new Object();
	video.fun = "video";
	video.data = obj;
	window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(video));
}
//当主播从直播间退出的时候修改主播间的人数
function updateMan(videoId,chatroomId,anchorId,number){
	/*alert(videoId+","+chatroomId+","+anchorId+","+number);*/
	if(number>0){
		var newPerson = number-1;
        var anch = $(".anchor:eq(0) li .cover");
        for(var i=0;i<anch.length;i++){
			if(anch.eq(i).attr("data-id") == anchorId){
                anch.eq(i).find(".top_num span").html(newPerson);
			}
        }
	}
}
//将字符串转成数字
function toNum(str){
    var str1 = str;
    var arr = str1.split(".");
    var newStr = arr.join("");
    var newNum = Number(newStr);
    return newNum;
}


