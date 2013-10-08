package de.enterprise.lokaServer.events.execution;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.tools.ImageHelper;

public class RequestThumbnailImageEvent extends ServerEvent {

	public RequestThumbnailImageEvent() {
		CMD = "RTHI";
	}

	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int picID = obj.getInt("PicID");
		//send images afterwards
		
		String picUrl = lokaServer.getDBQuery().getPicUrl(picID);
		try {
			URI uri = new URI(picUrl);
			BufferedImage img = ImageIO.read(new File(uri));
			byte[] imageData = ImageHelper.shrinkImage(img, 0.3d, 0.85f);
			JSONObject picJson = new JSONObject();
			picJson.put("pID", picID);
			System.out.println("image size: " + imageData.length);
			
			sendResponseMultipart(evt.getResponse(), picJson.toString(), imageData);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
