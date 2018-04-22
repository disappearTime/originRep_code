$(".other-num").click(function() {
	$(this).children("font").hide();
	$(this).children("input").show().focus();
});

$(".recharge-num li").click(
		function() {
			$(this).addClass("active").siblings().removeClass("active");
			$(this).append("<b class='right-mark'></b>").siblings().find(
					".right-mark").remove();
			if (!$(".other-num").hasClass("active")) {
				$(".other-num").children("font").show();
				$(".other-num").children("input").hide();
				$("#real").hide();
			} else {
				var amt = $("#other-amt").val();
				if (amt != "") {
					$("#real").show();
				}
			}
			if(res != null && res != undefined) {
				clearInterval(res);
			}
		});

$(".way-list li").click(function() {
	$(this).addClass("active").siblings().removeClass("active");
	$(this).append("<b class='mark'></b>").siblings().find(".mark").remove();
});
