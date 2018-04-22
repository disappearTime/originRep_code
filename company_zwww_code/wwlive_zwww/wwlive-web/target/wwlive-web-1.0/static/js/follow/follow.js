$(function(){
	setFollowText($("#anchorId").val());
	setFollowerCnt($("#anchorId").val());
});

/**
 * 设置粉丝数
 */
function setFollowerCnt(anchorId){
	$.ajax({
		url: $("#web").val() + "/external/app/anchor/followcnt.json?" + $("#listPage").val(),
		type: "POST",
		data: {
			anchorId: anchorId
		},
		success: function(returnData){
			var data = returnData.data;
			if(data != null || data != undefined){
				var fansNum = data.followerCnt + 1;
				/*alert(fansNum);*/
				$("em.fansNum").html(fansNum);
			}
		}
	});
}

/**
 * 关注或取消关注
 * @param followBtn
 */
function followAnchor(followBtn, userId) {
	var btnTxt = $(followBtn).text();
	if (btnTxt == "已关注") {
		unfollow(followBtn, userId);
	} else {
		follow(followBtn, userId);
	}
}

/**
 * 关注
 * @param followBtn
 */
function follow(followBtn, userId) {
    var app = getUrlParam("app");
    console.log($("#listPage").val());
	$.ajax({
		url : $("#web").val() + "/external/user/follow.json?" + $("#listPage").val()+"&app="+app,
		type : "POST",
		data : {
			userId : userId,
			anchorId : $("#anchorId").val(), 
			followFrom : "anchorCenter"
		},
		success : function(returnData) {
			var obj = new Object();
			obj.isCancel = "false";
			obj.anchorId = $("#anchorId").val();
			if (returnData.data.result == 1) {
				obj.result = "true";
				$(followBtn).text("已关注");
			} else if (returnData.data.result == 2) {
				//禁言
				gapTipShow();
				return;
			} else {
				obj.result = "false";
			}
			var live = new Object();
			live.fun = "follow";
			live.data = obj;
			window.stub.jsClient(JSON.stringify(live));
		}
	});
}

/**
 * 取关
 * @param followBtn
 */
function unfollow(followBtn, userId) {
	var app = getUrlParam("app");
	$.ajax({
		url : $("#web").val() + "/app/user/unfollow.json?" + $("#listPage").val()+"&app="+app,
		type : "POST",
		data : {
			userId : userId,
			anchorId : $("#anchorId").val(),
			unfollowFrom : "anchorCenter"
		},
		success : function(returnData) {
			var obj = new Object();
			obj.isCancel = "true";
			obj.anchorId = $("#anchorId").val();
			if (returnData.data.result == 1) {
				obj.result = "true";
				$(followBtn).text("+关注");
			} else {
				obj.result = "false";
			}
			var live = new Object();
			live.fun = "follow";
			live.data = obj;
			window.stub.jsClient(JSON.stringify(live));
		}
	});
}

function setFollowText(anchorId){
	$.ajax({
		url : $("#web").val() + "/external/user/follow/check.json?" + $("#listPage").val(),
		type : "POST",
		data : {
			anchorId : anchorId
		},
		success : function(returnData) {
			if(returnData.data.result == 1){
				$("#followLi").text("已关注");
				$("#followerCnt").text(Number($("#followerCnt").html()) + 1);
			}
		}
	});
}