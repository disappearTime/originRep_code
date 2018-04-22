/* 聊天室 */
$(function() {
	console.log("加载中。。。");
	var _secret = $("#_secret").val();
	var targetId = $("#chatroomId").val(); // 目标 Id
	if (_secret != null && _secret != "") {
		// 设置秘钥
		RongIMClient.init(_secret);
		var token = $("#rongToken").val();
		if (token != null && token != '') {
			RongIMClient.connect(token, {
				onSuccess : function(userId) {
					console.log("Login successfully." + userId);
					joinChatRoom(targetId);
				},
				onTokenIncorrect : function() {
					console.log('token无效');
					alert('token无效，无法接收信息');
				},
				onError : function(errorCode) {
					var info = '';
					switch (errorCode) {
					case RongIMLib.ErrorCode.TIMEOUT:
						info = '超时';
						break;
					case RongIMLib.ErrorCode.UNKNOWN_ERROR:
						info = '未知错误';
						break;
					case RongIMLib.ErrorCode.UNACCEPTABLE_PaROTOCOL_VERSION:
						info = '不可接受的协议版本';
						break;
					case RongIMLib.ErrorCode.IDENTIFIER_REJECTED:
						info = 'appkey不正确';
						break;
					case RongIMLib.ErrorCode.SERVER_UNAVAILABLE:
						info = '服务器不可用';
						break;
					}
					console.log(errorCode);
				}
			});

			// 消息监听
			setRongConnection();
		}
		/**
		 * 发送假消息
		 */
		// 定义消息类型,文字消息使用 RongIMLib.TextMessage
		setInterval("chatRoomKeep();", 600000);
	}

});

/**
 *
 * @param message
 */
