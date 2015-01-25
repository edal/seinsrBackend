package seinsr.notifications.web;

import play.data.Form;
import play.*;
import play.mvc.*;
import play.libs.F;
import play.libs.Json;
import play.mvc.WebSocket;
import play.libs.Akka;
import akka.actor.ActorRef;
import akka.actor.Props;

import seinsr.security.*;


public class WebNotificationsController extends Controller {

	@Security.Authenticated(Secured.class)
	public static WebSocket<String> webSocket() {
	  return new WebSocket<String>() {
	      
	    // Called when the Websocket Handshake is done.
	    public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
	    	final ActorRef userActor = Akka.system().actorOf(
	    		UserActor.props(in, out, SecurityController.getUser()), 
	    		SecurityController.getUser().getEmailAddress()
	    	);

	           
	    }
	    
	  };
	}
}