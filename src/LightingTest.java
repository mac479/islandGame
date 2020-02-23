import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;
import javax.swing.Timer;


public class LightingTest implements ActionListener {
	
	private BufferedImage p,lmap,test=new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
	
	private int[] pix,lm,lb;
	
	//private Color[][] pix=new Color[300][300];
	private int[][] map={
			{0,0,0,0,0,0},
			{0,0,0,0,0,0},
			{0,0,0,0,0,0},
			{0,0,0,0,0,0},
			{2,1,2,0,1,2},
			{0,0,0,0,0,0},
	};
	private int mapPw,mapPh;//The height and width in pixels that each int in the map covers.

	private JFrame frame;
	private Canvas canvas;
	private BufferStrategy bs;
	private Graphics2D g;
	private Timer animator;
	private long t1,t2;
	private final int ambient=0xff000000;
	
	private int pW=300,pH=300;
	
	final private int yStart=0,xStart=0;
	
	public LightingTest(){
		System.out.println(ambient);
		p=ImageLoader.load("/Textures/brick.png");
		test.getGraphics().drawImage(p, 0, 0,null);
		System.out.println("rgb: "+test.getRGB(0, 0));
		pix=((DataBufferInt)test.getRaster().getDataBuffer()).getData();
		lm=new int[pix.length];
		lb=new int[pix.length];
		//lmap=ImageLoader.load("/Textures/lightmap.png");
		
		mapPw=p.getWidth()/map[0].length;
		mapPh=p.getHeight()/map.length;
		System.out.println(mapPw+"<-WIDTH (MAP QUADRANT) HEIGHT->"+mapPh);
		
		createDisplay();
		canvas.setBackground(Color.black);
		canvas.createBufferStrategy(1);
		bs=canvas.getBufferStrategy();

		
		Light test=new Light(300,Color.white.getRGB());
		
		for(int i=0;i<lm.length;i++){
			lm[i]=ambient;
		}
		for(int i=0;i<pix.length;i++){
			if(pix[i]==0){
				pix[i]=Color.WHITE.getRGB();
			}
		}
		
		//Iregular lighting tests
		Point[] points=new Point[5],second=new Point[5],third=new Point[5];
		points[0]=new Point(100,300);
		points[1]=new Point(10,0);
		points[2]=new Point(0,-25);
		points[3]=new Point(90,0);
		points[4]=new Point(300,300);
		
		second[0]=new Point(50,300);
		second[1]=new Point(110,0);
		second[2]=new Point(150,-25);
		second[3]=new Point(190,0);
		second[4]=new Point(250,300);
		
		third[0]=new Point(0,300);
		third[1]=new Point(210,0);
		third[2]=new Point(300,-25);
		third[3]=new Point(290,0);
		third[4]=new Point(200,300);
		//Light lr=new Light(150,Color.red.getRGB(),points,0);
		//Light lg=new Light(150,Color.green.getRGB(),points,1);
		//Light lb=new Light(150,Color.blue.getRGB(),points,2);
		Light l=new Light(175,Color.RED.getRGB(),points,2);
		Light gre=new Light(175,Color.GREEN.getRGB(),second,2);
		test=new Light(175,Color.BLUE.getRGB(),third,2);
		
		
		Light back=new Light(300,Color.DARK_GRAY.getRGB());

		

		t1=System.currentTimeMillis();
		/*
		for(int x=xStart;x<lr.getDiameter()+xStart;x++){
			for(int y=yStart;y<lr.getDiameter()+yStart;y++){
				//System.out.println(x+" "+y);
				calcLight(x,y,lr.getLm()[(x-xStart)+(y-yStart)*lr.getDiameter()]);
			}
		}
		for(int x=xStart;x<lg.getDiameter()+xStart;x++){
			for(int y=yStart;y<lg.getDiameter()+yStart;y++){
				//System.out.println(x+" "+y);
				calcLight(x,y,lg.getLm()[(x-xStart)+(y-yStart)*lg.getDiameter()]);
			}
		}
		for(int x=xStart;x<lb.getDiameter()+xStart;x++){
			for(int y=yStart;y<lb.getDiameter()+yStart;y++){
				//System.out.println(x+" "+y);
				calcLight(x,y,lb.getLm()[(x-xStart)+(y-yStart)*lb.getDiameter()]);
			}
		}*/
		
		
		
		/*for(int x=xStart;x<l.getDiameter()+xStart;x++){
			for(int y=yStart;y<l.getDiameter()+yStart;y++){
				//System.out.println(x+" "+y);
				calcLight(x,y,l.getLm()[(x-xStart)+(y-yStart)*l.getDiameter()]);
			}
		}
		for(int x=xStart;x<test.getDiameter()+xStart;x++){
			for(int y=yStart;y<test.getDiameter()+yStart;y++){
				calcLight(x,y,test.getLm()[(x-xStart)+(y-yStart)*test.getDiameter()]);
			}
		}
		for(int x=xStart;x<gre.getDiameter()+xStart;x++){
			for(int y=yStart;y<gre.getDiameter()+yStart;y++){
				calcLight(x,y,gre.getLm()[(x-xStart)+(y-yStart)*gre.getDiameter()]);
			}
		}
		*/
		
		t2=System.currentTimeMillis();
		System.out.println("FPS: "+((t2-t1)));
		t1=t2;
		
		/*
		for(int x=0;x<back.getDiameter();x++){
			for(int y=0;y<back.getDiameter();y++){
				calcLight(x-150,y,back.getLm()[(x)+(y-0)*back.getDiameter()]);
			}
		}
		*/
		update();
		
		drawLight(test,300,10);
		drawLight(l,0,10);//uses the center point of the lights map;
		drawLight(gre,150,10);
		drawLight(back,150,150);
		
		pushLightMap();
		render();
		//animator=new Timer(1000,this);
		//animator.setActionCommand("fps");
		//animator.start();
	}

