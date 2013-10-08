package de.enterprise.lokaServer.events.execution;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;

import sun.misc.BASE64Encoder;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.PostPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class RequestPostDetail extends ServerEvent{

	public RequestPostDetail(){
		CMD = "RPD";
	}
	
	public void execute(String json){
		String[] ids = json.split("#");
		int id = Integer.parseInt(ids[1]);
		int userID = Integer.parseInt(ids[0]);
		PostPojo post = this.lokaServer.getDBQuery().getSinglePost(id);
		String newJson = JSONConverter.toJSON(post);
		try {
			try {
				URI uri = new URI(post.getMessage().getPicURL());
				BufferedImage img = ImageIO.read(new File(uri));
				BASE64Encoder enc = new BASE64Encoder();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageIO.write(img, "jpg", out);
				String base64 = enc.encode(out.toByteArray());
				lokaServer.getServerSocket().sendToUser(userID, CMD, newJson);
				lokaServer.getServerSocket().sendToUser(userID, "start");
				lokaServer.getServerSocket().sendToUser(userID, "IMG", base64);
				lokaServer.getServerSocket().sendToUser(userID, "end");
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void execute(String json, String extra) {
	}
	
}
