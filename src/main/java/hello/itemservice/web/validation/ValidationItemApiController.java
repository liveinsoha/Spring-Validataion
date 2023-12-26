package hello.itemservice.web.validation;

import hello.itemservice.domain.item.ItemSaveForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/validation/api/items")
@Slf4j
public class ValidationItemApiController {


    @PostMapping("/add")
    public Object addItem(@RequestBody @Validated ItemSaveForm form, BindingResult bindingResult) {

        log.info("API 컨트롤러 호출");

        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return bindingResult.getAllErrors();
        }

        log.info("성공 로직");
        return form;
    }

    /**
     * HttpMessageConverter` 에서 요청 JSON을 `ItemSaveForm` 객체로 생성하는데 실패한다.
     * 이 경우는 `ItemSaveForm` 객체를 만들지 못하기 때문에 컨트롤러 자체가 호출되지 않고 그 전에 예외가 발생한다. 물
     * 론 Validator도 실행되지 않는다
     */
}
