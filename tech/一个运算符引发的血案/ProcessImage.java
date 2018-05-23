import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;
public class ProcessImage{
	public static void main(String[] args)throws Exception{
		File file = new File("a.jpg");
		processImage(file);
	}

	public static void processImage(File file)throws Exception{
		BufferedImage img = ImageIO.read(file);
		System.out.println("��ʼ����ͼƬ...");
		for(int x = 0; x < img.getWidth(); x++){
			for(int y = 0; y < img.getHeight(); y++){
				int rgb = img.getRGB(x,y);// ��Ҫ�ṩ���� ��������
				int r = rgb >> 16 & 255;
				int g = rgb >> 8  & 255;
				int b = rgb &  255;
				r = processColor(r);
				g = processColor(g);
				b = processColor(b);
				int nc = r << 16 | g << 8 | b;
				img.setRGB(x,y,nc);
				/*
					Color c = new Color(rgb);
					int r = c.getRed();
					int g = c.getGreen();
					int b = c.getBlue();
				*/
				/*
					32 = 8+8+8+8
					Alpha		Red			Green		Blue
					00000000	01111001	00001111	01101001
				*/
			}
		}
		String name = file.getName();
		name = name.substring(0,name.lastIndexOf(".")) + "_finished.jpg";
		ImageIO.write(img,"jpeg",new File(file.getParent(),name));
		System.out.println("ͼƬ�������~~~~");
	}
	
	/**
		��ͼƬ����ĸ�����
	*/
	public static int processColor(int colorValue){
		colorValue = colorValue * 7 / 5;
		if(colorValue > 255){
			return 255;
		}
		return colorValue;
	}
}