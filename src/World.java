

//Main purpose of this class is to generate and keep track of the various aspects of a world.

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
public class World {
	
	//These variables control the major aspects of the world; Birth and death rates for step set in function as they are NEVER to be altered.
	public final int WorldSize=512;//Height and width of world
	public final int UndergroundY=135;//level to begin stone majority
	public final int Sealevel=189,SeaFloor=129;//Constants for sea generation NOTE: Difference between sealevel and seafloor does not affect sand dunes.
	public final int OceanRange=40,HillRange=40;//The maximum allowed height for hills and sand dunes.
	public final int endRange=75;//When a hill/sand dune get within endRange of its generation area (EX: Worldsize for hills) it'll begin making the final down/up hill to meet up with a beach.
	
	public boolean[][] oceanWater;
	public Block[][] overWorld=new Block[WorldSize][WorldSize],zOverWorld=new Block[WorldSize][WorldSize];//zOverWorld is a near perfect copy of OverWorld meant to act as a 1 thick 3rd dimension.
	private Random r;
	public World(long seed){
		r=new Random(seed);
		System.out.println("current seed: "+seed);//Displays current seed for testing reasons.
		for(int x=0;x<overWorld[0].length;x++){//Makes a basic flat world to begin generation
			for(int y=0;y<overWorld.length;y++){
				if(y<UndergroundY)
					overWorld[y][x]=new Block(1);
				else if(y<Sealevel)
					overWorld[y][x]=new Block(2);
				else
					overWorld[y][x]=new Block(0);
			}
		}
		
		//hill & ocean generation;
		int yf=r.nextInt(11)+5,ys=r.nextInt(11)+5;//variables are made in the main function so the two functions can share their start and end points.
		hillCreate(2,yf,ys);
		oceanCreate(6,yf,ys);
		
		//Records ocean water locations for use later in game.
		//This is to help make it so ocean water is infinite, when something tries to remove ocean water from the z array this tells it to stop unless replaced by a solid block.
		//If a solid block is in the Z in the ocean and then remove game will auto fill in water in that location.
		//NOTE: water in the z or with air behind it with slowly filter away if it is NOT within the ocean water area. 
		oceanWater=new boolean[Sealevel-SeaFloor+1][WorldSize/2];
		for(int x=0;x<WorldSize/2;x++){
			for(int y=Sealevel;y>=SeaFloor;y--){
				System.out.println((y-SeaFloor)+" "+x);
				if(overWorld[y][x].ID==7)
					oceanWater[y-SeaFloor][x]=true;
				else
					oceanWater[y-SeaFloor][x]=false;
			}
		}
		
		for(int i=0;i<3;i++){
			transition(2,1,UndergroundY,2,98,2);//to create a smoother transition between dirt and stone.
			//System.out.println(i);
		}
		veinMaker(6, 1, 0, (WorldSize/20), Sealevel-((Sealevel-SeaFloor)*3/8), Sealevel, 57, 7, 4, true);//blend sand with stone near the start of the world
		veinMaker(6, 1, (WorldSize/2)-(WorldSize/20), WorldSize/2, Sealevel-30, Sealevel, 57, 7, 4, true);//blend sand with stone near the end of the world
		veinMaker(6, 1, (WorldSize/25), (WorldSize/2)-(WorldSize/25), SeaFloor-5, Sealevel, 62, 7, 4, true);//Creates sand veins in the stone to create a decent mix
		veinMaker(6, 2, WorldSize*49/50, WorldSize*13/25, UndergroundY, Sealevel+yf+ys, 62, 7, 4, true);//helps make the beach seem more natural.
		
		//Both of these help make the two beaches mix with dirt a bit more naturally.
		veinMaker(2, 6, (WorldSize/2)-endRange, (WorldSize*13/25), UndergroundY-10, Sealevel+ys, 50, 15, 5, true);
		veinMaker(2, 6, WorldSize*24/25, endRange, UndergroundY-10, Sealevel+yf, 50, 15, 5, true);
		
		veinMaker(1, 2, 0, WorldSize, UndergroundY, Sealevel+91, 47, 10, 7, true);//adds pockets of stone into the dirt.
		veinMaker(2, 1, 0, WorldSize, 0, UndergroundY, 44, 7, 4, true);//adds dirt below sea level.
		
		addZ();//sets z array equal to current world without mineral veins.
		
		//mineral veins are created
		veinMaker(4, 1, 0, WorldSize, 0, Sealevel+(HillRange*3/4), 40, 4, 2, false);//coal
		veinMaker(3, 1, 0, WorldSize, 0, UndergroundY*7/8, 37, 3, 1, false);//iron
		
		caveCreate(45);
		oceanSpread();//Begins letting the ocean water spill out filling caves on ocean floor.
		makeTrees(77, 10, 3, 3, 7);
	}
	
