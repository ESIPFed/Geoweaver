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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.services.gmail.Gmail;

@Component
public class EmailService {

	
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