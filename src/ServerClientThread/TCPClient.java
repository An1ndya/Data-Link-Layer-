package ServerClientThread;
import java.io.*;
import java.net.*;
import java.util.Random;
class TCPClient
{
    public static void main(String argv[]) throws Exception
    {   
        String stdntid;
       
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = new Socket("localhost", 6789);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStreamWriter outToServer1=new OutputStreamWriter(clientSocket.getOutputStream());
       
        OutputStreamWriter osw=new OutputStreamWriter(clientSocket.getOutputStream());

            stdntid = inFromUser.readLine(); 
            outToServer.writeBytes(stdntid + '\n');
        
        System.out.println("to send file press s or press r to recieve");
        String input=inFromUser.readLine();
        outToServer.writeBytes(input + '\n');
        
        if(input.equals("s")==true)
        {   
       // SENDER SENDING FILE TO SERVER
           System.out.println("press reciever id");   
           String r=inFromUser.readLine();
           outToServer.writeBytes(r + '\n');
        
           File myfile = new File("g:/mydata.txt");
           FileInputStream fis = new FileInputStream(myfile);
           BufferedInputStream bis = new BufferedInputStream(fis); 
                        
            int packetsize=500,ackNo=0,sckNo=0;
            double nosofpackets=Math.ceil(((int) myfile.length())/packetsize);
            String s = Double.toString(nosofpackets);
            System.out.println(s);
            outToServer.writeBytes(s + '\n');
  
            for(double i=0;i<nosofpackets+1;i++)
            {
                sckNo=(int)i;
                byte[] mybytearray = new byte[packetsize];
                bis.read(mybytearray, 0, mybytearray.length);
                String payload=bitpattern(mybytearray);
                String frame=bitstuffing(makeframe(payload,ackNo,sckNo));
                
                outToServer.writeBytes(frame + '\n');
                System.out.println("Packet:"+(i+1)+" sent to server");
               // OutputStream os = clientSocket.getOutputStream();
                //os.write(mybytearray, 0,mybytearray.length);
                //os.flush();
                String ackmsg=inFromServer.readLine();
                if(ackmsg.equals("error")==false)
                {
                    ackNo=Integer.parseInt(ackmsg);
                }
                else 
                {
                    while(true)
                    {
                        outToServer.writeBytes(frame + '\n');
                        ackmsg=inFromServer.readLine();
                        if(ackmsg.equals("error")==false)
                        {
                            ackNo=Integer.parseInt(ackmsg);
                            break;
                        }
                    }
                    
                }
            }           
            System.out.println("File sent to server succesfully !");        
        }
        else if(input.equals("r")==true)
        {  
     //RECIEVER RECIEVEING FROM SERVER
                     //RECIEVER RECIEVEING FROM SERVER
                FileOutputStream fos = new FileOutputStream(new File("f:/mydata3.txt"));
                BufferedOutputStream bos = new BufferedOutputStream(fos);
    
                int packetsize=500;
        
                String s = inFromServer.readLine();
                double nosofpackets = Double.parseDouble(s);
                       
                for(double i=0;i<nosofpackets+1;i++)
                {
                    InputStream is = clientSocket.getInputStream();
                    byte[] mybytearray = new byte[packetsize];
                    int bytesRead = is.read(mybytearray, 0,mybytearray.length );
                    System.out.println("Packet:"+(i+1));
                    bos.write(mybytearray, 0,mybytearray.length);
                    bos.flush();
                }
                bos.close();
                System.out.println("File saved in reciver successfully!");
        
        }
  
    }
    static String  bitpattern(byte[]  bytes)
    {
        String str="";
        
        for(int i=0;i<bytes.length;i++)
        {
            byte b = bytes[i];
            String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            // System.out.println(s1); // 10000001
            str=str+s1;
            // System.out.println(str);
        }
        //System.out.println(str);
       return str; 
    }
    static int chksum(String s)
    {
        int sum=0;
        for(int i=0;i<s.length();i++)
        {
           if(s.charAt(i)=='1') sum=sum+1;
        }
        return sum%127;
    }
    static String intToBinStr(int a)
    {
         byte b=(byte)a;  
         String s = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
         return s;
    }
    static String makeframe(String payload,int ack,int sck)
    {
        String kindofframe="00000000";
        return kindofframe+intToBinStr(sck)+intToBinStr(ack)+payload+intToBinStr(chksum(payload));
    }
    static String bitstuffing(String s)
    {   int counter=0;
        String newS="",flag="01111110";
        for(int i=0;i<s.length();i++)
        {            
            if(s.charAt(i) == '1')
            {
                counter++;
                newS=newS+s.charAt(i);
            }
            else
            {
                newS=newS+s.charAt(i);
                counter = 0;
            }
            if(counter == 5)
            {
                newS=newS+s.charAt(i);
                counter = 0;
            }
        }
        return flag+newS+flag;
    }        
            
}