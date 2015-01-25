package test.seinsr.security;

import org.junit.*;
import java.util.*;
import com.avaje.ebean.*;
import play.*;
import play.mvc.*;
import play.test.*;
import play.libs.F.*;
import play.libs.Yaml;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;

import static play.test.Helpers.*;
import static org.junit.Assert.*;
import seinsr.security.*;

public class SecurityControllerTest extends WithApplication {
	private String emailAddress="dwilliams@seinsr.com";
	private String password="secret";
    public static FakeApplication app;

    @SuppressWarnings("unchecked")
    @Before
    public  void setUp() {
        app = Helpers.fakeApplication(Helpers.inMemoryDatabase(), Helpers.fakeGlobal());
        Helpers.start(app);

        Map<String,List<Object>> all = (Map<String,List<Object>>)Yaml.load("test-data.yml");

        Ebean.save(all.get("users"));
    }

    
	@Test 
    public void performGoodLogin() {
    	Map<String,String> data = new HashMap<String, String>();
            data.put("emailAddress", emailAddress);
            data.put("password", password);

        Result result = callAction(
        	routes.ref.SecurityController.login(),
	      fakeRequest().withFormUrlEncodedBody(data)
	    );

    	assertEquals("Usando credenciales de test-data.yml, login no autentica correctamente.",status(result), 200);

        
        String resp = contentAsString(result);
        
        JsonNode json = Json.parse(resp);
        int size = json.size();
        
        assertEquals("Tras realizar login, se devuelve un número incorrecto de parámetros. Se espera únicamente " + SecurityController.AUTH_TOKEN_HEADER,size, 1);

        
        String authToken = json.get("authToken").asText();
        assertNotEquals("Tras realizar login, se espera un token de seguridad", authToken, null);
        assertNotEquals("Tras realizar login, se espera un token de seguridad", authToken, "");


        // Aprovechamos el login para validar el check del token, solicitando el perfil
        result = callAction(
            routes.ref.SecurityController.getProfile(),
          fakeRequest().withHeader(SecurityController.AUTH_TOKEN_HEADER, authToken)
        );
        assertEquals("Obtenido código distinto de 200 al soliictar el perfil de usuario",status(result), 200);

        resp = contentAsString(result);
        
        json = Json.parse(resp);
        size = json.size();

        assertEquals("Al solicitar el perfil, se devuelve un número demasiado bajo de informacion",size>=2, true);


        // Realizamos logout 
        result = callAction(
            routes.ref.SecurityController.logout(),
          fakeRequest().withHeader(SecurityController.AUTH_TOKEN_HEADER, authToken)
        );
        assertEquals("Tras realizar un logout, se espera una redirección a la raíz de la aplicación.",status(result), 303);


        // Volvemos a solicitar el perfil, pasándole el token anterior, para validar que se ha invalidado
        result = callAction(
            routes.ref.SecurityController.getProfile(),
          fakeRequest().withHeader(SecurityController.AUTH_TOKEN_HEADER, authToken)
        );
        assertEquals("Tras logout, el sistema no debe permitir acceder al perfil",status(result), 401);
    }

	@Test 
    public void performNoExistentEmailLogin() {
    	Map<String,String> data = new HashMap<String, String>();
            data.put("emailAddress", emailAddress.substring(0, emailAddress.length()-2));
            data.put("password", password);

        Result result = callAction(
	      routes.ref.SecurityController.login(),
	      fakeRequest().withFormUrlEncodedBody(data)
	    );

    	assertEquals("Usando un correo electronico inexistente, login no devuelve 401", status(result), 401);
    }

	@Test 
    public void performBadEmailLogin() {
    	Map<String,String> data = new HashMap<String, String>();
            data.put("emailAddress", emailAddress.substring(0, 2));
            data.put("password", password);

        Result result = callAction(
	      routes.ref.SecurityController.login(),
	      fakeRequest().withFormUrlEncodedBody(data)
	    );

    	assertEquals("Usando un correo electronico con formato incorrecto, login no devuelve 400", status(result), 400);
    }

    @Test 
    public void performBadPasswordLogin() {
    	Map<String,String> data = new HashMap<String, String>();
            data.put("emailAddress", emailAddress);
            data.put("password", password.substring(1, 2));

        Result result = callAction(
	      routes.ref.SecurityController.login(),
	      fakeRequest().withFormUrlEncodedBody(data)
	    );

		assertEquals("Usando una contraseña incorrecta, login no devuelve 401",status(result), 401);
    }


    @Test 
    public void testSubclassLogin() {
        SecurityController.Login login = new SecurityController.Login();
        login.setEmailAddress(emailAddress);
        login.setPassword(password);


        assertEquals("Getters y setters incoherentes",login.getEmailAddress(), emailAddress);
        assertEquals("Getters y setters incoherentes",login.getPassword(), password);
    }

}
