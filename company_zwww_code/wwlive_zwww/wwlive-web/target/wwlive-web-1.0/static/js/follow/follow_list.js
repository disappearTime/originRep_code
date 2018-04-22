(function() {
    var pageNo = 1;
    var totalCnt = $("#followCnt").val();
    var pageSize = 10;
    var d = 0;
    setTimeout(g, 500);

    window.onscroll = function() {
        var j = Math.max(document.documentElement.scrollTop, document.body.scrollTop);
        if (j + document.documentElement.clientHeight >= document.documentElement.scrollHeight && !d) {
        	var auto = $("#autopbn");
        	auto.show();
            var l ="<p class=\"loading\"><span class=\"relative\"><b></b>正在加载更多记录</span></p>";
            auto.html(l);
            d = 1;
            setTimeout(g, 2000);
        }
    };
    
    function g() {
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		noInfoPage();
    		return;
    	}

		var commonParams = $("#commonParams").val();
    	if (commonParams == null || commonParams == undefined) {
    		noInfoPage();
    		return;
    	}
    	
        var i = path + "/external/app/user/follow/list/page?" + "dense_fog=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) + "&" + commonParams;
        var timestamp = $("input[name='timestamp']").last().val();
        if(timestamp == null || timestamp == undefined){
        	timestamp = "";
        }
        
        var cnid = getUrlParam("cnid");
        $.ajax({
    		url: i,
    		type: "GET",
    		timeout: 5000,
    		data: {
    			pageNo: pageNo,
    			timestamp: timestamp,
    			cnid: cnid
    		},
    		success: function(returnData){
    			if(returnData != null && returnData != undefined){
    				$("#followList").append(returnData);
    				// removeAutoPaging();
                    //判断是否为蓝版
                    var packname = getUrlParam("packname");
                    if( packname != "" && packname != undefined ){
                        if( packname == "com.mianfei.book" ){
                            $(".follow-flex p em").addClass("skyblue")
                        }
                    }
    				pageNo++;
    				d = 0;
    				Lazy.Init();
    			}
    			var itemCnt = $("input[name='timestamp']").length
    			if(itemCnt >= totalCnt){//itemCnt % 10 != 0 && itemCnt % 10 < pageSize){
    				removeAutoPaging();
    			}
    		}
    	});
    }

    function removeAutoPaging() {
        var auto = $("#autopbn");
    	auto.remove();
        window.onscroll = null;
    }

    function noInfoPage() {
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		path = "";
    	}
    	var html = "<div class='live-nothing'><img class='no-sr' src='" + path + "/static/images/no-gift.png' alt=''><p>暂无礼品详细</p></div>";
        $("#gift-list").append(html);
    }
    
    function getUrlParam(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
        var r = window.location.search.substr(1).match(reg);  //匹配目标参数
        //alert(r);
        if (r != null) return unescape(r[2]); return null; //返回参数值
    }
})();