function chatRoomUserInfo(message) {
    console.log(message);
	var extra = message.content.extra;
	if(extra != undefined && extra != null && extra != '') {
		var roomData = JSON.parse(extra);
		if (roomData.dataType == 9) {// 退出信息不处理
			return;
		} else if (roomData.dataType == 19) {//关注信息
			var user = message.content.user;
			console.log("roomData:");
			console.log(roomData);
			console.log(roomData.dataExtra)
			/*console.log(roomData.dataExtra);*/
			console.log(typeof roomData.dataExtra)
			var dataExtra = JSON.parse(roomData.dataExtra)
			var img = "";
			if (dataExtra.nobleCode == 6){
                img = "<img style='height: 18px;margin: -5px 4px 0 0;vertical-align: middle;' src='/static/images/nobleImages/shendian-right.png'>"
			}else if(dataExtra.nobleCode==5){
                img = "<img style='height: 18px;margin: -5px 4px 0 0;vertical-align: middle;' src='/static/images/nobleImages/zi-right.png'>"
			}else if (dataExtra.nobleCode==4){
                img = "<img style='height: 18px;margin: -5px 4px 0 0;vertical-align: middle;' src='/static/images/nobleImages/mofa-right.png'>"
			}else if (dataExtra.nobleCode==3){
                img = "<img style='height: 18px;margin: -5px 4px 0 0;vertical-align: middle;' src='/static/images/nobleImages/hei-right.png'>"
			}else if(dataExtra.nobleCode==2){
                img = "<img style='height: 18px;margin: -5px 4px 0 0;vertical-align: middle;' src='/static/images/nobleImages/long-right.png'>"
			}else if (dataExtra.nobleCode==1){
                img = "<img style='height: 18px;margin: -5px 4px 0 0;vertical-align: middle;' src='/static/images/nobleImages/sheng-right.png'>"
			}else {
				img = ""
			}
			var li = "<li><div class='join'><span>"+ img + user.name
			+ "<em>关注了主播</em></span>" + "</div></li>";
			$("#chatroom_con").append(li);
			return;
		}
	}

	var user = message.content.user;
	// 如果消息体中有loginId, 则从消息体中获取
	var extra1 = JSON.parse(message.content.extra);
    var dataExtra1 = JSON.parse(extra1.dataExtra);
    var loginId = dataExtra1.loginId;
	if (loginId != undefined && loginId != null) {
	} else if (user == undefined || user == null) {
		loginId = message.senderUserId;
    } else {
        loginId = message.content.user.id;
    }
	$.ajax({
				type : "POST",
				url : "/pc/user/info.json",
				data : {
					"loginId" : loginId
				},
				dataType : "json",
				success : function(result) {
					/*console.log(result);*/
					if (result != null) {
						var userInfo = result.data;
						if (typeof (extra) == "undefined") {
//							var li = txMsg(userInfo, message.content.content,
//									str_time);
//							$("#chatroom_con").append(li);
							// console.log(message.content);
						} else {
							var roomData = JSON.parse(extra);
							console.log("type = " + roomData.dataType);
							if (roomData.dataType == 6) {// 改为 content == null || content == ""
								var li = joinMsg(userInfo);
								$("#chatroom_con").append(li);
							} else if (roomData.dataType == 7) {
								var extraData = JSON.parse(roomData.dataExtra);
								// console.log("extraData = " + JSON.stringify(extraData));
								var isSequence = extraData.isSequence;//isSequence = 0, 非连送; = 1, 连送.
								if(isSequence == 0 || isSequence == "" || isSequence == null){
									// 走非连送礼物的处理分支
									var gift = roomData.dataExtra;
									var content = roomData.dataValue;
									var li = giftMsg(userInfo, content,
											gift);
									$("#chatroom_con").append(li);
								} else if(isSequence == 1){
									// 走连送礼物的处理分支
									//连送礼品
									var li = sequenceGiftMsg(userInfo, extraData);
									$("#chatroom_con").append(li);
								}
							} else if(roomData.dataType == 28 ){//火箭消息通知
                                var userName = "";
                                if (userInfo.userName){
                                    userName = userInfo.userName;
                                }
								var li = rokectMsg(userName);
								$("#chatroom_con").append(li);
							} else if(roomData.dataType == 32 ){//直播间购买贵族通知
                                var userName = "";
                                if (userInfo.userName){
                                	userName = userInfo.userName;
								}
                                var nobleName = dataExtra1.nobleName;
								var li = noblePurchaseMsg(userName,nobleName);
								$("#chatroom_con").append(li);
							} else if(roomData.dataType == 26 ){//弹幕消息
								var li = barrageMsg(userInfo, dataExtra1.content,roomData.dataType);
								$("#chatroom_con").append(li);
							} else if (roomData.dataType == 10) {//普通文本消息
								var li = txMsg(userInfo, message.content.content,roomData.dataType);
								$("#chatroom_con").append(li);
							} else if(roomData.dataType == 2){// 房管禁言普通用户消息
								muteMsg(roomData.dataValue);
							} else if (roomData.dataType == 16){// 机器人聊天消息
								robotMsg(roomData.dataExtra);
							} else if (roomData.dataType == 18){// 主播被禁用消息
								window.location.href = "/login/in"; // 跳转到登录页面
							} else if(roomData.dataType == 1){//红包消息
								/*console.log("here");
								console.log(message.content);*/
								var mes = message.content.content;
                                var li = "<li><div class='join'><span>" + mes + "</span></div></li> ";
                                $("#chatroom_con").append(li);
								/*console.log(roomData);*/
							} else if(roomData.dataType == 23 ){
                                var extraData = JSON.parse(roomData.dataExtra);
                                var openActivity = extraData.openActivity;
                                if(openActivity == 0 ){
                                    $(".levelP").hide(0);
								}else if(openActivity == 1){
                                    $(".levelP").show(0);
                                    $(".levelP .levelMes i:eq(0)").html(0);
                                    $(".levelP .levelMes .lev").html(0);
								}
							} else if (roomData.dataType == 24){//用户发送礼品会将关卡数和钻石数返回
                                var levelNow = $(".levelP .levelMes i:eq(0)").html();//当前关卡数
								var diamondsNow = $(".levelP .levelMes .lev").html();//当前管卡钻石数进度
								var level = roomData.dataExtra.levelNum;
								var diamond = roomData.dataExtra.diamondNum;
								if (level>=levelNow){
                                    $(".levelP .levelMes i:eq(0)").html(level)
                                    $(".levelP .levelMes .lev").html(diamond)
								}
							}else if (roomData.dataType == 35){//翻牌游戏最高奖中奖信息
									console.log(roomData);
                                	gamehighestaward(roomData.dataExtra)
							}else if (roomData.dataType == 34){
									console.log(roomData)
									gameaward(roomData.dataExtra)
							}
						}
						$('#chatroom_con').scrollTop($('#chatroom_con')[0].scrollHeight);

					}
				},
				error : function() {
					// alert("失败");
					$(".sure_div p").text("用户聊天异常请稍后。");
					$(".sure_div , .shadowUp").show();
				}
			});
}

