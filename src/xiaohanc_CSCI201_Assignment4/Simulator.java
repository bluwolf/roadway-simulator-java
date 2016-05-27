package xiaohanc_CSCI201_Assignment4;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class Simulator extends JFrame{
	
 Vector<Tile> cells = new Vector<Tile>();
 Vector<Car> cars = new Vector<Car>();
 String filename;
 char[] list = {'A','B','C','D','E','F','G','H','I'};
 int numCars;
 DataTable dataT =new DataTable(numCars);
 
	public Simulator(){
	super("Roadway Simulator");
	setSize(800, 600);
	setLocation(100, 100);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	this.setLayout(new FlowLayout());
	
	//set up menu
	JMenuBar jmb = new JMenuBar();
	final JMenuItem openItemMenu = new JMenuItem("Open File...");
	openItemMenu.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
			 JFileChooser chooser = new JFileChooser();
			    FileNameExtensionFilter filter = new FileNameExtensionFilter("xml files", "xml");
			    chooser.setFileFilter(filter);
			    File workingDirectory = new File(System.getProperty("user.dir"));
			    chooser.setCurrentDirectory(workingDirectory);
			    int returnVal = chooser.showOpenDialog(openItemMenu);
			    if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = chooser.getSelectedFile();
		            System.out.println("Opening: " + file.getName());
		            filename = file.getName();
		            try {		            	
						cells =parseXML(filename);
						cars = parseCars(filename);

					} catch (ParserConfigurationException | SAXException
							| IOException e1) {
						e1.printStackTrace();
					}
		            numCars = cars.size();
					add(new DrawingPanel(), BorderLayout.CENTER);
					add(new DataTable(numCars), BorderLayout.EAST);
					revalidate();
					for(int i=0;i<cars.size();i++){
						cars.get(i).startThread();
					}
//					cars.get(0).startThread();

		        } else {
		        	System.out.println("Open command cancelled by user");
		        }
		}

	});

	jmb.add(openItemMenu);
	setJMenuBar(jmb);
	setVisible(true);
	}
	
	
	 class DrawingPanel extends JPanel{
		 public DrawingPanel(){
		        setOpaque(true);
		        setBackground(Color.WHITE);  
		  }
		 
		 protected void paintComponent(Graphics g){
			 super.paintComponent(g);		 
			 int xc = this.getWidth()/2;
			 int yc = this.getHeight()/2;
			 int xst = xc-4*50-25;
			 int yst = yc-4*50-25;			 
			 int[] x = new int[9];
			 int[] y = new int[9];
			 int idy =0;
			 int idx =0;
			 //save x and y coordinaties
			 for(int xd=0;xd<9;xd++){
			     for(int yd=0;yd<9;yd++){
			         if(idy<9){y[idy] = yd*50+yst;}
			         idy++;			         
			     }
			     x[idx] = xd*50+xst;
			     idx++;
			 }
	 		 
			 // draw row and col labels
			 String[] rowLabel = {"A","B","C","D","E","F","G","H","I"};
			 for (int i=0;i<9;i++){
				 g.drawString(rowLabel[i],xst-20,yst+(i)*50+25);
			 }
			 int num =0;
			 while(num<81){
				 if(num%9==0){
					 g.drawString(Integer.toString(num/9+1),xst+(num/9)*50+20,yst-20); 
				 }
				 num++;
			 }			 
//			 System.out.println(Arrays.toString(x));
//			 System.out.println(Arrays.toString(y));
			 for(int i=0;i<cars.size();i++){ // put map info in car
				 cars.get(i).Xarr = x;
				 cars.get(i).Yarr = y;
				 cars.get(i).g =g;
			 }
			 
			 char[] list = {'A','B','C','D','E','F','G','H','I'};
			 for(int i=0;i<81;i++){
				 String type = cells.get(i).type;
				 int r = Integer.parseInt(cells.get(i).col)-1;
				 int c = (new String(list).indexOf(cells.get(i).row));	
				 int rotation =Integer.parseInt(cells.get(i).rotation);
				 
				 switch(type){
				 case "blank":
					 drawBlank(g,x,y,r,c);
					 break;
				 case "l":
					 drawL(g,x,y,r,c,rotation);
					 break;
				 case "i":
					 drawI(g,x,y,r,c,rotation);
					 break;
				 case "t":
					 drawT(g,x,y,r,c,rotation);
					 break;
				 case "+":
					 drawCross(g,x,y,r,c);
					 break;
				 
				 }
			 }
			 //x is column
			 g.setColor(Color.BLACK);
			 for(int xd=0;xd<9;xd++){
			     for(int yd=0;yd<9;yd++){
			         g.drawRect(xd*50+xst,yd*50+yst,50,50);        		         
			     }
			 }
			 for(int i=0;i<cars.size();i++){
				if(cars.get(i).visible==false){
					cars.get(i).carAppear();
					cars.get(i).visible = true;
				}
				else{
					cars.get(i).carGone();
					cars.get(i).visible= false;
				}
			 }

			 
		 }
		 
	 }
	 public void drawCar(Graphics g,int[] x,int []y,  int ix, int iy, String color, int num){
		 String n = Integer.toString(num);
		 g.setColor(stringToColor(color));
		 g.fillOval(x[ix]+10,y[iy]+10,30,30);
		 g.setColor(Color.BLACK);
		 g.setFont(new Font("default", Font.BOLD, 16));
		 g.drawString(n, x[ix]+21,y[iy]+27);
	 }
	 public void drawBlank(Graphics g,int[] x,int []y,  int ix, int iy){
		 g.setColor(Color.GREEN.darker());
		 g.fillRect(x[ix],y[iy],50,50);
	 }
	 public void drawL(Graphics g,int[] x,int []y, int ix, int iy, int rot){
		 g.setColor(Color.GREEN.darker());
		 g.fillRect(x[ix],y[iy],50,50);
		 g.setColor(Color.BLACK);
		 if(rot==0){
			 g.fillRect(x[ix]+15, y[iy], 20, 35);
			 g.fillRect(x[ix]+15, y[iy]+15, 35, 20);
		 }
		 if(rot ==90){
			 g.fillRect(x[ix]+15, y[iy], 20, 35);
			 g.fillRect(x[ix], y[iy]+15, 35, 20);
			 
		 }
		 if(rot ==180){
			 g.fillRect(x[ix]+15, y[iy]+15, 20, 35);
			 g.fillRect(x[ix], y[iy]+15, 35, 20);
			 
		 }
		 if(rot ==270){
			 g.fillRect(x[ix]+15, y[iy]+15, 20, 35);
			 g.fillRect(x[ix]+15, y[iy]+15, 35, 20);
		 }
	 }
	 public void drawT(Graphics g,int[] x,int []y, int ix, int iy, int rot){
		 g.setColor(Color.GREEN.darker());
		 g.fillRect(x[ix],y[iy],50,50);
		 g.setColor(Color.BLACK);
		 if(rot==0){
			 g.fillRect(x[ix], y[iy]+15, 50, 20);
			 g.fillRect(x[ix]+15, y[iy]+15, 20, 35);
		 }
		 if(rot ==90){
			 g.fillRect(x[ix]+15, y[iy]+15, 35, 20);
			 g.fillRect(x[ix]+15, y[iy], 20, 50);
			 
		 }
		 if(rot ==180){
			 g.fillRect(x[ix], y[iy]+15, 50, 20);
			 g.fillRect(x[ix]+15, y[iy], 20, 35);
			 
		 }
		 if(rot ==270){
			 g.fillRect(x[ix], y[iy]+15, 35, 20);
			 g.fillRect(x[ix]+15, y[iy], 20, 50);
			 
		 }
	 }
	 public void drawCross(Graphics g,int[] x,int []y, int ix, int iy){
		 g.setColor(Color.GREEN.darker());
		 g.fillRect(x[ix],y[iy],50,50);
		 g.setColor(Color.BLACK);
		 g.fillRect(x[ix]+15, y[iy], 20, 50);
		 g.fillRect(x[ix], y[iy]+15, 50, 20);
	 }
	 public void drawI(Graphics g,int[] x,int []y, int ix, int iy,int rot){
		 if(rot==0||rot==180){
			 g.setColor(Color.GREEN.darker());
			 g.fillRect(x[ix],y[iy],50,50);
			 g.setColor(Color.BLACK);
			 g.fillRect(x[ix]+15, y[iy], 20, 50);
		 }
		 else{
			 g.setColor(Color.GREEN.darker());
			 g.fillRect(x[ix],y[iy],50,50);
			 g.setColor(Color.BLACK);
			 g.fillRect(x[ix], y[iy]+15, 50, 20);
			 
		 }
		 
	 }
	 
	 class DataTable extends JPanel{
		 public DataTable(int size){
			
			 setResizable(true);
			 String[] columnNames = {"Car #","X","Y"};
			 Object [][] data = new Object[size][3];
			 for(int i=0;i<cars.size();i++){
				 data[i][0]= cars.get(i).number;
				 data[i][1] = cars.get(i).y+1;
				 data[i][2] = list[cars.get(i).x];
			 }
//			 DefaultTableModel model = new DefaultTableModel(new Object[]{"column1","column2"},0);
		     JTable tables = new JTable(data,columnNames);
		     JScrollPane jsp = new JScrollPane(tables);
		     jsp.setPreferredSize(new Dimension(200,600));
		     jsp.revalidate();
		     add(jsp);
		     
		 }
	
	 }
	
	public static void main (String args[]){
		System.out.println("Please give me an XML file");
		Simulator sim = new Simulator();
	}
    public Vector<Car> parseCars(String filename) throws ParserConfigurationException, SAXException, IOException{
    	Vector<Car> cars = new Vector<Car>();
    	
		File fXmlFile = new File(filename);		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		org.w3c.dom.Document doc = dBuilder.parse(fXmlFile);		
		((Node) doc.getDocumentElement()).normalize();		 
	//	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
		NodeList nList = doc.getElementsByTagName("car");
		
		double speed;
		int ai;
		int carnum=1;

		String color;
		
		for (int temp = 0; temp < nList.getLength(); temp++) {	
			Node nNode = nList.item(temp);	
	
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode; 
				speed = Double.parseDouble(eElement.getAttribute("speed"));
				ai = Integer.parseInt(eElement.getAttribute("ai"));
				color = eElement.getAttribute("color");
//				locx= Integer.parseInt(eElement.getElementsByTagName("location").item(1).getTextContent());
				
				Car c = new Car(color,0,0,ai,speed,null,carnum++); // initialiiy set without x and y
				cars.add(c);			
			}
		}// reset location x and y
		int locy;
		int locx;
		int ind=0;
		char[] list = {'A','B','C','D','E','F','G','H','I'};
		NodeList lList = doc.getElementsByTagName("location");
		for (int temp = 0; temp < lList.getLength(); temp++) {	
			Node nNode = lList.item(temp);			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode; 

				locx = (new String(list).indexOf(eElement.getAttribute("y")));
				locy = Integer.parseInt(eElement.getAttribute("x"));
				cars.get(ind).x = locx;
				cars.get(ind).y= locy-1;
			
				System.out.print(" speed: "+cars.get(ind).speed);			
				System.out.print(" ai : " + cars.get(ind).ai);
				System.out.print(" color : " + cars.get(ind).color);
				System.out.print(" locy : " + cars.get(ind).y);
				System.out.print(" locx : " + cars.get(ind).x);
				System.out.println("");
				ind++;
				
			}
		}
		return cars;
    }
	public Vector<Tile>  parseXML(String filename) throws ParserConfigurationException, SAXException, IOException{
		File fXmlFile = new File(filename);		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		org.w3c.dom.Document doc = dBuilder.parse(fXmlFile);		
		((Node) doc.getDocumentElement()).normalize();		 
	//	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
		String[] rowLabel = {"A","B","C","D","E","F","G","H","I"};
		NodeList nList = doc.getElementsByTagName("tile");
	//	NodeList rList = doc.getElementsByTagName("row");
		Vector<Tile> cells = new Vector<Tile>();
		
		String degree;
		String type;
		String col;
		String row;
		int rowIdx=0;;
		for (int temp = 0; temp < nList.getLength(); temp++) {	
			Node nNode = nList.item(temp);	 
	//		System.out.println("\nCurrent Element :" + nNode.getNodeName());		
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode; 
				degree = eElement.getAttribute("degree");
				type = eElement.getAttribute("type");
				col = eElement.getAttribute("column");
				row = rowLabel[rowIdx];
				Tile t = new Tile(row,col,type,degree);
				cells.add(t);			
//				System.out.print(" current row: "+rowLabel[rowIdx]);			
//				System.out.print(" tile degree : " + eElement.getAttribute("degree"));
//				System.out.print(" type : " + eElement.getAttribute("type"));
//				System.out.print(" column : " + eElement.getAttribute("column"));
//				System.out.println("");
				if (col.equals("9")){rowIdx++;};
			
			}
		}

		return cells;
	}
	 public static Color stringToColor(final String value) {
		    if (value == null) {
		      return Color.black;
		    }
		    try {
		      return Color.decode(value);
		    } catch (NumberFormatException nfe) {
			      try {
			        final Field f = Color.class.getField(value);
			        
			        return (Color) f.get(null);
			      } catch (Exception ce) {
			        return Color.black;
			      }
		    }
		  }
	 
	 public class Car implements Runnable{
			String color;
			int x;
			int y;
			int ai;
			double speed;
			Graphics g;
			int [] Xarr;
			int [] Yarr;
			Tile t;
			char[] list = {'A','B','C','D','E','F','G','H','I'};
			boolean visible;
			Thread th;
			String pmove = "left";
			boolean a1Flag= true;
			boolean moveSuccess = false;
			int number;
			boolean eastflag = true;
			boolean westflag = true;
//			ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
			
			public void carGone(){
				this.t = findTile();
		    	String type = t.type;
//		    	System.out.println(type);
				 int r = Integer.parseInt(t.col)-1;
				 int c = (new String(list).indexOf(t.row));	
				 int rotation =Integer.parseInt(t.rotation);

		         switch(type){
				 case "blank":
					 drawBlank(g,Xarr,Yarr,r,c);
					 break;
				 case "l":
					 drawL(g,Xarr,Yarr,r,c,rotation);
					 break;
				 case "i":
					 drawI(g,Xarr,Yarr,r,c,rotation);
					 break;
				 case "t":
					 drawT(g,Xarr,Yarr,r,c,rotation);
					 break;
				 case "+":
					 drawCross(g,Xarr,Yarr,r,c);
					 break;            
		    	}
			}
			
			public void carAppear(){
				switch(ai){
				case 1:
					if((y-1)>=0 && (!BlankLeft())&& (a1Flag == true)){
						y=y-1;
						drawCar(g,Xarr,Yarr,y,x,color,number);
						a1Flag = false;
						moveSuccess = true;
					}
					if(moveSuccess){
						if(pmove.equals("left")){
							if(x+1<=8 && !BlankBelow()){
								x=x+1;
								drawCar(g,Xarr,Yarr,y,x,color,number);
								pmove = "down";
								moveSuccess = true;
							}
							else moveSuccess =false;
						}
							
						else if(pmove.equals("down")){
								pmove = "right";
								if(y+1<=8 && !BlankRight()){
									y=y+1;
									drawCar(g,Xarr,Yarr,y,x,color,number);
									
									moveSuccess = true;
								}
								else moveSuccess =false;
							}
							else if(pmove.equals("right")){
								
								pmove = "up";
								if(x-1>=0 && !BlankAbove()){
									
									x=x-1;
									drawCar(g,Xarr,Yarr,y,x,color,number);
									
									moveSuccess = true;
								}
								else moveSuccess =false;
							}
							else if(pmove.equals("up")){
//								System.out.println("checking left");
								pmove = "left";
								if(y-1>=0 && !BlankLeft()){
//									System.out.println("left success");
									y=y-1;
									drawCar(g,Xarr,Yarr,y,x,color,number);
									
									moveSuccess = true;
								}
								else moveSuccess =false;
							}
					}
					else if(moveSuccess == false){
							if(pmove.equals("left")){
//								System.out.println("checking up c");
								pmove = "up";
								if(x-1>=0 && !BlankAbove() && !moveSuccess){
//									System.out.println("up success c");
									x=x-1;
									drawCar(g,Xarr,Yarr,y,x,color,number);
									
									moveSuccess = true;
								}
							}
								
							else if(pmove.equals("down")){
									pmove = "left";
									if(y-1>=0 && !BlankLeft()&& !moveSuccess){
										y=y-1;
										drawCar(g,Xarr,Yarr,y,x,color,number);
										
										moveSuccess = true;
									}
								}
							else if(pmove.equals("right")){
									pmove = "down";
									if(x+1<=8 && !BlankAbove()&& !moveSuccess){
										x=x+1;
										drawCar(g,Xarr,Yarr,y,x,color,number);
										
										moveSuccess = true;
									}
								}
							else if(pmove.equals("up")){
									pmove = "right";
									if(y+1<=8 && !BlankRight()&& !moveSuccess){
										y=y+1;
										drawCar(g,Xarr,Yarr,y,x,color,number);
										
										moveSuccess = true;
									}
								}
						}
//					add(new DataTable(numCars), BorderLayout.EAST);
//					revalidate();
//					repaint();
//					drawCar(g,Xarr,Yarr,y,x,color,1);
					break;
				case 2:
					Random randomGenerator = new Random();
					int num = randomGenerator.nextInt(4);
					switch (num){
						case 0:
							if(x+1<=8 && !BlankBelow()){
								x=x+1;
								drawCar(g,Xarr,Yarr,y,x,color,number);
							}
							break;
						case 1:
							if(y+1<=8 && !BlankRight()){
								y=y+1;
								drawCar(g,Xarr,Yarr,y,x,color,number);
		
						}
							break;
						case 2:
							if(x-1>=0 && !BlankAbove()){
								x=x-1;
								drawCar(g,Xarr,Yarr,y,x,color,number);
		
						}
							break;
						case 3:
							if(y-1>=0 && !BlankLeft()){
								y=y-1;
								drawCar(g,Xarr,Yarr,y,x,color,number);
	
						}
						break;
//						
					}
					drawCar(g,Xarr,Yarr,y,x,color,number);
					

					break;
				case 3:
					if(y+1<=8 && !BlankRight() && eastflag == true){
						y=y+1;
						drawCar(g,Xarr,Yarr,y,x,color,number);

					}
					else eastflag = false;

					if(y-1>=0 && !BlankLeft()&& !eastflag && westflag){
						y=y-1;
						drawCar(g,Xarr,Yarr,y,x,color,number);
					}
					else westflag =false;
					Random randomNum = new Random();
					int num1 = randomNum.nextInt(4);
					switch (num1){
						case 0:
							if(x+1<=8 && !BlankBelow()){
								x=x+1;
								drawCar(g,Xarr,Yarr,y,x,color,number);
							}
							break;
						case 1:
							if(y+1<=8 && !BlankRight()){
								y=y+1;
								drawCar(g,Xarr,Yarr,y,x,color,number);
		
						}
							break;
						case 2:
							if(x-1>=0 && !BlankAbove()){
								x=x-1;
								drawCar(g,Xarr,Yarr,y,x,color,number);
		
						}
							break;
						case 3:
							if(y-1>=0 && !BlankLeft()){
								y=y-1;
								drawCar(g,Xarr,Yarr,y,x,color,number);
	
						}
						break;
//						
					}
//					drawCar(g,Xarr,Yarr,y,x,color,number);
					break;
				case 4:
					Random randomGenerator1 = new Random();
					int num2 = randomGenerator1.nextInt(4);
					switch (num2){
						case 0:
							if(x+1<=8 && !BlankBelow()){
								x=x+1;
								drawCar(g,Xarr,Yarr,y,x,color,number);
							}
							break;
						case 1:
							if(y+1<=8 && !BlankRight()){
								y=y+1;
								drawCar(g,Xarr,Yarr,y,x,color,number);
		
						}
							break;
						case 2:
							if(x-1>=0 && !BlankAbove()){
								x=x-1;
								drawCar(g,Xarr,Yarr,y,x,color,number);
		
						}
							break;
						case 3:
							if(y-1>=0 && !BlankLeft()){
								y=y-1;
								drawCar(g,Xarr,Yarr,y,x,color,number);
	
						}
						break;
//						
					}
					drawCar(g,Xarr,Yarr,y,x,color,number);
					break;
				}
				
//				if()
			}
			public boolean ConnectedAbove(){			
				for (int i=0;i<cells.size();i++){
					if(((new String(list).indexOf(cells.get(i).row)) ==x-1) && ((Integer.parseInt(cells.get(i).col)-1) == y)){
						if(cells.get(i).type.equals("i")){
							if(cells.get(i).rotation.equals("0")) return true;
							if(cells.get(i).rotation.equals("180")) return true;
						}
						if(cells.get(i).type.equals("l")){
							if(cells.get(i).rotation.equals("270")) return true;
							if(cells.get(i).rotation.equals("180")) return true;
						}
						if(cells.get(i).type.equals("t")){
							if(cells.get(i).rotation.equals("0")) return true;
							if(cells.get(i).rotation.equals("90")) return true;
							if(cells.get(i).rotation.equals("270")) return true;
						}
						if(cells.get(i).type.equals("+")) return true;

					}
				}
				return false;
			}
			public boolean ConnectedBelow(){			
				for (int i=0;i<cells.size();i++){
					if(((new String(list).indexOf(cells.get(i).row)) ==x+1) && ((Integer.parseInt(cells.get(i).col)-1) == y)){
						if(cells.get(i).type.equals("i")){
							if(cells.get(i).rotation.equals("0")) return true;
							if(cells.get(i).rotation.equals("180")) return true;
						}
						if(cells.get(i).type.equals("l")){
							if(cells.get(i).rotation.equals("0")) return true;
							if(cells.get(i).rotation.equals("90")) return true;
						}
						if(cells.get(i).type.equals("t")){
							if(cells.get(i).rotation.equals("180")) return true;
							if(cells.get(i).rotation.equals("90")) return true;
							if(cells.get(i).rotation.equals("270")) return true;
						}
						if(cells.get(i).type.equals("+")) return true;

					}
				}
				return false;
			}
			public boolean ConnectedLeft(){			
				for (int i=0;i<cells.size();i++){
					if(((new String(list).indexOf(cells.get(i).row)) ==x) && ((Integer.parseInt(cells.get(i).col)-1) == y-1)){
						if(cells.get(i).type.equals("i")){
							if(cells.get(i).rotation.equals("90")) return true;
							if(cells.get(i).rotation.equals("270")) return true;
						}
						if(cells.get(i).type.equals("l")){
							if(cells.get(i).rotation.equals("0")) return true;
							if(cells.get(i).rotation.equals("270")) return true;
						}
						if(cells.get(i).type.equals("t")){
							if(cells.get(i).rotation.equals("0")) return true;
							if(cells.get(i).rotation.equals("90")) return true;
							if(cells.get(i).rotation.equals("270")) return true;
						}
						if(cells.get(i).type.equals("+")) return true;

					}
				}
				return false;
			}
			public boolean ConnectedRight(){			
				for (int i=0;i<cells.size();i++){
					if(((new String(list).indexOf(cells.get(i).row)) ==x) && ((Integer.parseInt(cells.get(i).col)-1) == y+1)){
						if(cells.get(i).type.equals("i")){
							if(cells.get(i).rotation.equals("90")) return true;
							if(cells.get(i).rotation.equals("270")) return true;
						}
						if(cells.get(i).type.equals("l")){
							if(cells.get(i).rotation.equals("90")) return true;
							if(cells.get(i).rotation.equals("180")) return true;
						}
						if(cells.get(i).type.equals("t")){
							if(cells.get(i).rotation.equals("0")) return true;
							if(cells.get(i).rotation.equals("180")) return true;
							if(cells.get(i).rotation.equals("270")) return true;
						}
						if(cells.get(i).type.equals("+")) return true;

					}
				}
				return false;
			}
			public boolean BlankAbove(){			
				for (int i=0;i<cells.size();i++){
					if(((new String(list).indexOf(cells.get(i).row)) ==x-1) && ((Integer.parseInt(cells.get(i).col)-1) == y)){
						if(cells.get(i).type.equals("blank"))
						return true;
					}
				}
				return false;
			}
			
			public boolean BlankBelow(){
				String type = null;			
				for (int i=0;i<cells.size();i++){
					if(((new String(list).indexOf(cells.get(i).row)) ==x+1) && ((Integer.parseInt(cells.get(i).col)-1) == y)){
						if(cells.get(i).type.equals("blank"))
						return true;
					}
				}
				return false;
			}
			public boolean BlankLeft(){
				String type = null;			
				for (int i=0;i<cells.size();i++){
					if(((new String(list).indexOf(cells.get(i).row)) ==x) && ((Integer.parseInt(cells.get(i).col)-1) == y-1)){
						if(cells.get(i).type.equals("blank"))
						return true;
					}
				}
				return false;
			}
			public boolean BlankRight(){			
				for (int i=0;i<cells.size();i++){
					if(((new String(list).indexOf(cells.get(i).row)) ==x) && ((Integer.parseInt(cells.get(i).col)-1) == y+1)){
						if(cells.get(i).type.equals("blank"))
						return true;
					}
				}
				return false;
			}
			public Tile findTile(){
				Tile t = null;			
				for (int i=0;i<cells.size();i++){
					if(((new String(list).indexOf(cells.get(i).row)) ==x) && ((Integer.parseInt(cells.get(i).col)-1) == y)){
						t = cells.get(i);
						return t;
					}
				}
				return t;
			}

			public Car(String c, int x, int y, int a, double s, Graphics g, int num){
				color =c;
				this.x =x;
				this.y = y;
				ai =a;
				speed = s;
				this.g = g;
				this.number=num;
			}

			public void startThread(){
				th = new Thread(this);
				th.start();
			}
			public void run() {
				try {
					while(true){
//						System.out.println("Running Thread");
						repaint();
						add(new DataTable(numCars), BorderLayout.EAST);
						revalidate();
						
						Thread.sleep((long) (speed*1000));
//						System.out.println("Done sleeping");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
}
