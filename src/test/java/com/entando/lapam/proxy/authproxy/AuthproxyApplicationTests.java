package com.entando.lapam.proxy.authproxy;

import com.entando.lapam.proxy.authproxy.domain.Metopack;
import com.entando.lapam.proxy.authproxy.domain.keycloak.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

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
