import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Timer;

/*
 * TODO
 * Rename game class and merge with Display to offer better control over the timer and save functions.
 */

public class Game extends WindowAdapter implements Runnable, ActionListener, KeyListener{
	
	private Display display;
	private Thread thread;
	private boolean running=false;
	private int step;
	private boolean stepD=true;
	private Timer animator;

	private JFrame frame;
	private Canvas canvas;
	
	private int x,y;//controls cameras position.
	
	private World Island;
	private Light Sun;
	private int tod=120;
	
	public int width,height;
	public String title;
	
	private int[] pix,lm,lb;
	private final int ambient=0xff171717;
	
	private BufferStrategy bs;
	private BufferedImage test;
	private ImageSheet textures;
	private Graphics g;
	
	public Game(String title,int width,int height){
		this.width=width;
		this.height=height;
		this.title=title;
	}
	
	private void init(){
		//Code that is only needed to be run once.
		Assets.init();
		
		String tempKey="abcdefghijklmnopqrstuvwxyz1234567890", finalString="";//tempKey is a list of acceptable characters to be added to finalString
		System.out.print("Enter a seed: ");
		String t=new Scanner(System.in).nextLine();
		t=t.toLowerCase();
		long seed;
		for(int i=0;i<t.length();i++){//scans t to find acceptable chracters to put in final string
			if(tempKey.indexOf(""+t.charAt(i))!=-1)
				finalString+=t.charAt(i);
		}
		if(finalString.length()>11)//If the string will be too big a number it trims it's size
			finalString=finalString.trim().substring(0,12);
			
		if(finalString.equalsIgnoreCase(""))//If no string was entered gets a random seed.
			seed=new Random().nextLong();
		else
			seed=Long.parseLong(finalString,36);//Treats string as a base 36 number and converts it into base 10.
		
		Island=new World(seed);
		x=Island.WorldSize;
		y=Island.WorldSize;
		//Island.toPng();

		createDisplay();
		canvas.setBackground(Color.CYAN);
		canvas.createBufferStrategy(3);
		canvas.addKeyListener(this);
		bs=canvas.getBufferStrategy();
		textures=new ImageSheet("\\Textures\\TextureSheet.png",40,40);
		test=new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
		pix=((DataBufferInt)test.getRaster().getDataBuffer()).getData();//Used to get pixel data into an integer array format
		lm=new int[pix.length];
		lb=new int[pix.length];
		for(int i=0;i<lm.length;i++){
			lm[i]=ambient;
		}
		

		Point[] points=new Point[5],second=new Point[5],third=new Point[5];
		points[0]=new Point(0,frame.getHeight());
		points[1]=new Point(0,0);
		points[2]=new Point(frame.getWidth()/2,-tod);
		points[3]=new Point(frame.getWidth(),0);
		points[4]=new Point(frame.getWidth(),frame.getHeight());
		Sun=new Light(3*tod, Color.WHITE.getRGB(), points, 2);
		
		animator=new Timer(120/1000,this);//Sets fps to 60
		animator.setActionCommand("fps");
		animator.start();
	}
	
