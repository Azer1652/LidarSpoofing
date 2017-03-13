import com.jogamp.opengl.awt.GLCanvas;
import world.World;
import worldview.WorldViewer;

import javax.swing.*;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.ExportException;


public class LidarSpoof {
	ServerSocket initSocket;
	static Socket connection = null;
	static BufferedReader in = null;
	static DataOutputStream out = null;
	boolean disconnected = false;

	static public World world;
    static DataSender sender;
    static Thread sendThread = null;

	static public boolean debug_commands = true;
	static public boolean debug_dataSender = false;
	
	public LidarSpoof(World world) throws IOException, InterruptedException{
		this.world = world;
	    sender = new DataSender(connection, world);

	    initSocket = new ServerSocket(10940);
		while(true){
			System.out.println("Socket open and waiting");
			connection = initSocket.accept();
			System.out.println("Socket accepted");
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			out = new DataOutputStream(connection.getOutputStream());
			sender.connection = connection;
			System.out.println("Reading Line");
			String command = in.readLine();
			disconnected = false;
			while(!disconnected){
				if(command == null){
					disconnected = true;
				}else{
					if(debug_commands)
						System.out.println(command);
					handleCommand(command);
					command = in.readLine();
				}
			}
			
			/*System.out.println("Received: " + clientSentence);
            capitalizedSentence = clientSentence.toUpperCase() + "\n";
            outToClient.writeBytes(capitalizedSentence);*/
			
		}
	}
	
	public static void handleCommand(String command) throws IOException, InterruptedException{
		StringBuilder builder = null;
		String model;
		switch(command){
		case "QT": 
			String s = "QT\n00P\n\n";
			if(sender != null)
				if(sender.type == DataSender.TYPE.MD)
					sender.exit(out, "QT\n00P\n\n");
				else
					out.write(s.getBytes());
			else
				out.write(s.getBytes());
			break;
		case "PP":
			builder = new StringBuilder("PP\n00P\n");
			model = "MODL:UST-10LX";
			appendTerminator(builder, model);
			model = "DMIN:20";
			appendTerminator(builder, model);
			model = "DMAX:30000";
			appendTerminator(builder, model);
			model = "ARES:1440";
			appendTerminator(builder, model);
			model = "AMIN:0";
			appendTerminator(builder, model);
			model = "AMAX:1080";
			appendTerminator(builder, model);
			model = "AFRT:540";
			appendTerminator(builder, model);
			model = "SCAN:2400";
			appendTerminator(builder, model);
			builder.append("\n");
			out.write(builder.toString().getBytes());
			break;
		case "II":
			builder = new StringBuilder("II\n00P\n");
			model = "MODL:UST-10LX";
			appendTerminator(builder, model);
			model = "LASR:OFF";
			appendTerminator(builder, model);
			model = "SCSP:2400";
			appendTerminator(builder, model);
			model = "MESM:Measuring by Sensitive Mode";
			appendTerminator(builder, model);
			model = "SBPS:Ethernet 100[Mbps]";
			appendTerminator(builder, model);
			model = "TIME:0Y\3";
			appendTerminator(builder, model);
			model = "STAT:sensor is working normally";
			appendTerminator(builder, model);
			builder.append("\n");
			out.write(builder.toString().getBytes());
			break;
		case "VV":
			builder = new StringBuilder("VV\n00P\n");
			model = "VEND:Hokuyo Automatic Co., Ltd.";
			appendTerminator(builder, model);
			model = "PROD:UST-10LX";
			appendTerminator(builder, model);
			model = "FIRM:2.23.0000";
			appendTerminator(builder, model);
			model = "PROT:SCIP 2.2";
			appendTerminator(builder, model);
			model = "SERI:H1614934";
			appendTerminator(builder, model);
			builder.append("\n");
			out.write(builder.toString().getBytes());
			break;
		case "MD0000108001000":
			//builder = new StringBuilder("MD0000108001000\n00P\n\n");
			//out.write(builder.toString().getBytes());
			sender.type = DataSender.TYPE.MD;
			sendThread = new Thread(sender, "Send Thread");
			sendThread.start();
			break;
		case "GD0000108001":
            //builder = new StringBuilder("GD0000108001\n00P\n\n");
            //out.write(builder.toString().getBytes());
			sender.type = DataSender.TYPE.GD;
			sendThread = new Thread(sender, "Send Thread");
			sendThread.start();
			break;
		case "GD":
			builder = new StringBuilder("GD\n10Q\n\n");
			out.write(builder.toString().getBytes());
			break;
		case "BM":
			builder = new StringBuilder("BM\n00P\n\n");
			out.write(builder.toString().getBytes());
			break;
		case "MD":
			builder = new StringBuilder("MD\n0Cc\n\n");
			out.write(builder.toString().getBytes());
			break;
		case "GE":
			builder = new StringBuilder("GE\n10Q\n\n");
			out.write(builder.toString().getBytes());
			break;
		case "ME":
			builder = new StringBuilder("ME\n0Cc\n\n");
			out.write(builder.toString().getBytes());
			break;
		default:
			builder = new StringBuilder(command + "\n0Ff\n\n");
			out.write(builder.toString().getBytes());
			break;
		}
		
			
	}
	
	public static void appendTerminator(StringBuilder builder, String model){
		builder.append(model);
		builder.append(';');
		builder.append((char) calculateChecksum(model));
		builder.append("\n");
	}

	private static int calculateChecksum(String st) {
        String data = st.substring(0, st.length());
        int expected_sum = 0x00;
        for (int i = 0; i < data.length(); i++) {
            expected_sum += (int) data.charAt(i);
        }
        expected_sum = (expected_sum & 0x3f) + 0x30;
        return expected_sum;
    }
	
	public static void main(String[] args) {
		World world = new World();
		new Thread(() -> {
			try {
				LidarSpoof lidarSpoof = new LidarSpoof(world);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WorldViewer worldViewer = new WorldViewer(world, 600, 600);
			}
		});
		//new World();

	}

}
