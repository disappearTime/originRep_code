(function() {
    var pageNo = 1;
    var totalCnt = 0;
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
			noRechargePage();
    		return;
    	}
		var userId = $('#userId').val();
		var origin = $("#origin").val();
        var i = path + "/external/excharge/list.json?" + "t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000));
        // console.log("pageNo: " + pageNo);
        $.ajax({
            type: "POST",
            url: i,
            data:{"pageSize" : pageSize, "pageNum" : pageNo, "IMEI" : "0", "userId" : userId, 'origin' : origin},
            timeout: 9000,
    		dataType : 'json',
            success: function(data) {
            	if (data == null) {
                	removeAutoPaging();
					noRechargePage();
      	            return;
            	}
            	var obj;
            	if ((typeof data) == "string"){
            		obj = JSON.parse(data);
            	} else {
            		obj = data;
            	}

				if(obj.pageInfo == null || obj.pageInfo.totalCount <= 0) {
					removeAutoPaging();
					noRechargePage();
				    return;
				}
            	var totalCnt = obj.pageInfo.totalCount;
            	var exchargeList = obj.exchargeList;
            	console.log("list = " + exchargeList);
                innerMoreHtml(exchargeList, "#data-div");
                if (exchargeList.length < pageSize 
                		|| ((pageNo * pageSize) == totalCnt)) {
                	removeAutoPaging();
      	            return;
      	        }
                pageNo++; 
                d = 0; 
                Lazy.Init();
            },
            error: function(k, j) {
                //alert("网络超时，点击更多重试");
            	console.log('异常...');
            },
        });
    }
    
    function removeAutoPaging() {
        var auto = $("#autopbn");
    	auto.remove();
        window.onscroll = null;
    }
    
    function innerMoreHtml(b, c) {
    	var html = formatObject(b);
        $(c).append(html);
    }

    function formatObject(b) {
    	var html = getHtml(b);
    	return html;
    }
    
    function getHtml(b) {
    	var html = "";
		var origin = $("#origin").val();
		html = "";
		if (origin == 1) {
    		html = "<ul class='dh-record'>";
    		for (i = 0; i < b.length; i++) {
    			var rechargeAmount = b[i].rechargeAmount;
    			var amt = b[i].amt;
    			var createTime = b[i].createTime;
    			html += ("<li><div class='record-left'><span>消耗积分:<b>" + amt + "</b></span><p>" + createTime + "</p></div>" 
    					+ "<em class='dh-num'>" + (rechargeAmount / 100) + "</em></li>");
    		}
		} else {
    		html = "<ul class='dh-record'>";
    		/*console.log(JSON.stringify(b));*/
    		for (i = 0; i < b.length; i++) {
    			var rechargeAmount = b[i].rechargeAmount;
    			var createTime = b[i].createTime;
    			var typeShouru = "";
    			if(b[i].origin==2 || b[i].origin==3 || b[i].origin==4){
                    typeShouru="充值收入";
				}else if(b[i].origin==5){
                    typeShouru="活动收入";
				}else if(b[i].origin==6) {
					typeShouru="开通贵族返钻";
				}else if(b[i].origin==7) {
					typeShouru="续费贵族返钻";
				}
                html += ("<li><div class='record-left'><span>"+typeShouru+"<b>" + "</b></span><p>" + createTime + "</p></div>"
                    + "<em class='dh-num'>" + (rechargeAmount / 100) + "</em></li>");
    			// html += ("<li><span>" + (rechargeAmount / 100) + "</span><em>" + createTime + "</em></li>");
                // html+= "<li>"+
					// "<div>"+
					// 	"<p>"+typeShouru+"</p>"+
                	// 	"<p>"+createTime+"</p>"+
					// "</div>"+
					// "<div>"+
                	// 	(rechargeAmount / 100)+
					// "</div>"+
                // "</li>";
    		}
		}
		html += "</ul>";
    	return html;
    }
    function noRechargePage() {
    	var origin = $("#origin").val();
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		path = "";
    	}
    	var html = "";
		if (origin == 1) {
    		html = "<div class='live-nothing'><img class='no-exchange' src='" + path + "/static/images/no-exchange.png' alt=''><p>暂无兑换记录</p></div>";
		} else {
    		html = "<div class='live-nothing'><img class='no-pay' src='" + path + "/static/images/no-pay.png' alt=''><p>暂无充值记录</p></div>";
		}
	    $("#data-div").append(html);
    }
})();
