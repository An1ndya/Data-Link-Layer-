package ServerClientThread;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;


public class TCPServer
{
    public static void main(String argv[]) throws Exception
    {
        int id = 0;
        ServerSocket welcomeSocket = new ServerSocket(6789);
        
        while(true)
        {
            Socket connectionSocket = welcomeSocket.accept();
            System.out.println(connectionSocket);
            WorkerThread wt = new WorkerThread(connectionSocket,id);
            Thread t = new Thread(wt);
            t.start();
            id++;
        }
		
    } 
}
class WorkerThread implements Runnable
{
    private Socket connectionSocket;
    private int id;
    public static ArrayList<IdPort> arr=new ArrayList<IdPort>();  
    public WorkerThread(Socket ConnectionSocket, int id) 
    {
        this.connectionSocket=ConnectionSocket;
        this.id=id;
    }
    public void run()
    {
        String stdntid;
       // String capitalizedSentence;
        while(true)
        {
            try
            {
                DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));    
                stdntid = inFromServer.readLine();
                OutputStreamWriter os=new OutputStreamWriter(connectionSocket.getOutputStream());
            
               System.out.println( stdntid +" connected ");

               IdPort ne =new IdPort(stdntid,connectionSocket);
               arr.add(ne);
               String input = inFromServer.readLine();
               if(input.equals("s")==true)
                {
                    //server recieveing from sender
                    String r=inFromServer.readLine();
                    FileOutputStream fos = new FileOutputStream(new File("e:/mydata2.txt"));
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
    
                    int packetsize=500;
        
                    String s = inFromServer.readLine();
                    double nosofpackets = Double.parseDouble(s);
        
                    for(double i=0;i<nosofpackets+1;i++)
                    {
                        InputStream is = connectionSocket.getInputStream();
                        byte[] mybytearray = new byte[packetsize];
                    //int bytesRead = is.read(mybytearray, 0,mybytearray.length );
                        String frame=inFromServer.readLine();
                    
                        String destuffed=destuffing(frame);
                        if(checkpayload(destuffed)==true)
                        {
                            int k=0;
                            String payload=destuffed.substring(24,destuffed.length()-8);
                            for(int j=0;j<payload.length();j=j+8)
                            {
                                mybytearray[k]= stringtobyte(payload.substring(j,j+8));
                                k++;
                            }
                  
                            bos.write(mybytearray, 0,mybytearray.length);
                            System.out.println("Packet:"+(i+1)+" saved in server");
                            int ack=(int)(i+1);
                   
                            outToServer.writeBytes(Integer.toString(ack)+'\n');
                        }
                        else 
                        {   
                            //REtransmission
                            System.out.println("checksumerror");
                            outToServer.writeBytes("error"+'\n');
                            while(true)
                            {
                                frame=inFromServer.readLine();
                                destuffed=destuffing(frame);
                                if(checkpayload(destuffed)==true)
                                {
                                    int k=0;
                                    String payload=destuffed.substring(24,destuffed.length()-8);
                                    for(int j=0;j<payload.length();j=j+8)
                                    {
                                        mybytearray[k]= stringtobyte(payload.substring(j,j+8));
                                        k++;
                                    }
                  
                                    bos.write(mybytearray, 0,mybytearray.length);
                                    System.out.println("Packet:"+(i+1)+" saved in server");
                                    int ack=(int)(i+1);
                   
                                    outToServer.writeBytes(Integer.toString(ack)+'\n');
                                    break;
                                }
                                else
                                {
                                    outToServer.writeBytes("error"+'\n');
                                }
                            }
                        }
                    }
                    bos.close();
                    System.out.println("File saved in server successfully!"); 
 // server creating connection with reciever 
                    Iterator itr=arr.iterator(); 
                    Socket rcvconnection = null;
                    while(itr.hasNext())
                    {
                        
                        IdPort st=(IdPort)itr.next();  
                        if(st.stdntId.equals(r)==true){
                            rcvconnection=st.s;
                           // System.out.println(rcvconnection);
                         break;}
                    }  
                    
                        File myfile = new File("e:/mydata2.txt");
                        FileInputStream fis = new FileInputStream(myfile);
                        BufferedInputStream bis = new BufferedInputStream(fis); 
          
                        DataOutputStream outToRcvr=new DataOutputStream(rcvconnection.getOutputStream());
                        BufferedReader inFromRcvr = new BufferedReader(new InputStreamReader(rcvconnection.getInputStream()));  
                        OutputStreamWriter outToRcvr1=new OutputStreamWriter(rcvconnection.getOutputStream());
     // server sending file to reciever 
                        //nosofpackets=Math.ceil(((int) myfile.length())/packetsize);
                        //s = Double.toString(nosofpackets);
                        outToRcvr.writeBytes(s + '\n');
                        
                        for(double i=0;i<nosofpackets+1;i++)
                        {
                        byte[] mybytearray = new byte[packetsize];
                        bis.read(mybytearray, 0, mybytearray.length);
                        //System.out.println("Packet:"+(i+1));
                        OutputStream ostoRcvr = rcvconnection.getOutputStream();
                        ostoRcvr.write(mybytearray, 0,mybytearray.length);
                        ostoRcvr.flush();
                        System.out.println("Packet:"+(i+1)+" sent to reciever");
                        }
                        bis.close();
                        System.out.println("File sent to reciever succesfully !"); 
                }                     
            }
            catch(Exception e){}          
        } 
    }
    String destuffing(String s)
    {
        String str=s.substring(8,s.length()-8); //escaping flag
        String destuff="";
        int counter=0;
            for(int i=0;i<str.length();i++)
                {
    
                    if(str.charAt(i) == '1')
                        {
                            
                            counter++;
                            destuff = destuff + str.charAt(i);
                           
                        }
                    else
                        {
                             destuff = destuff + str.charAt(i);
                             counter = 0;
                        }
                   if(counter == 5)
                        {
                              if((i+2)!=str.length())
                              destuff = destuff + str.charAt(i+2);
                              else
                              destuff=destuff + '1';
                              i=i+2;
                              counter = 1;
                        }
               }
        return destuff;
    }
    boolean checkpayload(String s)
    {
        String pay=s.substring(24,s.length()-8);
        String chksumstring=s.substring(s.length()-8,s.length());
        int sum=chksum(pay);
        String calchksumstr=intToBinStr(sum);
        return calchksumstr.equals(chksumstring);
    }
    int chksum(String s)
    {
        int sum=0;
        for(int i=0;i<s.length();i++)
        {
           if(s.charAt(i)=='1') sum=sum+1;
        }
        return sum%127;
    }
    String intToBinStr(int a)
    {
         byte b=(byte)a;  
         String s = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
         return s;
    }
    byte stringtobyte(String s)
    {
        //String s = "10000000";
        int val = Integer.parseInt(s, 2);
        byte b = (byte) val;
        return b;
    }
}