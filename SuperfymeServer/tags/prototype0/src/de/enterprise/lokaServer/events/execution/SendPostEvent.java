package de.enterprise.lokaServer.events.execution;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import sun.misc.BASE64Decoder;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.PostPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class SendPostEvent extends ServerEvent {

	public SendPostEvent() {
		CMD = "SP";
	}

	public void execute(String json, String extra) {
		PostPojo post = (PostPojo) JSONConverter.fromJSON(json, PostPojo.class.getName());
		String uri = processImage(extra);
		post.getMessage().setPicURL(uri);
		lokaServer.getDBQuery().insertPostPojo(post);
		System.out.println("received new post: " + post);
	}

	private String processImage(String image) {
		try {
			byte[] img;
			img = new BASE64Decoder().decodeBuffer(image);

			ByteArrayInputStream in = new ByteArrayInputStream(img);
			BufferedImage buff = ImageIO.read(in);
			String path = "D:/serverImages";
			String number = String.valueOf(Math.random() * 1000);
			File imgFile = new File(path + "/" + "image" + number + ".jpg");
			ImageIO.write(buff, "jpg", imgFile);
			return imgFile.toURI().toString();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void execute(String json) {
	}

}
