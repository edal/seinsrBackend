package seinsr.notifications.web;

import akka.actor.UntypedActor;
import play.libs.F;
import play.mvc.WebSocket;
import akka.actor.Props;
import akka.japi.Creator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.management.Notification;
import com.fasterxml.jackson.databind.JsonNode;

import seinsr.security.User;

public class UserActor extends UntypedActor {
    WebSocket.In<String> in;
    WebSocket.Out<String> out;
    User userData;

    public static Props props(WebSocket.In<String> in, WebSocket.Out<String> out, User user) {
        return Props.create(new Creator<UserActor>(){
            private static final long serialVersionUID = 1L;

             @Override
            public UserActor create() throws Exception {
                return new UserActor(in, out, user);
            }
        });
    }
    public UserActor(WebSocket.In<String> in, WebSocket.Out<String> out, User user) {
        this.in = in;
        this.out = out;
        this.userData=user;

        in.onMessage(new F.Callback<String>() {
            public void invoke(String event) {

                // Log events to the console
                System.out.print(event);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                out.write(sdf.format(cal.getTime()));
            }
        });

        out.write("Connection to Seinsr Web Notification socket successful. Welcome " + this.userData.fullName);
    }

    @Override
    public void onReceive(Object message) {
         if (message instanceof Notification) {
            Notification not = (Notification) message;

            Object source = not.getSource();
            long timestamp = not.getTimeStamp();
            String type = not.getType();


            out.write(not.toString());
        } else if (message instanceof String){
             out.write((String) message);
        }
        else {
            unhandled(message);
        }
    }



}