function robotMsg(content){//机器人消息
	var list = content.robotList;
	$.each(list, function(i, robot){
		var userInfo = {
			"userName": robot.nickName,
			"acctType": robot.acctType,
			"follower": robot.isCustomer,
			"userId": robot.userId
		};

		// 将机器人消息展示到页面上
		userInfo.loginId = "0_cx"
		var li = txMsg(userInfo, robot.msg,0);
		$("#chatroom_con").append(li);
		$('#chatroom_con').scrollTop($('#chatroom_con')[0].scrollHeight);
	});
}

function muteMsg(content){//禁言消息
	if (content == null || content == "") {
		return ;
	}
	var li = "<li><p class='red'>" + content + "</p></li> ";
	$("#chatroom_con").append(li);
}
function gamehighestaward(content){//翻牌游戏最高奖
    if (content == null || content == "") {
        return ;
    }
    /*console.log(typeof  content)*/
    var data = JSON.parse(content)
    var li = "<li style='background: #ffffff;color: #FF841B;'><img style='margin-right: 4px;height: 30px;vertical-align: middle;' src='"+ data.giftImg +"' alt=''>"+ data.giftName +"*"+ data.giftCnt +",恭喜"+ data.userName +"在[翻江龙]中获大奖</li> ";
    $("#chatroom_con").append(li);
}
function gameaward(content){//翻牌游戏中奖信息
    if (content == null || content == "") {
        return ;
    }
    var data = JSON.parse(content)
    if (data.pickTimes==8){//8连抽
        var li = "<li style='background: #ffffff;color: #e420eb;' >系统：[翻江龙]爆大奖，恭喜&nbsp;&nbsp;"+ data.userName +"&nbsp;&nbsp;8连抽时&nbsp;&nbsp;获&nbsp;&nbsp;"+ data.giftName +"*"+ data.giftCnt +"</li>";
	}else {
        var li = "<li style='background: #ffffff;color: #e420eb;'>系统：[翻江龙]爆大奖，恭喜&nbsp;&nbsp;"+ data.userName +"&nbsp;&nbsp;获&nbsp;&nbsp;"+ data.giftName +"*"+ data.giftCnt +"</li>";
	}

    $("#chatroom_con").append(li);
}
function setRongConnection() {
	RongIMClient.setConnectionStatusListener({
		onChanged : function(status) {
			switch (status) {
			// 链接成功
			case RongIMLib.ConnectionStatus.CONNECTED:
				console.log('链接成功');
				break;
			// 正在链接
			case RongIMLib.ConnectionStatus.CONNECTING:
				console.log('正在链接');
				break;
			// 重新链接
			case RongIMLib.ConnectionStatus.DISCONNECTED:
				console.log('断开连接');
				break;
			// 其他设备登录
			case RongIMLib.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT:
				console.log('其他设备登录');
				break;
			// 网络不可用
			case RongIMLib.ConnectionStatus.NETWORK_UNAVAILABLE:
				console.log('网络不可用');
				break;
			}
		}
	});

	// 消息监听器
	RongIMClient.setOnReceiveMessageListener({
		// 接收到的消息
		onReceived : function(message) {
			// 判断消息类型
			switch (message.messageType) {
			case RongIMClient.MessageType.TextMessage:
				// 发送的消息内容将会被打印
				// console.log(message.content.content);
				chatRoomUserInfo(message);
				break;
			case RongIMClient.MessageType.VoiceMessage:
				// 对声音进行预加载
				// message.content.content 格式为 AMR 格式的 base64 码
				RongIMLib.RongIMVoice.preLoaded(message.content.content);
				break;
			case RongIMClient.MessageType.ImageMessage:
				// do something...
				break;
			case RongIMClient.MessageType.DiscussionNotificationMessage:
				// do something...
				break;
			case RongIMClient.MessageType.LocationMessage:
				// do something...
				break;
			case RongIMClient.MessageType.RichContentMessage:
				// do something...
				break;
			case RongIMClient.MessageType.DiscussionNotificationMessage:
				// do something...
				break;
			case RongIMClient.MessageType.InformationNotificationMessage:
				// do something...
				break;
			case RongIMClient.MessageType.ContactNotificationMessage:
				// do something...
				break;
			case RongIMClient.MessageType.ProfileNotificationMessage:
				// do something...
				break;
			case RongIMClient.MessageType.CommandNotificationMessage:
				// do something...
				break;
			case RongIMClient.MessageType.CommandMessage:
				// do something...
				break;
			case RongIMClient.MessageType.UnknownMessage:
				// do something...
				break;
			default:
				// 自定义消息
				// do something...
			}
		}
	});
}

