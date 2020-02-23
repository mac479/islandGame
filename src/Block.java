
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Random;



public class Block implements Serializable{
	

	private boolean air;
	private boolean marker;
	public int ID=0;
	public int tdmg=0;
	public int dmg=0;
	public int toolR=0;
	public int toolLv=0;
	public String name="";
	public Block(int bid){
		marker=false;
		Id x=new Id(bid);
		name=x.name;
		ID=x.ID;
		tdmg=x.tdmg;
		toolR=x.toolR;
		toolLv=x.toolLv;
		if(bid==0){
			air=true;
		}
		//modify code for air later
		
	}
	public World vienMaker(World world,int bid, int x,int y,int bpv){
		Random r=new Random();
		int xl=world.overWorld[0].length,yl=world.overWorld.length;
		x+=xl;
		y+=yl;
		int rad=r.nextInt((int)(bpv/2))+4,pRad;
		pRad=rad;
		if(world.overWorld[y%yl][x%xl].ID==bid)
			world.overWorld[y%yl][x%xl]=new Block(ID);
		else
			return world;
		for(int c=0;c<bpv-4;c++){
			int run=0;
			boolean flag=true,frun=true;
			while(flag){
				if(frun){
					rad/=3;
					frun=false;
				}
				else
					rad=pRad;
				int nx=(r.nextInt(rad*2)-rad)+(x%xl),ny=(r.nextInt(rad*2)-rad)+(y%yl);
				nx+=xl;
				ny+=yl;
				if(Math.pow(nx-x,2)+Math.pow(ny-y,2)<=Math.pow(rad,2)){
					if(world.overWorld[ny%yl][nx%xl].ID==bid){//change to check if bid is good
						if(world.overWorld[ny%yl][(nx-1)%xl].ID==ID){//right center
							flag=false;
							world.overWorld[ny%yl][nx%xl]=new Block(ID);
						}
						else if(world.overWorld[ny%yl][(nx+1)%xl].ID==ID){//left center
							flag=false;
							world.overWorld[ny%yl][nx%xl]=new Block(ID);
						}
						else if(world.overWorld[(ny-1)%yl][nx%xl].ID==ID){//above center
							flag=false;
							world.overWorld[ny%yl][nx%xl]=new Block(ID);
						}
						else if(world.overWorld[(ny+1)%yl][nx%xl].ID==ID){//below center
							flag=false;
							world.overWorld[ny%yl][nx%xl]=new Block(ID);
						}
					}
				}
				if(run==500)
					flag=false;
				else 
					run++;
			}
			rad=pRad;
		}
		return world;
	}
	
	public boolean isAir(){
		return air;
	}
	
	public void setMarker(boolean z){
		marker=z;
	}
	
	public boolean getMarker(){
		return marker;
	}

}
