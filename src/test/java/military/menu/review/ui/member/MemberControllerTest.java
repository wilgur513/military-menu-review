package military.menu.review.ui.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import military.menu.review.common.RestDocsConfiguration;
import military.menu.review.domain.member.MemberType;
import military.menu.review.domain.member.Member;
import military.menu.review.security.LoginRequest;
import military.menu.review.application.member.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import(RestDocsConfiguration.class)
@Transactional
public class MemberControllerTest {
    @Autowired
    private MemberService memberService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("???????????? ????????? ?????????")
    @Transactional
    public void login() throws Exception {
        Member member = Member.of("wilgur513", "pass", "?????????", MemberType.SOLDIER);
        memberService.join(member);

        mockMvc.perform(post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new LoginRequest("wilgur513", "pass")))
                )
                .andDo(print())
                .andExpect(jsonPath("username").value("wilgur513"))
                .andExpect(jsonPath("name").value("?????????"))
                .andExpect(jsonPath("type").value("SOLDIER"))
                .andDo(document("login-member",
                        links(
                                linkWithRel("profile").description("profile URI")
                        ),
                        requestFields(
                                fieldWithPath("username").description("????????? ?????????"),
                                fieldWithPath("password").description("????????? ????????????")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("JWT ??????")
                        ),
                        responseFields(
                                fieldWithPath("id").description("????????? ??????"),
                                fieldWithPath("username").description("????????? ?????????"),
                                fieldWithPath("name").description("????????? ??????"),
                                fieldWithPath("type").description("????????? ??????(??????[SOLDIER])"),
                                fieldWithPath("_links.profile.href").description("profile URI")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("???????????? ?????? ?????? ?????????")
    @Transactional
    public void join() throws Exception {
        MemberRequest memberRequest = MemberRequest.builder()
                .username("wilgur513")
                .name("?????????")
                .password("pass")
                .type("SOLDIER")
                .build();

        mockMvc.perform(post("/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberRequest))
                )
                .andDo(print())
                .andDo(document("join-member",
                        links(
                                linkWithRel("login").description("????????? URI"),
                                linkWithRel("profile").description("profile URI")
                        ),
                        requestFields(
                                fieldWithPath("username").description("????????? ?????????"),
                                fieldWithPath("name").description("????????? ??????"),
                                fieldWithPath("password").description("????????? ????????????"),
                                fieldWithPath("type").description("????????? ??????(SOLDIER)")
                        ),
                        responseFields(
                                fieldWithPath("id").description("????????? ??????"),
                                fieldWithPath("username").description("????????? ?????????"),
                                fieldWithPath("name").description("????????? ??????"),
                                fieldWithPath("type").description("????????? ??????(??????[SOLDIER])"),
                                fieldWithPath("_links.login.href").description("????????? URI"),
                                fieldWithPath("_links.profile.href").description("profile URI")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("???????????? ?????? ???????????? ????????? ??? ?????? ?????????")
    public void unauthorizedLogin() throws Exception {
        LoginRequest request = new LoginRequest("wilgur513", "pass");
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("??? ????????? ???????????? ??? ?????? ?????????")
    public void emptyMemberInfoJoin() throws Exception{
        MemberRequest request = MemberRequest.builder().build();

        mockMvc.perform(post("/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }
}
