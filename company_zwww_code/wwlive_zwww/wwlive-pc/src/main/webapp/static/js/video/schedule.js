$(function() {
	var videoStatus1 = $("#videoStatus").val();
	if (videoStatus1 == 1 || videoStatus1 == 0) {
		setInterval("setRealtimeViewers()", 1000 * 5);
		setInterval("setRealtimeIncome()", 1000 * 60 * 3);
	} else if (videoStatus1 == 4) {
		var videoId = $("#videoId1").val();
		setInterval("viewersInfo(" + videoId + ", 1)", 1000 * 60 * 5);
		setInterval("setRealtimeViewers()", 1000 * 5);
	}
});
function setRealtimeViewers() {
	var videoId = $("#videoId1").val();
	var videoStatus = $("#videoStatus").val();
	$.ajax({
				url : "/live/getRealtimeViewers.json",
				type : "POST",
				data : {
					"videoId" : videoId,
					"videoStatus" : videoStatus
				},
				success : function(resultData) {
					if (resultData.realtimeViewers != null) {
						$("#realtimeViewers").text(resultData.realtimeViewers);
					}
				}
			});
}

function setRealtimeIncome() {
	var videoId = $("#videoId1").val();
	$.ajax({
		url : "/live/getRealtimeIncome.json",
		type : "POST",
		data : {
			"videoId" : videoId
		},
		success : function(resultData) {
			if (resultData.realtimeIncome != null
					&& resultData.realtimeIncome != "") {
				var income = resultData.realtimeIncome;
				if (income != 0) {
					$("#realtimeIncome").text(Math.round(income) / 100);
				}
			}
		}
	});
}