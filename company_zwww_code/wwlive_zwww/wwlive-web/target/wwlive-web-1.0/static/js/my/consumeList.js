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
    		return;
    	}
		var userId = $("#userId").val();
		
        var i = path + "/external/user/consume.json?" + "t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000));
        // console.log("pageNo: " + pageNo);
        $.ajax({
            type: "POST",
            url: i,
            data:{"pageSize" : pageSize, "pageNum" : pageNo, "IMEI" : "0", "userId" : userId},
            timeout: 9000,
    		dataType : 'json',
            success: function(data) {
            	if (data == null) {
                	removeAutoPaging();
                	noExpensePage();
      	            return;
            	}
            	var obj;
            	if ((typeof data) == "string") {
            		obj = JSON.parse(data);
            	} else {
            		obj = data;
            	}
            	
            	if (obj.pageInfo == null 
            			|| obj.pageInfo.totalCount <= 0) {
                	removeAutoPaging();
                	noExpensePage();
      	            return;
            	}
            	var totalCnt = obj.pageInfo.totalCount;
            	var expenseList = obj.expenseList;
                innerMoreHtml(expenseList, "#dh-record");
                if (expenseList.length < pageSize || ((pageNo * pageSize) == totalCnt)) {
                	removeAutoPaging();
      	            return;
      	        }
                pageNo++; 
                d = 0; 
                Lazy.Init();
            },
            error: function(k, j) {
                //alert("网络超时，点击更多重试");
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
    	var html = "";
    	var path = $("#web").val();
    	if (path != null && path != undefined) {
    		path += "";
    	}
		html = "";
    	console.log(JSON.stringify(b));
		for (i = 0; i < b.length; i++) {
			var payType = b[i].payType;
			var orderType = b[i].orderType;
			var amt = b[i].amt;

			if(orderType == 5 || orderType == 2){
				title = "贵族购买";
			}else if(orderType == 4) {
				title = "贵族弹幕";
			} else if(orderType == 1) {
                title = "贵族礼物";
            } else {
				title = "礼物赠送";
			}

			html += ("<li><div class='record-left'><span>"+title+"</span><p>" + b[i].goodsName+"*"+ b[i].goodsNum+ "</p></div><div class='record-right'>");
			if (payType == 4) {
				amt = ("<span>-&nbsp" + (amt / 100) + "</span>");
			} else if (payType == 2 || payType == 3) {
				amt = ("<em>" + (amt / 100) + "RMB</em>");
			} else if (payType == 1) {
				amt = ("<em>" + amt + "积分</em>");
			}
			html += (amt + "<p>" + b[i].createTime + "</p></div></li>");
    	}
    	return html;
    }
    function noExpensePage() {
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		path = "";
    	}
		html = "<div class='live-nothing'><img class='no-xf' src='" + path + "/static/images/no-xf.png' alt=''><p>暂无消费记录</p></div>";
        $("#dh-record").append(html);
    }
})();


