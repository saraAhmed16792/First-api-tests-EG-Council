package apiTests;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class RestfulBookerApiTests {
    //login precondition
    String accessToken;
    int bookingID;

    public RestfulBookerApiTests() {

    }

    @BeforeMethod
    public void setupPreCondition_logInToApp() {
        String endpoint = "https://restful-booker.herokuapp.com/auth";
        String body = """
                {
                    "username" : "admin",
                    "password" : "password123"
                } """;
        Response response = given().body(body).header("Content-Type", "application/json")
                .log().all()
                .when()
                .post(endpoint)
                .then().extract().response();
        JsonPath jsonPath = response.jsonPath();
        accessToken = jsonPath.getString("token");
        System.out.println(accessToken);
    }

    //Create booking
    @Test(priority = 0)
    public void testCreateBooking() {
        String endpoint = "https://restful-booker.herokuapp.com/booking";
        String body = """
                  {
                  "firstname" : "Jim",
                      "lastname" : "Brown",
                      "totalprice" : 111,
                      "depositpaid" : true,
                      "bookingdates" : {
                          "checkin" : "2018-01-01",
                          "checkout" : "2019-01-01"
                      },
                      "additionalneeds" : "Breakfast"
                }""";
        var responseToValidate = given().header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(body)
                .log().all().when()
                .post(endpoint).then();
        responseToValidate.body("booking.firstname", equalTo("Jim"));
        responseToValidate.body("booking.lastname", equalTo("Brown"));
        responseToValidate.body("booking.totalprice", equalTo(111));
        responseToValidate.statusCode(200);
        Response response = responseToValidate.extract().response();
        JsonPath jsonPath = response.jsonPath();
        bookingID = jsonPath.getInt("bookingid");
        responseToValidate.log().all();
    }

    //Edit the booking
    @Test(priority = 1)
    public void testEditingBooking() {
        String endpoint = "https://restful-booker.herokuapp.com/booking/" + bookingID;
        String body = """
                {
                    "firstname" : "James",
                    "lastname" : "Brown",
                    "totalprice" : 111,
                    "depositpaid" : true,
                    "bookingdates" : {
                        "checkin" : "2018-01-01",
                        "checkout" : "2019-01-01"
                    },
                    "additionalneeds" : "Breakfast"
                } """;
        var responseToValidate = given().header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Cookie", "token=" + accessToken)
                .body(body)
                .log().all().when()
                .put(endpoint).then();
        responseToValidate.body("firstname", equalTo("James"));
        responseToValidate.body("bookingdates.size()", greaterThanOrEqualTo(0));
        responseToValidate.statusCode(200);
    }

    //Get the booking
    @Test(priority = 2)
    public void testGetBooking() {
        String endpoint = "https://restful-booker.herokuapp.com/booking/" + bookingID;
        var responseToValidate = given().header("Content-Type", "application/json")
                .when().log().all()
                .get(endpoint)
                .then();
        responseToValidate.body("lastname", equalTo("Brown"));
        responseToValidate.body("additionalneeds", equalTo("Breakfast"));
        responseToValidate.statusCode(200);
    }

    //Delete the booking
    @Test(priority = 3)
    public void testDeleteBooking() {
        String endpoint = "https://restful-booker.herokuapp.com/booking/" + bookingID;
        var responseToValidate = given().header("Content-Type", "application/json")
                .header("Cookie", "token=" + accessToken)
                .when().log().all()
                .delete(endpoint)
                .then();
        responseToValidate.statusCode(201);
        Response response = responseToValidate.extract().response();
        Assert.assertEquals(response.asString(), "Created");
    }
}
