/* 广告 */
var adDataMap = new Map();
var stopMap = new Map();
var sendMsgs = new Array();
$(function() {
	initAD();
});
function initAD() {
	// 获得广告信息
	$.ajax({
		type : "POST",
		url : "/pc/ad/get",
		dataType : 'JSON',
		success : function(result) {
			var adData = result.data;
			if (adData instanceof Array) {
				if (adData.length > 0) {
					for (var i = 0; i < adData.length; i++) {
						var json = adData[i];
						// var json = JSON.parse(data);
						var advId = json.advId;
						var urlsdata = json.urlsdata;
						for (var j = 0; j < urlsdata.length; j++) {
							var adName= urlsdata[0].adname;
							var id = urlsdata[0].id;
							if (advId == "live-video") {
								$("#select_middle").append(
										"<option value='" + id + "." + advId + "'>" + adName
												+ "</option>");
								adDataMap.set(id + "", json);
								stopMap.set(id + "." + advId, advId);
							} else if (advId == "live-top") {
								$("#select_top").append(
										"<option value='" + id + "." + advId + "'>" + adName
												+ "</option>");
								adDataMap.set(id + "", json);
								stopMap.set(id + "." + advId, advId);
							} else if (advId == "live-bottom") {
								$("#select_bottom").append(
										"<option value='" + id + "." + advId + "'>" + adName
												+ "</option>");
								adDataMap.set(id + "", json);
								stopMap.set(id + "." + advId, advId);

							}
						}
					}
				}
			}
		},
		error : function() {
//			$(".sure_div p").text("加载广告异常，请联系管理员。");
//			$(".sure_div , .shadowUp").show();
		}
	});
}
function removeMsg(timeId, msgId) {
	$("#" + timeId).val("");
	$("#" + msgId).text("");
	$("#" + msgId).css({"display":"none"});
}
//调起
function broadcast() {
//	$("#word_ad1").css({
//		"display" : "block"
//	});
    $(".shadowUp").fadeIn();
	if (adDataMap.size == 0) {
		initAD();
	}
	sendMsgs = new Array();
	$("#select_top option:first").prop("selected", 'selected');
	$("#select_middle option:first").prop("selected", 'selected');
	$("#select_bottom option:first").prop("selected", 'selected');
	$("#word_ad2").css({
		"display" : "none"
	});

	$("#url_ad2").css({
		"display" : "none"
	});
	$("#video_ad1").css({
		"display" : "none"
	});
	$("#video_ad2").css({
		"display" : "none"
	});
	$("#video_play").val("");
	var videoId = $("#videoId1").val();
	$.ajax({
		type : "POST",
		url : "/pc/ad/init",
		data : {"videoId":videoId},
		dataType : 'JSON',
		success : function(result) {
			if (result.sendInfo != 1) {
				for (var adType = 0; adType < 3; adType++) {
					var tmp = result['ad' + adType];
					if (tmp != null && tmp != '' && tmp != 'undifend') { 
//						var type = result['adType'];
//						type = parseInt(type);
						var id = parseInt(tmp['id']);
						switch (adType) {
							case 0:
								$("#select_top").find("option[value='" + id + ".live-top']").attr("selected", true);
								$("#word_ad2").css({
									"display" : "block"
								});
								break;
							case 1:
								$("#select_middle").find("option[value='" + id + ".live-video']").attr("selected", true);
								var time = tmp.time;
								if (time == null || time == "") {
									return;
								}
								var minute = Math.round(time / 1000 / 60);
								$("#video_ad1").css({
									"display" : "none"
								});
								$("#video_ad2").css({
									"display" : "block"
								});
								$("#video_play").text(minute);
								
								break;
							case 2:
								$("#select_bottom").find("option[value='" + id + ".live-bottom']").attr("selected", true);
								$("#url_ad2").css({
									"display" : "block"
								});
								break;
						}
						
					}
				}
			}
			$("#gag_ad").fadeIn();
		},
		error : function() {
			$(".sure_div p").text("系统错误，请联系管理员。");
			$(".sure_div , .shadowUp").show();
		}
	});
	

}
function cancleAD() {
	$("#gag_ad").fadeOut();
    $(".shadowUp").fadeOut();
}

/*
 * 发送广告
 */
