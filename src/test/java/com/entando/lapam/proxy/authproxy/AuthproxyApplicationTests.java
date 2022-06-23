package com.entando.lapam.proxy.authproxy;

import com.entando.lapam.proxy.authproxy.domain.Metopack;
import com.entando.lapam.proxy.authproxy.domain.keycloak.Profile;
import com.entando.lapam.proxy.authproxy.util.JWTUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import static com.entando.lapam.proxy.authproxy.proxy.WebSocketServerProxyHandler.PARAM_JWT;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthproxyApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void unmarshalProfile() {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Profile[] p = objectMapper.readValue(PAYLOAD, Profile[].class);
//			System.out.println(">>> " + p[0].getAttributes());
			System.out.println(">>> " + p[0].getAccess().getClass());
			assertNotNull(p);
			assertTrue(p.length > 0);
			Map<String, Object> map2 = p[0].getAccess();
			for (Map.Entry<String, Object> cur: map2.entrySet()) {
				System.out.println(cur.getKey() + ":" + cur.getValue());
			}

			Map<String, ArrayList<Object>> map = p[0].getAttributes();
			for (Map.Entry<String, ArrayList<Object>> cur: map.entrySet()) {
				System.out.println(cur.getKey() + ":");
				cur.getValue().forEach(v -> System.out.print("]" + v + "[ " + v.getClass()));
				System.out.println();
			}
			assertTrue(map.containsKey("lapam.metopackcloud"));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Test
	void unmarshalLapam() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			Metopack info = objectMapper.readValue(LAPAM, Metopack.class);
			assertNotNull(info);
			System.out.println(">conn> " + info.getConnection());
			System.out.println(">utente> " + info.getUtente());
			System.out.println(">prog> " + info.getProg());
			if (info.getModules() != null && info.getModules().length > 0) {
				for (String cur: info.getModules()) {
					System.out.println(">modules> " + cur);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 *
	 * @param uri
	 * @return
	 */
	private String extractedJWT(URI uri) {
		// get the JWT
		if (StringUtils.isNotBlank(uri.getQuery())) {
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
	 *
	 * @param token
	 * @return
	 */
	protected boolean verifyToken(String token) {
		try {
			Verifier verifier = RSAVerifier.newVerifier(Paths.get("/tmp/pk.pem"));
			JWT jwt = JWT.getDecoder().decode(TEST_JWT, verifier);
		} catch (Throwable t) {
			return false;
		}
		return true;
	}

	@Test
	void misc() throws URISyntaxException {
		URI uri = new URI("ws://localhost:8080/postman?jwt="+TEST_JWT);
		System.out.println(uri.getQuery());
		String jwt = extractedJWT(uri);
		assertNotNull(jwt);
		verifyToken(jwt);
	}

	@Test
	public void testDecodeJWT() throws Throwable {
		Metopack data = JWTUtils.getLapamProperties(TEST_JWT);
		assertNotNull(data);
		assertEquals("1", data.getProg());

		data = JWTUtils.getLapamProperties(null);
		assertNull(data);
	}

	@Test
	public void testUserJWT() throws Throwable {
		String user = JWTUtils.getUsername(TEST_JWT);
		assertNotNull(user);
		assertEquals("matteo", user);
	}

	public static String TEST_JWT = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJWRm4yaklVTmotaVdNYnN2WEpadGdWX2psU2dsNlFmX1ZmSHJMYko0R2NzIn0.eyJleHAiOjE2NTU5OTA3NzYsImlhdCI6MTY1NTk5MDQ3NiwiYXV0aF90aW1lIjoxNjU1OTg4MjgyLCJqdGkiOiJlNmI0YzAyYS1hNzM3LTQ1NGEtODhkYy1hM2QyMjU4NGZhZDkiLCJpc3MiOiJodHRwczovL2ZvcnVtcGEuYXBwcy5wc2RlbW8uZW5nLWVudGFuZG8uY29tL2F1dGgvcmVhbG1zL2VudGFuZG8iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiYjIxOTYyNzctOTMzMi00ZTYzLWIxNGUtMWQzZTI5NDdmY2EzIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZW50YW5kby13ZWIiLCJub25jZSI6IjVlNDE2MTVhLThlMDgtNDM2Ni04N2U1LWQzM2UwZmZkMDBhZSIsInNlc3Npb25fc3RhdGUiOiI0N2U0OTcwNS1kODE5LTQzNTUtOTViNy01NWMwMjZmMTVlZTMiLCJhY3IiOiIwIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vZm9ydW1wYS5hcHBzLnBzZGVtby5lbmctZW50YW5kby5jb20iXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLWVudGFuZG8iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwic2lkIjoiNDdlNDk3MDUtZDgxOS00MzU1LTk1YjctNTVjMDI2ZjE1ZWUzIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJNYXR0ZW8gTWlubmFpIiwibGFwYW0iOnsibWV0b3BhY2tjbG91ZCI6eyJjb25uZWN0aW9uIjoiMzQuMTU5LjI1Mi4xNTE6NjE1MDAiLCJtb2R1bGVzIjpbInJlcG9ydC9iaWxhbmNpb1BlcmlvZGljbyJdLCJ1dGVudGUiOiIxMTc1MTMiLCJwcm9nIjoiMSJ9fSwicHJlZmVycmVkX3VzZXJuYW1lIjoibWF0dGVvIiwiZ2l2ZW5fbmFtZSI6Ik1hdHRlbyIsImZhbWlseV9uYW1lIjoiTWlubmFpIiwiZW1haWwiOiJtLm1pbm5haUBlbnRhbmRvLmNvbSJ9.PAG_VVqIAwh-RAZEhI2pIRdhmx0D3ajpz5QoHpPgEtcL1McL_WjuV3Uw-ZY01wG-ePS_BBKAYDvK4OVqY1jBQZHwevge9PZ9qvCNLgsp36wqvov8r9tlNBvWQsD2tzaC3jC7Hh0z2aLORihTq3o5at6h9xxPF5C8TB7VosE4DV__veRQvOhDFTDZuR4Uakw8SXtXNjUxssib3nl0O7Lw3t32qwpHPZgKRV1PzDWHg30zrm82zWtMAgYj2tKfAJPe3_zetMouu-N5bTelSl-SobYh6p4tbVjR7YR5EYVxyXxvwFg4N4l8mZTRKsN69_bvAhotYmUmcbFynlWFF-0lEg";

	public static String LAPAM = "{\n" +
		"  \"connection\": \"33.155.255.155:61500\",\n" +
		"  \"modules\": [\n" +
		"    \"report/budgetOnline\"\n" +
		"  ],\n" +
		"  \"utente\": \"112233\",\n" +
		"  \"prog\": \"1\"\n" +
		"}";

	public static String PAYLOAD = "[{\n" +
		"    \"id\": \"b2196277-9332-4e63-b14e-1d3e2947fca3\",\n" +
		"    \"createdTimestamp\": 1655906999358,\n" +
		"    \"username\": \"matteo\",\n" +
		"    \"enabled\": true,\n" +
		"    \"totp\": false,\n" +
		"    \"emailVerified\": true,\n" +
		"    \"firstName\": \"Matteo\",\n" +
		"    \"lastName\": \"Minnai\",\n" +
		"    \"email\": \"m.minnai@entando.com\",\n" +
		"    \"attributes\": {\n" +
		"      \"lapam.metopackcloud\": [\n" +
		"        \"{   \\\"connection\\\": \\\"33.155.255.155:61500\\\",   \\\"modules\\\": [\\\"report/budgetOnline\\\"],   \\\"utente\\\": \\\"112233\\\",   \\\"prog\\\": \\\"1\\\" }\"\n" +
		"      ]\n" +
		"    },\n" +
		"    \"disableableCredentialTypes\": [],\n" +
		"    \"requiredActions\": [],\n" +
		"    \"notBefore\": 0,\n" +
		"    \"access\": {\n" +
		"      \"manageGroupMembership\": true,\n" +
		"      \"view\": true,\n" +
		"      \"mapRoles\": true,\n" +
		"      \"impersonate\": true,\n" +
		"      \"manage\": true\n" +
		"    }\n" +
		"  }]";

}
