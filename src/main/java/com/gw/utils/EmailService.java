package com.gw.utils;

import java.util.Date;

import javax.mail.internet.MimeMessage;

import com.google.api.services.gmail.Gmail;
import com.gw.jpa.GWUser;
import com.gw.tools.UserTool;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailService {

	@Autowired
	UserTool ut;

	Logger logger = Logger.getLogger(this.getClass());

	public void send_resetpassword(GWUser user, String site_url){

		EmailMessage message = new EmailMessage();

		message.setSubject("You Requested to Reset Your Password on Geoweaver");

		message.setTo_address(user.getEmail());

		String token = new RandomString(30).nextString();

		String reset_url = site_url + "/../../user/reset_password?token=" + token;

		logger.info("******************************");
		logger.info("Password Reset URL for "+ user.getUsername()+" : " + reset_url);
		logger.info("******************************");

		UserTool.token2userid.put(token, user.getId());

		UserTool.token2date.put(token, new Date());

		message.setBody("Dear "+user.getUsername()+", <br/><br/>"+

		" You recently requested to reset your password or unlock your Geoweaver user account. Click the link below to continue.<br/><br/>"+
		
		" *********<br/> <a href=\""+reset_url+"\" style=\"\">Reset Password or Unlock Geoweaver User Account</a> <br/> *********<br/><br/>" +
		
		" If you didn't make this change, please ignore this email; or if you believe an unauthorized person has accessed your account, please reset your password immediately and sign into your account page to review and update your security settings. <br/><br/>"+
		
		"Sincerely, <br/><br/>Geoweaver Support Team<br/><br/>"+

		"<div class=\"background-color:gray;\">"+

		"<p><a href=\"https://github.com/ESIPFed/Geoweaver\">Geoweaver</a> | <a href=\"mailto:geoweaver.app@gmail.com\">Support</a> | <a href=\"https://github.com/ESIPFed/Geoweaver\">Privacy Policy</a></p><p>Copyright @ Geoweaver 2021. All rights reserved. </p>"+
		"</div><br/>");

		this.sendmail(message);

	}
	
	public void sendmail(EmailMessage emailmessage)   {

		try {
			
			Gmail service = GmailAPI.getGmailService();
			
			MimeMessage Mimemessage = GmailOperations.createEmail(emailmessage.getTo_address(),
					"geoweaver.app@gmail.com",
					emailmessage.getSubject(), 
					emailmessage.getBody());

			com.google.api.services.gmail.model.Message msg = GmailOperations.createMessageWithEmail(Mimemessage);

			service.users().messages().send("me", msg).execute();
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			System.err.print("Failed to send email..");
			
		}
		
		
	}
	
}