package military.menu.review.ui.meal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import military.menu.review.application.like.LikeService;
import military.menu.review.application.member.MemberService;
import military.menu.review.common.RestDocsConfiguration;
import military.menu.review.domain.meal.*;
import military.menu.review.domain.meal.MealRepository;
import military.menu.review.domain.member.Member;
import military.menu.review.domain.member.MemberType;
import military.menu.review.domain.menu.Menu;
import military.menu.review.domain.menu.MenuRepository;
import military.menu.review.security.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static java.time.LocalDate.of;
import static military.menu.review.domain.meal.MealType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
@Import(RestDocsConfiguration.class)
public class MealControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    MealRepository mealRepository;
    @Autowired
    MenuRepository menuRepository;
    @Autowired
    SelectedMenuRepository selectedMenuRepository;
    @Autowired
    LikeService likeService;
    @Autowired
    MemberService memberService;
    @Autowired
    ObjectMapper objectMapper;

    List<Menu> menus;
    List<Meal> meals;
    Member member;
    static final String USERNAME = "wilgur513";
    static final String PASSWORD = "pass";

    @BeforeEach
    void setUp() {
        member = Member.of(USERNAME, PASSWORD, "", MemberType.SOLDIER);
        memberService.join(member);
    }

    @Test
    @DisplayName("??? ????????? ??? ????????? ??????")
    public void queryMeals() throws Exception {
        saveMeals();

        mockMvc.perform(get("/meals")
                .param("year", "2021")
                .param("month", "9")
                .param("week", "1")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("meals[0].id").value(meals.get(0).getId()))
                .andExpect(jsonPath("meals[0].menus[0].id").value(menus.get(0).getId()))
                .andExpect(jsonPath("meals[1].id").value(meals.get(1).getId()))
                .andExpect(jsonPath("meals[1].menus[0].id").value(menus.get(1).getId()))
                .andExpect(jsonPath("meals[2].id").value(meals.get(2).getId()))
                .andExpect(jsonPath("meals[2].menus[0].id").value(menus.get(0).getId()))
                .andExpect(jsonPath("meals[2].menus[1].id").value(menus.get(1).getId()))
                .andExpect(jsonPath("meals[3].id").value(meals.get(3).getId()))
                .andExpect(jsonPath("meals[3].menus[0].id").value(menus.get(0).getId()))
                .andExpect(jsonPath("meals[3].menus[1].id").value(menus.get(1).getId()))
                .andExpect(jsonPath("_links.next-week.href").value(containsString("year=2021&month=9&week=2")))
                .andExpect(jsonPath("_links.prev-week.href").value(containsString("year=2021&month=8&week=5")))
                .andDo(document("query-meals",
                        links(
                                linkWithRel("self").description("self ??????"),
                                linkWithRel("next-week").description("?????? ??? ????????? ??????"),
                                linkWithRel("prev-week").description("?????? ??? ????????? ??????"),
                                linkWithRel("profile").description("profile ??????")
                        ),
                        requestParameters(
                                parameterWithName("year").description("????????? ??????"),
                                parameterWithName("month").description("????????? ???"),
                                parameterWithName("week").description("????????? ???")
                        ),
                        responseFields(
                                fieldWithPath("meals[].id").description("????????? ?????? ??????"),
                                fieldWithPath("meals[].date").description("?????? ??????"),
                                fieldWithPath("meals[].mealType").description("?????? ??????(??????, ??????, ??????)"),
                                fieldWithPath("meals[].menus[].id").description("?????? ?????? ??????"),
                                fieldWithPath("meals[].menus[].name").description("?????? ??????"),
                                fieldWithPath("meals[].menus[].kcal").description("?????? ?????????"),
                                fieldWithPath("meals[].menus[].like").description("?????? ?????????"),
                                fieldWithPath("meals[].menus[]._links.self.href").description("?????? self ??????"),
                                fieldWithPath("meals[]._links.self.href").description("?????? self ??????"),
                                fieldWithPath("_links.self.href").description("self ??????"),
                                fieldWithPath("_links.next-week.href").description("?????? ??? ????????? ??????"),
                                fieldWithPath("_links.prev-week.href").description("?????? ??? ????????? ??????"),
                                fieldWithPath("_links.profile.href").description("profile ??????")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("????????? ??? ????????? ??????")
    public void queryMealWithMember() throws Exception {
        saveMeals();

        mockMvc.perform(get("/meals")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(USERNAME, PASSWORD))
                .param("year", "2021")
                .param("month", "9")
                .param("week", "1")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("meals[0].menus[0]._links.cancel-like.href").exists())
                .andExpect(jsonPath("meals[0].menus[0]._links.like.href").doesNotExist())
                .andExpect(jsonPath("meals[1].menus[0]._links.like.href").exists())
                .andExpect(jsonPath("meals[1].menus[0]._links.cancel-like.href").doesNotExist())
        ;
    }

    @Test
    @DisplayName("??? ????????? ??? ????????? ?????? ??????")
    public void queryMeal() throws Exception {
        Meal meal = Meal.of(of(2021, 9, 6), BREAKFAST);
        Menu menu1 = Menu.of("a", 1.0);
        Menu menu2 = Menu.of("b", 2.0);
        mealRepository.save(meal);
        menuRepository.save(menu1);
        menuRepository.save(menu2);
        selectedMenuRepository.save(SelectedMenu.of(meal, menu1));
        selectedMenuRepository.save(SelectedMenu.of(meal, menu2));

        mockMvc.perform(get("/meals/{id}", meal.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.meals.href").value(containsString("/meals?year=2021&month=9&week=1")))
                .andExpect(jsonPath("id").value(meal.getId()))
                .andExpect(jsonPath("date").value(meal.getDate().toString()))
                .andExpect(jsonPath("menus[0].id").value(menu1.getId()))
                .andExpect(jsonPath("menus[1].id").value(menu2.getId()))
                .andDo(document("query-meal",
                        links(
                                linkWithRel("self").description("?????? self ??????"),
                                linkWithRel("meals").description("?????? ????????? ??????"),
                                linkWithRel("profile").description("profile ??????")
                        ),
                        pathParameters(
                                parameterWithName("id").description("?????? ?????? ??????")
                        ),
                        responseFields(
                                fieldWithPath("id").description("?????? ?????? ??????"),
                                fieldWithPath("date").description("?????? ??????"),
                                fieldWithPath("mealType").description("?????? ??????(??????, ??????, ??????)"),
                                fieldWithPath("menus[].id").description("?????? ?????? ??????"),
                                fieldWithPath("menus[].name").description("?????? ??????"),
                                fieldWithPath("menus[].kcal").description("?????? ?????????"),
                                fieldWithPath("menus[].like").description("?????? ?????????"),
                                fieldWithPath("menus[]._links.self.href").description("?????? self ??????"),
                                fieldWithPath("_links.self.href").description("?????? self ??????"),
                                fieldWithPath("_links.meals.href").description("?????? ????????? ??????"),
                                fieldWithPath("_links.profile.href").description("profile ??????")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("???????????? ?????? ?????? ?????? ????????? ?????? ??? not found")
    public void wrongMealId() throws Exception {
        mockMvc.perform(get("/meals/{id}", 11111))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("????????? ???,???,??? ?????? ??? BadRequest")
    public void wrongWeekValue() throws Exception {
        mockMvc.perform(get("/meals")
                .param("year", "2021")
                .param("month", "8")
                .param("week", "6")
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;

        mockMvc.perform(get("/meals")
                .param("year", "2021")
                .param("month", "8")
                .param("week", "-1")
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    private void saveMeals() {
        menus = Arrays.asList(
                Menu.of("a", 1.0), Menu.of("b", 2.0)
        );
        menuRepository.saveAll(menus);

        meals = Arrays.asList(
                Meal.of(of(2021, 9, 6), BREAKFAST),
                Meal.of(of(2021, 9, 6), DINNER),
                Meal.of(of(2021, 9, 6), LUNCH),
                Meal.of(of(2021, 9, 7), BREAKFAST)
        );
        mealRepository.saveAll(meals);

        selectedMenuRepository.save(SelectedMenu.of(meals.get(0), menus.get(0)));
        selectedMenuRepository.save(SelectedMenu.of(meals.get(1), menus.get(1)));

        for(int i = 2; i < 4; i++) {
            Meal meal = meals.get(i);

            for(Menu menu : menus) {
                SelectedMenu selectedMenu = SelectedMenu.of(meal, menu);
                selectedMenuRepository.save(selectedMenu);
            }
        }

        likeService.like(member, menus.get(0));
    }

    private String getBearerToken(String username, String password) throws Exception {
        return "Bearer " + mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(username, password)))
        )
                .andReturn().getResponse().getHeader(HttpHeaders.AUTHORIZATION);
    }
}