	private void createDisplay(){
		frame=new JFrame(title);
		frame.setSize(width, height);
		frame.addWindowListener(new WindowAdapter() {//to give me more control over the closing function.
            public void windowClosing(WindowEvent e) {
            	System.out.println("\nThat's all folks!");
            	stop();
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
	
	private void update(){//manage how the game works
		if(x<Island.WorldSize)
			x+=Island.WorldSize;
		if(y<Island.WorldSize)
			y+=Island.WorldSize;
		if(x>=Island.WorldSize*2)
			x-=Island.WorldSize;
		if(y>=Island.WorldSize*2)
			y-=Island.WorldSize;
		
	}
	private void render(){//draw game
		g=bs.getDrawGraphics();
		
		for(int i=0;i<pix.length;i++){//Clears array
			pix[i]=Color.CYAN.getRGB();
		}
		
		//TODO change this so it is able to handle blocks in the z array again also possibly generate z textures before hand.
		for(int c=0;c<Assets.block.length;c++){
			BufferedImage temp=Assets.block[c];
			for(int a=0,nx=x;a<width;a+=16,nx++){
				for(int b=height,ny=y;b>=0;b-=16,ny++){
					if(Island.overWorld[ny%Island.WorldSize][nx%Island.WorldSize].ID==7&&Island.zOverWorld[ny%Island.WorldSize][nx%Island.WorldSize].ID==7){
						if(c==6)
							test.getGraphics().drawImage(temp, a, b, 16, 16, null);
					}
					//If the theres water in the foreground but a block in the background draws the block.
					else if(Island.overWorld[ny%Island.WorldSize][nx%Island.WorldSize].ID==7&&Island.zOverWorld[ny%Island.WorldSize][nx%Island.WorldSize].ID!=7){
						if(Island.zOverWorld[ny%Island.WorldSize][nx%Island.WorldSize].ID-1==c)
							test.getGraphics().drawImage(temp, a, b, 16, 16, null);
					}
					//If the theres now block in the foreground but one in the background it draws
					else if(Island.overWorld[ny%Island.WorldSize][nx%Island.WorldSize].isAir()){
						if(Island.zOverWorld[ny%Island.WorldSize][nx%Island.WorldSize].ID-1==c){
							BufferedImage tz=temp;
							test.getGraphics().drawImage(tz, a, b, 16, 16, null);
						}
					}
					//draws block
					else if(Island.overWorld[ny%Island.WorldSize][nx%Island.WorldSize].ID-1==c)
						test.getGraphics().drawImage(temp, a, b, 16, 16, null);
				}
			}
		}
		test.getGraphics().setColor(new Color(0,0,0,100));//Shades in areas where there is a block in the z but not in the front.
		for(int a=0,nx=x;a<width;a+=16,nx++){
			for(int b=height,ny=y;b>=0;b-=16,ny++){
				if(Island.overWorld[ny%Island.WorldSize][nx%Island.WorldSize].isAir()&&!Island.zOverWorld[ny%Island.WorldSize][nx%Island.WorldSize].isAir())
					test.getGraphics().fillRect(a, b, 16, 16);
			}
		}
		test.getGraphics().setColor(new Color(0,0,160,160));//Shades in areas where there is a block in the z but water in the front.
		for(int a=0,nx=x;a<width;a+=16,nx++){
			for(int b=height,ny=y;b>=0;b-=16,ny++){
				if(Island.overWorld[ny%Island.WorldSize][nx%Island.WorldSize].ID==7&&Island.zOverWorld[ny%Island.WorldSize][nx%Island.WorldSize].ID!=7)
					test.getGraphics().fillRect(a, b, 16, 16);
			}
		}

		drawLight(Sun,frame.getWidth()/2,frame.getHeight()/2);
		for(int x=0;x<300;x++){
			for(int y=0;y<300;y++){
				float r=((lm[x+y*300] >> 16) & 0xFF)/255f;
				float g=((lm[x+y*300] >> 8) & 0xFF)/255f;
				float b=(lm[x+y*300] & 0xFF)/255f;
				
				pix[x+y*300]=((int)(((pix[x+y*300]>>16)&0xff)*r)<<16)|((int)(((pix[x+y*300]>>8)&0xff)*g)<<8)|(int)((pix[x+y*300]&0xff)*b);
			
			}
		}
		g.drawImage(test, 0, 0, null);
		
		bs.show();//pass the buffer
		g.dispose();//clear graphics
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
			if(screenX<0||screenX>=frame.getWidth()||screenY<0||screenY>=frame.getHeight());
			else if(lb[screenX+screenY*frame.getWidth()]==Light.PART){
				mult=(mult-.02)>0 ? mult-.02 : 0;
			}
			else if(lb[screenX+screenY*frame.getWidth()]==Light.PART2){
				mult=(mult-.015)>0 ? mult-.015 : 0;
			}
			else if(lb[screenX+screenY*frame.getWidth()]==Light.FULL)
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
	private void calcLight(int x,int y,int c) {
		if(x<0||x>=300||y<0||y>=300)
			return;
		
		lm[x+y*300]=Math.max((lm[x+y*300] >> 16) & 0xFF, (c >> 16) & 0xFF)<<16|Math.max((lm[x+y*300] >> 8) & 0xFF, (c >> 8) & 0xFF)<<8|Math.max(lm[x+y*300] & 0xFF, c & 0xFF);	
	}
	
	//Game code
	public void run() {
		init();
	}
	
	//Makes a seperate thread for game
	public synchronized void start(){
		if(running)
			return;
		running=true;
		thread=new Thread(this);
		thread.start();
		thread.setName("Test");
	}
	public synchronized void stop(){
		if(!running)
			return;
		System.out.println("ding");
		running=false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//Add animator/timer
	public void actionPerformed(ActionEvent a) {
		if(a.getActionCommand().equals("fps")){
			update();
			render();
		}
		
		
	}

	@Override
	public void keyPressed(KeyEvent k) {
		System.out.println(k.getKeyCode());
		if(k.getKeyCode()==80){
			Island.toPng();
			Island.AltPng();
			BufferedImage image = null;
			try {
				image = new Robot().createScreenCapture(frame.getBounds());
			} catch (AWTException e1) {
				e1.printStackTrace();
			}
			try {
				ImageIO.write(image, "png", new File("screenshot.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if(k.getKeyCode()==27){
			
		}
		if(k.getKeyCode()==87)
			y+=1;
		if(k.getKeyCode()==83)
			y-=1;
		if(k.getKeyCode()==68)
			x+=1;
		if(k.getKeyCode()==65)
			x-=1;
	}

	@Override
	public void keyReleased(KeyEvent k) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}
	
}
