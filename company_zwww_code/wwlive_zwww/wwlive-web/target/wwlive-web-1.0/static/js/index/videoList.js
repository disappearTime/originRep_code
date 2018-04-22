$(function() {
	initLivingVideos();
});
function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
    var r = window.location.search.substr(1).match(reg);  //匹配目标参数
    //alert(r);
    if (r != null) return unescape(r[2]); return null; //返回参数值
}

var userId = getUrlParam('userId');

function initLivingVideos() {
	if(userId == null || userId == "undefinde") {
		userId = "";
	}
	var path = $("#web").val();
	if (path == null || path == undefined) {
		return;
	}
	
	var cnid = getUrlParam("cnid");
	$.ajax({
		type:"POST",
		url : path + "/external/video/living",
		data:{"userId" : userId, "cnid":cnid},
//		dataType : 'html',
		success : function(result) {
			$("#page_bg").append(result);
		},
		error : function() {
			$("#page_bg").html("<div class='live_on' id='live_on'><p><img src='${rc.contextPath}/static/images/zbxx220.png' alt=''>主播休息了，客官稍后再来！</p></div>");
		}
	});
	
}

