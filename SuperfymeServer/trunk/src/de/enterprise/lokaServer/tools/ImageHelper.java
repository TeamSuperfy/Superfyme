package de.enterprise.lokaServer.tools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

public class ImageHelper {

	public static byte[] shrinkImage(BufferedImage originalImage, double scale, float compression){
		
	    int newW = (int)(originalImage.getWidth() * scale);
	    int newH = (int)(originalImage.getHeight() * scale);
	    BufferedImage resizedImage = new BufferedImage(newW, newH, originalImage.getType());
	    Graphics2D g = resizedImage.createGraphics();
	    g.drawImage(originalImage, 0, 0, newW, newH, null);
	    g.dispose();
	    ImageWriter writer = (ImageWriter) ImageIO.getImageWritersBySuffix("jpeg").next();
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    try {
			writer.setOutput(ImageIO.createImageOutputStream(out));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    ImageWriteParam param = writer.getDefaultWriteParam();
	    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	    param.setCompressionQuality(compression); //half
	    try {
			writer.write(null, new IIOImage(resizedImage, null, null),param);
		    return out.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return null;
	}
	
	
	
}
