import DTO.Good;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static io.restassured.RestAssured.basic;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;

public class FourAPIMethodTests {

    public static final String BASE_URI = "http://localhost:8080";
    public static final String LOGIN = "admin";
    public static final String PASSWORD = "secret123";

    public String uniqueName() {
        return "Test_" + UUID.randomUUID();
    }

    public float uniquePrice() {
        return Math.round(new Random().nextFloat(0, 100) * 100) / 100.0f;
    }

    RequestSpecification requestSpecification = new RequestSpecBuilder()
            .setBaseUri(BASE_URI)
            .setAuth(basic(LOGIN, PASSWORD))
            .build();

    @Test
    void getGoodsListEmpty() {
        given()
                .baseUri(BASE_URI)
                .log().all()
                .auth()
                .basic(LOGIN, PASSWORD)

                .when()
                .get("/goods/list")

                .then()
                .log().all()
                .statusCode(200)
                .body("goods", empty());
    }

    @Test
    void getGoodsListEmptyReq(){
        given()
                .spec(requestSpecification)
                .log().all()

                .when()
                .get("/goods/list")

                .then()
                .log().all()
                .statusCode(200)
                .body("goods", empty());
    }

    @Test
    void postGoodsRestAssured() {
        String expectedName = uniqueName();
        float expectedPrice = uniquePrice();

        Response response =
                given()
                        .spec(requestSpecification)
                        .contentType(JSON)
                        .body("""
                            {
                                "name": "%s",
                                "price": %s
                            }
                            """.formatted(expectedName, expectedPrice))

                        .when()
                        .post("/goods/add")

                        .then()
                        .statusCode(200)
                        .extract().response();

        int id = response.path("data.id");

        given()
                .spec(requestSpecification)

                .when()
                .get("/goods/list")

                .then()
                .statusCode(200)
                .body("goods.id", hasItem(id))
                .body("goods.name", hasItem(expectedName))
                .body("goods.price", hasItem(expectedPrice));
    }

    @Test
    void postGoodsAsserJ() {
        String expectedName = uniqueName();
        float expectedPrice = uniquePrice();

        Response response =
                given()
                .spec(requestSpecification)
                .log().all()
                .contentType(JSON)
                .body("""
                    {
                        "name": "%s",
                        "price": %s
                    }
                    """.formatted(expectedName, expectedPrice))
                .when()
                .post("/goods/add")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response();

        int id = response.path("data.id");

        List<Good> goods =
                given()
                        .spec(requestSpecification)
                        .log().all()

                        .when()
                        .get("/goods/list")

                        .then()
                        .log().all()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getList("goods", Good.class);

        Good actualGood = goods.stream()
                .filter(good -> good.getId() == id)
                .findFirst()
                .orElseThrow();

        Assertions.assertThat(actualGood.getName())
                .as("Проверка имени созданного товара")
                .isEqualTo(expectedName);

        Assertions.assertThat(actualGood.getPrice())
                .as("Проверка цены созданного товара")
                .isEqualTo(expectedPrice);

    }

}
