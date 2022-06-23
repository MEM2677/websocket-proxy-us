package com.entando.lapam.proxy.authproxy.util;

import com.entando.lapam.proxy.authproxy.domain.Metopack;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Paths;

public class JWTUtils {

  private static final Logger logger = LoggerFactory.getLogger(JWTUtils.class);

  public static String extractJWTFromURI(URI uri) {
    // get the JWT
    if (uri != null && StringUtils.isNotBlank(uri.getQuery())) {
      String[] queries = uri.getQuery().split("\\&");
      for (String cur: queries) {
        String[] query = cur.split("=");
        if (query[0].equals(PARAM_JWT) && query.length == 2) {
          return query[1];
        }
      }
    }
    return null;
  }

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
      logger.error("JWT verification failed");
      return false;
    }
    return true;
  }

  /**
   * Return teh payload of the JWT
   * @param jwt
   * @return
   */
  private static String getBody(String jwt){
    String[] split_string = jwt.split("\\.");
    String base64EncodedHeader = split_string[0];
    String base64EncodedBody = split_string[1];
    String base64EncodedSignature = split_string[2];
    Base64 base64Url = new Base64(true);

//    String header = new String(base64Url.decode(base64EncodedHeader));
    return new String(base64Url.decode(base64EncodedBody));
  }

  /**
   * Get Lapam attribute from JWT body
   * @param jwt the received JWT
   * @return Lapam metopack data
   * @throws Throwable
   */
  public static Metopack getLapamProperties(String jwt) throws Throwable {
    Metopack metopack = null;

    if (StringUtils.isNotBlank(jwt)) {
      ObjectMapper objectMapper = new ObjectMapper();
      String body = JWTUtils.getBody(jwt);
      JSONObject json = new JSONObject(body);
      JSONObject lapam = json.getJSONObject(JWT_PROPERTY_LAPAM);

      if (lapam.has(JWT_PROPERTY_METOPACK)) {
        JSONObject metopackJson = lapam.getJSONObject(JWT_PROPERTY_METOPACK);
        metopack = objectMapper.readValue(metopackJson.toString(), Metopack.class);
      }
    }
    return metopack;
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
