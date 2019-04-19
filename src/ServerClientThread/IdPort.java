/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerClientThread;

import java.net.Socket;

public class IdPort {
    String stdntId;
    Socket s;
    IdPort(String a, Socket b)
    {
        stdntId=a;
        s=b;
    }    
}
