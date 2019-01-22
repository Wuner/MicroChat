package org.heath.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.heath.utils.RandomCode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import net.sf.json.JSONArray;

@Controller
@RequestMapping("upload")
@ResponseBody
public class UploadController {
	
	@RequestMapping("fileUpload.action")
	private String fileUpload(HttpServletRequest request, @RequestParam("file") MultipartFile file, @RequestParam("folderPath") String folderPath) throws IOException {
		String fileName = "error";
		try {
			fileName = file.getOriginalFilename();
			fileName = "MicroChat_" + RandomCode.getRandomCode() + "_" + fileName;
			folderPath = request.getServletContext().getRealPath(folderPath);
			InputStream input = file.getInputStream();
			File folder = new File(folderPath);
			judeDirExists(folder);
			OutputStream outputStream = new FileOutputStream(
					folderPath + File.separator + fileName);
			byte[] b = new byte[1024];
			while ((input.read(b)) != -1) {
				outputStream.write(b);
			}
			input.close();
			outputStream.close();
			return fileName;
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
	
	@RequestMapping("filesUpload.action")
	private Map<String, Object> filesUpload(HttpServletRequest request, @RequestParam("files") MultipartFile[] files, @RequestParam("folderPath") String folderPath) throws IOException {
		JSONArray fileNames = new JSONArray();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		folderPath = request.getServletContext().getRealPath(folderPath);
		try {
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getOriginalFilename();
				fileName = "MicroChat_" + RandomCode.getRandomCode() + "_" + fileName;
				InputStream input = files[i].getInputStream();
				File folder = new File(folderPath);
				judeDirExists(folder);
				OutputStream outputStream = new FileOutputStream(
						folderPath + File.separator + fileName);
				byte[] b = new byte[1024];
				while ((input.read(b)) != -1) {
					outputStream.write(b);
				}
				input.close();
				outputStream.close();
				fileNames.add(fileName);
			}
			returnMap.put("code", "200");
			returnMap.put("msg", "success");
			returnMap.put("fileNames", fileNames);
			return returnMap;
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "414");
			returnMap.put("msg", "error");
			return returnMap;
		}
	}

	// 判断文件夹是否存在
	public static void judeDirExists(File file) {

		if (file.exists()) {
			if (file.isDirectory()) {
				System.out.println("dir exists");
			} else {
				System.out.println("the same name file exists, can not create dir");
			}
		} else {
			System.out.println("dir not exists, create it ...");
			file.mkdir();
		}

	}

}
