package xiaohanc_CSCI201_Assignment4;

public class Tile {
	String type;
	String row;
	String col;
	String rotation;
	boolean beenHere = false;
	public void minusOne(){
		int temp = Integer.parseInt(col);
		temp=temp-1;		
	}
	
	public Tile(String r, String c, String t, String rot){
		type = t;
		row =r;
		col = c;
		rotation = rot;
	}
}
