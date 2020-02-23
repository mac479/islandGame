

final public class Id {
	public int ID=0;
	public int tdmg=0;
	public int toolR=0;
	public int toolLv=0;
	public String name="";
	public Id(int n){
		ID=n;
		switch(n){
			case 0:
				name="¯\\_('-')_/¯";
				tdmg=-1;
				toolR=-1;
				toolLv=-1;
				break;
			case 1:
				name="Stone";
				tdmg=75;
				toolR=1;
				toolLv=1;
				break;
			case 2:
				name="Dirt";
				tdmg=35;
				toolR=0;
				toolLv=0;
				break;
			case 3:
				name="Iron Ore";
				tdmg=110;
				toolR=1;
				toolLv=1;
				break;
			case 4:
				name="Coal Ore";
				tdmg=90;
				toolR=1;
				toolLv=1;
				break;
					
		}
	}
}
