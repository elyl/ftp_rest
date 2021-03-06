package car.tp2;

import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.net.ftp.*;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

@Path("/ftp")
public class FTPResource {

	private FTPClient ftp;
	private String base_url;
	
	FTPResource()
	{
		this.ftp = new FTPClient();
		try
		{
			ftp.connect("", 21);
			ftp.login("",  "");
			base_url = "http://localhost:8080/rest/tp2/ftp"; 
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@GET
	@Produces("text/html")
	public String hello()
	{
		return ("<h1>" + ftp.getReplyString() + "</h1>");
	}
	
	@GET
	@Produces("text/html")
	@Path("list/{path : .*}")
	public String listDirectory(@PathParam("path") String path)
	{
		FTPFile[]	list;
		String		out;
		String		link;
		
		System.out.println(path);
		out = "<h1>" + path + "</h1>";
		try {
			list = ftp.listFiles(path);
			if (list.length == 0)
				out += ftp.getReplyString();
			else
				out += "<a href=\"" + base_url + "/list/" + path + "/..\">..</a><br/>";
			for (FTPFile file : list)
			{
				if (file.isDirectory())
					link = base_url + "/list/" + path + "/" +  file.getName();
				else
					link = base_url + "/file/" + path + "/" + file.getName();
				out += file.getSize() + "<a href=\"" + link + "\">	" + file.getName() + "</a><br/>";
			}
			out += "<br/><br/><form method=\"post\" action=\"" + base_url + "/upload\" enctype=\"multipart/form-data\">"
					+ "<label for=\"file\">File to upload:</label><input type=\"file\" name=\"file\" id=\"file\"/><br/>"
					+  "<label for=\"fileName\">File name:</label><input type=\"text\" name=\"fileName\" id=\"fileName\"/><br/>"
					+ "<input type=\"hidden\" name=\"path\" value=\""+ path + "\"/><br/>"
					+ "<input type=\"submit\" value=\"Upload\"/></form>";
		} catch (IOException e) {
			out += ftp.getReplyString();
			e.printStackTrace();
		}
		return (out);
	}
	
	@GET
	@Produces("application/octet-stream")
	@Path("file/{file : .*}")
	public InputStream getFIle(@PathParam("file") String file)
	{
		try {
			return (ftp.retrieveFileStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (null);
	}
	
	@DELETE
	@Produces("application/text-html")
	@Path("file/{file : .*}")
	public String fileDelete(@PathParam("file") String file) throws Exception
	{
		ftp.deleteFile(file);
		return ("<h1>" + ftp.getReplyString() + "</h1>");
	}
	
	@GET
	@Produces("text/html")
	@Path("pwd")
	public String getPWD()
	{
		try {
			ftp.pwd();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ("<h1>" + ftp.getReplyString() + "</h1>");
	}
	
	@POST
	@Path("upload")
	@Produces("text/html")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String fileUpload(@Multipart("file") InputStream fileUploadStream,
							@Multipart("fileName") String fileName,
							@Multipart("path") String path)
	{
		try
		{
			ftp.changeWorkingDirectory(path);
			if (ftp.storeFile(fileName, fileUploadStream))
				return ("<h1>Upload complete</h1>" + ftp.getReplyString());
			else
				return ("<h1>Upload error</h1>" + ftp.getReplyString());
		} catch (IOException e)	{
			e.printStackTrace();
			return ("<h1>Upload error</h1>" + ftp.getReplyString());
		}
	}
}
