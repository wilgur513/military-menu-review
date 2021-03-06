package military.menu.review.ui.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import military.menu.review.application.like.LikeService;
import military.menu.review.common.RestDocsConfiguration;
import military.menu.review.domain.member.MemberType;
import military.menu.review.domain.member.Member;
import military.menu.review.domain.menu.Menu;
import military.menu.review.domain.menu.MenuRepository;
import military.menu.review.security.LoginRequest;
import military.menu.review.application.member.MemberService;
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

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@Transactional
public class MenuControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    MenuRepository menuRepository;

    @Autowired
    MemberService memberService;

    @Autowired
    LikeService likeService;

    @Autowired
    ObjectMapper objectMapper;

    static final String USERNAME = "wilgur513";
    static final String PASSWORD = "pass";
    Member member;
    Menu menu;

    @BeforeEach
    void setUp() {
        member = Member.of(USERNAME, PASSWORD, "?????????", MemberType.SOLDIER);
        memberService.join(member);
        menu = Menu.of("a", 1.0);
        menuRepository.save(menu);
    }

    @Test
    @DisplayName("??? ????????? ??? 10??? ????????? 3?????? 2?????? ????????? ????????????")
    public void getMenusWithAnonymous() throws Exception {
        saveMenus();

        mockMvc.perform(get("/menus")
                .param("page", "1")
                .param("size", "3")
                .param("sort", "name,ASC")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("query-menus",
                        links(
                                linkWithRel("first").description("?????? ?????????"),
                                linkWithRel("prev").description("?????? ?????????"),
                                linkWithRel("self").description("?????? ?????????"),
                                linkWithRel("next").description("?????? ?????????"),
                                linkWithRel("last").description("????????? ?????????"),
                                linkWithRel("profile").description("profile URI")
                        ),
                        requestParameters(
                                parameterWithName("page").description("????????? ??????"),
                                parameterWithName("size").description("????????? ??? ?????? ??????"),
                                parameterWithName("sort").description("?????? ??????")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.menuResponseList[].name").description("?????? ??????"),
                                fieldWithPath("_embedded.menuResponseList[].kcal").description("?????? ?????????"),
                                fieldWithPath("_embedded.menuResponseList[].like").description("?????? ????????? ??????"),
                                fieldWithPath("_embedded.menuResponseList[].id").description("?????? ?????? ??????"),
                                fieldWithPath("_embedded.menuResponseList[]._links.self.href").description("?????? ?????? ?????? ??????"),
                                fieldWithPath("page.size").description("????????? ??? ?????? ??????"),
                                fieldWithPath("page.totalElements").description("?????? ?????? ??????"),
                                fieldWithPath("page.totalPages").description("?????? ????????? ???"),
                                fieldWithPath("page.number").description("?????? ????????? ??????(0?????? ??????)"),
                                fieldWithPath("_links.first.href").description("?????? ?????????"),
                                fieldWithPath("_links.prev.href").description("?????? ?????????"),
                                fieldWithPath("_links.self.href").description("?????? ?????????"),
                                fieldWithPath("_links.next.href").description("?????? ?????????"),
                                fieldWithPath("_links.last.href").description("????????? ?????????"),
                                fieldWithPath("_links.profile.href").description("profile URI")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("????????? ??? ???????????? ????????? ?????? 10??? ????????? 3?????? 1?????? ?????????")
    public void getNotLikedMenusWithMember() throws Exception {
        saveMenus();

        mockMvc.perform(get("/menus")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(USERNAME, PASSWORD))
                .param("size", "3")
                .param("page", "0")
                .param("sort", "name,ASC")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("query-menus-with-member",
                        links(
                                linkWithRel("first").description("?????? ?????????"),
                                linkWithRel("self").description("?????? ?????????"),
                                linkWithRel("next").description("?????? ?????????"),
                                linkWithRel("last").description("????????? ?????????"),
                                linkWithRel("profile").description("profile URI")
                        ),
                        requestParameters(
                                parameterWithName("page").description("????????? ??????"),
                                parameterWithName("size").description("????????? ??? ?????? ??????"),
                                parameterWithName("sort").description("?????? ??????")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.menuResponseList[].name").description("?????? ??????"),
                                fieldWithPath("_embedded.menuResponseList[].kcal").description("?????? ?????????"),
                                fieldWithPath("_embedded.menuResponseList[].like").description("?????? ????????? ??????"),
                                fieldWithPath("_embedded.menuResponseList[].id").description("?????? ?????? ??????"),
                                fieldWithPath("_embedded.menuResponseList[]._links.self.href").description("?????? ?????? ?????? ??????"),
                                fieldWithPath("_embedded.menuResponseList[]._links.like.href").description("?????? ????????? ??????"),
                                fieldWithPath("page.size").description("????????? ??? ?????? ??????"),
                                fieldWithPath("page.totalElements").description("?????? ?????? ??????"),
                                fieldWithPath("page.totalPages").description("?????? ????????? ???"),
                                fieldWithPath("page.number").description("?????? ????????? ??????(0?????? ??????)"),
                                fieldWithPath("_links.self.href").description("?????? ?????????"),
                                fieldWithPath("_links.first.href").description("?????? ?????????"),
                                fieldWithPath("_links.next.href").description("?????? ?????????"),
                                fieldWithPath("_links.last.href").description("????????? ?????????"),
                                fieldWithPath("_links.profile.href").description("profile URI")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("????????? ??? 10??? ????????? 3?????? 1?????? ?????????")
    public void getLikedMenusWithMember() throws Exception {
        saveMenus();
        likeService.like(member, menu);

        mockMvc.perform(get("/menus")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(USERNAME, PASSWORD))
                .param("size", "3")
                .param("page", "0")
                .param("sort", "name,ASC")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.menuResponseList[0]._links.like.href").doesNotExist())
                .andExpect(jsonPath("_embedded.menuResponseList[0]._links.cancel-like.href").exists())
                .andExpect(jsonPath("_embedded.menuResponseList[1]._links.like.href").exists())
                .andExpect(jsonPath("_embedded.menuResponseList[1]._links.cancel-like.href").doesNotExist())
                .andExpect(jsonPath("_embedded.menuResponseList[2]._links.like.href").exists())
                .andExpect(jsonPath("_embedded.menuResponseList[2]._links.cancel-like.href").doesNotExist())
                .andDo(document("query-menus-with-member",
                        links(
                                linkWithRel("first").description("?????? ?????????"),
                                linkWithRel("self").description("?????? ?????????"),
                                linkWithRel("next").description("?????? ?????????"),
                                linkWithRel("last").description("????????? ?????????"),
                                linkWithRel("profile").description("profile URI")
                        ),
                        requestParameters(
                                parameterWithName("page").description("????????? ??????"),
                                parameterWithName("size").description("????????? ??? ?????? ??????"),
                                parameterWithName("sort").description("?????? ??????")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("_embedded.menuResponseList[].name").description("?????? ??????"),
                                fieldWithPath("_embedded.menuResponseList[].kcal").description("?????? ?????????"),
                                fieldWithPath("_embedded.menuResponseList[].like").description("?????? ????????? ??????"),
                                fieldWithPath("_embedded.menuResponseList[].id").description("?????? ?????? ??????"),
                                fieldWithPath("_embedded.menuResponseList[]._links.self.href").description("?????? ?????? ?????? ??????"),
                                fieldWithPath("page.size").description("????????? ??? ?????? ??????"),
                                fieldWithPath("page.totalElements").description("?????? ?????? ??????"),
                                fieldWithPath("page.totalPages").description("?????? ????????? ???"),
                                fieldWithPath("page.number").description("?????? ????????? ??????(0?????? ??????)"),
                                fieldWithPath("_links.self.href").description("?????? ?????????"),
                                fieldWithPath("_links.first.href").description("??? ?????????"),
                                fieldWithPath("_links.last.href").description("?????? ?????????"),
                                fieldWithPath("_links.next.href").description("????????? ?????????"),
                                fieldWithPath("_links.profile.href").description("profile URI")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("??? ????????? ??? ?????? ?????? ????????????")
    public void getMenuWithAnonymous() throws Exception {
        mockMvc.perform(get("/menus/{id}", menu.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("a"))
                .andExpect(jsonPath("kcal").value("1.0"))
                .andExpect(jsonPath("id").value(menu.getId()))
                .andExpect(jsonPath("like").value("0"))
                .andDo(document("query-menu",
                        links(
                                linkWithRel("self").description("?????? ?????? ?????? ??????"),
                                linkWithRel("profile").description("profile URI")
                        ),
                        pathParameters(
                                parameterWithName("id").description("?????? ?????? ??????")
                        ),
                        responseFields(
                                fieldWithPath("id").description("?????? ?????? ??????"),
                                fieldWithPath("name").description("?????? ??????"),
                                fieldWithPath("kcal").description("?????? ?????????"),
                                fieldWithPath("like").description("?????? ????????? ???"),
                                fieldWithPath("_links.self.href").description("?????? self ??????"),
                                fieldWithPath("_links.profile.href").description("profile URI")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("????????? ??? ?????? ?????? ??????")
    public void queryMenuWithMember() throws Exception {
        mockMvc.perform(get("/menus/{id}", menu.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(USERNAME, PASSWORD))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.like.href").exists())
                .andExpect(jsonPath("_links.cancel-like.href").doesNotExist())
        ;

        likeService.like(member, menu);

        mockMvc.perform(get("/menus/{id}", menu.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(USERNAME, PASSWORD))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.like.href").doesNotExist())
                .andExpect(jsonPath("_links.cancel-like.href").exists())
        ;
    }

    @Test
    @DisplayName("???????????? ?????? ?????? ????????????")
    public void queryEmptyMenu() throws Exception {
        mockMvc.perform(get("/menus/100000"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private void saveMenus() {
        List<Menu> menus = Arrays.asList(
                Menu.of("b", 2.0), Menu.of("c", 3.0),
                Menu.of("d", 4.0), Menu.of("e", 5.0), Menu.of("f", 6.0),
                Menu.of("g", 7.0), Menu.of("h", 8.0), Menu.of("i", 9.0),
                Menu.of("j", 10.0)
        );

        menuRepository.saveAll(menus);
    }

    private String getBearerToken(String username, String password) throws Exception {
        return "Bearer " + mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(username, password)))
        )
                .andReturn().getResponse().getHeader(HttpHeaders.AUTHORIZATION);
    }

}
