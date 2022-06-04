import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.Argument;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

public class DeckOfCardsApiTest {

   String getOneDeckUrl = "https://deckofcardsapi.com/api/deck/new/shuffle/?deck_count=1";
   String deck_id;

    @Test
    public void testDeckOfCardsSchema() throws ClassNotFoundException {

//        test to check that response match to json schema
        Response response = given()
                .when()
                .get(getOneDeckUrl);

        deck_id = response.jsonPath().get("deck_id");

        response
                .then()
                .assertThat().body(matchesJsonSchemaInClasspath("schema.json"));

//        variables and request to draw 52 cards from the deck
        int amountOfCardToDraw = 52;
        String getCardsUrl = "https://deckofcardsapi.com/api/deck/" + deck_id + "/draw";

        Response responseDrawCards = given()
                .queryParams("count", amountOfCardToDraw)
                .when()
                .get(getCardsUrl)
                .then()
                .extract().response();

        JsonPath json = responseDrawCards.jsonPath();

//        Assertion to check if remaining after drawn equals 0
        int remainingCardsAfterDraw = (int) json.get("remaining");
        Assertions.assertEquals(0, remainingCardsAfterDraw);

//        creating the list of card objects
        List<Object> cards = json.getList("cards");

//        test if 52 cards have been drawn
        Assertions.assertEquals(52, cards.size());


//        Below, My futile attempts to get card with code 8C. response i get was some kind of
//        weirdy objects (example in weird_response.txt) and I couldn't get or convert data from it

        //        ArrayList<String> codes = null;
//
//        for (Object card : cards) {
//            Arrays.asList(card);
//            for (Object value: Arrays.asList(card)) {
//                System.out.println(value.toString());
//                String value1 = value.toString();
//                codes.add(value1);
////                codes.add(value.toString());
//            }
//        }

        //        System.out.println(cards.get(1));
////        assertThat(cards, hasItem("code=8C"));
//        assertThat(cards, Matchers.hasProperty("code=8C"));
//        assertThat(cards, contains(
//                hasToString("code=8C")
//        ));
//        assertThat(cards, stringContainsInOrder("code=8C"));
    }


    //    Class below provides the parameters for the next test. First param is a class type, second is an attribute we want to it's type
    private static Stream<Arguments> createDataForAttributesValue() {
        return Stream.of(
                Arguments.of(Boolean.class, "success"),
                Arguments.of(String.class, "deck_id"),
                Arguments.of(Boolean.class, "shuffled"),
                Arguments.of(Integer.class, "remaining")
        );
    }

    @ParameterizedTest
    @MethodSource("createDataForAttributesValue")
    public void testDeckOfCardsAttributeValues(Class<Object> classType, String attributeName){

        Response response = given()
                .when()
                .get(getOneDeckUrl);

        JsonPath json = response.jsonPath();

        Assertions.assertInstanceOf(classType, json.get(attributeName));
    }




}
