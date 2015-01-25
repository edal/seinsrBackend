package seinsr.security;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.Form;
import play.*;
import play.mvc.*;
import play.libs.F;
import play.libs.Json;
import seinsr.security.User;

import play.data.validation.Constraints;

public class SecurityController extends Controller {
	public final static String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";
    public static final String AUTH_TOKEN = "authToken";


    public static Result login() {
    	Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
    	
    	if (loginForm.hasErrors()) {
            return badRequest(loginForm.errorsAsJson());
        }

        Login login = loginForm.get();
        User user = User.findByEmailAddressAndPassword(login.emailAddress, login.password);

        if (user == null) {
            return unauthorized();
        }
        else {
            String authToken = user.createAuthToken();
            ObjectNode authTokenJson = Json.newObject();
            authTokenJson.put(AUTH_TOKEN, authToken);
            response().setCookie(AUTH_TOKEN, authToken);
            return ok(authTokenJson);
        }
    }

    public static User getUser() {
        return (User)Http.Context.current().args.get("user");
    }

    @Security.Authenticated(Secured.class)
    public static Result logout() {
        response().discardCookie(AUTH_TOKEN);
        getUser().deleteAuthToken();
        return redirect("/");
    }

    @Security.Authenticated(Secured.class)
    public static Result getProfile() {
        return ok(Json.toJson(getUser()));
    }
     public static class Login {

        @Constraints.Required
        @Constraints.Email
        public String emailAddress;

        @Constraints.Required
        public String password;

    }
}
