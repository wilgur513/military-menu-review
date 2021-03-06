package military.menu.review.security;

import military.menu.review.domain.member.Member;
import military.menu.review.domain.member.MemberAdapter;
import military.menu.review.domain.member.MemberRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class JwtTokenCheckFilter extends BasicAuthenticationFilter {
    private final JWTUtils jwtUtils;
    private final MemberRepository memberRepository;

    public JwtTokenCheckFilter(AuthenticationManager authenticationManager, JWTUtils jwtUtils, MemberRepository memberRepository) {
        super(authenticationManager);
        this.jwtUtils = jwtUtils;
        this.memberRepository = memberRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = request.getHeader(JWTUtils.HEADER);

        if(token == null || !token.startsWith(JWTUtils.BEARER)) {
            chain.doFilter(request, response);
            return;
        }

        VerifyResult result = jwtUtils.verify(token.substring(JWTUtils.BEARER.length()));

        if(result.isVerified()) {
            Member member = memberRepository.findByUsername(result.getUsername());
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority(member.getMemberType().name()));

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    new MemberAdapter(member), null, authorities
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }else {
            SecurityContextHolder.getContext().setAuthentication(null);
        }

        chain.doFilter(request, response);
    }
}
