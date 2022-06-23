package com.entando.lapam.proxy.authproxy.keycloack;


import com.entando.lapam.proxy.authproxy.domain.keycloak.AuthenticationFlow;
import com.entando.lapam.proxy.authproxy.domain.keycloak.Execution;
import com.entando.lapam.proxy.authproxy.domain.keycloak.IdentityProvider;
import com.entando.lapam.proxy.authproxy.domain.keycloak.Profile;
import com.entando.lapam.proxy.authproxy.domain.keycloak.Token;
import com.entando.lapam.proxy.authproxy.dto.ConnectionInfo;
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
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;


@Component
public class KeycloakClient {

    private final Logger logger = LoggerFactory.getLogger(KeycloakClient.class);

    @PostConstruct
    private void setup() {
        KeycloakUtils.setup(this, null);
    }

    /**
     *
     * @param connection
     * @return
     */
    public Token getAdminToken(ConnectionInfo connection) {
        Token token = null;
        WebClient client = WebClient.create();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        final String REST_URI = connection.getHost() + "/auth/realms/master/protocol/openid-connect/token";

        body.add("username", connection.getUsername());
        body.add("password", connection.getPassword());
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

    /**
     *
     * @param host
     * @param token
     * @return
     */
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

    /**
     *
     * @param host
     * @param token
     * @return
     */
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

    /**
     *
     * @param host
     * @param token
     * @return
     */
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

    /**
     *
     * @param host
     * @param token
     * @param id
     * @return
     */
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

    /**
     *
     * @param host
     * @param token
     * @param executions
     * @param executionName
     * @param requirement
     * @return
     */
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

    /**
     *
     * @param host
     * @param token
     * @param execution
     * @return
     */
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

    /**
     *
     * @param host
     * @param token
     * @param idp
     * @return
     */
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

    /**
     *
     * @param host
     * @param token
     * @return
     */
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

    /**
     *
     * @param host
     * @param token
     * @param payload
     * @return
     */
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

    /**
     *
     * @param host
     * @param token
     * @param id
     * @return
     */
    public Profile getUser(String host, Token token, String id) {
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
    private static String USERNAME_MAPPER_CFG =
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

    private static String ATTRIBUTE_MAPPER_CFG =
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