	private void oceanSpread() {
		boolean flag=false;//the system will run over the ocean again and again trying to figure out if a spread is needed if not it returns.
		while(!flag){
			flag=true;
			for(int x=0;x<WorldSize;x++){//Because a cave system might go across the entire world this function must include entire world.
				for(int y=Sealevel;y>=0;y--){
					if(overWorld[y][x].ID==7){//Here's where the real magic happens.
						for(int nx=x+WorldSize-1; nx<x+WorldSize+2; nx++){
					        for(int ny=y-1; ny<y+1; ny++){//Does not fill upwards!
					            //If we're looking at the middle point
					            if(!((nx == x+WorldSize && ny == y)||ny < 0 || ny >= overWorld.length)){
					            	//System.out.println(y+" "+x+" "+ny+" "+(nx%WorldSize));
					            	if(overWorld[ny][nx%WorldSize].isAir()){
					            		overWorld[ny][nx%WorldSize]=new Block(7);
					            		flag=false;
					            	}
					            }
					        }
					    }
					}
				}
			}
		}
	}

	public void veinMaker(int nBid,int oBid,int x,int xMax, int y, int yMax, int chance, int maxStep,int minStep,boolean same){
		//nBid-Block to generate vein of. oBid-Blocks to be written over. x & y starting location. xMax & yMax maximum x and y location to make a square where the function can run.
		boolean dirt=(oBid==2);//Checks to see if the block being replaced is dirt for use later
		if(x>xMax)
			xMax+=WorldSize;//If the max is less than the starting point it'll run over the border back to the start of the world.
		for(int a=y;a<yMax;a++){
			for(int b=x;b<xMax;b++){
				int c=r.nextInt(100);
				if(c<chance&&(overWorld[a][b%WorldSize].ID==oBid||//If it is replacing the proper block type.
							(overWorld[a][b%WorldSize].ID==5&&dirt)||//Makes grass seem the same as dirt if dirt is the block being replaced.
							(overWorld[a][b%WorldSize].ID==nBid&&same)))//If it is allowed to replace blocks of its own kind.
				{
					overWorld[a][b%WorldSize].setMarker(true);//Marks a block if proper conditions are met to be altered by step.
				}
			}
		}
		int c=r.nextInt(maxStep+1)+minStep;
		for(int n=0;n<c;n++)
			step(x,y,xMax,yMax);//Smooths out random clusters of marked blocks only changing markers to avoid altering world.
		for(int a=y;a<yMax;a++){
			for(int b=x;b<xMax;b++){
				if(overWorld[a][b%WorldSize].getMarker()){//Find and replace marked blocks.
					overWorld[a][b%WorldSize]=new Block(nBid);
					overWorld[a][b%WorldSize].setMarker(false);
				}
			}
		}

	}
	
