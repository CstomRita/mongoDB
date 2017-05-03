package com.tgb.upload.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.ServiceMode;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.FileRenamePolicy;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.Part;

@SuppressWarnings("serial")
@ServiceMode
public class fileServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String method=request.getParameter("method");
		if(method.equals("upload")){
			upload(request,response);
		}else if(method.equals("delete")){
			delete(request,response);
		}else if(method.equals("showList")){
			showList(request,response);
		}else if(method.equals("downloadFile")){
			downloadFile(request,response);
		}
	
	}
	
	
	/**
	 * 鏂囦欢涓婁紶
	 * @param request
	 * @param response
	 */
	private void upload(HttpServletRequest request ,HttpServletResponse response){
		try{
			Mongo mongo = new Mongo();
			DB db = mongo.getDB("test");
			GridFS gridFS= new GridFS(db,"fs");
			GridFSFile file = null;
			FileRenamePolicy rfrp = new DefaultFileRenamePolicy();
			MultipartParser mp = new MultipartParser(request, 1024 * 1024 * 1024,
					true, true, "GB18030");//鈥淕B18030鈥濆繀椤诲拰jsp缂栫爜鏍煎紡鐩稿悓锛屼笉鐒朵細浜х敓涓枃涔辩爜
			FilePart filePart = null;
			Part part = null;
			int pot =0;
			while ((part = mp.readNextPart()) != null) {
				if (part.isFile()) {// it's a file part
					filePart = (FilePart) part;
					filePart.setRenamePolicy(rfrp);
					String fileName = filePart.getFileName();
					InputStream in = filePart.getInputStream();
					pot = fileName.lastIndexOf(".");
					file = gridFS.createFile(in);// 鍒涘缓gridfs鏂囦欢
					file.put("filename", fileName);
					file.put("userId", 1);
					file.put("uploadDate", new Date());
					file.put("contentType", fileName.substring(pot));
					file.save();
				}
			}
			request.setAttribute("uploadResult", "涓婁紶鎴愬姛!");
			request.getRequestDispatcher("/uploadResult.jsp").forward(request, response);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	/**
	 * 鏂囦欢鍒犻櫎
	 * @param request
	 * @param response
	 */
	private void delete(HttpServletRequest request ,HttpServletResponse response){
		try{
			String fileId = (String)request.getParameter("id");
			Mongo mongo = new Mongo();
			DB db = mongo.getDB("test");
			GridFS gridFS= new GridFS(db,"fs");
			
			ObjectId objId = new ObjectId(fileId);
			gridFS.remove(objId);
			showList(request,response);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	/**
	 * 鏌ョ湅鏂囦欢鍒楄〃
	 * @param request
	 * @param response
	 */
	private void showList(HttpServletRequest request ,HttpServletResponse response){
		try{
			Mongo mongo = new Mongo();
			DB db = mongo.getDB("test");
			GridFS gridFS= new GridFS(db,"fs");
			
			
			DBObject query=new BasicDBObject("userId", 1);
			List<GridFSDBFile> gridFSDBFileList = gridFS.find(query);
				
			request.setAttribute("gridFSDBFileList", gridFSDBFileList);
			
			request.getRequestDispatcher("/fileList.jsp").forward(request, response);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	/**
	 * 鏌ョ湅鍗曚釜鏂囦欢銆佷笅杞芥枃浠�
	 * @param request
	 * @param response
	 */
	private void downloadFile(HttpServletRequest request ,HttpServletResponse response){
		try{
			String fileId = request.getParameter("id");
			Mongo mongo = new Mongo();
			DB db = mongo.getDB("test");
			GridFS gridFS= new GridFS(db,"fs");
			ObjectId objId = new ObjectId(fileId);
			GridFSDBFile gridFSDBFile =(GridFSDBFile)gridFS.findOne(objId);
			
			if (gridFSDBFile != null) {

				OutputStream sos = response.getOutputStream();

				response.setContentType("application/octet-stream");
				// 鑾峰彇鍘熸枃浠跺悕
				String name = (String) gridFSDBFile.get("filename");
				String fileName = new String(name.getBytes("GBK"), "ISO8859-1");

				// 璁剧疆涓嬭浇鏂囦欢鍚�
				response.addHeader("Content-Disposition", "attachment; filename=\""
						+ fileName + "\"");

				// 鍚戝鎴风杈撳嚭鏂囦欢
				gridFSDBFile.writeTo(sos);
				sos.flush();
				sos.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}


}