function sendMsg(targetId, msg) {
	var conversationtype = RongIMLib.ConversationType.CHATROOM; // 群聊
	RongIMClient.getInstance().sendMessage(conversationtype, targetId, msg, {
		// 发送消息成功
		onSuccess : function(message) {
			// message 为发送的消息对象并且包含服务器返回的消息唯一Id和发送消息时间戳
			console.log("Send successfully");
		},
		onError : function(errorCode, message) {
			var info = '';
			switch (errorCode) {
			case RongIMLib.ErrorCode.TIMEOUT:
				info = '超时';
				break;
			case RongIMLib.ErrorCode.UNKNOWN_ERROR:
				info = '未知错误';
				break;
			case RongIMLib.ErrorCode.REJECTED_BY_BLACKLIST:
				info = '在黑名单中，无法向对方发送消息';
				break;
			case RongIMLib.ErrorCode.NOT_IN_DISCUSSION:
				info = '不在讨论组中';
				break;
			case RongIMLib.ErrorCode.NOT_IN_GROUP:
				info = '不在群组中';
				break;
			case RongIMLib.ErrorCode.NOT_IN_CHATROOM:
				info = '不在聊天室中';
				joinChatRoom(targetId);
				break;
			default:
				info = "未知";
				break;
			}
			console.log('发送失败:' + info);
		}
	});
}

function joinChatRoom(chatRoomId) {// 聊天室 Id。
	var count = 0;// 拉取最近聊天最多 20 条。
	RongIMClient.getInstance().joinChatRoom(chatRoomId, count, {
	  onSuccess: function() {
	       // 加入聊天室成功。
		  console.log("join success");
	  },
	  onError: function(error) {
	    // 加入聊天室失败
		  console.log("join fail");
	  }
	});
}

