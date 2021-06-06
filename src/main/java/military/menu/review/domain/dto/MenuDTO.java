package military.menu.review.domain.dto;

import lombok.Getter;
import lombok.Setter;
import military.menu.review.domain.entity.Menu;

import java.util.Objects;

@Getter @Setter
public class MenuDTO {
    private Long id;
    private String name;
    private Double kcal;
    private Integer like;

    private MenuDTO() {}

    public MenuDTO(Menu menu) {
        this.id = menu.getId();
        this.name = menu.getName();
        this.kcal = menu.getKcal();
        this.like = menu.getLike();
    }

    public static MenuDTO of(String name, double kcal) {
        MenuDTO dto = new MenuDTO();
        dto.setName(name);
        dto.setKcal(kcal);
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuDTO menuDTO = (MenuDTO) o;
        return Objects.equals(name, menuDTO.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}