function saveDay() {
	var birthday = $.trim($("#birthday").val());
	var anchorId = $.trim($("#anchorId").val());
	if (isBlank(anchorId)) {
		return;
	}
	var path = $("#web").val();
	if (path == null || path == undefined) {
		return;
	}
	console.log(birthday + " " + anchorId);
	var submitData = new Object();
	submitData.anchorId = anchorId;
	submitData.birthday = birthday;
	var modifyUrl = $.trim($("#modifyUrl").val());
	$.ajax({
		type : "POST",
		url : path + "/launch/anchor/modify/birthday.json?"
				+ modifyUrl,
		data : submitData,
		timeout : 9000,
		dataType : 'json',
		success : function(data) {
        	if (data == null) {
                return;
        	}
			var value = data.data;
        	if (value == 1) {
        		reloadWebView();
        		return;
        	}
		},
		error : function(k, j) {
			console.log("异常：" + k + " " + j);
		},
	});
}