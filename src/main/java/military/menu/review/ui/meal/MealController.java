package military.menu.review.ui.meal;

import lombok.RequiredArgsConstructor;
import military.menu.review.domain.meal.MealDao;
import military.menu.review.domain.meal.MealDto;
import military.menu.review.domain.member.Member;
import military.menu.review.security.CurrentMember;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/meals")
public class MealController {
    private final MealDao mealDao;
    private final WeekValidator weekValidator;

    @InitBinder("weekRequest")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(weekValidator);
    }

    @GetMapping
    public ResponseEntity meals(@Valid @ModelAttribute WeekRequest weekRequest, @CurrentMember Member member, Errors errors) {
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        List<MealDto> mealsDto = mealDao.selectByDateBetweenWithIsLiked(weekRequest.firstDate(), weekRequest.lastDate(), member);
        MealsResponse response = new MealsResponse(mealsDto, weekRequest);
        response.add(Link.of("/docs/index.html#resources-query-meals").withRel("profile"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity meal(@PathVariable Long id, @CurrentMember Member member) {
        MealDto mealDto = mealDao.selectByIdWithIsLiked(id, member);
        if(mealDto == null) {
            return ResponseEntity.notFound().build();
        }
        MealResponse response = new MealResponse(mealDto);
        WeekRequest weekRequest = WeekRequest.from(mealDto.getDate());
        response.add(Link.of(String.format("/meals?year=%d&month=%d&week=%d", weekRequest.getYear(), weekRequest.getMonth(), weekRequest.getWeek())).withRel("meals"));
        response.add(Link.of("docs/index.html#query-meal").withRel("profile"));
        return ResponseEntity.ok(response);
    }
}