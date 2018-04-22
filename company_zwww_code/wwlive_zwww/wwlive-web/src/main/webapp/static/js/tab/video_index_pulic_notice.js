$(function (){
	var path = $("#web").val();
	if (path == null || path == undefined) {
		return;
	}
	var extra = "";
	$.ajax({
        type: "POST",
        url: path + "/external/index/todayprenotice.json",
        data:{},
        timeout: 9000,
		dataType : 'json',
        success: function(data) {
        	var code = data.code;
        	if (code == 0) {
            	var result = data.data;
            	if (result != null && result != undefined) {
            		var notices = new Array();
            		notices = result.split("|");
            		for(i = 0;i < notices.length - 1;i++){
            			$("#notice").append("<li><div>" + notices[i] + "</div><em></em></li>");
            		}
            	} else {
            		$("#notice").css("display", "none");
            	}
        	}
        },
        error: function(k, j) {
            //alert("网络超时，点击更多重试");
        	console.log("异常：" + k + " " + j);
        },
    });
})
