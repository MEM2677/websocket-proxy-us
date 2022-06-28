package com.entando.lapam.proxy.authproxy.util;

import com.entando.lapam.proxy.authproxy.domain.Metopack;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import static com.entando.lapam.proxy.authproxy.util.JWTUtils.JWT_PROPERTY_LAPAM;
import static com.entando.lapam.proxy.authproxy.util.JWTUtils.JWT_PROPERTY_METOPACK;

public class LapamUtils {

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

      if (json.has(JWT_PROPERTY_LAPAM)) {
        JSONObject lapam = json.getJSONObject(JWT_PROPERTY_LAPAM);

        if (lapam != null && lapam.has(JWT_PROPERTY_METOPACK)) {
          JSONObject metopackJson = lapam.getJSONObject(JWT_PROPERTY_METOPACK);
          metopack = objectMapper.readValue(metopackJson.toString(), Metopack.class);
        }
      }
    }
    return metopack;
  }

  /**
   * Generate the Lapam connection string
   * @param modulo module id in query string
   * @param data Metopack data for the current user
   * @return the connection String
   */
  public static String encodeLapamRequest(String modulo, Metopack data) {
    return encodeLapamRequest(modulo, data.getUtente(), data.getProg());
  }


  // program report/bilancioPeriodico args {  -utente 117513 -prog 1 }
  /**
   * Generate the Lapam connection string
   * @param modulo module id in query string
   * @param utente lapam user id
   * @param prog program number
   * @return the connection String
   */
  public static String encodeLapamRequest(String modulo, String utente, String prog) {
    String result = null;
    if (StringUtils.isNotBlank(modulo)
      && StringUtils.isNotBlank(utente)
      && StringUtils.isNotBlank(prog)) {
      String query = "program " + modulo + " args {  -utente " + utente + " -prog " + prog + " }";
      byte[] bytes = Base64.getEncoder().encode(query.getBytes(StandardCharsets.UTF_8));
      result = new String(bytes);
    }
    return result;
  }

  /**
   * Check whether the module requested is enabled for the current user
   * @param modulo
   * @param data
   * @return
   */
  public static boolean checkModule(String modulo, Metopack data) {
    return (data != null
      && StringUtils.isNotBlank(modulo)
      && data.getModules() != null
      && Arrays.stream(data.getModules()).anyMatch(m -> m.equals(modulo.trim())));
  }


  /**
   * Exxtract the given parameter in query string
   * @param uri the URI
   * @param param the desired parameter
   * @return the value of the parameter or null
   */
  public static String extractParameter(URI uri, String param) {
    // get the JWT
    if (uri != null && StringUtils.isNotBlank(uri.getQuery())) {
      String[] queries = uri.getQuery().split("\\&");
      for (String cur: queries) {
        String[] query = cur.split("=");
        if (query[0].equals(param) && query.length == 2) {
          return query[1];
        }
      }
    }
    return null;
  }

  // "ws://34.159.252.151:61500/wish.tcl?cHJvZ3JhbSByZXBvcnQvYmlsYW5jaW9QZXJpb2RpY28gYXJncyB7ICAtdXRlbnRlIDExNzUxMyAtcHJvZyAxIH0=";

  /**
   * Generate Lapam connection URI
   * @param modulo
   * @param data
   * @return
   */
  public static String generateLapamURI(String modulo, Metopack data) {
    String uri = null;

    if (StringUtils.isNotBlank(modulo) && data != null) {
      uri = "ws://" + data.getConnection() + "/wish.tcl?" + encodeLapamRequest(modulo, data);
    }
    return uri;
  }

}
