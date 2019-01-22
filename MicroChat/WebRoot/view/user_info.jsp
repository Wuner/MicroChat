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
<link rel="stylesheet/less" type="text/css" href="./css/user_info.less" />
<script src="./js/less.min.js" type="text/javascript" charset="utf-8"></script>
<script src="./js/jquery-3.3.1.min.js"></script>
<script src="./js/common.js" type="text/javascript" charset="utf-8"></script>
</head>

<body>
	<header>
	<h1>
		<img src="./img/logo.png" />用户信息
	</h1>
	</header>
	<div id="content">
		<ul></ul>
	</div>
	<script type="text/javascript">
		var pageSize = 9;
		var pageNum = 1;
		$.ajax({
			type : "POST",
			url : message.httpAddress + "user/queryUserinfo.action",
			data : {
				"pageSize" : pageSize,
				"pageNum" : pageNum
			},
			dataType : "json",
			success : function(data) {
				setData(data.list);
			},
			error : function(msg) {
				console.log(msg);
			}
		});
		function setData(list) {
			var html = "";
			for (var i = 0; i < list.length; i++) {
				html += '<li data-account="' + list[i].account + '">' +
					'<div class="user_info">' +
					'<div class="left">' +
					'<h3>' + list[i].nickname + '</h3>' +
					'<h4>' + list[i].account + '</h4>' +
					'<p>' + list[i].sign + '</p>' +
					'<p><span>年龄</span><span style="margin-left:5px;">' + list[i].gender + '</span></p>' +
					'</div>' +
					'<div class="right">' +
					'<img alt="" src="' + message.httpAddress + 'upload/user_icon/' + list[i].icon + '" />' +
					'</div>' +
					'</div>' +
					'<div class="bottom">' +
					'<ul>' +
					'<li>' +
					'<p>关注数</p>' +
					'<p>' + list[i].followNum + '</p>' +
					'</li>' +
					'<li>' +
					'<p>动态数</p>' +
					'<p>' + list[i].dynamicNum + '</p>' +
					'</li>' +
					'<li>' +
					'<p>封禁状态</p>' +
					'<p>' + list[i].state + '</p>' +
					'</li>' +
					'</ul>' +
					'<input class="account_ban" class="input" type="button" value="封禁账号" />' +
					'<input class="query_dynamic" class="input" type="button" value="查看动态" />' +
					'<input class="query_message" class="input" type="button" value="查看消息" />' +
					'</div>' +
					'</li>';
			}
			$("#content ul").html(html);
			$(".account_ban").click(function(){
				window.location.href = "view/account_ban.jsp?account="+$(this).parent().parent().attr("data-account");
			})
			$(".query_dynamic").click(function(){
				window.location.href = "view/dynamic_query.jsp?account="+$(this).parent().parent().attr("data-account");
			})
			$(".query_message").click(function(){
				window.location.href = "view/message_query.jsp?account="+$(this).parent().parent().attr("data-account");
			})
		}
	</script>
</body>
</html>