	public static void main(String[] args) {
		new LightingTest();

	}
	private void createDisplay(){
		frame=new JFrame("test");
		frame.setSize(300, 300);
		frame.addWindowListener(new WindowAdapter() {//to give me more control over the closing function.
            public void windowClosing(WindowEvent e) {
            	System.out.println("\nThat's all folks!");
                System.exit(0);
            }
        });
		frame.setResizable(false);
		frame.setVisible(true);
		canvas=new Canvas();
		canvas.setPreferredSize(frame.getSize());
		canvas.setMaximumSize(frame.getSize());
		canvas.setMinimumSize(frame.getSize());
		
		frame.add(canvas);
		frame.pack();
		
	}

	@Override
	public void actionPerformed(ActionEvent a) {
		if(a.getActionCommand().equals("fps")){
			update();
			render();
		}
	}
	
	public void drawLight(Light l,int offX,int offY){
		for(int i=0;i<=l.getDiameter();i++){
			drawLightLine(l,l.getDrawX(),l.getDrawY(),i,0,offX,offY);
			drawLightLine(l,l.getDrawX(),l.getDrawY(),i,l.getDiameter(),offX,offY);
			drawLightLine(l,l.getDrawX(),l.getDrawY(),0,i,offX,offY);
			drawLightLine(l,l.getDrawX(),l.getDrawY(),l.getDiameter(),i,offX,offY);
		}
	}
	private void drawLightLine(Light l,int x0,int y0,int x1,int y1,int offX,int offY){
		double mult=1;
		int dx=Math.abs(x1-x0);
		int dy=Math.abs(y1-y0);
		
		int modX=0,modY=0;
		if(l.getPointLight()){
			modX=Math.min(modX, l.getStartPoint().x);
			modY=Math.min(modY, l.getStartPoint().y);
		}
		
		//System.out.println(dx+" - "+dy+"  "+x1+" - "+y1);
		
		int sx=x0<x1 ? 1 : -1;//if (condition) then, else.
		int sy=y0<y1 ? 1 : -1;
		
		int err= dx-dy;
		int e2;
		
		
		while(true){
			int screenX=x0-l.getDrawX()+offX+modX;
			int screenY=y0-l.getDrawY()+offY+modY;
			
			
			
			int lc=l.getLightValue(x0, y0);
			if(screenX<0||screenX>=pW||screenY<0||screenY>=pH);
			else if(lb[screenX+screenY*pW]==Light.PART){
				mult=(mult-.02)>0 ? mult-.02 : 0;
			}
			else if(lb[screenX+screenY*pW]==Light.PART2){
				mult=(mult-.015)>0 ? mult-.015 : 0;
			}
			else if(lb[screenX+screenY*pW]==Light.FULL)
				return;
			lc=(int)(((lc >> 16) & 0xFF)*mult)<<16|(int)(((lc >> 8) & 0xFF)*mult)<<8|(int)((lc & 0xFF)*mult);	
			if(lc==0&&x0>-1&&y0>-1)
				return;
				
				
			calcLight(screenX,screenY,lc);
				
				
			

			if(x0==x1&&y0==y1){
				break;
			}
			
			e2=2*err;
			if(e2>-1*dy){
				err-=dy;
				x0+=sx;
			}
			if(e2<dx){
				err+=dx;
				y0+=sy;
			}
		}
	}

