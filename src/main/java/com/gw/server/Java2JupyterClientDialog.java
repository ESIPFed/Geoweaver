package com.gw.server;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.gw.jpa.Host;

public class Java2JupyterClientDialog extends JFrame {

   private final JLabel messageLabel =
      new JLabel("Client Message");
   private final JTextField messageField =
      new JTextField(10);
   private final JButton sendButton =
      new JButton("Send");
   private final JTextArea serverMessageText =
      new JTextArea("");
   private Java2JupyterClientEndpoint client;
   
   
   Map<String, List<String>> getHeaders(){
	  
//     GET ws://localhost:8080/Geoweaver/jupyter-socket/api/kernels/50b8eae7-e877-45a5-9686-fb99f179bec4/channels?session_id=7587997d388b4669965953d6506c9c33 HTTP/1.1
//	  Host: localhost:8080
//	  Connection: Upgrade
//	  Pragma: no-cache
//	  Cache-Control: no-cache
//	  User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36
//	  Upgrade: websocket
//	  Origin: http://localhost:8080
//	  Sec-WebSocket-Version: 13
//	  Accept-Encoding: gzip, deflate, br
//	  Accept-Language: en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7,lb;q=0.6
//	  Cookie: JSESSIONID=6A76ED20085E0D3F2C56248F370D765C; username-localhost-8888="2|1:0|10:1589056727|23:username-localhost-8888|44:OTc5NGFkYjk5Mjc0NGZhODk4YjdlZTljMTdlYjhkYzg=|2b129b076ea248da7bb60b67ef5215ace4e1f22d5b8357892740daf831011afb"; _xsrf=2|71c72b4a|edbb6345b2cfe9820c5f7ae7bbb029af|1589331447
//	  Sec-WebSocket-Key: cu+bm2FWa4WMw21AoK848Q==
//	  Sec-WebSocket-Extensions: permessage-deflate; client_max_window_bits
	  
	  Map<String, List<String>> headers = new HashMap<String, List<String>>();
      List vals = new ArrayList();
      vals.add("Upgrade");
      headers.put("Connection", vals);
      vals.clear();
      vals.add("no-cache");
      headers.put("Cache-Control", vals);
      vals.clear();
      vals.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
      headers.put("User-Agent", vals);
      vals.clear();
      vals.add("13");
      headers.put("Sec-WebSocket-Version", vals);
      vals.clear();
      vals.add("JSESSIONID=B6740B35283A0081A0A75BEFD3CE9265; _xsrf=2|56aa5ecc|8c1e5470fb6844ba80e26ca0f3fb00ac|1592420813; username-localhost-8888=\"2|1:0|10:1592420823|23:username-localhost-8888|44:YmJlOWU4NTljMjEwNGQ2YTllNDJhNDZlYzBmNDBkZTU=|6b6e1012a25db4c8d9191921dd9dfd92290eafdecd171e6b4fad2678ab6cfaf3\"");
      headers.put("Cookie", vals);
      vals.clear();
      vals.add("FI1AraAMT7iChOkIzp2tfA==");
      headers.put("Sec-WebSocket-Key", vals);
      vals.clear();
      vals.add("permessage-deflate; client_max_window_bits");
      headers.put("Sec-WebSocket-Extensions", vals);
      vals.clear();
      vals.add("gzip, deflate, br");
      headers.put("Accept-Encoding", vals);
      vals.clear();
      vals.add("en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7,lb;q=0.6");
      headers.put("Accept-Language", vals);
      
      return headers;
	   
   }

   public Java2JupyterClientDialog() throws Exception {
      setSize(400, 400);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setVisible(true);
      getContentPane().setLayout(new BorderLayout(10, 10));

      JPanel p = new JPanel();
      p.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
      p.add(messageLabel);
      p.add(messageField);
      p.add(sendButton);

      add(p, BorderLayout.NORTH);
      JScrollPane scroll = new JScrollPane(
         JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scroll.setViewportView(serverMessageText);
      add(scroll, BorderLayout.CENTER);
      
//      String wsurl = "ws://localhost:8080/Geoweaver/shell-socket";
      
      String wsurl = "ws://localhost:8080/Geoweaver/jupyter-socket/api/kernels/8b7e9d57-c83b-4449-91d8-c0486c99c389/channels?session_id=e792c1994197490d8c8780dac70d3d07";
      
      Host h = new Host();
      
//      client = new Java2JupyterClientEndpoint(new URI(wsurl), null, getHeaders(), h);
      client.init(new URI(wsurl), null, getHeaders(), h, "");
      
//      client.setWindow(this);
      
//      client = new Java2JupyterClientEndpoint(new URI("ws://localhost:8888/api/kernels/884447f1-bac6-4913-be86-99da11b2a78a/channels?session_id=42b8261488884e869213604975141d8c"), null, getHeaders());
      
      sendButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            client.sendMessage(messageField.getText());
         }
      });
      
   }

   public void writeServerMessage(String message) {
      serverMessageText.setText(serverMessageText.getText()
         + "\n" + message);
   }

   public static void main(String[] args) throws Exception {
	   
	   Java2JupyterClientDialog clientWindow = new Java2JupyterClientDialog();
	   
   }
   
   
   
}