function txMsg(userInfo, content,dataType) {//普通文本消息
	if (content == null || content == "") {
		return joinMsg(userInfo);
	}
	var headImg = "<img src='"
			+ userInfo.headImg
			+ "' alt='' width='40' height='40' class='td_pic' onerror='this.src=\"/static/images/user_cover.png\"'/>";
	if (userInfo.follower > 0) {
		headImg = headImg + nobleImg("star1",userInfo);
				// + "<img src='../static/images/star.png' alt='' class='star1'>";
	}

	var acctType = userInfo.acctType;
	var li = "";
	var gag = "";
	var userName = userInfo.userName;
	//土豪勋章
	var localRich = "";
	var medals = userInfo.medals;
	if(medals!=undefined && medals!=null && medals!="" && medals.length>0){
		for (var j=0;j<medals.length;j++){
			if (medals[j] == "土豪勋章"){
                localRich = "<img class='localRich' src='/static/images/localRich.png' alt=''>"
			}
		}
	}
	var sex = getUserSex(userInfo);

	//房管特殊标记
	var isAdmin = userInfo.isAdmin;
	/*if(isAdmin == 1){
		userName = "<font color='#f00'>[房管]" + userName + "</font>";
	}*/
	if(isAdmin == 1){
		userName = "<b class='b-fg'>房管</b>" + userName + sex;
	}
	if(acctType == 1){
		userName = "<b class='b-cg'>超管</b>" + userName + sex;
		content = "<p>" + content + "</p>";
	} else if(isAdmin != 1){
		userName = "<i class='pt-user'>" + userName + sex + "</i>";
		content = "<p class='pt-black'>" + content + "</p>";
	}
	//是否有土豪勋章的判断。
	if(dataType == 10){
		userName = localRich + userName;
	}
//	else{
//		userName = localRich + "弹幕" + userName;
//	}

	if(acctType == 0){
		gag = "<em><img src='/static/images/setting.png' onclick='isBlack("
			+ userInfo.userId + ", \"" + userInfo.userName + "\", 1, \"" + userInfo.loginId + "\");'</em>";
	}
	li = "<li><div class='user_cover' onclick='userInfo(" + userInfo.userId + ")'>"
		+ headImg
		+ "</div>"
		+ "<div class='user_word user-manage'>"
		+ "<h3>"
		+ "<i onclick='userInfo(" + userInfo.userId + ")'>"
		+ userName
		+ "</i>"
		+ gag
		+ "</h3>"
		+ "<p>"
		+ "<em>"
		+ content
		+ "</em>"
		+ "</p>"
		+ "</div><img src='/static/images/chat_jt.png' alt='' class='horn'/></li>";
	return li;
}

function sequenceGiftMsg(userInfo, extraData) {
	// console.log("开始创造li");
	//extraData类似于{"giftName":"啪啪啪", "giveTimes":2, "isSequence":0, "giftUrl":"...", "acctType": 1}r
	var headImg = "<img src='"
		+ userInfo.headImg
		+ "' alt='' width='40' height='40' class='td_pic' onerror='this.src=\"/static/images/user_cover.png\"'/>";
	if (userInfo.follower > 0) {
		headImg = headImg + nobleImg("star1",userInfo);
		// + "<img src='../static/images/star.png' alt='' class='star1'>";
	}
	var giftHtml = "";
	if (extraData != null && extraData != '' && extraData != undefined) {
		var tmpGift = extraData;
		giftHtml = "<img src='"
			+ tmpGift.giftUrl
			+ "' alt='' width='56px'/>";
	}
    //土豪勋章
    var localRich = "";
    var medals = userInfo.medals;
    if(medals!=undefined && medals!=null && medals!="" && medals.length>0){
        for (var j=0;j<medals.length;j++){
            if (medals[j] == "土豪勋章"){
                localRich = "<img class='localRich' src='/static/images/localRich.png' alt=''>"
            }
        }
    }
    /*localRich = "<img class='localRich' src='/static/images/localRich.png' alt=''>"*/
	var userName = userInfo.userName;
	var gag = "";

	var sex = getUserSex(userInfo);
	//房管特殊标记
	var isAdmin = userInfo.isAdmin;
	if(isAdmin == 1){
		userName = "<b class='b-fg'>房管</b>" + userName;
	}

	if (userInfo.acctType == 1) {
		userName ="<b class='b-cg'>超管</b>" + userName;
//		gag = "<em>" + str_time + "</em>";
	} else {
		gag = "<em><img src='/static/images/setting.png' onclick='isBlack(" + userInfo.userId + ", \"" + userInfo.userName + "\", 1);'</em>";
	}
	userName = localRich + userName;
	var li = "<li><div class='user_cover' onclick='userInfo(" + userInfo.userId + ")'>"
		+ headImg
		+ "</div>"
		+ "<div class='user_word'>"
		+ "<h3>"
		+ "<i onclick='userInfo(" + userInfo.userId + ")'>"
		+ userName + sex
		+ "</i>"
		+ gag
		+ "</h3>"
		+ "<p class='give-gift'>"
		+ "<em>"
		+ "送给主播" + extraData.giftName
		+ "</em>"
		+ giftHtml
		+ "<font>×"
		+ extraData.giveTimes
		+ "</font>"
		+ "</p>"
		+ "</div><img src='/static/images/chat_jt.png' alt='' class='horn'/></li>";
	return li;
}

