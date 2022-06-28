package com.entando.lapam.proxy.authproxy.util;

import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class JWTUtils {

  private static final Logger logger = LoggerFactory.getLogger(JWTUtils.class);


  /**
   * Verify the given JWT
   * @param token the jwt to validate
   * @return
   */
  public static boolean verify(String token) {
    try {
      Verifier verifier = RSAVerifier.newVerifier(Paths.get(KeycloakUtils.getPKPath()));
      JWT jwt = JWT.getDecoder().decode(token, verifier);
    } catch (Throwable t) {
      logger.debug("JWT verification failed");
      return false;
    }
    return true;
  }

  /**
   * Return teh payload of the JWT
   * @param jwt
   * @return
   */
  public static String getBody(String jwt){
    String[] split_string = jwt.split("\\.");
    String base64EncodedHeader = split_string[0];
    String base64EncodedBody = split_string[1];
    String base64EncodedSignature = split_string[2];
    Base64 base64Url = new Base64(true);

//    String header = new String(base64Url.decode(base64EncodedHeader));
    return new String(base64Url.decode(base64EncodedBody));
  }


  /**
   * Return the user this JWT was assigned to
   * @param jwt the received JST
   * @return the JWT owner
   */
  public static String getUsername(String jwt) {
    String username = null;
    if (StringUtils.isNotBlank(jwt)) {
      String body = JWTUtils.getBody(jwt);
      JSONObject json = new JSONObject(body);

      if (json.has(JWT_PROPERTY_USERNAME)) {
        username = json.getString(JWT_PROPERTY_USERNAME);
      }
    }
    return username;
  }

  public static final String PARAM_JWT = "jwt";

  public static final String JWT_PROPERTY_LAPAM = "lapam";
  public static final String JWT_PROPERTY_METOPACK = "metopackcloud";
  public static final String JWT_PROPERTY_USERNAME = "preferred_username";

}
