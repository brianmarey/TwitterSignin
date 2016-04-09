package com.careydevelopment.twittersignin.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

@Controller
public class GetTokenController {
	
	private static final Logger LOGGER = Logger.getLogger(GetTokenController.class);
	
	private static final String TWITTER_PROPERTIES = "/etc/tomcat8/resources/twitter.properties";
	private static final String LOCALHOST_PROPERTIES = "/etc/tomcat8/resources/localhost.properties";
	
    @RequestMapping("/getToken")
    public RedirectView getToken(HttpServletRequest request,Model model) {
    	//this will be the URL that we take the user to
    	String twitterUrl = "";
    	
		try {
			Twitter twitter = TwitterFactory.getSingleton();
			
			//set the credentials
			setApplication(twitter);
			
			//get the callback url so they get back here
			String callbackUrl = getCallbackUrl();

			//go get the request token from Twitter
			RequestToken requestToken = twitter.getOAuthRequestToken(callbackUrl);
			
			//put the token in the session because we'll need it later
			request.getSession().setAttribute("requestToken", requestToken);
			
			//let's put Twitter in the session as well
			request.getSession().setAttribute("twitter", twitter);
			
			//now get the authorization URL from the token
			twitterUrl = requestToken.getAuthorizationURL();
			
			LOGGER.info("Authorization url is " + twitterUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
		RedirectView redirectView = new RedirectView();
	    redirectView.setUrl(twitterUrl);
	    return redirectView;
    }

    
    private String getCallbackUrl() {
    	String callbackUrl = "";
    	
    	try {
	    	StringBuilder sb = new StringBuilder();
	    	
			Properties props = new Properties();
			File file = new File(LOCALHOST_PROPERTIES);
			FileInputStream inStream = new FileInputStream (file);
			props.load(inStream);
			
			String prefix = props.getProperty("localhost.prefix");
			sb.append(prefix);
			sb.append("/TwitterSignin/twitterCallback");
			
			callbackUrl = sb.toString();
			
			//LOGGER.info("Callback URL is " + callbackUrl);
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw new RuntimeException("Problem setting callback URL!");
    	}
    	
    	return callbackUrl;
    }
    
    /**
     * Sets the consumer key and auth for the application that the user
     * wants to use
     */
    private void setApplication(Twitter twitter) {
    	try {
			Properties props = new Properties();
			File file = new File(TWITTER_PROPERTIES);
			FileInputStream inStream = new FileInputStream(file);
			props.load(inStream);
			
			String consumerKey=props.getProperty("brianmcarey.consumerKey");
			String consumerSecret=props.getProperty("brianmcarey.consumerSecret");
			
			LOGGER.info(consumerKey + " " + consumerSecret);
			
			twitter.setOAuthConsumer(consumerKey, consumerSecret);
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw new RuntimeException("Couldn't set application properties!");
    	}
    }
}
