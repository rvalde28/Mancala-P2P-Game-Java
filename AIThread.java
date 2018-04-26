import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.*;
import java.lang.Thread.*;

public class AIThread extends Thread {
  
  public AIThread() {}
  
  public void run() {
    //wait a moment, then launch the AI program
    try {
      try {
        Thread.sleep(1000);
      } 
      catch(InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
      
      Process AIprocess = Runtime.getRuntime().exec("java ConnectingAI &");
      
      int exitVal = AIprocess.waitFor();
      
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}