package military.menu.review.domain;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
@Table(name="Likes")
public class Like {
    @Id @GeneratedValue @Column(name="like_id")
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, cascade = CascadeType.ALL) @JoinColumn(name="member_id")
    private Member member;
    @ManyToOne(fetch=FetchType.LAZY, cascade = CascadeType.ALL) @JoinColumn(name="menu_id")
    private Menu menu;
    @Embedded
    private Week week;

    protected Like() {}

    private Like(Member member, Menu menu, Week week) {
        this.member = member;
        this.menu = menu;
        this.week = week;
    }

    public static Like of(Member member, Menu menu, Week week) {
        return new Like(member, menu, week);
    }
}