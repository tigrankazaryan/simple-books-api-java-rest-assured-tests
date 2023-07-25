import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import local.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static local.CustomFunctions.*;
import static local.CustomSpecifications.*;
import static local.CustomData.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("All Simple Books API tests")
public class SimpleBooksApiTest {

    @Test
    @Order(0)
    @DisplayName("Preparatory function before all tests run")
    @Description("Setting first available and first unavailable books identifiers. Not a real test.")
    public void setFirstAvailableAndUnavailableBooksIds() {
        setEnvironmentValue("firstAvailableBookId", FirstAvailableBookId());
        setEnvironmentValue("firstUnavailableBookId", FirstUnavailableBookId());
    }

    @Test
    @Order(1)
    @DisplayName("GET /status")
    @Description("Shows the status of the API.")
    public void getStatus() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        String status = given().when().get("/status").then().log().all().
                body(matchesJsonSchemaInClasspath("status_schema.json")).
                extract().body().jsonPath().get("status");
        assertEquals("OK", status, "Response body value check: status");
    }

    @Test
    @Order(2)
    @DisplayName("GET /books")
    @Description("Shows a list of all books.")
    public void getBooks() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        given().when().get("/books").then().log().all().
                body(matchesJsonSchemaInClasspath("books_list_schema.json"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /books | type: fiction")
    @Description("Shows a list of fiction books.")
    public void getBooksTypeFiction() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        List<BookFromList> books = given().when().get("/books?type=fiction").then().log().all().
                body(matchesJsonSchemaInClasspath("books_list_schema.json")).
                extract().body().jsonPath().getList(".", BookFromList.class);
        for (BookFromList book : books)
            assertEquals("fiction", book.getType(),
                    "Checking all \"type\" keys for having \"fiction\" value");
    }

    @Test
    @Order(4)
    @DisplayName("GET /books | type: non-fiction")
    @Description("Shows a list of non-fiction books.")
    public void getBooksTypeNonFiction() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        List<BookFromList> books = given().when().get("/books?type=non-fiction").then().log().all().
                body(matchesJsonSchemaInClasspath("books_list_schema.json")).
                extract().body().jsonPath().getList(".", BookFromList.class);
        for (BookFromList book : books)
            assertEquals("non-fiction", book.getType(),
                    "Checking all \"type\" keys for having \"non-fiction\" value");
    }

    @Test
    @Order(5)
    @DisplayName("GET /books | wrong type")
    @Description("Attempt to call GET /books method with nonexistent value of type parameter.")
    public void getBooksWrongType() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        String error = given().when().get("/books?type=" + WRONG_TYPE).then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("Invalid value for query parameter 'type'. Must be one of: fiction, non-fiction.",
                error, "Response body value check: error");
    }

    @Test
    @Order(6)
    @DisplayName("GET /books | empty type")
    @Description("Calling GET /books method with empty value of type parameter.")
    public void getBooksEmptyType() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        given().when().get("/books?type=").then().log().all().
                body(matchesJsonSchemaInClasspath("books_list_schema.json"));
    }

    @Test
    @Order(7)
    @DisplayName("GET /books | limit = 1")
    @Description("Calling GET /books method with the value of limit parameter equal to 1.")
    public void getBooksLimitIs1() {
        getBooksCorrectLimit(1);
    }

    @Test
    @Order(8)
    @DisplayName("GET /books | limit = 2")
    @Description("Calling GET /books method with the value of limit parameter equal to 2.")
    public void getBooksLimitIs2() {
        getBooksCorrectLimit(2);
    }

    @Test
    @Order(9)
    @DisplayName("GET /books | limit is current books number")
    @Description("Calling GET /books method with the value of limit parameter equal to current books number.")
    public void getBooksLimitIsCurrentBooksNumber() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        List<BookFromList> books = given().when().get("/books?limit=" + CURRENT_BOOKS_NUMBER).then().log().all().
                body(matchesJsonSchemaInClasspath("books_list_schema.json")).
                extract().body().jsonPath().getList(".", BookFromList.class);
        assertEquals(CURRENT_BOOKS_NUMBER, books.size(), "Books list length is current books number");
    }

    @Test
    @Order(10)
    @DisplayName("GET /books | limit = 19")
    @Description("Calling GET /books method with the value of limit parameter equal to 19 (the value preceding the maximum valid value of limit parameter).")
    public void getBooksLimitIs19() {
        getBooksCorrectLimit(19);
    }

    @Test
    @Order(11)
    @DisplayName("GET /books | limit = 20")
    @Description("Calling GET /books method with the value of limit parameter equal to 20 (the maximum valid value of limit parameter).")
    public void getBooksLimitIs20() {
        getBooksCorrectLimit(20);
    }

    @Test
    @Order(12)
    @DisplayName("GET /books | limit = 21")
    @Description("Attempt to call GET /books method with the value of limit parameter equal to 21 (the value above the maximum valid value of limit parameter).")
    public void getBooksLimitIs21() {
        int limit = 21;
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        String error = given().when().get("/books?limit=" + limit).then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("Invalid value for query parameter 'limit'. Cannot be greater than 20.",
                error, "Response body value check: error");
    }

    @Test
    @Order(13)
    @DisplayName("GET /books | limit = 0")
    @Description("Calling GET /books method with the value of limit parameter equal to 0.")
    public void getBooksLimitIs0() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        given().when().get("/books?limit=0").then().log().all().
                body(matchesJsonSchemaInClasspath("books_list_schema.json"));
    }

    @Test
    @Order(14)
    @DisplayName("GET /books | limit = -1")
    @Description("Attempt to call GET /books method with the value of limit parameter equal to -1 (negative value).")
    public void getBooksLimitIsNegative() {
        int limit = -1;
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        String error = given().when().get("/books?limit=" + limit).then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("Invalid value for query parameter 'limit'. Must be greater than 0.",
                error, "Response body value check: error");
    }

    @Test
    @Order(15)
    @DisplayName("GET /books | limit = 2.5")
    @Description("Calling GET /books method with the value of limit parameter equal to 2.5 (fractional number). Fractional part is expected to be ignored.")
    public void getBooksLimitIsFractional() {
        double limit = 2.5;
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        List<BookFromList> books = given().when().get("/books?limit=" + limit).then().log().all().
                body(matchesJsonSchemaInClasspath("books_list_schema.json")).
                extract().body().jsonPath().getList(".", BookFromList.class);
        assertThat(String.format("Books list length is %d or current books number", (int) limit),
                books.size(), anyOf(is((int) limit), is(CURRENT_BOOKS_NUMBER)));
    }

    @Test
    @Order(16)
    @DisplayName("GET /books | limit is not a number")
    @Description("Calling GET /books method with non-numeric value of limit parameter. The non-numeric value is expected to be ignored.")
    public void getBooksLimitIsNotANumber() {
        String limit = "test";
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        given().when().get("/books?limit=" + limit).then().log().all().
                body(matchesJsonSchemaInClasspath("books_list_schema.json"));
    }

    @Test
    @Order(17)
    @DisplayName("GET /books | Empty limit")
    @Description("Calling GET /books method with empty value of limit parameter.")
    public void getBooksEmptyLimit() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        given().when().get("/books?limit=").then().log().all().
                body(matchesJsonSchemaInClasspath("books_list_schema.json"));
    }

    @Test
    @Order(18)
    @DisplayName("GET /books | type: fiction & limit = 2")
    @Description("Shows only first two fiction books.")
    public void getBooksTypeFictionAndLimitIs2() {
        String type = "fiction";
        int limit = 2;
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        List<BookFromList> books = given().when().
                get(String.format("/books?type=%s&limit=%d", type, limit)).then().log().all().
                body(matchesJsonSchemaInClasspath("books_list_schema.json")).
                extract().body().jsonPath().getList(".", BookFromList.class);
        assertEquals(limit, books.size(), "Books list length is " + limit);
        for (BookFromList book : books)
            assertEquals(type, book.getType(),
                    "Checking all \"type\" keys for having \"fiction\" value");
    }

    @Test
    @Order(19)
    @DisplayName("GET /books | type: non-fiction & limit = 1")
    @Description("Show the first non-fiction book.")
    public void getBooksTypeNonFictionAndLimitIs1() {
        String type = "non-fiction";
        int limit = 1;
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        List<BookFromList> books = given().when().
                get(String.format("/books?type=%s&limit=%d", type, limit)).then().log().all().
                body(matchesJsonSchemaInClasspath("books_list_schema.json")).
                extract().body().jsonPath().getList(".", BookFromList.class);
        assertEquals(limit, books.size(), "Books list length is " + limit);
        for (BookFromList book : books)
            assertEquals(type, book.getType(),
                    "Checking all \"type\" keys for having \"non-fiction\" value");
    }

    @Test
    @Order(20)
    @DisplayName("GET /books | id")
    @Description("Shows detailed information about book with passed identifier.")
    public void getBooksId() {
        int id = 1;
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        SingleBook book = given().when().get("/books/" + id).then().log().all().
                body(matchesJsonSchemaInClasspath("single_book_schema.json")).
                extract().body().jsonPath().getObject(".", SingleBook.class);
        assertEquals(id, book.getId(), "Book id is " + id);
    }

    @Test
    @Order(21)
    @DisplayName("GET /books | id is out of range")
    @Description("Attempt to call GET /books method with the value of id parameter out of valid values range.")
    public void getBooksIdIsOutOfRange() {
        getBooksIncorrectId(100);
    }

    @Test
    @Order(22)
    @DisplayName("GET /books | id = 0")
    @Description("Attempt to call GET /books method with the value of id parameter equal to 0.")
    public void getBooksIdIs0() {
        getBooksIncorrectId(0);
    }

    @Test
    @Order(23)
    @DisplayName("GET /books | id = 2.5")
    @Description("Calling GET /books method with the value of id parameter equal to 2.5 (fractional number). Fractional part is expected to be ignored.")
    public void getBooksIdIsFractional() {
        double id = 2.5;
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        SingleBook book = given().when().get("/books/" + id).then().log().all().
                body(matchesJsonSchemaInClasspath("single_book_schema.json")).
                extract().body().jsonPath().getObject(".", SingleBook.class);
        assertEquals((int) id, book.getId(), "Book id is " + (int) id);
    }

    @Test
    @Order(24)
    @DisplayName("GET /books | id = -1")
    @Description("Attempt to call GET /books method with the value of id parameter equal to -1 (negative value).")
    public void getBooksIdIsNegative() {
        getBooksIncorrectId(-1);
    }

    @Test
    @Order(25)
    @DisplayName("GET /books | id is text")
    @Description("Attempt to call GET /books method with non-numeric value of id parameter.")
    public void getBooksIdIsText() {
        String id = "test";
        setSpecifications(requestSpec(BASE_URL), responseSpec(404));
        String error = given().when().get("/books/" + id).then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("No book with id NaN", error, "Response body value check: error");
    }

    @Test
    @Order(26)
    @DisplayName("POST /api-clients")
    @Description("Registering an API client.")
    public void postApiClients() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(201));
        Client client = new Client(randomString(10), randomEmail());
        given().body(client).when().post("/api-clients").then().log().all().
                body(matchesJsonSchemaInClasspath("token_schema.json"));
        setEnvironmentValue("occupiedEmail", client.getClientEmail());
    }

    @Test
    @Order(27)
    @DisplayName("POST /api-clients | clientEmail is occupied")
    @Description("Attempt to call POST /api-clients method with already occupied email.")
    public void postApiClientsClientEmailIsOccupied() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(409));
        Client client = new Client(randomString(10), getEnvironmentValue("occupiedEmail"));
        String error = given().body(client).when().post("/api-clients").then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("API client already registered. Try a different email.", error,
                "Response body value check: error");
    }

    @Test
    @Order(28)
    @DisplayName("POST /api-clients | No body")
    @Description("Attempt to call POST /api-clients method without request body.")
    public void postApiClientsNoBody() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        String error = given().when().post("/api-clients").then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("Invalid or missing client name.", error,
                "Response body value check: error");
    }

    @Test
    @Order(29)
    @DisplayName("POST /api-clients | Empty body")
    @Description("Attempt to call POST /api-clients method with empty request body.")
    public void postApiClientsEmptyBody() {
        postApiClientsWrongBody("{}", "Invalid or missing client name.");
    }

    @Test
    @Order(30)
    @DisplayName("POST /api-clients | No clientName")
    @Description("Attempt to call POST /api-clients method without client name.")
    public void postApiClientsNoClientName() {
        postApiClientsWrongBody(String.format("{\"clientEmail\": \"%s\"}", randomEmail()),
                "Invalid or missing client name.");
    }

    @Test
    @Order(31)
    @DisplayName("POST /api-clients | No clientEmail")
    @Description("Attempt to call POST /api-clients method without client email.")
    public void postApiClientsNoClientEmail() {
        postApiClientsWrongBody(String.format("{\"clientName\": \"%s\"}", randomString(10)),
                "Invalid or missing client email.");
    }

    @Test
    @Order(32)
    @DisplayName("POST /api-clients | Empty clientName")
    @Description("Attempt to call POST /api-clients method with empty client name.")
    public void postApiClientsEmptyClientName() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        Client client = new Client("", randomEmail());
        String error = given().body(client).when().post("/api-clients").then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("Invalid or missing client name.", error,
                "Response body value check: error");
    }

    @Test
    @Order(33)
    @DisplayName("POST /api-clients | clientName contains only 1 symbol")
    @Description("Attempt to call POST /api-clients method with client name containing only one symbol.")
    public void postApiClientsClientNameContainsOnlyOneSymbol() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        Client client = new Client("a", randomEmail());
        String error = given().body(client).when().post("/api-clients").then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("Invalid or missing client name.", error,
                "Response body value check: error");
    }

    @Test
    @Order(34)
    @DisplayName("POST /api-clients | Empty clientEmail")
    @Description("Attempt to call POST /api-clients method with empty client email.")
    public void postApiClientsEmptyClientEmail() {
        postApiClientsWrongEmail("");
    }

    @Test
    @Order(35)
    @DisplayName("POST /api-clients | Invalid clientEmail: no @ symbol")
    @Description("Attempt to call POST /api-clients method with client email which doesn't contain @ symbol.")
    public void postApiClientsInvalidClientEmailNoAtSymbol() {
        postApiClientsWrongEmail("username");
    }

    @Test
    @Order(36)
    @DisplayName("POST /api-clients | Invalid clientEmail: no domain")
    @Description("Attempt to call POST /api-clients method with client email which doesn't contain domain.")
    public void postApiClientsInvalidClientEmailNoDomain() {
        postApiClientsWrongEmail("username@");
    }

    @Test
    @Order(37)
    @DisplayName("POST /api-clients | Invalid clientEmail: no dot in domain")
    @Description("Attempt to call POST /api-clients method with client email which doesn't contain dot in its domain.")
    public void postApiClientsInvalidClientEmailNoDotInDomain() {
        postApiClientsWrongEmail("username@example");
    }

    @Test
    @Order(38)
    @DisplayName("POST /api-clients | Invalid clientEmail: no top-level domain after dot")
    @Description("Attempt to call POST /api-clients method with client email which doesn't contain top-level domain after dot.")
    public void postApiClientsInvalidClientEmailNoTopLevelDomainAfterDot() {
        postApiClientsWrongEmail("username@example.");
    }

    @Test
    @Order(39)
    @DisplayName("POST /api-clients | Invalid clientEmail: top-level domain contains only 1 symbol")
    @Description("Attempt to call POST /api-clients method with client email which contains one-character top-level domain.")
    public void postApiClientsInvalidClientEmailTopLevelDomainContainsOnlyOneSymbol() {
        postApiClientsWrongEmail("username@example.c");
    }

    @Test
    @Order(40)
    @DisplayName("Create client token")
    @Description("Registering an API client.")
    public void postApiClientsToken() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(201));
        Client client = new Client(randomString(10), randomEmail());
        String accessToken = given().body(client).when().post("/api-clients").then().log().all().
                extract().body().jsonPath().get("accessToken");
        setEnvironmentValue("accessToken", accessToken);
    }

    @Test
    @Order(41)
    @DisplayName("Create other client token")
    @Description("Registering another API client.")
    public void postApiClientsOtherToken() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(201));
        Client client = new Client(randomString(10), randomEmail());
        String accessToken = given().body(client).when().post("/api-clients").then().log().all().
                extract().body().jsonPath().get("accessToken");
        setEnvironmentValue("otherAccessToken", accessToken);
    }

    @Test
    @Order(42)
    @DisplayName("POST /orders")
    @Description("Creating a new order.")
    public void postOrders() {
        int firstAvailableBookId = Integer.parseInt(getEnvironmentValue("firstAvailableBookId"));
        setSpecifications(requestSpec(BASE_URL), responseSpec(201));
        RequestOrder order = new RequestOrder(firstAvailableBookId, randomString(10));
        CreatedOrder createdOrder = given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                body(order).when().post("/orders").then().log().all().
                body(matchesJsonSchemaInClasspath("created_order_schema.json")).
                extract().body().jsonPath().getObject(".", CreatedOrder.class);
        assertTrue(createdOrder.getCreated(), "Response body value check: created");
        setEnvironmentValue("bookId", firstAvailableBookId);
        setEnvironmentValue("orderId", createdOrder.getOrderId());
        setEnvironmentValue("customerName", order.getCustomerName());
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        DetailedOrder detailedOrder = given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                when().get("/orders/" + getEnvironmentValue("orderId")).then().log().all().
                body(matchesJsonSchemaInClasspath("detailed_order_schema.json")).
                extract().body().jsonPath().getObject(".", DetailedOrder.class);
        assertEquals(getEnvironmentValue("orderId"), detailedOrder.getId(),
                "Response body value check: id");
        assertEquals(firstAvailableBookId, detailedOrder.getBookId(),
                "Response body value check: bookId");
        assertEquals(getEnvironmentValue("customerName"), detailedOrder.getCustomerName(),
                "Response body value check: customerName");
        setEnvironmentValue("createdBy", detailedOrder.getCreatedBy());
        setEnvironmentValue("quantity", detailedOrder.getQuantity());
        setEnvironmentValue("timestamp", detailedOrder.getTimestamp());
    }

    @Test
    @Order(43)
    @DisplayName("POST /orders | Book is not in stock")
    @Description("Attempt to call POST /orders method with identifier of a book which is not in stock.")
    public void postOrdersBookIsNotInStock() {
        int firstUnavailableBookId = Integer.parseInt(getEnvironmentValue("firstUnavailableBookId"));
        setSpecifications(requestSpec(BASE_URL), responseSpec(404));
        RequestOrder order = new RequestOrder(firstUnavailableBookId, randomString(10));
        String error = given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                body(order).when().post("/orders").then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("This book is not in stock. Try again later.", error,
                "Response body value check: error");
    }

    @Test
    @Order(44)
    @DisplayName("POST /orders | No auth")
    @Description("Attempt to call POST /orders method without authorization.")
    public void postOrdersNoAuth() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(401));
        RequestOrder order = new RequestOrder(1, randomString(10));
        String error = given().body(order).when().post("/orders").then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("Missing Authorization header.", error, "Response body value check: error");
    }

    @Test
    @Order(45)
    @DisplayName("POST /orders | No body")
    @Description("Attempt to call POST /orders method without request body.")
    public void postOrdersNoBody() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        String error = given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                when().post("/orders").then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("Invalid or missing bookId.", error, "Response body value check: error");
    }

    @Test
    @Order(46)
    @DisplayName("POST /orders | Empty body")
    @Description("Attempt to call POST /orders method with empty request body.")
    public void postOrdersEmptyBody() {
        postOrdersWrongBody("{}", "Invalid or missing bookId.");
    }

    @Test
    @Order(47)
    @DisplayName("POST /orders | No bookId")
    @Description("Attempt to call POST /orders method without bookId parameter.")
    public void postOrdersNoBookId() {
        postOrdersWrongBody(String.format("{\"customerName\": \"%s\"}", randomString(10)),
                "Invalid or missing bookId.");
    }

    @Test
    @Order(48)
    @DisplayName("POST /orders | Empty bookId")
    @Description("Attempt to call POST /orders method with empty value of bookId parameter.")
    public void postOrdersEmptyBookId() {
        postOrdersWrongBody(String.format("{\"bookId\": \"\", \"customerName\": \"%s\"}",
                randomString(10)), "Invalid or missing bookId.");
    }

    @Test
    @Order(49)
    @DisplayName("POST /orders | bookId = 0")
    @Description("Attempt to call POST /orders method with the value of bookId parameter equal to 0.")
    public void postOrdersBookIdIs0() {
        postOrdersWrongBookId(0);
    }

    @Test
    @Order(50)
    @DisplayName("POST /orders | bookId is out of range")
    @Description("Attempt to call POST /orders method with the value of bookId parameter out of valid values range.")
    public void postOrdersBookIdIsOutOfRange() {
        postOrdersWrongBookId(100);
    }

    @Test
    @Order(51)
    @DisplayName("POST /orders | bookId = -1")
    @Description("Attempt to call POST /orders method with the value of bookId parameter equal to -1 (negative value).")
    public void postOrdersBookIdIsNegative() {
        postOrdersWrongBookId(-1);
    }

    @Test
    @Order(52)
    @DisplayName("POST /orders | bookId is fractional")
    @Description("Calling POST /orders method with the value of bookId parameter equal to 2.5 (fractional number). Fractional part is expected to be ignored.")
    public void postOrdersBookIdIsFractional() {
        double fractionalId = Integer.parseInt(getEnvironmentValue("firstUnavailableBookId")) + 0.5;
        setSpecifications(requestSpec(BASE_URL), responseSpec(404));
        String error = given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                body(String.format("{\"bookId\": %s, \"customerName\": \"%s\"}",
                        fractionalId, randomString(10))).
                when().post("/orders").then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("This book is not in stock. Try again later.", error,
                "Response body value check: error");
    }

    @Test
    @Order(53)
    @DisplayName("POST /orders | bookId is text")
    @Description("Attempt to call POST /orders method with non-numeric value of bookId parameter.")
    public void postOrdersBookIdIsText() {
        String textId = "test";
        postOrdersWrongBody(String.format("{\"bookId\": \"%s\", \"customerName\": \"%s\"}",
                textId, randomString(10)), "Invalid or missing bookId.");
    }

    @Test
    @Order(54)
    @DisplayName("POST /orders | No customerName")
    @Description("Attempt to call POST /orders method without customerName parameter.")
    public void postOrdersNoCustomerName() {
        int firstAvailableBookId = Integer.parseInt(getEnvironmentValue("firstAvailableBookId"));
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                body(String.format("{\"bookId\": %d}", firstAvailableBookId)).
                when().post("/orders").then().log().all();
    }

    @Test
    @Order(55)
    @DisplayName("POST /orders | Empty customerName")
    @Description("Attempt to call POST /orders method with empty value of customerName parameter.")
    public void postOrdersEmptyCustomerName() {
        int firstAvailableBookId = Integer.parseInt(getEnvironmentValue("firstAvailableBookId"));
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        RequestOrder order = new RequestOrder(firstAvailableBookId, "");
        given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                body(order).when().post("/orders").then().log().all();
    }

    @Test
    @Order(56)
    @DisplayName("GET /orders")
    @Description("Shows all orders of a user.")
    public void getOrders() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                when().get("/orders").then().log().all().
                body(matchesJsonSchemaInClasspath("orders_list_schema.json"));
    }

    @Test
    @Order(57)
    @DisplayName("GET /orders | No auth")
    @Description("Attempt to call GET /orders method without authorization.")
    public void getOrdersNoAuth() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(401));
        String error = given().when().get("/orders").then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("Missing Authorization header.", error, "Response body value check: error");
    }

    @Test
    @Order(58)
    @DisplayName("GET /orders | id")
    @Description("Shows detailed information about order with passed identifier.")
    public void getOrdersId() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                when().get("/orders/" + getEnvironmentValue("orderId")).then().log().all().
                body(matchesJsonSchemaInClasspath("detailed_order_schema.json"));
    }

    @Test
    @Order(59)
    @DisplayName("GET /orders | id: Other user token")
    @Description("Attempt to show information about order by calling GET /orders method with token of a user who is not the owner of the order.")
    public void getOrdersIdOtherUserToken() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(404));
        String error = given().auth().preemptive().oauth2(getEnvironmentValue("otherAccessToken")).
                when().get("/orders/" + getEnvironmentValue("orderId")).then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals(String.format("No order with id %s.", getEnvironmentValue("orderId")), error,
                "Response body value check: error");
    }

    @Test
    @Order(60)
    @DisplayName("GET /orders | Nonexistent id")
    @Description("Attempt to call GET /orders method with nonexistent identifier.")
    public void getOrdersNonexistentId() {
        String orderId = "test";
        setSpecifications(requestSpec(BASE_URL), responseSpec(404));
        String error = given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                when().get("/orders/" + orderId).then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals(String.format("No order with id %s.", orderId), error,
                "Response body value check: error");
    }

    @Test
    @Order(61)
    @DisplayName("PATCH /orders")
    @Description("Updating an existing order.")
    public void patchOrders() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(204));
        String customerName = "patchTestUsername";
        given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                body(String.format("{\"customerName\": \"%s\"}", customerName)).when().
                patch("/orders/" + getEnvironmentValue("orderId")).then().log().all();
        setEnvironmentValue("customerName", customerName);
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        DetailedOrder detailedOrder = given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                when().get("/orders/" + getEnvironmentValue("orderId")).then().log().all().
                body(matchesJsonSchemaInClasspath("detailed_order_schema.json")).
                extract().body().jsonPath().getObject(".", DetailedOrder.class);
        assertEquals(getEnvironmentValue("orderId"), detailedOrder.getId(),
                "Response body value check: id");
        assertEquals(Integer.parseInt(getEnvironmentValue("bookId")), detailedOrder.getBookId(),
                "Response body value check: bookId");
        assertEquals(customerName, detailedOrder.getCustomerName(),
                "Response body value check: customerName");
        assertEquals(getEnvironmentValue("createdBy"), detailedOrder.getCreatedBy(),
                "Response body value check: createdBy");
        assertEquals(Integer.parseInt(getEnvironmentValue("quantity")), detailedOrder.getQuantity(),
                "Response body value check: quantity");
        assertEquals(Long.parseLong(getEnvironmentValue("timestamp")), detailedOrder.getTimestamp(),
                "Response body value check: timestamp");
    }

    @Test
    @Order(62)
    @DisplayName("PATCH /orders | Editing all parameters")
    @Description("Trying to edit all parameters of order.")
    public void patchOrdersEditingAllParameters() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(204));
        DetailedOrder patchDetailedOrder = new DetailedOrder("testId",
                Integer.parseInt(getEnvironmentValue("bookId")) + 2,
                "editingAllParametersTest", "testCreatedBy",
                Integer.parseInt(getEnvironmentValue("quantity")) + 1, 876506400000L);
        given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                body(patchDetailedOrder).when().patch("/orders/" + getEnvironmentValue("orderId")).
                then().log().all();
        setEnvironmentValue("customerName", patchDetailedOrder.getCustomerName());
        setSpecifications(requestSpec(BASE_URL), responseSpec(200));
        DetailedOrder getDetailedOrder = given().auth().preemptive().
                oauth2(getEnvironmentValue("accessToken")).when().
                get("/orders/" + getEnvironmentValue("orderId")).then().log().all().
                body(matchesJsonSchemaInClasspath("detailed_order_schema.json")).
                extract().body().jsonPath().getObject(".", DetailedOrder.class);
        assertEquals(getEnvironmentValue("orderId"), getDetailedOrder.getId(),
                "Response body value check: id");
        assertEquals(Integer.parseInt(getEnvironmentValue("bookId")), getDetailedOrder.getBookId(),
                "Response body value check: bookId");
        assertEquals(patchDetailedOrder.getCustomerName(), getDetailedOrder.getCustomerName(),
                "Response body value check: customerName");
        assertEquals(getEnvironmentValue("createdBy"), getDetailedOrder.getCreatedBy(),
                "Response body value check: createdBy");
        assertEquals(Integer.parseInt(getEnvironmentValue("quantity")), getDetailedOrder.getQuantity(),
                "Response body value check: quantity");
        assertEquals(Long.parseLong(getEnvironmentValue("timestamp")), getDetailedOrder.getTimestamp(),
                "Response body value check: timestamp");
    }

    @Test
    @Order(63)
    @DisplayName("PATCH /orders | No auth")
    @Description("Attempt to call PATCH /orders method without authorization.")
    public void patchOrdersNoAuth() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(401));
        String customerName = "patchTestUsername";
        String error = given().body(String.format("{\"customerName\": \"%s\"}", customerName)).
                when().patch("/orders/" + getEnvironmentValue("orderId")).then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("Missing Authorization header.", error, "Response body value check: error");
    }

    @Test
    @Order(64)
    @DisplayName("PATCH /orders | Other user token")
    @Description("Attempt to update order by calling PATCH /orders method with token of a user who is not the owner of the order.")
    public void patchOrdersOtherUserToken() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(404));
        String customerName = "patchTestUsername";
        String error = given().auth().preemptive().oauth2(getEnvironmentValue("otherAccessToken")).
                body(String.format("{\"customerName\": \"%s\"}", customerName)).
                when().patch("/orders/" + getEnvironmentValue("orderId")).then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals(String.format("No order with id %s.", getEnvironmentValue("orderId")), error,
                "Response body value check: error");
    }

    @Test
    @Order(65)
    @DisplayName("PATCH /orders | No id")
    @Description("Attempt to call PATCH /orders method without id parameter.")
    public void patchOrdersNoId() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(404));
        String customerName = "John";
        given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                body(String.format("{\"customerName\": \"%s\"}", customerName)).
                when().patch("/orders").then().log().all();
    }

    @Test
    @Order(66)
    @DisplayName("PATCH /orders | Nonexistent id")
    @Description("Attempt to call PATCH /orders method with nonexistent identifier.")
    public void patchOrdersNonexistentId() {
        String orderId = "test";
        setSpecifications(requestSpec(BASE_URL), responseSpec(404));
        String customerName = "patchTestUsername";
        String error = given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                body(String.format("{\"customerName\": \"%s\"}", customerName)).
                when().patch("/orders/" + orderId).then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals(String.format("No order with id %s.", orderId), error,
                "Response body value check: error");
    }

    @Test
    @Order(67)
    @DisplayName("PATCH /orders | No body")
    @Description("Attempt to call PATCH /orders method without request body.")
    public void patchOrdersNoBody() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                when().patch("/orders/" + getEnvironmentValue("orderId")).then().log().all();
    }

    @Test
    @Order(68)
    @DisplayName("PATCH /orders | Empty body")
    @Description("Attempt to call PATCH /orders method with empty request body.")
    public void patchOrdersEmptyBody() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).body("{}").
                when().patch("/orders/" + getEnvironmentValue("orderId")).then().log().all();
    }

    @Test
    @Order(69)
    @DisplayName("PATCH /orders | Empty customerName")
    @Description("Attempt to call PATCH /orders method with empty value of customerName parameter.")
    public void patchOrdersEmptyCustomerName() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(400));
        given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                body("{\"customerName\": \"\"}").when().
                patch("/orders/" + getEnvironmentValue("orderId")).then().log().all();
    }

    @Test
    @Order(70)
    @DisplayName("DELETE /orders")
    @Description("Deletes an existing order.")
    public void deleteOrders() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(204));
        given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                when().delete("/orders/" + getEnvironmentValue("orderId")).then().log().all();
        setSpecifications(requestSpec(BASE_URL), responseSpec(404));
        String error = given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                when().get("/orders/" + getEnvironmentValue("orderId")).then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals(String.format("No order with id %s.", getEnvironmentValue("orderId")), error,
                "Response body value check: error");
    }

    @Test
    @Order(71)
    @DisplayName("Create a new order")
    @Description("Creating a new order.")
    public void postOrdersNewOrder() {
        postOrders();
    }

    @Test
    @Order(72)
    @DisplayName("DELETE /orders | No auth")
    @Description("Attempt to call DELETE /orders method without authorization.")
    public void deleteOrdersNoAuth() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(401));
        String error = given().when().delete("/orders/" + getEnvironmentValue("orderId")).
                then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals("Missing Authorization header.", error, "Response body value check: error");
    }

    @Test
    @Order(73)
    @DisplayName("DELETE /orders | Other user token")
    @Description("Attempt to delete order by calling DELETE /orders method with token of a user who is not the owner of the order.")
    public void deleteOrdersOtherUserToken() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(404));
        String error = given().auth().preemptive().oauth2(getEnvironmentValue("otherAccessToken")).
                when().delete("/orders/" + getEnvironmentValue("orderId")).then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals(String.format("No order with id %s.", getEnvironmentValue("orderId")), error,
                "Response body value check: error");
    }

    @Test
    @Order(74)
    @DisplayName("DELETE /orders | No id")
    @Description("Attempt to call DELETE /orders method without id parameter.")
    public void deleteOrdersNoId() {
        setSpecifications(requestSpec(BASE_URL), responseSpec(404));
        given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                when().delete("/orders").then().log().all();
    }

    @Test
    @Order(75)
    @DisplayName("DELETE /orders | Nonexistent id")
    @Description("Attempt to call DELETE /orders method with nonexistent identifier.")
    public void deleteOrdersNonexistentId() {
        String orderId = "test";
        setSpecifications(requestSpec(BASE_URL), responseSpec(404));
        String error = given().auth().preemptive().oauth2(getEnvironmentValue("accessToken")).
                when().delete("/orders/" + orderId).then().log().all().
                body(matchesJsonSchemaInClasspath("error_schema.json")).
                extract().body().jsonPath().get("error");
        assertEquals(String.format("No order with id %s.", orderId), error,
                "Response body value check: error");
    }

}
