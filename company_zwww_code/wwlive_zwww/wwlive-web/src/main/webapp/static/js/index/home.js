$(function() {
	getNameAndCompleteTask();
});
function getNameAndCompleteTask() {
	var userId = getUrlParam('userId');
	nameAndTask(userId);
}

function nameAndTask(userId) {
	if(isBlank(userId)) {
		return;
	}
	userId = parseInt(userId);
	if (userId <= 0) {
		return;
	}
	var version = $("#version").val();
	if (version != null && version > "2.0.1") {
		return;
	}
	var path = $("#web").val();
	if (path == null || path == undefined) {
		return;
	}
	$.ajax({
		type:"POST",
		url : path + "/external/user/get/name.json",
		data:{"userId" : userId},
		dataType : 'json',
		success : function(result) {
			if (result == null) {
				return;
			}
			var userName = result.userName;
			var origin = result.origin;
			var loginId = result.loginId;
			if (isBlank(loginId) || isBlank(userName)
					 || isBlank(origin)) {
				return;
			}
			$("#userName").val(userName);
			if(origin != 0) {//创新版修改昵称
				return;
			}
			if (isDefaultName(userName)) {
				fadeIn();
			}
		},
		error : function(e) {
			console.log(e);
		}
	});
}

function modifyName() {
	var userId = $("#userId").val();
	var nickName = $("#nickName").val();
	var path = $("#web").val();
	if (path == null || path == undefined) {
		fadeOut();
		return;
	}
	if (!isCorrectName(nickName)) {
		//$("#confirm").attr("disabled", true);
		return;
	}
	$.ajax({
		type:"POST",
		url : path + "/external/user/modify/name.json",
		data:{"userId" : userId, "userName" : nickName},
		dataType : 'json',
		success : function(result) {
			if (result == null 
					|| result == undefined
					|| result == "") {
		    	$("#err_msg").css({"color":"red"});
		        $("#err_msg").text('该昵称已存在！');
				return;
			}
			var code = result.code;
			if (code == 1) {
		    	$("#err_msg").css({"color":"red"});
		        $("#err_msg").text('该昵称已存在！');
				return;
			} else if (code == 3) {
		    	$("#err_msg").css({"color":"red"});
		        $("#err_msg").text('昵称可由汉字、字母、数字、下划线组成，3-8个字');
				return;
			}
			fadeOut();
		},
		error : function(e) {
			console.log(e);
		}
	});
}

function isDefaultName(userName) {
	return /^[M|m]+[0-9]*$/.test(userName);
}

function isBlank(string) {
	if (string == null || string == undefined) {
		return true;
	}
	if (string.replace(/(^s*)|(s*$)/g, "").length ==0) {
		return true;
	}
}

function fadeIn() {
	$("#shadowUp").fadeIn();
	$("#make-name").fadeIn();
}

function fadeOut() {
	$("#shadowUp").fadeOut();
	$("#make-name").fadeOut();
}
function isCorrectName(nickName) {
	if (isBlank(nickName)) {
		$("#err_msg").text('请输入昵称');
		return false;
	}
	if (isDefaultName(nickName)) {
		$("#err_msg").text('昵称请勿M+纯数字的形式，可由汉字、字母、数字组成，3-8个字');
		return false;
	}
	var rex = new RegExp("^[a-zA-Z0-9_\u4E00-\uFA29]{3,8}$");
    
	if (!rex.test(nickName)) {
        $("#err_msg").text('昵称可由汉字、字母、数字、下划线组成，3-8个字 '); 
		return false;
	}
	return true;
}
function removeErr(){
    $("#err_msg").text('');
	$("#err_msg").removeAttr('style');
	$("#err_msg").addClass("write-name");
}

function checkName() {
	var nickName = $("#nickName").val();
	if (!isCorrectName(nickName)) {
		//$("#confirm").attr("disabled", true);
		return;
	} else {
	    $("#err_msg").text('');
		$("#err_msg").removeAttr('style');
		//$("#confirm").attr("disabled", false);
	}
}