package org.heath.controller;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.heath.utils.CheckSumBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("CheckSum")
@ResponseBody
public class GetCheckSumController {

	@RequestMapping("getCheckSum.do")
	private void GetCheckSum(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("utf-8");
		String appSecret = request.getParameter("appSecret");
		String timestamp = request.getParameter("timestamp");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = null;
		try {
			String check = CheckSumBuilder.getCheckSum(appSecret, "heath", timestamp);
			out = response.getWriter();
			out.print(check);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
