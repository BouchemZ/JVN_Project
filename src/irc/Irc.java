/***
 * Irc class : simple implementation of a chat using JAVANAISE 
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.awt.*;
import java.awt.event.*; 


import jvn.*;
import jvn.impl.JvnObjectProxy;
import jvn.impl.JvnServerImpl;

import java.io.*;


public class Irc {
	public TextArea		text;
	public TextField	data;
	Frame 			frame;
	ISentence       sentence;


  /**
  * main method
  * create a JVN object nammed IRC for representing the Chat application
  **/
	public static void main(String argv[]) {
	   try {
		   
		// initialize JVN
		JvnServerImpl js = JvnServerImpl.jvnGetServer();
		   
        ISentence sentence = (ISentence) JvnObjectProxy.newInstance("IRC", new Sentence(), js);

		// create the graphical part of the Chat application
		 new Irc(sentence);
	   
	   } catch (Exception e) {
		   System.out.println("IRC problem : " + e.getMessage());
	   }
	}

  /**
   * IRC Constructor
   @param jos the Sentence representing the Chat
   **/
	public Irc(ISentence jos) {
		sentence = jos;
		frame=new Frame();
		frame.setLayout(new GridLayout(1,1));
		text=new TextArea(10,60);
		text.setEditable(false);
		text.setForeground(Color.red);
		frame.add(text);
		data=new TextField(40);
		frame.add(data);
		Button read_button = new Button("read");
		read_button.addActionListener(new readListener(this));
		frame.add(read_button);
		Button write_button = new Button("write");
		write_button.addActionListener(new writeListener(this));
		frame.add(write_button);
        Button kill_button = new Button("kill");
        kill_button.addActionListener(new killListener(this));
        frame.add(kill_button);
		frame.setSize(545,201);
		text.setBackground(Color.black); 
		frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    JvnServerImpl js = JvnServerImpl.jvnGetServer();
                    js.jvnTerminate();
                    System.exit(0);
                } catch (JvnException je) {
                    System.out.println("IRC problem : " + je.getMessage());
                }
            }
        });
	}
}


 /**
  * Internal class to manage user events (read) on the CHAT application
  **/
 class readListener implements ActionListener {
	Irc irc;
  
	public readListener (Irc i) {
		irc = i;
	}
   
 /**
  * Management of user events
  **/
	public void actionPerformed (ActionEvent e) {

		// invoke the method
		String s = irc.sentence.read();

		// display the read value
		irc.data.setText(s);
		irc.text.append(s+"\n");
	}
}

 /**
  * Internal class to manage user events (write) on the CHAT application
  **/
 class writeListener implements ActionListener {
	Irc irc;

	public writeListener (Irc i) {
        	irc = i;
	}

  /**
    * Management of user events
   **/
	public void actionPerformed (ActionEvent e) {
        // get the value to be written from the buffer
        String s = irc.data.getText();

        // invoke the method
        irc.sentence.write(s);
    }
}

/**
 * Internal class to manage user events (kill) on the CHAT application
 **/
class killListener implements ActionListener {
    Irc irc;

    public killListener (Irc i) {
        irc = i;
    }

    /**
     * Management of user events - Kill the process without cleanup
     **/
    public void actionPerformed (ActionEvent e) {
        System.out.println("CRASH SIMULATION - Arrêt brutal du processus sans cleanup");
        System.exit(1);  // Arrêt brutal sans appeler jvnTerminate
    }
}



