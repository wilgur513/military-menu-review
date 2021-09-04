package military.menu.review.application.like;

import lombok.RequiredArgsConstructor;
import military.menu.review.application.like.exception.LikeIsAlreadyExistException;
import military.menu.review.application.like.exception.LikeIsNotExistException;
import military.menu.review.domain.like.Like;
import military.menu.review.domain.like.LikeRepository;
import military.menu.review.domain.member.Member;
import military.menu.review.domain.menu.Menu;
import military.menu.review.domain.menu.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final MenuRepository menuRepository;

    public Like like(Member member, Menu menu) {
        if(likeRepository.findByMemberIdAndMenuId(member.getId(), menu.getId()) != null) {
            throw new LikeIsAlreadyExistException(member.getId(), menu.getId());
        }

        Like like = Like.builder()
                .memberId(member.getId())
                .menuId(menu.getId())
                .dateTime(LocalDateTime.now())
                .build();
        menu.like();
        likeRepository.save(like);
        return like;
    }

    public void cancel(Member member, Menu menu) {
        if(likeRepository.findByMemberIdAndMenuId(member.getId(), menu.getId()) == null) {
            throw new LikeIsNotExistException(member.getId(), menu.getId());
        }

        menu.unlike();
        likeRepository.deleteByMemberIdAndMenuId(member.getId(), menu.getId());
    }
}
