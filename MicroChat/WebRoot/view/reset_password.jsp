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

<title>My JSP 'reset_password.jsp' starting page</title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
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
			<img src="./img/logo.png" />重置密码？
		</h1>
	</header>
	<div id="content">
		<input id="account" class="input" type="text" name="account" placeholder="请输入你要重置的账号" />
		<input id="next" class="input" type="button" value="下一步" />
		<p style="text-align:center;color:red" id="return_text"></p>
	</div>
	<script type="text/javascript">
		$("#next").click(function() {
			var account = $("#account").val();
			if (account.length == 0) {
				alert("请输入账号");
				return false;
			}
			$.ajax({
				type : "POST",
				url : message.httpAddress + "user/accountResetPassword.action",
				data : {
					"account" : account,
				},
				dataType : "json",
				success : function(data) {
					$("#return_text").text("重置密码为："+data.password);
				},
				error : function(msg) {
					console.log(msg);
				}
			});
		})
	</script>
</body>
</html>
