package military.menu.review.mndapi.convertor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import military.menu.review.model.menu.Menu;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

@DisplayName("JsonToMenuConvertor 테스트")
public class JsonToMenuConvertorTest {
    JsonToMenuConvertor basic = new JsonToMenuConvertor() {
        @Override
        public String getMenuNameColumn() {
            return "mymenu";
        }

        @Override
        public String getCalorieColumn() {
            return "mycalorie";
        }
    };
    JsonToMenuConvertor breakfast = new JsonToBreakfastConvertor();
    JsonToMenuConvertor lunch = new JsonToLunchConvertor();
    JsonToMenuConvertor dinner = new JsonToDinnerConvertor();

    @Test
    @DisplayName("템플릿 메서드를 통한 메뉴 컬럼 매핑")
    public void shouldMappingByGetXXXMethod() {
        Optional<Menu> menu = basic.convert(jsonMap("{\"mymenu\": \"hello\", \"mycalorie\":\"23.32\"}"));
        assertThat(menu, is(Optional.of(Menu.of("hello", 23.32))));
    }

    @Test
    @DisplayName("메뉴와 칼로리 값중 필요없는 문자 제거")
    void shouldRemoveRedundantText() {
        Optional<Menu> menu = basic.convert(jsonMap("{\"mymenu\": \"hello(123(123))(123)\", \"mycalorie\":\"23.3kcal\"}"));
        assertThat(menu, is(Optional.of(Menu.of("hello", 23.32))));
    }

    @Test
    @DisplayName("아침 메뉴 변환")
    public void shouldGetBreakfastMenu() {
        Optional<Menu> menu = breakfast.convert(jsonMap("{\"brst\":\"아침\", \"brst_cal\":\"123kcal\"}"));
        assertThat(menu, is(Optional.of(Menu.of("아침", 123.0))));
    }

    @Test
    @DisplayName("점심 메뉴 변환")
    public void shouldGetLunchMenu() {
        Optional<Menu> menu = lunch.convert(jsonMap("{\"lunc\":\"점심\", \"lunc_cal\":\"123kcal\"}"));
        assertThat(menu, is(Optional.of(Menu.of("점심", 123.0))));
    }

    @Test
    @DisplayName("저녁 메뉴 변환")
    public void shouldGetDinnerMenu() {
        Optional<Menu> menu = dinner.convert(jsonMap("{\"dinr\":\"저녁\", \"dinr_cal\":\"123kcal\"}"));
        assertThat(menu, is(Optional.of(Menu.of("저녁", 123.0))));
    }

    private Map<String, String> jsonMap(String json) {
        try {
            Map result = new ObjectMapper().readValue(json, Map.class);
            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
