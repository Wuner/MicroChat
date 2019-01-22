<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<base href="<%=basePath%>">

<title></title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
<link rel="stylesheet/less" type="text/css" href="./css/pubic.less" />
<link rel="stylesheet/less" type="text/css"
	href="./css/account_ban.less" />
<script src="./js/less.min.js" type="text/javascript" charset="utf-8"></script>
<script src="./js/jquery-3.3.1.min.js"></script>
<script src="./js/common.js" type="text/javascript" charset="utf-8"></script>

</head>

<body>
	<header>
		<h1>
			<img src="./img/logo.png" />封号，从这里开始
		</h1>
	</header>
	<div id="content">
		<input id="account" class="input" type="text" name="account"
			placeholder="请输入要封禁的账号" />
		<div id="time">
			<input type="number" name="day" id="day" value="" />天<input
				type="number" name="hour" id="hour" value="" />小时
		</div>
		<input id="next" class="input" type="button" value="下一步" />
	</div>
	<script type="text/javascript">
		var url = location.search;
		var arr = url.split("=");
		if(url.length!=0){
			$("#account").val(arr[1]);
		}
		$("#next").click(function() {
			var day = $("#day").val();
			var hour = $("#hour").val();
			var account = $("#account").val();
			var regu = "^([0-9]*[.0-9])$"; // 小数测试
			var re = new RegExp(regu);
			if (day < 0) {
				alert("不能输入负数");
				return false;
			} else if (hour < 0) {
				alert("不能输入负数");
				return false;
			} else if (account.length == 0) {
				alert("请输入账号");
				return false;
			} else if (day.length == 0 && hour.length == 0) {
				alert("请输入要封禁的时长");
				return false;
			} else {
				if (day.length == 0) {
					day = "0";
				}
				if (hour.length == 0) {
					hour = "0";
				}
				if (hour.search(re) == -1) {
					alert("不能输入小数，请输入整数");
					return false;
				}
				if (day.search(re) == -1) {
					alert("不能输入小数，请输入整数");
					return false;
				}
			}
			$.ajax({
				type : "POST",
				url : message.httpAddress + "user/accountBan.action",
				data : {
					"account" : account,
					"day" : day,
					"hour" : hour
				},
				dataType : "json",
				success : function(data) {
					alert(data.msg);
				},
				error : function(msg) {
					console.log(msg);
				}
			});
		})
	</script>
</body>
</html>