function giftMsg(userInfo, content, gift) {
	var tmpGift = {};
	var headImg = "<img src='"
			+ userInfo.headImg
			+ "' alt='' width='40' height='40' class='td_pic' onerror='this.src=\"/static/images/user_cover.png\"'/>";
	if (userInfo.follower > 0) {
		headImg = headImg  + nobleImg("star1",userInfo);
				// + "<img src='../static/images/star.png' alt='' class='star1'>";
	}
	var giftHtml = "";
	if (gift != null && gift != '' && gift != undefined) {
		tmpGift = JSON.parse(gift);
		giftHtml = "<img src='"
		+ tmpGift.giftUrl
		+ "' alt='' width='56px'/>";
	}
    //土豪勋章
    var localRich = "";
    var medals = userInfo.medals;
    if(medals!=undefined && medals!=null && medals!="" && medals.length>0){
        for (var j=0;j<medals.length;j++){
            if (medals[j] == "土豪勋章"){
                localRich = "<img class='localRich' src='/static/images/localRich.png' alt=''>"
            }
        }
    }
    /*localRich = "<img class='localRich' src='/static/images/localRich.png' alt=''>"*/
	var userName = userInfo.userName;
	var gag = "";

	var sex = getUserSex(userInfo);

	//房管特殊标记
	var isAdmin = userInfo.isAdmin;
	if(isAdmin == 1){
		userName = "<b class='b-fg'>房管</b>" +"<i class='user-blue'>"+ userName + sex +"</i>";
	}

	if (userInfo.acctType == 1) {
		userName ="<b class='b-cg'>超管</b>" +"<i class='user-blue'>"+ userName +"</i>";
//		gag = "<em>" + str_time + "</em>";
	} else if(isAdmin != 1){
		userName = "<i class='pt-user'>" + userName + sex + "</i>";
	}
	userName = localRich + userName;
	if(userInfo.acctType == 0){
		gag = "<em><img src='/static/images/setting.png' onclick='isBlack("
			+ userInfo.userId + ", \"" + userInfo.userName + "\", 1);'</em>";
	}
	var li = "<li><div class='user_cover' onclick='userInfo(" + userInfo.userId + ")'>"
			+ headImg
			+ "</div>"
			+ "<div class='user_word user-manage'>"
			+ "<h3>"
			+ "<i onclick='userInfo(" + userInfo.userId + ")'>"
		    + userName
		    + "</i>"
			+ gag
			+ "</h3>"
			+ "<p class='give-gift'>"
			+ "<em>"
		    + "送给主播" + tmpGift.giftName
			+ "</em>"
			+ giftHtml
        + "<font>×"
        + tmpGift.giveTimes
        + "</font>"
			+ "</p>"
			+ "</div><img src='/static/images/chat_jt.png' alt='' class='horn'/></li>";

	return li;
}

function joinMsg(userInfo) {
	var li = "";
	var color = "";
	var userName = userInfo.userName;

	//房管特殊标记
	var isAdmin = userInfo.isAdmin;
	if(isAdmin == 1){
		userName = "<b class='b-fg'>房管</b>" + userName;
	}

	if(userInfo.acctType == 1){
		color = "#f00";
		userName = "<b class='b-cg'>超管</b>" + userName;
	} else {
		color = "#e420eb";
    }
    var img = nobleImg("adminNobleImg",userInfo);
    if(userInfo.nobelCode > 0 ) {
        clas = "join1";
    }else {
        clas = "join";
    }
	li = "<li><div class='"+ clas +"'><span>" + img + userName
	+ "<em>加入直播间</em></span>" + "</div></li>";
	return li;
}

function blackMsg(userName, user) {
	// <li><p class="red">系统消息：用户昵称被管理员禁言</p></li>
	var msgTxt = appendBlackMsg(userName);
	var targetId = $("#chatroomId").val(); // 目标 Id
	var msg = createMsg(2, msgTxt, user);
	sendMsg(targetId, msg);

}

/**
 * 设置房管消息
 * @param userName
 * @param user
 */
function setAdminMsg(userName, user){
	var msgTxt = appendSetAdminMsg(userName);
	/*var targetId = $("#chatroomId").val(); // 目标 Id
	var msg = createMsg('14', '', user);
	sendMsg(targetId, msg);*/
}

