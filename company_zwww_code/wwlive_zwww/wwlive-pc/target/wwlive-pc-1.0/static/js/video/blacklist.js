//加入黑名单
function getSay(userId) {
	var isBlack = $("#say" + userId).val();
	var userName = $("#userName" + userId).val();
	if (isBlack == 0) {
		sayNo(userId, userName);
	} else {
		say(userId, userName);
	}
}

function sayNo(userId, userName) {
	if (userId == null && userId == "") {
		return;
	}
	$("#black_id").val(userId);
	$("#black_name").val(userName);
	
	//TODO
	$("#mute_admin_div").fadeIn();	
	
	//$("#black_div").fadeIn();
	$("#shadowUp").fadeIn();
    $("#no_say_time_error").css({"display":"none"});
}

function removeTimeError() {
	$("#no_say_time_error").css({"display":"none"});
	$("#time_msg").text("");
}

var vldFlag = false;//禁言时间是否正确flag

function addBlackList() {
	var blacktime = $("#no_say_time").val();
	var reg = new RegExp("^[0-9]*$");
	if (!reg.test(blacktime)) {
		$(".p-notice").text("*请输入正确的时间！");
		$(".p-notice").css("color", "#f00");
		vldFlag = false;
		return;
	}
	if (blacktime == null || blacktime == "") {
		$(".p-notice").text("*请输入时间！");
		$(".p-notice").css("color", "#f00");
		vldFlag = false;
        return;
	}
	vldFlag = true;
	
	var blacksecond = blacktime * 60;
	var userId = $("#black_id").val();
	var userName = $("#black_name").val();
	var loginId = $("#black_loginId").val();
	var videoId = $("#videoId1").val();
	$.ajax({
		type : "POST",
		url : "/pc/anchor/addBlackList",
		data : {
			"userId" : userId,
			"time" : blacksecond,
			"videoId" : videoId
		},
		dataType : "json",
		success : function(result) {
			if (result != null) {
				$("#no_say_time").val("");
				//TODO
				$("#mute_admin_div").fadeOut();
				
				//$("#black_div").fadeOut();
				$("#shadowUp").fadeOut();
				$("#sayTd" + userId).addClass("say_no");
				$("#say" + userId).val("1");
				var user = new Object();
				user.userId = userId;
				user.gagTime = blacksecond;
				user.videoId = videoId;
				user.loginId = loginId;
				debugger;
				blackMsg(userName, user);
				
			}
		},
		error : function() {
			//TODO
			$("#mute_admin_div").fadeOut();
			
			//$("#black_div").fadeOut();
			$(".sure_div p").text("失败");
			$(".sure_div , .shadowUp").show();
		}
	});
}

function cancleAdd() {
	//TODO
	$("#mute_admin_div").fadeOut();
	
	//$("#black_div").fadeOut();
	$("#shadowUp").fadeOut();
}
// 从黑名单中删除
function say(userId, userName) {
	if (userId == null || userId == "" || userId == undefined) {
		return;
	}
	removeBlackMsg(userId, userName);
	var videoId = $("#videoId1").val();
	$.ajax({
		type : "POST",
		url : "/pc/anchor/delBlackList",
		data : {
			"userId" : userId,
			"videoId" : videoId
		},
		dataType : "json",
		success : function(result) {
			if (result != null) {
				// alert("成功");
				$("#sayTd" + userId).removeClass("say_no").addClass("say");
				$("#say" + userId).val("0");
			}
		},
		error : function() {
			$(".sure_div p").text("黑名单中删除操作异常请稍后。");
			$(".sure_div , .shadowUp").show();
		}
	});
}

/**
 * 用户是否被禁言
 * @param userId
 * @param userName
 * @param type 1禁言 0解禁
 */
//TODO 
function isBlack_(userId, userName, type) {
	if (userId == null || userId == "" || userId == undefined) {
		return;
	}
	var videoId = $("#videoId1").val();
	$.ajax({
		type : "POST",
		url : "/pc/user/isblack.json",
		data : {
			"userId" : userId,
			"videoId" : videoId
		},
		dataType : "json",
		success : function(result) {
			if (result != null) {
				var val = result.data;
				if ((val == 1 && type == 1) || (val == 0 && type == 1)) {//已禁言
					if (type == val) {
						appendBlackMsg(userName);
					} else {
						sayNo(userId, userName);
					}
				} else if ((val == 1 && type == 0) || (val == 0 && type == 0)) {//未禁言
					if (type == val) {
						var msgTxt = "系统消息：" + userName + "未被管理员禁言"
						appendRemoveBlackMsg(msgTxt);
					} else {
						say(userId, userName);
					}
				}
			}
		},
		error : function(result) {
			console.log(result);
		}
	});
}

