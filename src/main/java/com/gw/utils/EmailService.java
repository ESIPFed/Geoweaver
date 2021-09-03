package com.gw.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.services.gmail.Gmail;
import com.gw.jpa.GWUser;
import com.gw.tools.UserTool;

@Component
public class EmailService {

	@Autowired
	UserTool ut;

	public void send_resetpassword(GWUser user, String site_url){

		EmailMessage message = new EmailMessage();

		message.setSubject("You Requested to Reset Your Password on Geoweaver");

		message.setTo_address(user.getEmail());

		String token = new RandomString(30).nextString();

		String reset_url = site_url + "reset_password?token=" + token;

		ut.token2userid.put(token, user.getId());

		ut.token2date.put(token, new Date());

		message.setBody("Hello zsun@gmu.edu! \n"+

		" Someone has requested a link to change your password. You can do this through the link below.\n"+
		
		"  <a href=\""+reset_url+"\">Change my password</a> \n" +
		
		" If you didn't request this, please ignore this email. \n"+
		
		"Your password won't change until you access the link above and create a new one. \n");

		this.sendmail(message);

	}
	
	public void sendmail(EmailMessage emailmessage)   {

		try {
			
			Gmail service = GmailAPI.getGmailService();

			GmailOperations gmailOperations = new GmailOperations();

			MimeMessage Mimemessage = gmailOperations.createEmail(emailmessage.getTo_address(),
					"Geoweaver App",
					emailmessage.getSubject(), 
					emailmessage.getBody());

			com.google.api.services.gmail.model.Message msg = gmailOperations.createMessageWithEmail(Mimemessage);

			service.users().messages().send("Geoweaver App", msg).execute();
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			System.err.print("Failed to send email..");
			
		}
		
		
	}
	
}