function appendSetAdminMsg(userName) {
	var msgTxt = "系统消息：" + userName + "已被设为房管";
	var li = "<li><p class='red'>" + msgTxt + "</p></li> ";
	$("#chatroom_con").append(li);
	$('#chatroom_con').scrollTop($('#chatroom_con')[0].scrollHeight);
	return msgTxt;
}

function appendBlackMsg(userName) {
	var msgTxt = "系统消息：" + userName + "已被管理员禁言"
	var li = "<li><p class='red'>" + msgTxt + "</p></li> ";
	$("#chatroom_con").append(li);
	$('#chatroom_con').scrollTop($('#chatroom_con')[0].scrollHeight);
	return msgTxt;
}

/**
 * 移除房管消息
 * @param userId
 * @param userName
 */
function removeAdminMsg(userName, user) {
	var msgTxt = "系统消息：" + userName + "已被解除房管";
	appendRemoveAdminMsg(msgTxt);

	/*var targetId = $("#chatroomId").val(); // 目标 Id
	var msg = createMsg('15', '', user);
	sendMsg(targetId, msg);*/

}

function appendRemoveAdminMsg(msgTxt) {
	var li = "<li><p class='red'>" + msgTxt + "</p></li> ";
	$("#chatroom_con").append(li);
	$('#chatroom_con').scrollTop($('#chatroom_con')[0].scrollHeight);
}

function removeBlackMsg(userId, userName) {
	var msgTxt = "系统消息：" + userName + "已被管理员解禁"
	appendRemoveBlackMsg(msgTxt);

	var targetId = $("#chatroomId").val(); // 目标 Id
	var msg = createMsg(8, userId, '');
	sendMsg(targetId, msg);

}

function appendRemoveBlackMsg(msgTxt) {
	var li = "<li><p class='red'>" + msgTxt + "</p></li> ";
	$("#chatroom_con").append(li);
	$('#chatroom_con').scrollTop($('#chatroom_con')[0].scrollHeight);
}

function chatRoomKeep() {
	var targetId = $("#chatroomId").val(); // 目标 Id
	var msg = createMsg(0, '', '');
	sendMsg(targetId, msg);

}

function stopLiveMsg() {
	var targetId = $("#chatroomId").val(); // 目标 Id
	var msg = createMsg(5, '', '');
	sendMsg(targetId, msg);
}

function createMsg(dataType, dataValue, dataExtra) {
	var roomData = {
			dataType : dataType,
			dataValue : dataValue,
			dataExtra : dataExtra
	};
	var jsonMsg = JSON.stringify(roomData);
	console.log(typeof jsonMsg)
	console.log(jsonMsg);
	var msg = new RongIMLib.TextMessage({
		content : "",
		extra : jsonMsg
	});
	return msg;
}

//贵族
function nobleImg(clas,userInfo) {
	console.log("userInfo=" + JSON.stringify(userInfo));
    var img = "";
    if(userInfo.nobelCode == 6){
        img = "<img class='"+clas+"' src='/static/images/nobleImages/shendian-right.png'>";
    }
    if(userInfo.nobelCode == 5){
        img = "<img class='"+clas+"' src='/static/images/nobleImages/zi-right.png'>";
    }
    if(userInfo.nobelCode == 4){
        img = "<img class='"+clas+"' src='/static/images/nobleImages/mofa-right.png'>";
    }
    if(userInfo.nobelCode == 3){
        img = "<img class='"+clas+"' src='/static/images/nobleImages/hei-right.png'>";
    }
    if(userInfo.nobelCode == 2){
        img = "<img class='"+clas+"' src='/static/images/nobleImages/long-right.png'>";
    }
    if(userInfo.nobelCode == 1){
        img = "<img class='"+clas+"' src='/static/images/nobleImages/sheng-right.png'>";
    }
    return img;
}

function getUserSex(userInfo) {
    var sex = "";
    if(userInfo.sex == 0){
        sex = "<img style='margin-left: 4px;transform: translateY(3px);' src='/static/images/sex-woman.png'/>";
    }
    if(userInfo.sex == 1){
        sex = "<img style='margin-left: 4px;transform: translateY(3px);' src='/static/images/sex-man.png'/>";
    }
    return sex;
}

