
import java.awt.image.BufferedImage;

public class Assets {
	
	private static final int sWidth=40,sHeight=40;
	
	public static BufferedImage[] block=new BufferedImage[9];
	
	public static void init(){
		ImageSheet blocksSheet=new ImageSheet("Textures\\TextureSheet.png",sWidth,sHeight);
		for(int i=0;i<block.length;i++){
			block[i]=blocksSheet.getImage(i+1);
		}
		
		
	}

}
