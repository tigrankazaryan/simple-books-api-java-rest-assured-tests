package local;

import io.restassured.http.ContentType;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static local.CustomSpecifications.*;
import static local.CustomData.BASE_URL;
import static local.CustomData.CURRENT_BOOKS_NUMBER;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomFunctions {

    public static void getBooksCorrectLimit(int limit) {
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        List<BookFromList> books = given().when().get("/books?limit=" + limit).then().log().all().
                body(matchesJsonSchemaInClasspath("books_list_schema.json")).
                extract().body().jsonPath().getList(".", BookFromList.class);
        assertThat(String.format("Books list length is %d or current books number", limit),
                books.size(), anyOf(is(limit), is(CURRENT_BOOKS_NUMBER)));
    }

    public static void getBooksIncorrectId(int id) {
        setSpecifications(requestSpec(BASE_URL), responseSpec(404));
        String error = given().when().get("/books/" + id).then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("No book with id " + id, error, "Response body value check: error");
    }

    public static String randomString(int targetLength) {
        int leftLimit = 97;
        int rightLimit = 122;
        Random random = new Random();
        return random.ints(leftLimit, rightLimit + 1).limit(targetLength).
                collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    public static String randomEmail() {
        return String.format("%s@%s.%s", randomString(16), randomString(10),
                randomString(2));
    }

    public static void setEnvironmentValue(String key, Object value) {
        Properties properties = new Properties();
        try {
            InputStream input = new FileInputStream("src/test/resources/environment.properties");
            properties.load(input);
            properties.remove(key);
            input.close();
            OutputStream output = new FileOutputStream("src/test/resources/environment.properties");
            properties.setProperty(key, value.toString());
            properties.store(output, null);
            output.close();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static String getEnvironmentValue(String key) {
        String value = "";
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("src/test/resources/environment.properties")) {
            properties.load(input);
            value = properties.getProperty(key);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        return value;
    }

    public static void postApiClientsWrongBody(String body, String expectedError) {
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        String error = given().body(body).when().post("/api-clients").then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals(expectedError, error, "Response body value check: error");
    }

    public static void postApiClientsWrongEmail(String email) {
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        Client client = new Client(randomString(10), email);
        String error = given().body(client).when().post("/api-clients").then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("Invalid or missing client email.", error,
                "Response body value check: error");
    }

    public static int FirstAvailableBookId() {
        int firstAvailableBookId = 0;
        List<BookFromList> books = given().when().contentType(ContentType.JSON).
                get(BASE_URL + "/books").then().
                extract().body().jsonPath().getList(".", BookFromList.class);
        for (BookFromList book : books)
            if (book.getAvailable()) {
                firstAvailableBookId = book.getId();
                break;
            }
        return firstAvailableBookId;
    }

    public static int FirstUnavailableBookId() {
        int firstUnavailableBookId = 0;
        List<BookFromList> books = given().when().contentType(ContentType.JSON).
                get(BASE_URL + "/books").then().
                extract().body().jsonPath().getList(".", BookFromList.class);
        for (BookFromList book : books)
            if (!book.getAvailable()) {
                firstUnavailableBookId = book.getId();
                break;
            }
        return firstUnavailableBookId;
    }

    public static void postOrdersWrongBody(String body, String expectedError) {
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        String error = given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                body(body).when().post("/orders").then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals(expectedError, error, "Response body value check: error");
    }

    public static void postOrdersWrongBookId(int bookId) {
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        RequestOrder order = new RequestOrder(bookId, randomString(10));
        String error = given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                body(order).when().post("/orders").then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("Invalid or missing bookId.", error, "Response body value check: error");
    }

}