function barrageMsg(userInfo, content,dataType) {//http://wiki.fcread.com/pages/viewpage.action?pageId=10256578 弹幕消息 26

    if (content == null || content == "") {
        return joinMsg(userInfo);
    }
    var headImg = "<img src='"
        + userInfo.headImg
        + "' alt='' width='40' height='40' class='td_pic' onerror='this.src=\"/static/images/user_cover.png\"'/>";
    if (userInfo.follower > 0) {
        headImg = headImg + nobleImg("star1",userInfo);
        // + "<img src='../static/images/star.png' alt='' class='star1'>";
    }

    var acctType = userInfo.acctType;
    var li = "";
    var gag = "";
    var userName = userInfo.userName;
    //土豪勋章
    var localRich = "";
    var medals = userInfo.medals;
    if(medals!=undefined && medals!=null && medals!="" && medals.length>0){
        for (var j=0;j<medals.length;j++){
            if (medals[j] == "土豪勋章"){
                localRich = "<img class='localRich' src='/static/images/localRich.png' alt=''>"
            }
        }
    }
    var sex = getUserSex(userInfo);

    //房管特殊标记
    var isAdmin = userInfo.isAdmin;
	/*if(isAdmin == 1){
	 userName = "<font color='#f00'>[房管]" + userName + "</font>";
	 }*/
    if(isAdmin == 1){
        userName = "<b class='b-fg'>房管</b>" + userName + sex;
        content = "<p><img style='width:16px;vertical-align: middle;margin: -2px 4px 0 0;' src='/static/images/danmu.png' />" + content + "</p>";
    }
    if(acctType == 1){
        userName = "<b class='b-cg'>超管</b>" + userName + sex;
        content = "<p><img style='width:16px;vertical-align: middle;margin: -2px 4px 0 0;' src='/static/images/danmu.png' />" + content + "</p>";
    } else if(isAdmin != 1){
        userName = "<i class='pt-user'>" + userName + sex + "</i>";
        content = "<p class='pt-black'><img style='width:16px;vertical-align: middle;margin: -2px 4px 0 0;' src='/static/images/danmu.png' />" + content + "</p>";
    }
    //是否有土豪勋章的判断。
    if(dataType == 10){
        userName = localRich + userName;
    }
//	else{
//		userName = localRich + "弹幕" + userName;
//	}

    if(acctType == 0){
        gag = "<em><img src='/static/images/setting.png' onclick='isBlack("
            + userInfo.userId + ", \"" + userInfo.userName + "\", 1, \"" + userInfo.loginId + "\");'</em>";
    }
    li = "<li><div class='user_cover' onclick='userInfo(" + userInfo.userId + ")'>"
        + headImg
        + "</div>"
        + "<div class='user_word user-manage'>"
        + "<h3>"
        + "<i onclick='userInfo(" + userInfo.userId + ")'>"
        + userName
        + "</i>"
        + gag
        + "</h3>"
        + "<p>"
        + "<em>"
        + content
        + "</em>"
        + "</p>"
        + "</div><img src='/static/images/chat_jt.png' alt='' class='horn'/></li>";
    return li;
}

function noblePurchaseMsg(userName,nobleType) {//http://wiki.fcread.com/pages/viewpage.action?pageId=10256578 直播间贵族购买消息 32
	var li = "<li><div class='systemMes'><img src='/static/images/xttz.png' alt=''>"+ userName +"开通了<i>"+ nobleType +"</i></div></li>"
	return li;
}

function rokectMsg(userName) {//http://wiki.fcread.com/pages/viewpage.action?pageId=10256578 送火箭礼物消息 28
	/*alert("发送火箭消息");*/
    var li = "<li><div class='systemMes'><img src='/static/images/xttz.png' alt=''>"+ userName +"发送了一个火箭，主播将上热门10min</div></li>"
	return li;
}
/*//普通的系统消息
function publicTxt(msg) {
    var li = "<li><p class='red'>" + msg + "</p></li> ";
    $("#chatroom_con").append(li);
}*/



