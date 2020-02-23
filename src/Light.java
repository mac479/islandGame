import java.awt.Point;


public class Light {
	public static final int NONE=0;
	public static final int PART=2;
	public static final int PART2=3;
	public static final int FULL=1;
	
	private boolean pointLight;
	

	private int radius,diameter,color;
	
	
	private Point[] shape;
	private Point start;
	private int[] lm;
	private int drawX,drawY;
	
	public Light(int r,int c){
		radius=r;
		diameter=r*2;
		color=c;
		start=new Point(radius,radius);
		pointLight=false;
		
		drawX=radius;
		drawY=radius;
		
		lm=new int[diameter*diameter];
		
		for(int y=0;y<diameter;y++){
			for(int x=0;x<diameter;x++){
				double d=Math.sqrt(((x-radius)*(x-radius))+((y-radius)*(y-radius)));
				
				if(x<0||x>=diameter||y<0||y>=diameter);
				else if(d<radius){
					double power=1-(d/(double)radius);
					lm[x+y*diameter]=(int)(((color >> 16)& 0xff)*power) << 16|(int)(((color >> 8)& 0xff)*power) << 8|(int)((color & 0xff)*power);
				}
				else{
					lm[x+y*diameter]=0;
				}
			}
		}
	}
	
	/*
	 * Dear me,
	 * 	Today we got drunk yay. What we wanted to do was detect if the light is being generated within a specific quadrant 
	 * 	then add a variable to the distance SO WHAT WE GONNA DO IS HAVE A VAR TO ADD A TINY DISTANCE TO THE REGULAR DISTANCE THEN
	 * 	THAT DISTANCE GETS DRAWN. WE NEED TO TAKE IN A MAP OF THE AREA.
	 * 
	 * Todo:
	 *  1.Adjust the point based lights to comply with the shadow system so they can be drawn correctly.
	 */
	public Light(int r,int c,Point[] p,int index){//use for triangular lighting/polygonal lighting
		radius=r;
		diameter=r*2;
		color=c;
		shape=p;
		start=p[index];
		pointLight=true;
		
		drawX=p[index].x;
		drawY=p[index].y;
		
		System.out.println("Draw "+drawX+" "+drawY);
		
		lm=new int[diameter*diameter];
		
		int length=-1;
		for(int i=1;i<shape.length-1;i++){
			length=(int) Math.max(Math.sqrt(((shape[i].y-shape[i+1].y)*(shape[i].y-shape[i+1].y))+((shape[i].x-shape[i+1].x)*(shape[i].x-shape[i+1].x))), length);
		}
		System.out.println(length+" l + d "+diameter);
		
		
		for(int y=0;y<diameter;y++){
			for(int x=0;x<diameter;x++){
				double d=Math.sqrt(((x-shape[index].x)*(x-shape[index].x))+((y-shape[index].y)*(y-shape[index].y)));

				if(x<0||x>=300||y<0||y>=300);
				else if(checkPoint(new Point(x,y))){
					double power=1-Math.min(1,(d/(double)diameter));
					lm[x+y*diameter]=(int)(((color >> 16)& 0xff)*power) << 16|(int)(((color >> 8)& 0xff)*power) << 8|(int)((color & 0xff)*power);
				}
				else{
					lm[x+y*diameter]=0;
				}
			}
		}
	}
	
	private boolean checkPoint(Point test){
		boolean result = false;
	    for (int i = 0, j = shape.length - 1; i < shape.length; j = i++) {
	    	if ((shape[i].y > test.y) != (shape[j].y > test.y) && 
	    		(test.x < (shape[j].x - shape[i].x) * (test.y - shape[i].y) / (shape[j].y-shape[i].y) + shape[i].x)) {
	    		result = !result;
	        }
	    }
	    return result;
	}

	public Point getStartPoint(){
		return start;
	}
	public int[] getLm(){
		return lm;
	}
	public boolean getPointLight(){
		return (shape!=null);
	}
	public int getRadius(){
		return radius;
	}
	public int getDrawX(){
		return drawX;
	}
	public int getDrawY(){
		return drawY;
	}
	public int getDiameter(){
		return diameter;
	}
	public int getColor(){
		return color;
	}
	public int getLightValue(int x,int y){
		if(x<0||x>=diameter||y<0||y>=diameter){
			return 0;
		}
		return lm[x+y*diameter];
	}
	
	
}
