package military.menu.review.ui.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import military.menu.review.application.member.MemberService;
import military.menu.review.common.RestDocsConfiguration;
import military.menu.review.domain.meal.Meal;
import military.menu.review.domain.meal.MealRepository;
import military.menu.review.domain.meal.MealType;
import military.menu.review.domain.member.Member;
import military.menu.review.domain.member.MemberType;
import military.menu.review.domain.review.Review;
import military.menu.review.domain.review.ReviewRepository;
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

import javax.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Transactional
@Import(RestDocsConfiguration.class)
public class ReviewControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    MemberService memberService;
    @Autowired
    MealRepository mealRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    EntityManager em;

    Member member1;
    Member member2;
    Meal meal;
    Review review;

    final String username = "wilgur513";
    final String password = "pass";
    final LocalDate date = LocalDate.of(2021, 9, 8);
    final MealType mealType = MealType.BREAKFAST;

    @BeforeEach
    void setUp() {
        member1 = Member.of(username, password, "wilgur513", MemberType.SOLDIER);
        member2 = Member.of("user", "pass", "user1", MemberType.SOLDIER);
        memberService.join(member1);
        memberService.join(member2);
        meal = Meal.of(date, mealType);
        mealRepository.save(meal);
    }

    @Test
    @DisplayName("?????? ??????")
    public void createReview() throws Exception {
        mockMvc.perform(post("/meals/{id}/reviews", meal.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReviewRequest("contents")))
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("create-review",
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("????????? ?????? URI")
                        ),
                        links(
                                linkWithRel("self").description("self ??????"),
                                linkWithRel("reviews").description("????????? ??????"),
                                linkWithRel("delete-review").description("?????? ?????? ??????"),
                                linkWithRel("update-review").description("?????? ?????? ??????"),
                                linkWithRel("profile").description("profile URI")
                        ),
                        responseFields(
                                fieldWithPath("id").description("?????? ?????? ??????"),
                                fieldWithPath("memberId").description("?????? ????????? ?????? ??????"),
                                fieldWithPath("mealId").description("?????? ?????? ?????? ??????"),
                                fieldWithPath("content").description("?????? ??????"),
                                fieldWithPath("created").description("?????? ?????? ??????"),
                                fieldWithPath("_links.self.href").description("self ??????"),
                                fieldWithPath("_links.reviews.href").description("?????? ????????? ?????? ??????"),
                                fieldWithPath("_links.delete-review.href").description("?????? ?????? ??????"),
                                fieldWithPath("_links.update-review.href").description("?????? ?????? ??????"),
                                fieldWithPath("_links.profile.href").description("profile URI")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("??? ????????? ??? ?????? ?????? ??????")
    public void createReviewWithAnonymous() throws Exception {
        mockMvc.perform(post("/meals/{id}/reviews", meal.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReviewRequest("contents")))
        )
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("??? ????????? ?????? ?????? ??? ??????")
    public void emptyContent() throws Exception {
        mockMvc.perform(post("/meals/{id}/reviews", meal.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReviewRequest("")))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("???????????? ?????? ????????? ?????? ?????? ??? ??????")
    public void emptyMealReview() throws Exception {
        mockMvc.perform(post("/meals/{id}/reviews", meal.getId() + 1111111)
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReviewRequest("contents")))
        )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("??? ????????? ??? ?????? ????????? ??????")
    public void queryReviews() throws Exception {
        saveReviews();

        mockMvc.perform(get("/meals/{id}/reviews", meal.getId())
                .param("size", "3")
                .param("page", "0")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("query-reviews",
                        links(
                                linkWithRel("self").description("self ??????"),
                                linkWithRel("first").description("?????? ?????????"),
                                linkWithRel("next").description("?????? ?????????"),
                                linkWithRel("last").description("????????? ?????????"),
                                linkWithRel("profile").description("profile URI")
                        ),
                        requestParameters(
                                parameterWithName("size").description("??? ???????????? ?????? ?????? ??????"),
                                parameterWithName("page").description("????????? ????????? ??????")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.reviewResponseList[].id").description("?????? ?????? ??????"),
                                fieldWithPath("_embedded.reviewResponseList[].mealId").description("?????? ?????? ?????? ??????"),
                                fieldWithPath("_embedded.reviewResponseList[].memberId").description("?????? ????????? ?????? ??????"),
                                fieldWithPath("_embedded.reviewResponseList[].content").description("?????? ?????? ??????"),
                                fieldWithPath("_embedded.reviewResponseList[].created").description("?????? ?????? ??????"),
                                fieldWithPath("_embedded.reviewResponseList[]._links.self.href").description("self ??????"),
                                fieldWithPath("_embedded.reviewResponseList[]._links.reviews.href").description("?????? ????????? ?????? ??????"),
                                fieldWithPath("page.size").description("??? ???????????? ?????? ?????? ??????"),
                                fieldWithPath("page.totalElements").description("?????? ????????? ???"),
                                fieldWithPath("page.totalPages").description("?????? ????????? ???"),
                                fieldWithPath("page.number").description("?????? ????????? ??????(0?????? ??????)"),
                                fieldWithPath("_links.self.href").description("?????? ?????????"),
                                fieldWithPath("_links.first.href").description("?????? ?????????"),
                                fieldWithPath("_links.next.href").description("?????? ?????????"),
                                fieldWithPath("_links.last.href").description("????????? ?????????"),
                                fieldWithPath("_links.profile.href").description("profile URI")
                        )
                ));
    }

    @Test
    @DisplayName("????????? ??? ????????? ??????")
    public void queryReviewsWithMember() throws Exception {
        saveReviews();

        mockMvc.perform(get("/meals/{id}/reviews", meal.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(username, password))
                .param("size", "3")
                .param("page", "0")
        )
                .andDo(print())
                .andExpect(jsonPath("_links.create-review.href").exists())
                .andExpect(jsonPath("_embedded.reviewResponseList[0]._links.update-review.href").exists())
                .andExpect(jsonPath("_embedded.reviewResponseList[0]._links.delete-review.href").exists())
        ;
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    public void queryReview() throws Exception {
        saveReviews();

        mockMvc.perform(get("/meals/{id}/reviews/{reviewId}", meal.getId(), review.getId()))
                .andExpect(status().isOk())
                .andDo(document("query-review",
                        links(
                                linkWithRel("self").description("self ??????"),
                                linkWithRel("reviews").description("?????? ????????? ?????? ??????"),
                                linkWithRel("profile").description("profile URI")
                        ),
                        responseFields(
                                fieldWithPath("id").description("?????? ?????? ??????"),
                                fieldWithPath("mealId").description("?????? ?????? ?????? ??????"),
                                fieldWithPath("memberId").description("?????? ????????? ?????? ??????"),
                                fieldWithPath("content").description("?????? ?????? ??????"),
                                fieldWithPath("created").description("?????? ?????? ??????"),
                                fieldWithPath("_links.self.href").description("self ??????"),
                                fieldWithPath("_links.reviews.href").description("?????? ????????? ?????? ??????"),
                                fieldWithPath("_links.profile.href").description("profile URI")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("???????????? ?????? ???????????? ?????? ?????? ?????? ??? ??????")
    public void queryEmptyMealReview() throws Exception {
        mockMvc.perform(get("/meals/{id}/reviews/{reviewId}", meal.getId() + 1111, 1111))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }
    
    @Test
    @DisplayName("???????????? ???????????? ?????? ?????? ?????? ??? ??????")
    public void queryMealEmptyReview() throws Exception {
        saveReviews();

        mockMvc.perform(get("/meals/{id}/reviews/{reviewId}", meal.getId(), review.getId() + 1111))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("????????? ?????? ??????")
    public void deleteReview() throws Exception {
        saveReviews();

        mockMvc.perform(delete("/meals/{id}/reviews/{reviewId}", meal.getId(), review.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(username, password))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("delete-review",
                        links(
                                linkWithRel("create-review").description("?????? ??????"),
                                linkWithRel("reviews").description("?????? ????????? ??????"),
                                linkWithRel("profile").description("profile URI")
                        )
                ))
        ;

        Optional<Review> r = reviewRepository.findById(review.getId());
        assertThat(r.isPresent()).isFalse();
    }

    @Test
    @DisplayName("??? ????????? ??? ?????? ?????? ?????? ??? ??????")
    public void deleteReviewWithAnonymous() throws Exception {
        saveReviews();

        mockMvc.perform(delete("/meals/{id}/reviews/{reviewId}", meal.getId(), review.getId()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("???????????? ?????? ???????????? ?????? ?????? ???")
    public void deleteReviewFromEmptyMeal() throws Exception {
        mockMvc.perform(delete("/meals/{id}/reviews/{reviewId}", meal.getId() + 1111, 1111)
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(username, password))
        )
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("???????????? ???????????? ?????? ?????? ?????? ??? ??????")
    public void deleteNotCreatedReview() throws Exception {
        saveReviews();

        mockMvc.perform(delete("/meals/{id}/reviews/{reviewId}", meal.getId(), review.getId() + 1111)
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(username, password))
        )
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("????????? ????????? ???????????? ?????? ?????? ??? ??????")
    public void deleteReviewWithNotCreator() throws Exception {
        saveReviews();

        mockMvc.perform(delete("/meals/{id}/reviews/{reviewId}", meal.getId(), review.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken("user", "pass"))
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("?????? ??????")
    public void updateReview() throws Exception {
        saveReviews();

        LocalDateTime oldTime = review.getCreated();
        String oldContent = review.getContent();

        mockMvc.perform(put("/meals/{id}/reviews/{reviewId}", meal.getId(), review.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReviewRequest("update content")))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("update-review",
                        links(
                                linkWithRel("self").description("self ??????"),
                                linkWithRel("delete-review").description("?????? ?????? ??????"),
                                linkWithRel("update-review").description("?????? ?????? ??????"),
                                linkWithRel("reviews").description("?????? ????????? ?????? ??????"),
                                linkWithRel("profile").description("profile URI")
                        ),
                        responseFields(
                                fieldWithPath("id").description("?????? ?????? ??????"),
                                fieldWithPath("mealId").description("?????? ?????? ?????? ??????"),
                                fieldWithPath("memberId").description("?????? ????????? ?????? ??????"),
                                fieldWithPath("content").description("?????? ?????? ??????"),
                                fieldWithPath("created").description("?????? ?????? ??????"),
                                fieldWithPath("_links.self.href").description("self ??????"),
                                fieldWithPath("_links.reviews.href").description("?????? ????????? ?????? ??????"),
                                fieldWithPath("_links.delete-review.href").description("?????? ?????? ??????"),
                                fieldWithPath("_links.update-review.href").description("?????? ?????? ??????"),
                                fieldWithPath("_links.profile.href").description("profile URI")
                        )
                ));

        Optional<Review> optional = reviewRepository.findById(review.getId());
        assertThat(optional.get().getContent()).isNotEqualTo(oldContent);
        assertThat(optional.get().getCreated()).isNotEqualTo(oldTime);
    }

    private void saveReviews() {
        review = Review.of(member1, meal, "content1");
        reviewRepository.save(review);

        for(int i = 0; i < 9; i++) {
            reviewRepository.save(Review.of(member2, meal, "content" + (i + 2)));
        }
    }


    private String getBearerToken(String username, String password) throws Exception {
        return "Bearer " + mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(username, password)))
        )
                .andReturn().getResponse().getHeader(HttpHeaders.AUTHORIZATION);
    }
}
