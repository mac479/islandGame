import java.awt.image.BufferedImage;

public class ImageSheet {
	
	private BufferedImage sheet;
	private int sWidth,sHeight;
	private int Length,Height;
	
	public ImageSheet(String path,int sw,int sh){
		sWidth=sw;
		sHeight=sh;
		sheet=ImageLoader.load(path);
		Length=sheet.getWidth()/sw;
		Height=sheet.getHeight()/sh;
	}
	public BufferedImage getImage(int id){
		id--;
		int x,y=0;
		x=id%Length;
		y=(id-(id%Length))/Length;
		try{
			return sheet.getSubimage(x*sWidth, y*sHeight, sWidth, sHeight);
		}
		catch(Exception e){
			System.out.println(x+" "+y+" "+id);
			System.out.println(Length+" "+Height);
			e.printStackTrace();
		}
		return null;
	}

}
