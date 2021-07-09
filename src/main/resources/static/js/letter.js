$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");
	// 获取发送方内容和文本内容
	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		CONTEXT_PATH + "/letter/send",
		{"toName":toName, "content":content},
		// 处理返回的结果
		function(data) {
			data = $.parseJSON(data);
			if (data.code == 0) {
				$("#hintBody").text("发送成功")
			}
			else {
				$("#hintBody").text(data.msg);
			}
			/*刷新界面*/
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload()
			}, 2000);
		}
	)
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}