	private void render() {
		g=(Graphics2D) bs.getDrawGraphics();
		
		g.clearRect(0, 0, 300, 300);//Clears Screen\
		//System.out.println(pix[150+150*300]);
		
		//Code to render screen.
		
		//Mapping method.
		
		

		//calcLight(x,y,l.getLm()[x+y*l.getDiameter()]);
		
		g.drawImage(test, 0, 0, null);
		
		//Image multiplication technique
		/*
		for(int i=0;i<300;i++){
			for(int j=0;j<300;j++){
				int r1=(p.getRGB(i, j) >> 16) & 0x00FF,r2=(lm.getRGB(i, j) >> 16) & 0x00FF;
				int g1=(p.getRGB(i, j) >> 8) & 0x00FF,g2=(lm.getRGB(i, j) >> 8) & 0x00FF;
				int b1=p.getRGB(i, j) & 0x00FF,b2=lm.getRGB(i, j) & 0x00FF;
				
				Color temp=new Color((r1*r2/255)<<16|(g1*g2/255)<<8|(b1*b2/255));
				
				if(temp!=canvas.getBackground()){
					g.setColor(temp);
					g.fillRect(i*2, j*2, 2, 2);
				}
			}
		}*/
		//g.drawImage(p, 0, 0, 300, 300, null);

		bs.show();//pass the buffer
		g.dispose();//clear graphics
	}

	private void calcLight(int x,int y,int c) {
		if(x<0||x>=300||y<0||y>=300)
			return;
		
		//System.out.println();//I hate my life IF THIS IS A DIFFERENT STATEMENT THE PROGRAM BREAKS WHY WHY THE FUCK IS THIS NEEDED. I FUCKING HATE THIS
		
		//double mult=getMapMult(x,y);
		double mult=1;
		
		lm[x+y*300]=(int)(Math.max((lm[x+y*300] >> 16) & 0xFF, (c >> 16) & 0xFF)*mult)<<16|(int)(Math.max((lm[x+y*300] >> 8) & 0xFF, (c >> 8) & 0xFF)*mult)<<8|(int)(Math.max(lm[x+y*300] & 0xFF, c & 0xFF)*mult);	
	}
	private void pushLightMap(){
		
		for(int x=0;x<300;x++){
			for(int y=0;y<300;y++){
				float r=((lm[x+y*300] >> 16) & 0xFF)/255f;
				float g=((lm[x+y*300] >> 8) & 0xFF)/255f;
				float b=(lm[x+y*300] & 0xFF)/255f;
				
				pix[x+y*300]=((int)(((pix[x+y*300]>>16)&0xff)*r)<<16)|((int)(((pix[x+y*300]>>8)&0xff)*g)<<8)|(int)((pix[x+y*300]&0xff)*b);
			
			}
		}
	}

	private void setLightBlock(int x,int y,int l){
		if(x<0||x>=pW||y<0||y>=pH)
			return;
		
		lb[x+y*pW]=l;
	}
	
	private int getMapBlock(int x,int y) {
		int val=map[(y-(y%mapPh))/mapPh][(x-(x%mapPw))/mapPw];
		switch(val){
			case 1:
				return Light.FULL;
			case 2:
				return Light.PART;
			case 3:
				return Light.PART2;
		}
		return Light.NONE;
	}

	private void update() {
		for(int i=0;i<300;i++){
			for(int j=0;j<300;j++){
				setLightBlock(i,j,getMapBlock(i,j));
			}
		}
		//System.out.println(pix[0]);
	}

}
