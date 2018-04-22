/* 播放视频  */
$(document).ready(
		function() {
			var videoStatus = $("#videoStatus").val();
			var videoId = $("#videoId1").val();
			var standURL = $("#standURL").val();
			if (videoStatus != null && videoStatus != ""
					&& (videoStatus == 1 || videoStatus == 0)) {
				//
				// $("#video_source").attr("src", standURL);

				jwplayer('mediaspace').setup({
					'flashplayer' : '/static/player.swf',
					'file' : standURL,
					'controlbar' : 'bottom',
					'width' : '800',
					'height' : '520',
					'volume' : '0'
				});
			}
			if (videoStatus == 4) {
				viewersInfo(videoId, 1);
			}
			/*  */
			/*
			 * $("#jquery_jplayer_1").jPlayer({ ready: function () {
			 * $(this).jPlayer("setMedia", { m4v: "mi4.m4v", ogv: "mi4.ogv",
			 * webmv: "mi4.webm", poster: "mi4.png" }); }, swfPath: "js",
			 * supplied: "webmv, ogv, m4v", size: { width: "800px", height:
			 * "500px", } });
			 */
		});

function stopVideo(videoId, streamName) {
	if (videoId == null && streamName == null) {
		return;
	}
	if (videoId == '' || streamName == '') {
		return;
	}
	var vdoid_url = $("#vdoid").val();
	var vdoid = 0;
	if (vdoid_url != null && vdoid_url.trim() != "") {
		vdoid = 1;
	}

	$.ajax({
		type : "POST",
		url : "/live/stop",
		data : {
			"videoId" : videoId,
			"streamName" : streamName,
			"vdoid" : vdoid
		},
		dataType : "json",
		success : function(result) {
			if (result.data != null && result.data.trim() != "") {
				stopLiveMsg();
				window.location.href = result.data;

			} else {
				$(".sure_div p").text("数据同步出现问题，请联系管理员。");
				$(".sure_div , .shadowUp").show();
				$("#del_live").fadeOut();
			}
		},
		error : function() {
			$(".sure_div p").text("停止视频直播异常请稍后。");
			$(".sure_div , .shadowUp").show();
			$("#del_live").fadeOut();
		}
	});
}