function sendAD() {
	if (!isEmty("word_time")) {
		if (!isCorrect("word_time", "word_error_msg")) {
			return;
		}
	}
	if (!isEmty("video_time")) {
		if (!isCorrect("video_time", "video_error_msg")) {
			return;
		}
	}
	if (!isEmty("url_time")) {
		if (!isCorrect("url_time", "url_error_msg")) {
			return;
		}
	}
	var sends = getADs();// 广告
	if (typeof(sends) == "undefined" || sends.length <= 0) {
		$("#tips").text("未选择广告！");
		$("#tips").css({"color":"red"});
		return;
	}
//	console.log(sends);
	var data = JSON.stringify(sends);
	var videoId = $("#videoId1").val();
	$("#gag_ad").fadeOut();
	$.ajax({
		type : "POST",
		url : "/pc/ad/send",
		data : {"data":data, "videoId":videoId},
		dataType : "json",
		success : function(result) {
			var code = result.code;
			if (code == 0) {
				$(".sure_div p").text("发送成功！");
				$(".sure_div , .shadowUp").show();
				
				//var ids = $("select option:selected").val();
				//if (ids.length > 0) {}
				/*for (var i = 0; i < ids.length; i++) {
					var obj = adDataMap.get(ids[0]);
		         	sends.push(obj);
				}*/
				if (sendMsgs.length > 0) {
		         	var roomData = {'dataType':'3','dataValue':JSON.stringify(sendMsgs).replace(/"/g, "'"),'dataExtra':''};
		         	var jsonMsg = JSON.stringify(roomData);
					var msg = new RongIMLib.TextMessage({content:"",extra:jsonMsg});
					var targetId = $("#chatroomId").val(); // 目标 Id 
		         	sendMsg(targetId, msg);
					console.log(jsonMsg);
				}
			} else if (code == 1) {
				var toolTip = result.toolTip;
				$(".sure_div p").text(toolTip);
				$(".sure_div , .shadowUp").show();
			}
			
		},
		error : function() {
			$(".sure_div p").text("系统错误，请联系管理员。");
			$(".sure_div , .shadowUp").show();
		}

	});
	
}

function getADs() {
	var sends = new Array();// 广告
	var ADInfo = getADInfo("word_ad2", "word_time", "word_error_msg", "select_top", "0");
	if (ADInfo != null) {
		sends.push(ADInfo);
	}
	var ADInfo = getADInfo("video_ad2", "video_time", "video_error_msg", "select_middle", "1");
	if (ADInfo != null) {
		sends.push(ADInfo);
	}
	var ADInfo = getADInfo("url_ad2", "url_time", "url_error_msg", "select_bottom", "2");
	if (ADInfo != null) {
		sends.push(ADInfo);
	}
	return sends;
}

function getADInfo(adType, adTime, adMsg, selecetId, selectedType) {
	if ($("#" + adType).css("display") === 'none') {
		var time = $("#" + adTime).val();
		if (time == null || time == "") {
			return null;
		}
		time *= 60;
		var ADInfo = selectedVal(selecetId, selectedType, time);
		return ADInfo;
	}
	return null;
}
//选择广告
function selectedVal(selectedId, selectedType, time) {
	var value = $("#" + selectedId).val();
	if (value == null || value == "" || value == "--请选择--") {
		return null;
	}
	value = value.split(".")[0];
	var obj = adDataMap.get(value);
	if (obj == null) {
		return null;
	}
	var videoId = $("#videoId1").val();
	var roomNum = $("#roomNum").val();
	//var ADInfo = new ADInfo(videoId, obj);

	function ADInfo(channelNum, versionNum, adId, adType) {
		this.videoId = $("#videoId1").val();
		this.anchorId = $("#anchorId").val();
		this.channelNum = channelNum;
		this.versionNum = versionNum;
		this.adId = adId;
		this.adType = adType;
		this.roomNum = $("#roomNum").val();
		this.time = time;
	}
	if (selectedId == 'select_middle') {
		obj['time'] = time;
	}
	sendMsgs.push(obj);
	var ADInfo = new ADInfo(obj.cnid, obj.vsersion, value, selectedType);
	return ADInfo;
}

function errMsg(timeId, msgId) {
	var reg = new RegExp("^[0-9]*$");
	var wordTime = $("#" + timeId).val();
	if ((wordTime != null && wordTime != "") 
			&& !reg.test(wordTime)) {
		$("#" + msgId).text("请输入数字!");
		return false;
	}
	return true;
}

function isEmty(id) {
	var val = $("#" + id).val();
	if (val != null && val != "") {
		return false;
	}
	
	return true;
}

function isCorrect(id, msgId) {
	var val = $("#" + id).val();
	if (!errMsg(val, msgId)) {
		return false;
	}
	if (parseInt(val) > 1440) {
		$("#" + msgId).text("设置时间过长，请设置在一天以内");
		$("#" + msgId).css({"display":"block"});
		return false;
	}
	return true;
}

function selected(id, type, adType, showId) {
	$("#tips").text("选择要播放的广告");
	$("#tips").css({"color":"black"});
	
	var videoId = $("#videoId1").val();
	if (id == null || id == "" || id == "--请选择--") {
		$("#" + showId).css({
			"display" : "none"
		});
		if (showId == 'video_ad2') {
			$("#video_ad1").css({
				"display" : "none"
			});
		}
		return;
	}

	if (adType === null || adType === "") {
		return;
	}
	$.ajax({
		type : "POST",
		url : "/pc/ad/hassend",
		data : {
			"id" : id,
			"videoId" : videoId,
			"adType" : adType
		},
		dataType : "json",
		success : function(result) {
			var sendInfo = result.sendInfo;
			var num = parseInt(sendInfo);
			if (typeof (num) != 'number') {
				$(".sure_div p").text("系统错误，请联系管理员。");
				$(".sure_div , .shadowUp").show();
				return;
			}
			if (num == 0) {
				if (type == "select_middle") {
					var time = result.time;
					if (time == null || time == "") {
						return;
					}
					var minute = Math.round(time / 1000 / 60);
					$("#video_ad1").css({
						"display" : "none"
					});
					$("#video_ad2").css({
						"display" : "block"
					});
					$("#video_play").text(minute);
				} else if (type == "select_top") {
					$("#word_ad2").css({
						"display" : "block"
					});
				} else if (type == "select_bottom") {
					$("#url_ad2").css({
						"display" : "block"
					});
				}
			} else {
				if (type == "select_middle") {
					$("#video_ad1").css({
						"display" : "block"
					});
					$("#video_ad2").css({
						"display" : "none"
					});
				} else if (type == "select_top") {
					$("#word_ad2").css({
						"display" : "none"
					});
				} else if (type == "select_bottom") {
					$("#url_ad2").css({
						"display" : "none"
					});

				}
			}
		},
		error : function() {
			$(".sure_div p").text("系统错误，请联系管理员。");
			$(".sure_div , .shadowUp").show();
		}

	});
}

//停止播放广告
function stopAD(input) {
	var id;
	if (input == 0) {
		id = $("#select_top").val();
	}else if (input == 1) {
		id = $("#select_middle").val();
	}else if (input == 2) {
		id = $("#select_bottom").val();
	}
	var videoId = $("#videoId1").val();
	var tmp = id.split(".")[0];
	$.ajax({
		type : "POST",
		url : "/pc/ad/stop",
		data : {
			"id" : tmp,
			"videoId" : videoId,
			"adType" : input
		},
		dataType : "json",
		success : function(result) {
			var stopInfo = result.stopInfo;
			var num = parseInt(stopInfo);
			if (typeof (num) != 'number') {
				$(".sure_div p").text("系统错误，请联系管理员。");
				$(".sure_div , .shadowUp").show();
				return;
			}
			if (input == 0) {
				$("#select_top option:first").attr("selected", 'selected');
				$("#word_ad2").css({
					"display" : "none"
				});
			}else if (input == 1) {
				$("#select_middle option:first").attr("selected", 'selected');
				$("#video_ad2").css({
					"display" : "none"
				});
			}else if (input == 2) {
				$("#select_bottom option:first").attr("selected", 'selected');
				$("#url_ad2").css({
					"display" : "none"
				});
			}

			var obj = stopMap.get(id);
         	var roomData = {'dataType':'4','dataValue':obj,'dataExtra':''};
         	var jsonMsg = JSON.stringify(roomData);
			var msg = new RongIMLib.TextMessage({content:"",extra:jsonMsg});

         	var targetId = $("#chatroomId").val(); // 目标 Id 
         	sendMsg(targetId, msg);
			console.log(jsonMsg);
		},
		error : function() {
			$(".sure_div p").text("系统错误，请联系管理员。");
			$(".sure_div , .shadowUp").show();
		}

	});
}

$(".sure_inp input").click(function(){
    $(".sure_div p").text("");
	$(".sure_div , .shadowUp").hide();
	//window.location.reload();
})
