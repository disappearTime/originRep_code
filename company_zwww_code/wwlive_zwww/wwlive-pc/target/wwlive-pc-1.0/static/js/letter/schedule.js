$(function () {
	setInterval("getNoReadLetterNum()", 1000 * 60 * 5);
});
function getNoReadLetterNum() {
    	$.ajax({
			type : "POST",
			url : "/pc/letter/notread",
			data : {
			},
			dataType : "json",
			success : function(data) {
				if (data != null) {
					if (data != undefined && data > 0) {
						$("#letter_msg").show();
						$("#letter_msg").text(data);
					} else {
						$("#letter_msg").hide();
					}
				}
			},
			error : function(result) {
				console.log(result);
			}
		});
    }