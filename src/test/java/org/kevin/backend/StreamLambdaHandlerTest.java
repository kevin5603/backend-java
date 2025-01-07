package org.kevin.backend;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.kevin.backend.aws.StreamLambdaHandler;
import org.kevin.backend.model.User;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StreamLambdaHandlerTest {

  private static StreamLambdaHandler handler;
  private static Context lambdaContext;

  @BeforeAll
  public static void setUp() {
    handler = new StreamLambdaHandler();
    lambdaContext = new MockLambdaContext();
  }

  @Test
  @Order(1)
  public void getUsersStreamRequest() throws JsonProcessingException {

    InputStream requestStream = new AwsProxyRequestBuilder("/users", HttpMethod.GET)
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
      .buildStream();
    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

    handle(requestStream, responseStream);

    AwsProxyResponse response = readResponse(responseStream);
    assertNotNull(response);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());

    ObjectMapper mapper = new ObjectMapper();
    List<User> users = mapper.readValue(response.getBody(), new TypeReference<List<User>>() {
    });

    assertNotNull(users);
    assertFalse(users.isEmpty());
    assertEquals("Kevin", users.getFirst().getName());
    assertEquals(35, users.getFirst().getAge());

    assertFalse(response.isBase64Encoded());

    assertTrue(response.getMultiValueHeaders().containsKey(HttpHeaders.CONTENT_TYPE));
    assertTrue(response.getMultiValueHeaders().getFirst(HttpHeaders.CONTENT_TYPE)
      .startsWith(MediaType.APPLICATION_JSON));
  }

  @Test
  @Order(2)
  public void getUserByNameStreamRequest() throws JsonProcessingException {
    InputStream requestStream = new AwsProxyRequestBuilder("/users/search/Kevin", HttpMethod.GET)
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
      .buildStream();
    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

    handle(requestStream, responseStream);

    AwsProxyResponse response = readResponse(responseStream);
    assertNotNull(response);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());

    ObjectMapper mapper = new ObjectMapper();
    User user = mapper.readValue(response.getBody(), User.class);

    assertEquals("Kevin", user.getName());
    assertEquals(35, user.getAge());

    assertFalse(response.isBase64Encoded());

    assertTrue(response.getMultiValueHeaders().containsKey(HttpHeaders.CONTENT_TYPE));
    assertTrue(response.getMultiValueHeaders().getFirst(HttpHeaders.CONTENT_TYPE)
      .startsWith(MediaType.APPLICATION_JSON));
  }

  @Test
  public void saveUserStreamRequest() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    String payload = mapper.writeValueAsString(new User("David", 20));

    InputStream requestStream = new AwsProxyRequestBuilder("/users", HttpMethod.POST)
      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
      .body(payload)
      .buildStream();

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

    handle(requestStream, responseStream);

    AwsProxyResponse response = readResponse(responseStream);
    assertNotNull(response);
    assertEquals(Status.CREATED.getStatusCode(), response.getStatusCode());
  }

  @Test
  @Order(3)
  public void removeUserStreamRequest() {
    InputStream requestStream = new AwsProxyRequestBuilder("/users/1", HttpMethod.DELETE)
      .buildStream();

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

    handle(requestStream, responseStream);

    AwsProxyResponse response = readResponse(responseStream);
    assertNotNull(response);
    assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
  }

  private void handle(InputStream is, ByteArrayOutputStream os) {
    try {
      handler.handleRequest(is, os, lambdaContext);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  private AwsProxyResponse readResponse(ByteArrayOutputStream responseStream) {
    try {
      return LambdaContainerHandler.getObjectMapper()
        .readValue(responseStream.toByteArray(), AwsProxyResponse.class);
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error while parsing response: " + e.getMessage());
    }
    return null;
  }
}
