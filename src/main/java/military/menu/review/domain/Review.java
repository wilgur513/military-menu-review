package military.menu.review.domain;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
public class Review {
    @Id @GeneratedValue @Column(name="review_id")
    private Long id;
    private String content;
    private LocalDate created;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="meal_id")
    private Meal meal;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="member_id")
    private Member member;

    protected Review() {}

    private Review(String content, LocalDate created, Meal meal, Member member) {
        this.content = content;
        this.created = created;
        this.meal = meal;
        this.member = member;
    }

    public static Review of(String content, LocalDate created, Meal meal, Member member) {
        return new Review(content, created, meal, member);
    }
}