package com.entando.lapam.proxy.authproxy.keycloack.impl;


import com.entando.lapam.proxy.authproxy.domain.keycloak.AuthenticationFlow;
import com.entando.lapam.proxy.authproxy.domain.keycloak.Execution;
import com.entando.lapam.proxy.authproxy.domain.keycloak.IdentityProvider;
import com.entando.lapam.proxy.authproxy.domain.keycloak.Profile;
import com.entando.lapam.proxy.authproxy.domain.keycloak.RealmPublicKey;
import com.entando.lapam.proxy.authproxy.domain.keycloak.Token;
import com.entando.lapam.proxy.authproxy.dto.KeycloakClient;
import com.entando.lapam.proxy.authproxy.keycloack.Constants;
import com.entando.lapam.proxy.authproxy.keycloack.KeycloakClientService;
import com.entando.lapam.proxy.authproxy.util.KeycloakUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;


@Component
public class KeycloakClientServiceImpl implements KeycloakClientService {

    private final Logger logger = LoggerFactory.getLogger(KeycloakClientServiceImpl.class);

    @PostConstruct
    private void setup() {
        KeycloakClient keycloakClient = new KeycloakClient("https://forumpa.apps.psdemo.eng-entando.com");


        try {
            // save public key of the realm to a file
            RealmPublicKey realmPublicKey = getRealmPublicKey(keycloakClient.getHost());
            String pem = "-----BEGIN PUBLIC KEY-----" + realmPublicKey.getPublicKey() + "-----END PUBLIC KEY-----";
            Set<PosixFilePermission> access =
              PosixFilePermissions.fromString("rw-rw-rw-");
            Path certFile =
              Files.createTempFile("keycloakPublicKey", ".pem", PosixFilePermissions.asFileAttribute(access));
            try (FileOutputStream fout = new FileOutputStream(certFile.toFile())) {
                fout.write(pem.getBytes(StandardCharsets.UTF_8));
                logger.debug("Keycloak public key saved in " + certFile.toAbsolutePath());
            } catch (Throwable t) {
                logger.error("Error saving public key", t);
                throw t;
            }
            // setup helper class
            KeycloakUtils.setup(this, keycloakClient, certFile.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Token getAdminToken(KeycloakClient keycloakClient) {
        Token token = null;
        WebClient client = WebClient.create();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        final String REST_URI = keycloakClient.getHost() + "/auth/realms/master/protocol/openid-connect/token";

        body.add("username", keycloakClient.getUsername());
        body.add("password", keycloakClient.getPassword());
        body.add("grant_type", "password");
        body.add("client_id", "admin-cli");
        body.add("client_secret", "admin-cli");
        body.add("scope", "openid");

        try {
            token =
                client.post()
                    .uri(new URI(REST_URI))
                    //                .header("Authorization", "Bearer MY_SECRET_TOKEN")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromFormData(body))
//                .retrieve()
//                .bodyToMono(Token.class)
//                .block();
                    .exchangeToMono(result -> {
                        if (result.statusCode()
                            .equals(HttpStatus.OK)) {
                            return result.bodyToMono(Token.class);
                        } else {
                            logger.error("Unexpected status: {}" , result.statusCode());
                            return Mono.empty();
                        }
                    })
                    .block();
        } catch (Throwable t) {
            logger.error("error getting the admin access token", t);
        }
        return token;
    }

    protected boolean duplicateAuthFlow(String host, Token token) {
        final String REST_URI = encodePath(
            host + "/auth/admin/realms/" + Constants.KEYCLOAK_DEFAULT_REALM + "/authentication/flows/" + Constants.KEYCLOAK_DEFAULT_AUTH_FLOW + "/copy"
        );
        // for a simple payload there's no need to disturb Jackson
        String payload = "{\"newName\":\"" + Constants.KEYCLOAK_NEW_AUTH_FLOW_NAME + "\"}";
        Boolean created = false;
        WebClient client = WebClient.create();

        try {
            created = client.post()
                    .uri(new URI(REST_URI))
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(payload))
                    .exchangeToMono(result -> {
                        if (result.statusCode()
                            .equals(HttpStatus.CREATED)) {
                            return Mono.just(true);
                        } else {
                            logger.error("Unexpected status: {}" , result.statusCode());
                            return Mono.just(false);
                        }
                    })
                    .block();
        } catch (Throwable t) {
            logger.error("error in duplicateAuthFlow", t);
        }
        return created;
    }

    protected boolean addExecutable(String host, Token token) {
        final String REST_URI = encodePath(host + "/auth/admin/realms/" + Constants.KEYCLOAK_DEFAULT_REALM + "/authentication/flows/" + Constants.KEYCLOAK_EXECUTION_HANDLE_EXISTING_ACCOUNT_NAME + "/executions/execution");
        WebClient client = WebClient.create();
        // for a simple payload there's no need to disturb Jackson
        String payload = "{\"provider\":\"idp-auto-link\"}";
        Boolean created = false;

        try {
            created = client.post()
                    .uri(new URI(REST_URI))
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(payload))
                    .exchangeToMono(result -> {
                        if (result.statusCode()
                            .equals(HttpStatus.CREATED)) {
                            return Mono.just(true);
                        } else {
                            logger.error("Unexpected status: {}" , result.statusCode());
                            return Mono.just(false);
                        }
                    })
                    .block();
        } catch (Throwable t) {
            logger.error("error in duplicateAuthFlow", t);
        }
        return created;
    }

    protected Execution[] getExecutions(String host, Token token) {
        Execution[] executions = null;
        final String REST_URI = encodePath(
            host + "/auth/admin/realms/" + Constants.KEYCLOAK_DEFAULT_REALM + "/authentication/flows/" + Constants.KEYCLOAK_NEW_AUTH_FLOW_NAME + "/executions"
        );
        WebClient client = WebClient.create();

        try {
            executions = (Execution[]) client
                    .get()
                    .uri(new URI(REST_URI))
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .accept(MediaType.APPLICATION_JSON)
                    .exchangeToMono(result -> {
                        if (result.statusCode()
                            .equals(HttpStatus.OK)) {
                            return result.bodyToMono(Execution[].class);
                        } else {
                            logger.error("Unexpected status: {}" , result.statusCode());
                            return Mono.empty();
                        }
                    })
                    .block();
        } catch (Throwable t) {
            logger.error("error in getExecutions", t);
        }
        return executions;
    }

    protected boolean raiseExecutionPriority(String host, Token token, String id) {
        final String REST_URI = encodePath(
            host + "/auth/admin/realms/" + Constants.KEYCLOAK_DEFAULT_REALM + "/authentication/executions/" + id + "/raise-priority"
        );
        WebClient client = WebClient.create();
        Boolean success = true;

        try {
            success =
                client
                    .post()
                    .uri(new URI(REST_URI))
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .exchangeToMono(result -> {
                        if (result.statusCode()
                            .equals(HttpStatus.NO_CONTENT)) {
                            return Mono.just(true);
                        } else {
                            logger.error("Unexpected status: {}" , result.statusCode());
                            return Mono.just(false);
                        }
                    })
                    .block();
        } catch (Throwable t) {
            logger.error("error in raiseExecutionPriority", t);
        }
        return success;
    }

    protected boolean updateExecutionRequirement(String host, Token token, Execution[] executions, String executionName, String requirement) {
        AuthenticationFlow updated = null;
        Optional<Execution> execOpt = findExecution(executions, executionName);

        if (execOpt.isPresent()) {
            Execution execution = execOpt.get();
            execution.setRequirement(requirement); // TODO constant
            updated = updateExecution(host, token, execution);
        } else {
            logger.error("Cannot find target execution " + executionName + ", aborting setup");
        }
        return updated != null;
    }

    protected AuthenticationFlow updateExecution(String host, Token token, Execution execution) {
        final String REST_URI = encodePath(
            host + "/auth/admin/realms/" + Constants.KEYCLOAK_DEFAULT_REALM + "/authentication/flows/" + Constants.KEYCLOAK_NEW_AUTH_FLOW_NAME + "/executions"
        );
        AuthenticationFlow flow = null;
        WebClient client = WebClient.create();

        try {
            flow =
                client
                    .put()
                    .uri(new URI(REST_URI))
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(execution))
                    .exchangeToMono(result -> {
                        if (result.statusCode()
                            .equals(HttpStatus.ACCEPTED)) {
                            return result.bodyToMono(AuthenticationFlow.class);
                        } else {
                            logger.error("Unexpected status: {}" , result.statusCode());
                            return Mono.empty();
                        }
                    })
                    .block();
        } catch (Throwable t) {
            logger.error("error in updateExecution", t);
        }
        return flow;
    }


