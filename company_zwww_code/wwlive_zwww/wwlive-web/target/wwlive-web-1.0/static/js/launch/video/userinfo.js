function getUserInfo(loginId) {
	if (isBlank(loginId)) {
		return;
	}
	var cnid = $("#cnid").val();
	var version = $("#version").val();
	var model = $("#model").val();
	var IMEI = $("#IMEI").val();
	var platform = $("#platform").val();
	var path = $("#web").val();
	var userId = $("#userId").val();
	var anchorId = $("#anchorId").val();
	var nonce = $("#nonce").val();
	var coverKey = $("#coverKey").val();
	var requestId = $("#requestId").val();
	var way = $("#way").val();
	if (path == null || path == undefined) {
		return;
	}
	if (isBlank(requestId)) {
		return;
	}
	if (isBlank(coverKey)) {
		return;
	}
	if (isBlank(nonce)) {
		return;
	}
	if (isBlank(userId)) {
		return;
	}
	/*if (platform != null && platform != undefined && platform.toLocaleUpperCase() == 'IOS') {
		userInfo4IOS(loginId);
		return;
	}*/
	
	$.ajax({
		type : "POST",
		url : path + "/app/user/detail.json",
		dataType : 'JSON',
		data : {
			"viewId" : loginId,
			"userId" : userId,
			"cnid" : cnid,
			"version" : version,
			"anchorId" : anchorId,
			"model" : model,
			"IMEI" : IMEI,
			"nonce" : nonce,
			"coverKey" : coverKey,
			"requestId" : requestId,
			"platform" : platform
		},
		success : function(result) {
			var devotePage = $("#devotePage").val();
			if(devotePage=="devotePage"){
                $("#data-card").empty();
                var user = result.data.userInfo;
                console.log(user);
                var info = $("#data-card");
                var userTypeImg = "";
                info.append("<img class='devotePageClose' src="+path+"/static/images/noble-close.png alt='' />");
                if (user.sex == 0) {
                    info.append("<img src='" + user.headImg + "' alt='' class='userCardDataPic' onerror='this.src=\"" + path + "/static/images/defaultWoman.png\"'/>");
                } else if (user.sex == 1){
                    info.append("<img src='" + user.headImg + "' alt='' class='userCardDataPic' onerror='this.src=\"" + path + "/static/images/defaultMan.png\"'/>");
                } else {
                    info.append("<img src='" + user.headImg + "' alt='' class='userCardDataPic' onerror='this.src=\"" + path + "/static/images/defaultMan.png\"'/>");
                }

                if (user.userType == 1) {
                    // info.append("<img src='/static/images/zb_cg.png' alt='' class='room_manage'/>");
                    userTypeImg = "<img src='/static/images/zb_cg.png' alt='' class='imgroom'/>"
                } else if (user.userType == 2) {
                    // info.append("<img src='/static/images/zb_fg.png' alt='' class='room_manage'/>");
                    userTypeImg = "<img src='/static/images/zb_fg.png' alt='' class='imgroom'/>"

                }
                var sex = "";
                if (user.sex == 0) {
                    sexIcon = " <img class='sexImg' src='" + path + "/static/images/woman.png'/>";
                } else if (user.sex == 1){
                    sexIcon = " <img class='sexImg' src='" + path + "/static/images/man.png'/>";
                } else {
                    sexIcon = " ";
                }
                var richSign = "";
                if( user.medals != "" && user.medals != undefined && user.medals.length > 0 ){
                    for(var i = 0;i<user.medals.length;i++){
                        if(user.medals[i] == "土豪勋章"){
                            richSign = " <img class='richSign' src='" + path + "/static/images/localRich.png'/>";
                        }
                    }
                }
                var noblesImgStr=""
                if (user.nobles!=null && user.nobles!=undefined && user.nobles.length>0){
                    var noblesarr = user.nobles;
                    for (var i =0;i<noblesarr.length;i++){
                        if (noblesarr[i]==1){
                            noblesImgStr += "<img style='margin: 0 10px;height: 25px;' src='"+ path +"/static/images/sqx.png'/>"
                        }else if(noblesarr[i]==2){
                            noblesImgStr += "<img style='margin: 0 10px;height: 25px;' class='nobleImg' src='"+ path +"/static/images/lqs.png'/>"
                        }else if(noblesarr[i]==3){
                            noblesImgStr += "<img style='margin: 0 10px;height: 25px;' class='nobleImg' src='"+ path +"/static/images/hqs.png'/>"
                        }else if(noblesarr[i]==4){
                            noblesImgStr += "<img style='margin: 0 10px;height: 25px;' class='nobleImg' src='"+ path +"/static/images/mfqs.png'/>"
                        }else if(noblesarr[i]==5){
                            noblesImgStr += "<img style='margin: 0 10px;height: 25px;' class='nobleImg' src='"+ path +"/static/images/zjqs.png'/>"
                        }else if(noblesarr[i]==6){
                            noblesImgStr += "<img style='margin: 0 10px;height: 25px;' class='nobleImg' src='"+ path +"/static/images/sdqs.png'/>"
                        }
                    }
                }

                info.append("<p class='usercardName'>" + richSign + user.userName + userTypeImg + sexIcon + "</p>");
                info.append("<p style='margin-top: 15px;' class='noble-box'>"+ noblesImgStr +"</p>")
                var birthday = user.birthday;

                if (!isBlank(birthday)) {
                    var date = str2Date(birthday);
                    var m = (date.getMonth() + 1);
                    var d = date.getDate();
                    info.append("<p class='ages'>年龄:" + user.age + "岁&nbsp;&nbsp;星座:" + user.zodiac + "</p>");
                } else {
                    info.append("<p class='ages'>年龄:" + user.age + "岁&nbsp;&nbsp;星座:双鱼座</p>");
                }
                info.append("<div class='userDataInfo'><div><p class=''>" + user.followNum + "</p><p class='userInfo-p'>关注数</p></div>"+"<div><p class=''>" + user.totalAmt + "</p><p class='userInfo-p'>贡献值</p></div>"+"<div><p class=''>" + user.rank + "</p><p class='userInfo-p'>全站排名</p></div></div>");
                // info.append("<div><p class=''>" + user.totalAmt + "</p><p>贡献值</p></div>");
                // info.append("<div><p class=''>" + user.rank + "</p><p>全站排名</p></div></div>");
                $(".shadowUp-new2").css({"display":"block"});
                $(".devoteUsercard").css({"display":"block"});
                $(".devotePageClose").bind("click", function () {
                    hidenInfo();
                });
			}else{
                $("#data-card").empty();
                var user = result.data.userInfo;
                /*console.log(user);*/
                var info = $("#data-card");
                info.append("<img src='" + user.headImg + "' alt='' class='data-pic' onerror='this.src=\"" + path + "/static/images/user_cover.png\"'/>");
                if (user.userType == 1) {
                    info.append("<img src='/static/images/zb_cg.png' alt='' class='room_manage'/>");
                } else if (user.userType == 2) {
                    info.append("<img src='/static/images/zb_fg.png' alt='' class='room_manage'/>");

                }
                var sex = "";
                if (user.sex == 0) {
                    sexIcon = " <img src='" + path + "/static/images/woman.png'/>";
                } else if (user.sex == 1){
                    sexIcon = " <img src='" + path + "/static/images/man.png'/>";
                } else {
                    sexIcon = " ";
                }
                var richSign = "";
                if( user.medals != "" && user.medals != undefined && user.medals.length > 0 ){
                    for(var i = 0;i<user.medals.length;i++){
                        if(user.medals[i] == "土豪勋章"){
                            richSign = " <img class='richSign' src='" + path + "/static/images/localRich.png'/>";
                        }
                    }
                }
                info.append("<p class='anchor-name'>" + richSign + user.userName + sexIcon + "</p>");
                var birthday = user.birthday;

                if (!isBlank(birthday)) {
                    var date = str2Date(birthday);
                    var m = (date.getMonth() + 1);
                    var d = date.getDate();
                    info.append("<p class='ages'>年龄:" + user.age + "岁&nbsp;&nbsp;" + user.zodiac + "</p>");
                } else {
                    info.append("<p class='ages'>年龄:" + user.age + "岁&nbsp;&nbsp;双鱼座</p>");
                }
                info.append("<p class='orange'>贡献值：" + user.totalAmt + "</p>");
                info.append("<p class='orange'>全站排名：" + user.rank + "</p>");
                $("#shadowUp").css({"display":"block"});
                $("#indexcard").css({"display":"block"});
			}

			
		},
		error : function() {
			console.log('失败');
		}
	});
}
$("#indexcard").bind("click", function () {
	hidenInfo();
});
$("#shadowUp").bind("click", function () {
    hidenInfo();
});
$(".shadowUp-new2").bind("click", function () {
    hidenInfo();
});

function hidenInfo() {
	var event_f = function(e){e.preventDefault();}  
	$("#data-card").empty();
	$("#indexcard").hide();
	$("#shadowUp").hide();
	$(".shadowUp-new2").hide();
	$(".devoteUsercard").hide();
}
function str2Date(strDate) {
    var str = strDate.replace(/-/g, '/');
    var date = new Date(str);
    return date;
}
function isBlank(string) {
    if (string == null || string == undefined) {
        return true;
    }
    if (string.replace(/(^s*)|(s*$)/g, "").length ==0) {
        return true;
    }
}
function userInfo4IOS(loginId) {
	if (loginId == null || loginId == undefined) {
		return;
	}
	var obj = new Object();
	obj.loginId = loginId;
	var user = new Object();
	user.fun = "userInfo";
	user.data = obj;
	window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(user));
}