	public void caveCreate(int chance){
		for(int x=0; x<WorldSize; x++){
	        for(int y=0; y<WorldSize; y++){
	        	int z=r.nextInt(100);
	        	if(x<105&&z<chance+3)
	        		overWorld[x][y].setMarker(true);
	        	else if(z < chance)
	        		overWorld[x][y].setMarker(true);
	        }
	    }
		for(int n=0;n<15;n++)
			step(0,0,WorldSize,WorldSize);
		for(int x=0;x<WorldSize;x++){
			for(int y=0;y<WorldSize;y++){
				if(overWorld[x][y].getMarker()&&overWorld[x][y].ID!=7){
					overWorld[x][y]=new Block(0);
					overWorld[x][y].setMarker(false);
				}
				else if(overWorld[x][y].getMarker()){
					overWorld[x][y].setMarker(false);
				}
			}
		}
	}
	
	
	private void step(int x1,int y1,int x2,int y2) {//¯\_(o_o)_/¯
		Block[][] temp=new Block[WorldSize][WorldSize];
		if(x1>x2)
			x2+=WorldSize;
		for(int y=y1; y<y2; y++){
	        for(int x=x1; x<x2; x++){
	            int nbs = countAliveNeighbours(y, x%WorldSize);
	            //The new value is based on our simulation rules
	            //First, if a cell is alive but has too few neighbours, kill it.
	            if(overWorld[y][x%WorldSize].getMarker()){
	                if(nbs < 4){//If living cells are surrounded by less than 4 cells they die
	                    temp[y][x%WorldSize]=overWorld[y][x%WorldSize];
	                    temp[y][x%WorldSize].setMarker(false);
	                }
	                else{
	                    temp[y][x%WorldSize]=overWorld[y][x%WorldSize];
	                    temp[y][x%WorldSize].setMarker(true);
	                }
	            } //Otherwise, if the cell is dead now, check if it has the right number of neighbours to be 'born'
	            else{
	               if(nbs > 4){//If dead cells are surrounded by more than 4 cells they live
	                    temp[y][x%WorldSize]=overWorld[y][x%WorldSize];
	                    temp[y][x%WorldSize].setMarker(true);
	                }
	                else{
	                	temp[y][x%WorldSize]=overWorld[y][x%WorldSize];
	                	temp[y][x%WorldSize].setMarker(false);
	                }
	           }
	        }
	    }
		for(int a=x1;a<x2;a++){
			for(int b=y1;b<y2;b++)
				overWorld[b][a%WorldSize]=temp[b][a%WorldSize];
		}
		
	}

	
	private int countAliveNeighbours(int y, int x) {//fix count code so it doesnt need to take variables backwards
		int count = 0;
	    for(int nx=x+WorldSize-1; nx<x+WorldSize+2; nx++){
	        for(int ny=y-1; ny<y+2; ny++){
	            //If we're looking at the middle point
	            if(!((nx == x+WorldSize && ny == y)||ny < 0 || ny >= overWorld.length)){
	            	//System.out.println(y+" "+x+" "+ny+" "+(nx%WorldSize));
	            	if(overWorld[ny][nx%WorldSize].getMarker()){
		                count++;
		            }
	            }
	        }
	    }
	    return count;
	}

