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
<link rel="stylesheet" type="text/css" href="./css/page.css" />
<link rel="stylesheet/less" type="text/css" href="./css/pubic.less" />
<link rel="stylesheet" type="text/css" href="./css/iconfont.css" />
<link rel="stylesheet" type="text/css" href="./css/ckin.min.css" />
<link rel="stylesheet/less" type="text/css"
	href="./css/message_query.less" />
<script src="./js/less.min.js" type="text/javascript" charset="utf-8"></script>
<script src="./js/jquery-3.3.1.min.js"></script>
<script src="./js/common.js" type="text/javascript" charset="utf-8"></script>
<script src="./js/jqPaginator.min.js" type="text/javascript"></script>
<script src="./js/page.js" type="text/javascript" charset="utf-8"></script>

</head>

<body>
	<header>
	<h3>
		<img src="./img/logo.png" />消息查询
	</h3>
	<input id="content" type="text" name="content" placeholder="请输入你要查询的内容" />
	<i id="select" class="iconfont icon-zheng-triangle"></i> <span
		id="btnSearch">搜索</span>
	<ul id="hidden">
		<li><span id=""> 精确类型 </span> <label><input
				class="accurateType" type="radio" name="accurateType"
				checked="checked" value="1" />模糊搜索 </label> <label><input
				class="accurateType" type="radio" name="accurateType" value="2" />精确搜索
		</label></li>
		<li><span id=""> 搜索类型 </span> <label><input
				class="searchType" type="radio" name="searchType" checked="checked"
				value="1" />全部</label> <label><input class="searchType"
				type="radio" name="searchType" value="2" />账号 </label> <label><input
				class="searchType" type="radio" name="searchType" value="3" />文本 </label> <label><input
				class="searchType" type="radio" name="searchType" value="4" />时间 </label></li>
		<li><span id=""> 消息类型 </span> <label><input
				class="messageType" type="radio" name="messageType"
				checked="checked" value="1" />全部 </label> <label><input
				class="messageType" type="radio" name="messageType" value="2" />文本</label>
			<label><input class="messageType" type="radio"
				name="messageType" value="3" />图片 </label> <label><input
				class="messageType" type="radio" name="messageType" value="4" />语音
		</label> <label><input class="messageType" type="radio"
				name="messageType" value="5" />视频 </label></li>
		<li><span id=""> 会话类型 </span> <label><input
				class="sessionType" type="radio" name="sessionType"
				checked="checked" value="1" />全部 </label> <label><input
				class="sessionType" type="radio" name="sessionType" value="2" />P2P</label>
			<label><input class="sessionType" type="radio"
				name="sessionType" value="3" />Team </label></li>
	</ul>
	</header>
	<div id="data">
		<ul id="waterfall">
		</ul>
	</div>
	<div style="width:1px;height:1px;" id="playerQT"></div>
	<div id="page" style="clear:both">
		<ul class="pagination" id="pagination">
		</ul>
	</div>
	<script src="./js/ckin.js"></script>
	<script src="./js/clipboard.min.js" type="text/javascript" charset="utf-8"></script>
	<script type="text/javascript">
		var url = location.search;
		var arr = url.split("=");
		if(url.length!=0){
			$(".accurateType").eq(0).attr("checked","");
			$(".accurateType").eq(1).attr("checked","checked");
			$(".searchType").eq(0).attr("checked","");
			$(".searchType").eq(1).attr("checked","checked");
			$("#content").val(arr[1]);
			search();
		}
		$("#select").click(function() {
			if ($("#select").hasClass("select")) {
				$("#select").removeClass("icon-sanjiaoxing");
				$("#select").removeClass("select");
				$("#select").addClass("icon-zheng-triangle");
				$("#hidden").hide();
			} else {
				$("#select").addClass("select");
				$("#select").removeClass("icon-zheng-triangle");
				$("#select").addClass("icon-sanjiaoxing");
				$("#hidden").show();
			}
		});
		$("#btnSearch").click(function() {
			search();
		});
		$("header").keypress(function(e) {
			var code = event.keyCode;
			if (13 == code) {
				search();
			}
		});
		function search() {
			$("#select").removeClass("icon-sanjiaoxing");
			$("#select").removeClass("select");
			$("#select").addClass("icon-zheng-triangle");
			$("#hidden").hide();
			var pageNum = 1;
			var searchType = $(".searchType:checked").val();
			var messageType = $(".messageType:checked").val();
			var sessionType = $(".sessionType:checked").val();
			var accurateType = $(".accurateType:checked").val();
			var content = $("#content").val();
			var pageSize = 9;
			var parameters = {
				"pageSize" : pageSize,
				"pageNum" : pageNum,
				"content" : content,
				"searchType" : searchType,
				"messageType" : messageType,
				"sessionType" : sessionType,
				"accurateType" : accurateType,
			}
			$.ajax({
				type : "POST",
				url : message.httpAddress + "message/queryMessage.action",
				data : parameters,
				dataType : "json",
				success : function(data) {
					setData(data.list);
					if (data.count != 0)
						$.page(parseInt(data.count), pageSize, 5, function(num) {
							pageNum = num;
							var parameters = {
								"pageSize" : pageSize,
								"pageNum" : pageNum,
								"content" : content,
								"searchType" : searchType,
								"messageType" : messageType,
								"sessionType" : sessionType,
								"accurateType" : accurateType,
							}
							$.ajax({
								type : "POST",
								url : message.httpAddress + "message/queryMessage.action",
								data : parameters,
								dataType : "json",
								success : function(data) {
									setData(data.list);
								},
								error : function(msg) {
									console.log(msg);
								}
							});
						});
					else
						$("#pagination").html("");
				},
				error : function(msg) {
					console.log(msg);
				}
			});
		}
		function setData(data) {
			var htmlString = "";
			for (var i = 0; i < data.length; i++) {
				console.log(data[i].account)
				var html = "";
				var typeHtml = "";
				switch (data[i].messageType) {
				case "voice":
					html = '<p><span class="title">语音路径</span><span class="content" title="' + message.httpAddress + 'upload/message/' + data[i].content + '">' + message.httpAddress + 'upload/message/' + data[i].content + '</span></p>';
					typeHtml = '<li class="voice">';
					break;
				case "text":
					html = '<p><span class="title">消息内容</span><span class="content" title="' + data[i].content + '">' + data[i].content + '</span></p>';
					typeHtml = '<li class="text">';
					break;
				case "image":
					html = '<p><img src="' + message.httpAddress + 'upload/message/' + data[i].content + '" /></p>';
					typeHtml = '<li class="image">';
					break;
				case "video":
					html = '<div class="videoShow"><video data-ckin="default" data-overlay="1" src="' + message.httpAddress + 'upload/message/' + data[i].content + '"></video></div>';
					typeHtml = '<li class="video">';
					break;
	
				}
				htmlString += typeHtml +
					'<h3><span>' + data[i].messageType + '</span></h3>' +
					'<p><span class="title">接收者</span><span class="content">' + data[i].account + '</span></p>' +
					'<p><span class="title">发送者</span><span class="content">' + data[i].fromAccount + '</span></p>' +
					'<p><span class="title">发送时间</span><span class="content">' + data[i].sendTime + '</span></p>' +
					'<div id="messageContent">' +
						html +
					'</div>' +
					'</li>';
			}
			$("#waterfall").html(htmlString);
			$.video();
			$("html,body").animate({scrollTop:0}, 0);
			var self;
			var clipboard = new ClipboardJS('.content', {
		        text: function(e) {
		        	self = e;
		            return $(e).text();
		        }
		    });
		    clipboard.on('success', function(e) {
		    	$(self).append('<span class="toast">复制成功</span>');
		    	setTimeout(function(){
		    		$(".toast").remove();
		    	},1000);
		    });
		
		    clipboard.on('error', function(e) {
		        console.log(e);
		    });
		}
	</script>
</body>
</html>
