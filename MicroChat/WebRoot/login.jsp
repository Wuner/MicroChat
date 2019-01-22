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

<title>管理员登录</title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
<link rel="stylesheet/less" type="text/css" href="./css/pubic.less" />
<link rel="stylesheet" type="text/css" href="./css/jquery-confirm.min.css" />
<link rel="stylesheet/less" type="text/css"
	href="./css/login.less" />
<script src="./js/jquery-3.3.1.min.js"></script>
<script src="./js/less.min.js" type="text/javascript" charset="utf-8"></script>
<script src="./js/common.js" type="text/javascript" charset="utf-8"></script>
<script src="./js/jquery-confirm.min.js" type="text/javascript" charset="utf-8"></script>
</head>

<body>
	<header>
		<h1>
			<img src="./img/logo.png" />管理员登录
		</h1>
	</header>
	<div id="content">
		<input id="account" class="input" type="text" name="account" placeholder="请输入账号" />
		<input id="password" class="input" type="password" name="password" placeholder="请输入密码" /> 
		<input id="next" class="input" type="button" value="登录" />
	</div>
	<script type="text/javascript">
		$("#content").keypress(function(e) {
			var code = event.keyCode;
			if (13 == code) {
				login();
			}
		});
		$("#next").click(function(){
			login();
		});
		function login(){
			var parameters = {"account":$("#account").val(),"password":$("#password").val()};
			$.ajax({
				type : "POST",
				url : message.httpAddress + "user/adminLogin.action",
				data : parameters,
				dataType : "json",
				success : function(data) {
					if(data.code=="200"){
						sessionStorage.setItem("username",$("#account").val());
						window.location.href = "index.jsp";
					}else{
						$.alert({
	                      title: '弹窗',
	                      content: data.msg,
	                      confirmButton: "确定"
		                });
					}
				},
				error : function(msg) {
					console.log(msg);
				}
			});
		}
	</script>
</body>
</html>