	public void hillCreate(int bid,int yf,int ys) {
		int f=0,h=0,ph=0,x=0,ox=WorldSize/2,y=0;
		y=ys;
		ph=y;
		boolean du=true;
		h=r.nextInt((HillRange+1)/2)+1;
		f=r.nextInt(70)+5;
		for(x=0;x<f;x++){
			y=(int)(Math.round((Math.sin((x*Math.PI/f)-.5*Math.PI))*h + h + ph));//up hill
			filldown(x+ox,y+Sealevel,bid);//clears the blocks below it with given block ID.
		}
		while(x+ox<=WorldSize-endRange){//MAGIC BITCH
			ox+=f;
			ph=y;
			du=r.nextBoolean();//Randomize up or down
			f=r.nextInt(45)+5;//length
			h=r.nextInt(10)+1;//height
			if((2*h)+ph>HillRange)
				du=false;
			if(ph-(2*h)<0)
				du=true;
			for(x=0;x<f;x++){
				if(du)
					y=(int)(Math.round((Math.sin((x*Math.PI/f)-.5*Math.PI))*h + h + ph));//up hill
				else
					y=(int)(Math.round((-Math.sin((x*Math.PI/f)-.5*Math.PI))*h - h + ph));//down hill
				filldown(x+ox,y+Sealevel,bid);//fills in all the air below with given ID
			}
		}
		ox+=f;
		ph=y;
		f=WorldSize-(ox);
		//down hill final
		if(y-yf>0){
			h=(y-yf)/2;
			for(x=0;x<f;x++){
				y=(int)(Math.round((-Math.sin((x*Math.PI/f)-.5*Math.PI))*h - h + ph));//down hill
				filldown(x+ox,y+Sealevel,bid);
			}
		}
		//up hill final
		else{
			h=(yf-y)/2;
			for(x=0;x<f;x++){
				y=(int)(Math.round((Math.sin((x*Math.PI/f)-.5*Math.PI))*h + h + ph));//up hill
				filldown(x+ox,y+Sealevel,bid);
			}
		}
		
		//Math.round((Math.sin((a*Math.PI/5)-.5*Math.PI))*5 + 5)
		//NEVER DELETE THIS FUCKING FORMULA!!!
		
	}
	
	public void oceanCreate(int bid,int yf,int ys) {
		int f=0,h=0,ph=0,x=0,ox=0,y=0;
		ph=yf+(Sealevel-SeaFloor);
		boolean du=false;//runs one set down hill to get it below sealevel.
		//while(ys-h>114)
		h=(r.nextInt(4)+yf)/2;
		f=r.nextInt(21)+10;
		for(x=0;x<f;x++){
			y=(int)(Math.round((-Math.sin((x*Math.PI/f)-.5*Math.PI))*h - h + ph));//down hill
			clear(x+ox,y+SeaFloor);//clears the blocks above it with air.
			filldown(x+ox,y+SeaFloor,bid);
		}
		//steep downhill after beach.
		ox+=f;
		ph=y;
		h=r.nextInt((Sealevel-SeaFloor)/8)+((Sealevel-SeaFloor)*3/8);
		f=r.nextInt(26)+20;
		for(x=0;x<f;x++){
			y=(int)(Math.round((-Math.sin((x*Math.PI/f)-.5*Math.PI))*h - h + ph));//down hill
			clear(x+ox,y+SeaFloor);//clears the blocks above it with air.
			filldown(x+ox,y+SeaFloor,bid);
		}
		while(x+ox<=(WorldSize/2)-endRange){
			ox+=f;
			ph=y;
			du=r.nextBoolean();//Randomize up or down
			f=r.nextInt(25)+5;
			h=r.nextInt(7)+1;
			if((2*h)+ph>OceanRange)//if current numbers will go out of allowed range alter du to force direction. 
				du=false;
			if(ph-(2*h)<0)
				du=true;
			for(x=0;x<f;x++){
				if(du)
					y=(int)(Math.round((Math.sin((x*Math.PI/f)-.5*Math.PI))*h + h + ph));//up hill
				else
					y=(int)(Math.round((-Math.sin((x*Math.PI/f)-.5*Math.PI))*h - h + ph));//down hill
				clear(x+ox,y+SeaFloor);//fills in all the air below with given ID
				filldown(x+ox,y+SeaFloor,bid);
			}
		}
		ox+=f;
		ph=y;
		//return to beach.
		h=(Sealevel-SeaFloor-y-r.nextInt(4)+1)/2;
		f=r.nextInt(26)+20;
		for(x=0;x<f;x++){
			y=(int)(Math.round((Math.sin((x*Math.PI/f)-.5*Math.PI))*h + h + ph));//up hill
			clear(x+ox,y+SeaFloor);//clears the blocks above it with air.
			filldown(x+ox,y+SeaFloor,bid);
		}
		//final beach
		ox+=f;
		ph=y;
		f=(WorldSize/2)-(ox);
		h=(ys+Sealevel-SeaFloor-y)/2;
		for(x=0;x<f;x++){
			y=(int)(Math.round((Math.sin((x*Math.PI/f)-.5*Math.PI))*h + h + ph));//up hill
			clear(x+ox,y+SeaFloor);
			filldown(x+ox,y+SeaFloor,bid);
		}
		
		for(int o=0;o<WorldSize/2;o++){
			filldown(o,Sealevel,7);
		}
		
		//Math.round((Math.sin((a*Math.PI/5)-.5*Math.PI))*5 + 5)
		//NEVER DELETE THIS FUCKING FORMULA!!!
	}
	
