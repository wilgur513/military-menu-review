package military.menu.review.ui.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import military.menu.review.application.like.LikeService;
import military.menu.review.domain.Role;
import military.menu.review.domain.member.Member;
import military.menu.review.domain.menu.Menu;
import military.menu.review.domain.menu.MenuRepository;
import military.menu.review.security.LoginRequest;
import military.menu.review.application.member.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    @BeforeEach
    void setUp() {
        member = Member.of(USERNAME, PASSWORD, "정진혁", null, Role.NORMAL);
        memberService.join(member);
    }

    @Test
    @DisplayName("미 로그인 시 10개 메뉴중 3개씩 2번째 페이지 가져오기")
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
                                linkWithRel("first").description("시작 페이지"),
                                linkWithRel("prev").description("이전 페이지"),
                                linkWithRel("self").description("현재 페이지"),
                                linkWithRel("next").description("다음 페이지"),
                                linkWithRel("last").description("마지막 페이지")
                        ),
                        requestParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 당 메뉴 개수"),
                                parameterWithName("sort").description("정렬 옵션")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.menuResponseList[].name").description("메뉴 이름"),
                                fieldWithPath("_embedded.menuResponseList[].kcal").description("메뉴 칼로리"),
                                fieldWithPath("_embedded.menuResponseList[].like").description("메뉴 좋아요 개수"),
                                fieldWithPath("_embedded.menuResponseList[].id").description("메뉴 식별 번호"),
                                fieldWithPath("_embedded.menuResponseList[]._links.self.href").description("메뉴 개별 조회 링크"),
                                fieldWithPath("page.size").description("페이지 당 메뉴 개수"),
                                fieldWithPath("page.totalElements").description("전체 메뉴 개수"),
                                fieldWithPath("page.totalPages").description("전체 페이지 수"),
                                fieldWithPath("page.number").description("현재 페이지 번호(0부터 시작)"),
                                fieldWithPath("_links.first.href").description("시작 페이지"),
                                fieldWithPath("_links.prev.href").description("이전 페이지"),
                                fieldWithPath("_links.self.href").description("현재 페이지"),
                                fieldWithPath("_links.next.href").description("다음 페이지"),
                                fieldWithPath("_links.last.href").description("마지막 페이지")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("미 로그인 시 메뉴 단건 조회하기")
    public void getMenuWithAnonymous() throws Exception {
        Menu menu = Menu.of("a", 1.0);
        menuRepository.save(menu);

        mockMvc.perform(get("/menus/{id}", menu.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("a"))
                .andExpect(jsonPath("kcal").value("1.0"))
                .andExpect(jsonPath("id").value(menu.getId()))
                .andExpect(jsonPath("like").value("0"))
                .andDo(document("query-menu",
                        links(
                                linkWithRel("self").description("단건 메뉴 조회 링크")
                        ),
                        pathParameters(
                                parameterWithName("id").description("메뉴 식별 번호")
                        ),
                        responseFields(
                                fieldWithPath("id").description("메뉴 식별 번호"),
                                fieldWithPath("name").description("메뉴 이름"),
                                fieldWithPath("kcal").description("메뉴 칼로리"),
                                fieldWithPath("like").description("메뉴 좋아요 수"),
                                fieldWithPath("_links.self.href").description("메뉴 self 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("로그인 후 메뉴 단건 조회")
    @Disabled
    public void queryMenuWithMember() throws Exception {
        Menu menu = Menu.of("a", 1.0);
        menuRepository.save(menu);

        mockMvc.perform(get("/menus/{id}", menu.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(USERNAME, PASSWORD))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.like.href").exists())
        ;
    }

    @Test
    @DisplayName("존재하지 않는 메뉴 조회하기")
    public void queryEmptyMenu() throws Exception {
        mockMvc.perform(get("/menus/100000"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private void saveMenus() {
        List<Menu> menus = Arrays.asList(
                Menu.of("a", 1.0), Menu.of("b", 2.0), Menu.of("c", 3.0),
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
