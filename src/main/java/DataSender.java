import world.World;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DataSender implements Runnable{

    public static enum TYPE{
        GD,
        MD
    }
	
	Socket connection = null;
	boolean finishFrame = false;
	boolean finished = false;
	
	Integer time = 0;

	public TYPE type = TYPE.GD;

	public World world;

    public DataSender(Socket conn, World world){
        this.connection = conn;
        this.world = world;
    }
	
	public DataSender(Socket conn, World world, TYPE type){
		this.connection = conn;
		this.world = world;
		this.type = type;
	}

	public void exit(DataOutputStream out, String s) throws IOException, InterruptedException {
		finishFrame = true;
		System.out.println("Stopping data sending");
		while(!finished){
			Thread.sleep(100);
		}
		out.write(s.getBytes());
		
	}
	
	private int calculateChecksum(String st) {
        String data = st.substring(0, st.length());
        int expected_sum = 0x00;
        for (int i = 0; i < data.length(); i++) {
            expected_sum += (int) data.charAt(i);
        }
        expected_sum = (expected_sum & 0x3f) + 0x30;
        return (char) expected_sum;
    }
	
	private String createData(){
		//World world = new World();
		return world.getDataString();
	}
	
	private List<String> splitEquallysplitEqually(String text, int size) {
		// Give the list the right capacity to start with. You could use an array
	    // instead if you wanted.
	    List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

	    for (int start = 0; start < text.length(); start += size) {
	    	ret.add(text.substring(start, Math.min(text.length(), start + size)));
	    }
	    return ret;
	}
	
	public String parseDataString(String s){
		StringBuilder finalString = new StringBuilder();

		List<String> strings = splitEquallysplitEqually(s, 64);
		for(String t: strings){
			t = t + (char) calculateChecksum(t) + "\n";
			finalString.append(t);
		}
		finalString.append("\n");
		
		return finalString.toString();
	}

	public String encodeTime() {
		StringBuilder data= new StringBuilder();
		int value = this.time;
		char firstChar = (char) (((value & 0x3F000) >> 12) + 0x30);
		char secondChar = (char) (((value & 0xFC0) >> 6) + 0x30);
		char thirdChar = (char) ((value & 0x3F) + 0x30);
		data.append(firstChar);
		data.append(secondChar);
		data.append(thirdChar);
		return data.toString();
	}

	@Override
	synchronized public void run() {
		//standard full frame header
        if(LidarSpoof.debug_dataSender)
		    System.out.println("Starting data sending");
        StringBuilder builder = new StringBuilder();
		switch (type){
            case MD:{
                builder = new StringBuilder("MD000108001000\n99b\n");
                break;
            }
            case GD:{
                builder = new StringBuilder("GD0000108001\n00P\n");
                break;
            }
        }
        do{
            //timestamp+checksum
            builder.append(encodeTime());
            builder.append((char) calculateChecksum(encodeTime()) + "\n");
            String s = parseDataString(createData());
            builder.append(s);

            try {
                connection.getOutputStream().write(builder.toString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
                if(LidarSpoof.debug_dataSender)
                    System.err.println("Error stopping sending");
                finishFrame = true;
            }
            time++;

        }while (type == TYPE.MD && !finishFrame);
        finished = true;
	}
}