var userId_g;
var userName_g;
var loginId_g;

/**
 * 用户是否被禁言
 * @param userId
 * @param userName
 * @param type 1禁言 0解禁
 * @param loginId 用户登录Id
 */
function isBlack(userId, userName, type, loginId) {
	userId_g = userId;
	userName_g = userName;
	loginId_g = loginId;
	if (userId == null || userId == "" || userId == undefined) {
		return;
	}
	var videoId = $("#videoId1").val();
	$.ajax({
		type : "POST",
		url : "/pc/user/isblackoradmin.json",
		data : {
			"userId" : userId,
			"videoId" : videoId
		},
		dataType : "json",
		success : function(result) {
			if (result != null) {
				var isBlack = result.data.isBlack;
				var isAdmin = result.data.isAdmin;
				
				//TODO 设置div文本
				var muteTxt = (isBlack == 1) ? "解禁":"禁言";
				var adminTxt = (isAdmin == 1) ? "解除房管":"设为房管";
				$("#muteSpan").text(muteTxt);
				$("#adminSpan").text(adminTxt);
				/*if(muteTxt == "解禁"){
					$(".gag").css("display", "none");
				}*/
				
				$("#mute_admin_div").fadeIn();	
				
				//$("#black_div").fadeIn();
				$("#shadowUp").fadeIn();
			    $("#no_say_time_error").css({"display":"none"});
				
				/*if ((isBlack == 1 && type == 1) || (isBlack == 0 && type == 1)) {//已禁言
					if (type == isBlack) {
						appendBlackMsg(userName);
					} else {
						sayNo(userId, userName);
					}
				} else if ((isBlack == 1 && type == 0) || (isBlack == 0 && type == 0)) {//未禁言
					if (type == isBlack) {
						var msgTxt = "系统消息：" + userName + "未被管理员禁言"
						appendRemoveBlackMsg(msgTxt);
					} else {
						say(userId, userName);
					}
				}*/
			}
		},
		error : function(result) {
			console.log(result);
		}
	});
}

/**
 * 关闭禁言/设置房管div
 */
function closeMADiv(){
	//TODO
	$("#mute_admin_div").fadeOut();
	$("#shadowUp").fadeOut();
	$(".gag").hide();
	$("#adminCheck").attr("checked", "true");
	$(".p-notice").text("*设为房管后，该用户拥有禁言其他用户权限");
	$(".p-notice").css("color", "");
}

function muteOrSetAdmin(){
	
	if($("#muteCheck").attr("checked")){//禁言
		var txt = $("#muteSpan").text();
		if(txt == "禁言"){
			if (userId_g == null && userName_g == "") {
				return;
			}
			$("#black_id").val(userId_g);
			$("#black_name").val(userName_g);
			$("#black_loginId").val(loginId_g);
			addBlackList();
		} else{
			say(userId_g, userName_g);//解禁
		}
	} else if($("#adminCheck").attr("checked")){//房管
		vldFlag = true;
		var txt = $("#adminSpan").text();
		var chatroomId = $("#chatroomId").val();
		if(txt == "设为房管"){
			setAdmin(userId_g, userName_g, chatroomId);
		} else{
			removeAdmin(userId_g, userName_g, chatroomId);
		}
	}
	
	if(!vldFlag){
		return;
	}
	closeMADiv();//关闭操作框
}

/**
 * 设置房管
 * @param userId
 * @param chartRoomId
 */
function setAdmin(userId, userName, chatRoomId){
	if (userId == null || userId == "" || userId == undefined) {
		return;
	}	
	
	$.ajax({
		type : "POST",
		url : "/pc/user/setAdmin",
		data : {
			"userId" : userId,
			"chatRoomId" : chatRoomId
		},
		dataType : "json",
		success : function(returnData) {
			if (returnData.data.result == 1) {
				var user = new Object();
				user.userId = userId;
				user.userName = userName;
				setAdminMsg(userName, user);//设置房管消息红色
			}
		}
	});	
}

/**
 * 移除房管
 * @param userId
 * @param chartRoomId
 */
function removeAdmin(userId, userName, chatRoomId){
	if (userId == null || userId == "" || userId == undefined) {
		return;
	}
	$.ajax({
		type : "POST",
		url : "/pc/user/removeAdmin",
		data : {
			"userId" : userId,
			"chatRoomId" : chatRoomId
		},
		dataType : "json",
		success : function(returnData) {
			if (returnData.data.result == 1) {
				var user = new Object();
				user.userId = userId;
				user.userName = userName;
				removeAdminMsg(userName, user);//移除房管消息蓝色
			}
		},
		error : function() {
			$(".sure_div p").text("黑名单中删除操作异常请稍后。");
			$(".sure_div , .shadowUp").show();
		}
	});
}
