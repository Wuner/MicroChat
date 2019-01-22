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

<title>后台管理</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
<link rel="stylesheet" type="text/css" href="css/iconfont.css" />
<link rel="stylesheet/less" type="text/css" href="css/pubic.less" />
<link rel="stylesheet/less" type="text/css" href="css/index.less" />
<script src="js/less.min.js"></script>
</head>

<body>
	<header>
	<h3>
		<img src="img/logo.png" />后台管理 | <span id="title">首页</span>
	</h3>
	<div id="account_info">
		<span id="nickname"></span><span id="exit">注销</span>
	</div>
	</header>
	<div id="content">
		<div id="list">
			<ul>
				<li id="account_management" class="first_class"><i
					class="iconfont icon-iconzh1"></i>账号管理
					<ul style="display: none;">
						<li class="second_class"><i class="iconfont icon-dian"></i>账号封禁</li>
						<li class="second_class"><i class="iconfont icon-dian"></i>账号解封</li>
						<li class="second_class"><i class="iconfont icon-dian"></i>重置密码</li>
					</ul>
				</li>
				<li id="user_management" class="first_class"><i
					class="iconfont icon-iconzh1"></i>用户管理
					<ul style="display: none;">
						<li class="second_class"><i class="iconfont icon-dian"></i>用户注册量</li>
						<li class="second_class"><i class="iconfont icon-dian"></i>用户信息</li>
					</ul>
				</li>
				<li id="message_management" class="first_class"><i
					class="iconfont icon-buoumaotubiao14"></i>消息管理
					<ul style="display: none;">
						<li class="second_class"><i class="iconfont icon-dian"></i>消息查询</li>
						<li class="second_class"><i class="iconfont icon-dian"></i>设消息敏感词汇</li>
						<li class="second_class"><i class="iconfont icon-dian"></i>消息推送</li>
					</ul>
				</li>
				<li id="dynamic_management" class="first_class"><i
					class="iconfont icon-dongtai"></i>动态管理
					<ul style="display: none;">
						<li class="second_class"><i class="iconfont icon-dian"></i>动态查询</li>
					</ul></li>
			</ul>
			<div class="list_bottom"></div>
		</div>
		<div id="list_content">
			<iframe id="iframe" src="view/index.jsp"
				style="overflow: hidden;width: 100%;height: 100%;"></iframe>
		</div>
	</div>
	<script src="js/jquery-3.3.1.min.js" type="text/javascript"
		charset="utf-8"></script>
	<script src="js/common.js" type="text/javascript" charset="utf-8"></script>
	<script type="text/javascript">
		if(sessionStorage.getItem("username")){
			$("#nickname").text(sessionStorage.getItem("username"));
		}else{
			alert("你未登录！");
			window.location.href = "login.jsp";
		}
		$("#exit").click(function(){
			sessionStorage.clear();
			window.location.href = "login.jsp";
		});
		if (sessionStorage.getItem("iframe_url")) {
			$("#list_content iframe").attr("src", sessionStorage.getItem("iframe_url"));
			$("#title").text(sessionStorage.getItem("iframe_title"));
			$(".first_class").eq(sessionStorage.getItem("index")).addClass("select").siblings().removeClass("select");
			$(".first_class").eq(sessionStorage.getItem("index")).siblings().children("ul").hide();
			$(".first_class").eq(sessionStorage.getItem("index")).children("ul").show();
			$(".first_class").eq(sessionStorage.getItem("index")).children("ul").children(".second_class").eq(sessionStorage.getItem("second_index")).addClass("select_second");
		}
		$(".first_class").hover(function() {
			var index = $(this).index();
			if (!$(".first_class").eq(index).hasClass("select")) {
				$(".first_class").eq(index).addClass("hover");
			}
		}, function() {
			$(".first_class").removeClass("hover");
		}).click(function() {
			var index = $(this).index();
			setIframe("view/index.jsp", "首页");
			$(".select_second").removeClass("select_second");
			if ($(".first_class").eq(index).hasClass("select")) {
				$(".first_class").eq(index).removeClass("select");
				$(".first_class").eq(index).children("ul").hide();
			} else {
				sessionStorage.setItem("index", index);
				$(".first_class").eq(index).addClass("select").siblings().removeClass("select");
				$(".first_class").eq(index).siblings().children("ul").hide();
				$(".first_class").eq(index).children("ul").show();
			}
		});
		$("#account_management .second_class").hover(function() {
			var index = $(this).index();
			$("#account_management .second_class").eq(index).addClass("hover");
		}, function() {
			$("#account_management .second_class").removeClass("hover");
		}).click(function(e) {
			e.stopPropagation();
			var index = $(this).index();
			sessionStorage.setItem("second_index", index);
			$("#account_management .second_class").eq(index).addClass("select_second").siblings().removeClass("select_second");
			switch (index) {
			case 0:
				setIframe("view/account_ban.jsp", "账号封禁");
				break;
			case 1:
				setIframe("view/account_decapsulation.jsp", "账号解封");
				break;
			case 2:
				setIframe("view/reset_password.jsp", "重置密码");
				break;
			default:
				break;
			}
		});
		$("#user_management .second_class").hover(function() {
			var index = $(this).index();
			$("#user_management .second_class").eq(index).addClass("hover");
		}, function() {
			$("#user_management .second_class").removeClass("hover");
		}).click(function(e) {
			e.stopPropagation();
			var index = $(this).index();
			sessionStorage.setItem("second_index", index);
			$("#user_management .second_class").eq(index).addClass("select_second").siblings().removeClass("select_second");
			switch (index) {
			case 0:
				setIframe("view/user_register_num.jsp", "用户注册量");
				break;
			case 1:
				setIframe("view/user_info.jsp", "用户信息");
				break;
			default:
				break;
			}
		});
		$("#message_management .second_class").hover(function() {
			var index = $(this).index();
			$("#message_management .second_class").eq(index).addClass("hover");
		}, function() {
			$("#message_management .second_class").removeClass("hover");
		}).click(function(e) {
			e.stopPropagation();
			var index = $(this).index();
			sessionStorage.setItem("second_index", index);
			$("#message_management .second_class").eq(index).addClass("select_second").siblings().removeClass("select_second");
			switch (index) {
			case 0:
				setIframe("view/message_query.jsp", "消息查询");
				break;
			default:
				break;
			}
		});
		$("#dynamic_management .second_class").hover(function() {
			var index = $(this).index();
			$("#dynamic_management .second_class").eq(index).addClass("hover");
		}, function() {
			$("#dynamic_management .second_class").removeClass("hover");
		}).click(function(e) {
			e.stopPropagation();
			var index = $(this).index();
			sessionStorage.setItem("second_index", index);
			$("#dynamic_management .second_class").eq(index).addClass("select_second").siblings().removeClass("select_second");
			switch (index) {
			case 0:
				var height = $(window).height() - 50;
				localStorage.setItem("height",height);
				setIframe("view/dynamic_query.jsp", "动态查询");
				break;
			default:
				break;
			}
		});
	</script>
</body>
</html>