	private void clear(int x, int y) {//Opposite of filldown
		for(int a=y;a<Sealevel;a++){
			overWorld[a][x]=new Block(0);
		}
	}

	private void filldown(int x, int y,int bid) {
		boolean flag=true;
		if(bid==2&&overWorld[y][x].isAir())//if it is filling in dirt sets first block to grass.
			overWorld[y][x]=new Block(5);
		else if(!overWorld[y-1][x].isAir()&&bid!=7){//if there is a block right below the x,y in the function the block above it is set to that except with water.
			//overWorld[y][x]=new Block(overWorld[y-1][x].ID);
			return;
		}
		else if(overWorld[y][x].isAir())
			overWorld[y][x]=new Block(bid);
		y--;
		while(flag){//fills in blocks until it hits something.
			if(!(overWorld[y][x].isAir()))
				flag=false;
			else{
				overWorld[y][x]=new Block(bid);
				y--;
			}
		}
	}
	public void toPng() {
		BufferedImage img=new BufferedImage(WorldSize+101, WorldSize,BufferedImage.TYPE_INT_RGB);
		int rgb=0;
		int c=0;
		for(int a=WorldSize-1;a>=0;a--){
			for(int b=0;b<WorldSize+100;b++){
				int d=r.nextInt(21)-10;//offset to help randomize each pixel slightly.
				if(b>=WorldSize)
					c=b+1;
				else
					c=b;
				if(overWorld[a][c%WorldSize].ID==0){
					if(!zOverWorld[a][c%WorldSize].isAir()){
						if(zOverWorld[a][c%WorldSize].ID==1)
							rgb=(138+d<<16)|(138+d<<8)|138+d;
						if(zOverWorld[a][c%WorldSize].ID==2)
							rgb=(131+d<<16)|(66+d<<8)|31+d;
						if(zOverWorld[a][c%WorldSize].ID==3)
							rgb=(154+d<<16)|(125+d<<8)|106+d;
						if(zOverWorld[a][c%WorldSize].ID==4)
							rgb=(25+d<<16)|(25+d<<8)|25+d;
						if(zOverWorld[a][c%WorldSize].ID==5)
							rgb=(0<<16)|(137+d<<8)|36+d;
						if(zOverWorld[a][c%WorldSize].ID==6)
							rgb=(198+d<<16)|(189+d<<8)|136+d;
						if(zOverWorld[a][c%WorldSize].ID==7)
							rgb=(0<<16)|(0<<8)|120+d;
						if(zOverWorld[a][c%WorldSize].ID==8)
							rgb=(154+d<<16)|(95+d<<8)|14+d;
						if(zOverWorld[a][c%WorldSize].ID==9)
							rgb=(0<<16)|(124+d<<8)|0;
					}
					else
						rgb=(0<<16)|(206+d<<8)|230+d;
				}
				if(overWorld[a][c%WorldSize].ID==1)
					rgb=(178+d<<16)|(178+d<<8)|178+d;
				if(overWorld[a][c%WorldSize].ID==2)
					rgb=(171+d<<16)|(106+d<<8)|71+d;
				if(overWorld[a][c%WorldSize].ID==3)
					rgb=(194+d<<16)|(165+d<<8)|146+d;
				if(overWorld[a][c%WorldSize].ID==4)
					rgb=(65+d<<16)|(65+d<<8)|65+d;
				if(overWorld[a][c%WorldSize].ID==5)
					rgb=(34+d<<16)|(177+d<<8)|76+d;
				if(overWorld[a][c%WorldSize].ID==6)
					rgb=(239+d<<16)|(228+d<<8)|176+d;
				if(overWorld[a][c%WorldSize].ID==7){
					if(zOverWorld[a][c%WorldSize].ID!=7){
						if(zOverWorld[a][c%WorldSize].isAir())
							rgb=(0<<16)|(166+d<<8)|255;
						if(zOverWorld[a][c%WorldSize].ID==1)
							rgb=(98+d<<16)|(98+d<<8)|218+d;
						if(zOverWorld[a][c%WorldSize].ID==2)
							rgb=(91+d<<16)|(26+d<<8)|111+d;
						if(zOverWorld[a][c%WorldSize].ID==3)
							rgb=(114+d<<16)|(85+d<<8)|186+d;
						if(zOverWorld[a][c%WorldSize].ID==4)
							rgb=(0<<16)|(0<<8)|105+d;
						if(zOverWorld[a][c%WorldSize].ID==5)
							rgb=(0<<16)|(97+d<<8)|116+d;
						if(zOverWorld[a][c%WorldSize].ID==6)
							rgb=(158+d<<16)|(149+d<<8)|216+d;
						if(zOverWorld[a][c%WorldSize].ID==8)
							rgb=(114+d<<16)|(55+d<<8)|94+d;
						if(zOverWorld[a][c%WorldSize].ID==9)
							rgb=(0<<16)|(84+d<<8)|80;
					}
					else
						rgb=(0<<16)|(0<<8)|160+d;
				}
				if(overWorld[a][c%WorldSize].ID==8)
					rgb=(154+d<<16)|(95+d<<8)|14+d;
				if(overWorld[a][c%WorldSize].ID==9)
					rgb=(27+d<<16)|(164+d<<8)|19+d;
				img.setRGB(c, WorldSize-a-1, rgb);//address to png issue with a
			}
			for(int red=0;red<WorldSize;red++)
				img.setRGB(WorldSize,red,Color.RED.getRGB());	
		}
		File f = new File("world.png");
		if(!f.exists())
			try {
				f.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		try {
			ImageIO.write(img, "PNG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void addZ() {//copies current world into the z array;
		for(int x=0;x<WorldSize;x++){
			for(int y=0;y<WorldSize;y++)
				zOverWorld[x][y]=overWorld[x][y];
		}
		
	}
	
	public void makeTrees(int chance,int tChance,int rad,int step,int tHeight){
		for(int x=WorldSize/2;x<WorldSize;x++){
			if(r.nextInt(100)<tChance){
				//Begins by getting y for the location of the grass block to begin generation
				int y;
				boolean breaker=true;
				for(y=Sealevel;y<WorldSize&&breaker;y++){
					if(overWorld[y][x].ID==5)
						breaker=false;//Breaks the loop after getting the correct y
				}
				if(y!=WorldSize){//In the event it chose to build a tree where dirt is not the surface block

					for(int m=y;m<y+tHeight+rad;m++)//Begins making the trunk of the tree in the zArray so people can walk under trees.
						zOverWorld[m][x]=new Block(8);
					for(int m=y+tHeight+rad-1;m<y+tHeight+rad+2;m++){
						for(int n=x-1;n<x+2;n++){
							overWorld[m][n%WorldSize].setMarker(true);;
						}
					}
					for(int a=y+tHeight-rad;a<y+tHeight+(2*rad);a++){//randomly places leaves in a cube around the trunk.
						for(int b=x-rad;b<x+rad;b++){
							int c=r.nextInt(100);
							if(c<chance&&(overWorld[a][b%WorldSize].isAir()||overWorld[a][b%WorldSize].ID==9))
								overWorld[a][b%WorldSize].setMarker(true);
						}
					}
				
				}
			}
		}
		for(int i=0;i<step;i++)
			step(WorldSize/2,Sealevel,WorldSize,WorldSize);//smoothes over all the leaves to make them look natural. Can also lead to some trees connecting.
		for(int fx=0;fx<WorldSize;fx++){
			for(int fy=0;fy<WorldSize;fy++){
				if(overWorld[fx][fy].getMarker()){
					overWorld[fx][fy]=new Block(9);//9 represents leaves.
					if(zOverWorld[fx][fy].isAir())
						zOverWorld[fx][fy]=new Block(9);
					overWorld[fx][fy].setMarker(false);
				}
			}
		}
		return;
		
	}

	public void transition(int oBid, int nBid,int y,int varience,int chance,int step) {
		int xl=overWorld[0].length;
		int c=r.nextInt(5)+1;
		for(int d=0;d<c;d++){
			for(int n=xl;n-xl<overWorld[0].length;n+=2){
				if(r.nextInt(100)<chance){//chance of a vein being created
					int m=r.nextInt(varience)+y;//get a random y value for the specific x
					int bpv=r.nextInt(16)+10;
					int rad=r.nextInt((int)(bpv/2))+4,pRad;//Gets a radius based on blocks per vein.
					pRad=rad;
					if(overWorld[m][n%xl].ID==oBid){
						overWorld[m][n%xl].setMarker(true);
						for(int i=0;i<bpv-4;i++){
							int run=0;
							boolean flag=true,frun=true;
							while(flag){
								if(frun){//gives it a smaller radius to generate for the first placement
									rad/=3;
									frun=false;
								}
								else
									rad=pRad;
								int nx=(r.nextInt(rad*2)-rad)+(n%xl),ny=(r.nextInt(rad*2)-rad)+m;//sets a new point to set to block.
								nx+=xl;
								if(Math.pow(nx-n,2)+Math.pow(ny-m,2)<=Math.pow(rad,2)){//see if it's within the radius where the vien can generate.
									if(overWorld[ny][nx%xl].ID==oBid){//change to check if bid is good
										for(int a=ny-1;a<ny+2;a++){
											for(int b=nx-1;b<nx+2;b++){
												if(!((a==ny&&b==nx)||a<0||a>overWorld.length-1)){
													if(overWorld[a][b%xl].getMarker()){//sees if it can connect to the current vien.
														flag=false;
														overWorld[ny][nx%xl].setMarker(true);;
													}
												}
											}
										}
									}
								}
								if(run==500)//in the event that it is taking to long to place a block it kills the loop.
									flag=false;
								else 
									run++;
							}
							rad=pRad;
						}
					}
				}
			}
		}
		for(int n=0;n<step;n++)//smoothes the rough veins into cleaner pockets.
			step(0,UndergroundY-5,WorldSize,Sealevel);
		for(int fx=0;fx<WorldSize;fx++){
			for(int fy=UndergroundY-5;fy<Sealevel;fy++){
				if(overWorld[fy][fx].getMarker()){
					overWorld[fy][fx]=new Block(nBid);
					overWorld[fy][fx].setMarker(false);
				}
			}
		}
		return;
	}

	public void AltPng() {//Creates a secondary picture more for display purposes rather than to tweak the map.
		BufferedImage img=new BufferedImage(WorldSize, WorldSize,BufferedImage.TYPE_INT_RGB);
		int rgb=0;
		int c=0;
		for(int a=WorldSize-1;a>=0;a--){
			for(int b=0;b<WorldSize;b++){
				int d=r.nextInt(21)-10;//offset to help randomize each pixel slightly.
				c=b+(WorldSize/4);
				if(overWorld[a][c%WorldSize].ID==0){
					if(!zOverWorld[a][c%WorldSize].isAir()){
						if(zOverWorld[a][c%WorldSize].ID==1)
							rgb=(138+d<<16)|(138+d<<8)|138+d;
						if(zOverWorld[a][c%WorldSize].ID==2)
							rgb=(131+d<<16)|(66+d<<8)|31+d;
						if(zOverWorld[a][c%WorldSize].ID==3)
							rgb=(154+d<<16)|(125+d<<8)|106+d;
						if(zOverWorld[a][c%WorldSize].ID==4)
							rgb=(25+d<<16)|(25+d<<8)|25+d;
						if(zOverWorld[a][c%WorldSize].ID==5)
							rgb=(0<<16)|(137+d<<8)|36+d;
						if(zOverWorld[a][c%WorldSize].ID==6)
							rgb=(198+d<<16)|(189+d<<8)|136+d;
						if(zOverWorld[a][c%WorldSize].ID==7)
							rgb=(0<<16)|(0<<8)|120+d;
						if(zOverWorld[a][c%WorldSize].ID==8)
							rgb=(154+d<<16)|(95+d<<8)|14+d;
						if(zOverWorld[a][c%WorldSize].ID==9)
							rgb=(0<<16)|(124+d<<8)|0;
					}
					else
						rgb=(0<<16)|(206+d<<8)|230+d;
				}
				if(overWorld[a][c%WorldSize].ID==1)
					rgb=(178+d<<16)|(178+d<<8)|178+d;
				if(overWorld[a][c%WorldSize].ID==2)
					rgb=(171+d<<16)|(106+d<<8)|71+d;
				if(overWorld[a][c%WorldSize].ID==3)
					rgb=(194+d<<16)|(165+d<<8)|146+d;
				if(overWorld[a][c%WorldSize].ID==4)
					rgb=(65+d<<16)|(65+d<<8)|65+d;
				if(overWorld[a][c%WorldSize].ID==5)
					rgb=(34+d<<16)|(177+d<<8)|76+d;
				if(overWorld[a][c%WorldSize].ID==6)
					rgb=(239+d<<16)|(228+d<<8)|176+d;
				if(overWorld[a][c%WorldSize].ID==7){
					if(zOverWorld[a][c%WorldSize].ID!=7){
						if(zOverWorld[a][c%WorldSize].isAir())
							rgb=(0<<16)|(166+d<<8)|255;
						if(zOverWorld[a][c%WorldSize].ID==1)
							rgb=(98+d<<16)|(98+d<<8)|218+d;
						if(zOverWorld[a][c%WorldSize].ID==2)
							rgb=(91+d<<16)|(26+d<<8)|111+d;
						if(zOverWorld[a][c%WorldSize].ID==3)
							rgb=(114+d<<16)|(85+d<<8)|186+d;
						if(zOverWorld[a][c%WorldSize].ID==4)
							rgb=(0<<16)|(0<<8)|105+d;
						if(zOverWorld[a][c%WorldSize].ID==5)
							rgb=(0<<16)|(97+d<<8)|116+d;
						if(zOverWorld[a][c%WorldSize].ID==6)
							rgb=(158+d<<16)|(149+d<<8)|216+d;
						if(zOverWorld[a][c%WorldSize].ID==8)
							rgb=(114+d<<16)|(55+d<<8)|94+d;
						if(zOverWorld[a][c%WorldSize].ID==9)
							rgb=(0<<16)|(84+d<<8)|80;
					}
					else
						rgb=(0<<16)|(0<<8)|160+d;
				}
				if(overWorld[a][c%WorldSize].ID==8)
					rgb=(154+d<<16)|(95+d<<8)|14+d;
				if(overWorld[a][c%WorldSize].ID==9)
					rgb=(27+d<<16)|(164+d<<8)|19+d;
				img.setRGB(b, WorldSize-a-1, rgb);//address to png issue with a
			}
		}
		File f = new File("Island.png");
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		try {
			ImageIO.write(img, "PNG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}