    protected boolean createIdentityProvider(String host, Token token, IdentityProvider idp) {
        final String REST_URI = encodePath(host + "/auth/admin/realms/" + Constants.KEYCLOAK_DEFAULT_REALM + "/identity-provider/instances");
        WebClient client = WebClient.create();
        Boolean created = false;
        try {
            created =
                client
                    .post()
                    .uri(new URI(REST_URI))
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(idp))
                    .exchangeToMono(result -> {
                        if (result.statusCode()
                            .equals(HttpStatus.CREATED)) {
                            return Mono.just(true);
                        } else {
                            logger.error("Unexpected status: {}" , result.statusCode());
                            return Mono.just(false);
                        }
                    })
                    .block();
        } catch (Throwable t) {
            logger.error("error in createIdentityProvider", t);
        }
        return created;
    }


    protected boolean addMapperUsername(String host, Token token) {
        return addMapperElement(host, token, USERNAME_MAPPER_CFG);
    }

    protected boolean addMapperGeneric(String host, Token token, String name, String attributeName, String userAttributeName) {
        String payload = ATTRIBUTE_MAPPER_CFG
            .replace("_ATTRIBUTE_NAME_", attributeName)
            .replace("_USER_ATTRIBUTE_", userAttributeName)
            .replace("_NAME_", name);
        return addMapperElement(host, token, payload);
    }


    private boolean addMapperElement(String host, Token token, String payload) {
        final String REST_URI = encodePath(
            host + "/auth/admin/realms/" + Constants.KEYCLOAK_DEFAULT_REALM + "/identity-provider/instances/" + Constants.KEYCLOAK_IDP_ALIAS + "/mappers"
        );
        Boolean created = false;
        WebClient client = WebClient.create();

        try {
            created =
                client
                    .post()
                    .uri(new URI(REST_URI))
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(payload))
                    .exchangeToMono(result -> {
                        if (result.statusCode()
                            .equals(HttpStatus.CREATED)) {
                            return Mono.just(true);
                        } else {
                            logger.error("Unexpected status: {}" , result.statusCode());
                            return Mono.just(false);
                        }
                    })
                    .block();
        } catch (Throwable t) {
            logger.error("error in addMapperElement", t);
        }
        return created;
    }


    public Profile getUserProfile(String host, Token token, String id) {
        Profile[] profiles = null;
        final String REST_URI = encodePath(host + "/auth/admin/realms/" + Constants.KEYCLOAK_DEFAULT_REALM + "/users") + "?briefRepresentation=false&first=0&max=20&search=" + id;
        WebClient client = WebClient.create();

        try {
            profiles = client
              .get()
              .uri(new URI(REST_URI))
              .header("Authorization", "Bearer " + token.getAccessToken())
              .accept(MediaType.APPLICATION_JSON)
              .exchangeToMono(result -> {
                  if (result.statusCode()
                    .equals(HttpStatus.OK)) {
                      return result.bodyToMono(Profile[].class);
                  } else {
                      logger.error("Unexpected status: {}" , result.statusCode());
                      return Mono.empty();
                  }
              })
              .block();
        } catch (Throwable t) {
            logger.error("error in getExecutions", t);
        }

        if (profiles != null && profiles.length > 0) {
            return profiles[0];
        } else {
            return null;
        }
    }

    @Override
    public RealmPublicKey getRealmPublicKey(String host) {
        RealmPublicKey realmKey = null;
        final String REST_URI = encodePath(host + "/auth/realms/" + Constants.KEYCLOAK_DEFAULT_REALM);
        WebClient client = WebClient.create();

        try {
            realmKey = client
              .get()
              .uri(new URI(REST_URI))
              .accept(MediaType.APPLICATION_JSON)
              .exchangeToMono(result -> {
                  if (result.statusCode()
                    .equals(HttpStatus.OK)) {
                      return result.bodyToMono(RealmPublicKey.class);
                  } else {
                      logger.error("Unexpected status: {}" , result.statusCode());
                      return Mono.empty();
                  }
              })
              .block();
        } catch (Throwable t) {
            logger.error("error in getExecutions", t);
        }
        return realmKey;
    }

    /**
     *
     * @param executions
     * @param displayName
     * @return
     */
    protected Optional<Execution> findExecution(Execution[] executions, String displayName) {
        return Arrays
            .asList(executions)
            .stream()
            //      .peek(e -> System.out.println(">?> " + e.getDisplayName()))
            .filter(e -> e.getDisplayName().equals(displayName))
            .findFirst();
    }

    private String encodePath(String path) {
        path = UriUtils.encodePath(path, "UTF-8");
        return path;
    }

    // templates for mappers
    private static final String USERNAME_MAPPER_CFG =
        "{\n" +
            "   \"identityProviderAlias\":\"" +
            Constants.KEYCLOAK_IDP_ALIAS +
            "\",\n" +
            "   \"config\":{\n" +
            "      \"syncMode\":\"INHERIT\",\n" +
            "      \"template\":\"${ATTRIBUTE.fiscalNumber}\"\n" +
            "   },\n" +
            "   \"name\":\"User Name\",\n" +
            "   \"identityProviderMapper\":\"spid-saml-username-idp-mapper\"\n" +
            "}";

    private static final String ATTRIBUTE_MAPPER_CFG =
        "{\n" +
            "   \"identityProviderAlias\":\"" +
            Constants.KEYCLOAK_IDP_ALIAS +
            "\",\n" +
            "   \"config\":{\n" +
            "      \"syncMode\":\"INHERIT\",\n" +
            "      \"attribute.name\":\"_ATTRIBUTE_NAME_\",\n" +
            "      \"user.attribute\":\"_USER_ATTRIBUTE_\"\n" +
            "   },\n" +
            "   \"name\":\"_NAME_\",\n" +
            "   \"identityProviderMapper\":\"spid-user-attribute-idp-mapper\"\n" +
            "}";
}
