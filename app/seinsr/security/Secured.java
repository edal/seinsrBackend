package seinsr.security;

import seinsr.security.SecurityController;
import controllers.routes;
import seinsr.security.User;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;


public class Secured extends Security.Authenticator {

    @Override
    public String getUsername(Context ctx) {
        User user = null;
        String[] authTokenHeaderValues = ctx.request().headers().get(SecurityController.AUTH_TOKEN_HEADER);
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
            user = User.findByAuthToken(authTokenHeaderValues[0]);
            if (user != null) {
                ctx.args.put("user", user);
                return user.getEmailAddress();
            }
        }

        return null;
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        //return redirect(routes.SecurityController.login());
        return unauthorized();
    }
}