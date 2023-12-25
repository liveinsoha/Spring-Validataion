package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
@Slf4j
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator; //컴포넌트 스캔했고, 생성자 주입이 된다.


    @InitBinder
    public void init(WebDataBinder webDataBinder) {
        log.info("init Binder = {}", webDataBinder);
        webDataBinder.addValidators(itemValidator);
    }//컨트롤러가 호출 될 떄마다 자동으로 검증기를 webDataBinder에 넣어둔다.

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

    /**
     * Error가 발생할 경우에도 @ModelAttribute어노테이션이 item에 담긴 정보를 그대로 담아서 다시 뷰로 전달한다.
     * 사용자는 자신이 입력한 내용을 그대로 다시 볼 수 있다.
     */
    //@PostMapping("/add")
    public String addItem(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        itemValidator.validate(item, bindingResult);
        if (bindingResult.hasErrors()) { //부정의 부정은 리팩토링을 통해 이해하기 쉽게.
            log.info("bindingResult = {}", bindingResult);
            return "validation/v2/addForm";
        }
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    /**
     * `@Validated` 는 검증기를 실행하라는 애노테이션이다.
     * 이 애노테이션이 붙으면 앞서 `WebDataBinder` 에 등록한 검증기를 찾아서 실행한다. 그런데 여러 검증기를 등록한다
     * 면 그 중에 어떤 검증기가 실행되어야 할지 구분이 필요하다. 이때 `supports()` 가 사용된다. 여기서는
     * `supports(Item.class)` 호출되고, 결과가 `true` 이므로 `ItemValidator` 의 `validate()` 가 호출된다.
     */
    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
       log.info("info log = {addItemV6}");
        if (bindingResult.hasErrors()) { //부정의 부정은 리팩토링을 통해 이해하기 쉽게.
            log.info("bindingResult = {}", bindingResult);
            return "validation/v2/addForm";
        }
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

    private void addValidationV1(Item item, BindingResult bindingResult) {

        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", "이름은 반드시 입력"));
        }
        if (item.getPrice() == null || item.getPrice() < 1_000 || item.getPrice() > 100_000) {
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000원 이상 100,000원 이하여야 합니다"));
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0 || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", "수량은 1개 이상 9999개 이하여야 합니다"));
        }
        if (item.getQuantity() != null && item.getPrice() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10_000) {
                bindingResult.addError(new ObjectError("item", "총 가격은 10000원 이상이어야 합니다. 현재값 : " + resultPrice));
            }
        }
    }


    /**
     * BindingResult에 검증 오류를 적용하는 3가지 방법**
     * `@ModelAttribute` 의 객체에 타입 오류 등으로 바인딩이 실패하는 경우 스프링이 `FieldError` 생성해서
     * `BindingResult` 에 넣어준다.
     * 개발자가 직접 넣어준다.
     * `Validator` 사용 이것은 뒤에서 설명
     */
    private void addValidationV2(Item item, BindingResult bindingResult) {

        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),
                    false, null, null, "이름은 반드시 입력"));
        }

        //바인딩은 잘 되어서 데이터가 잘 들어왔으므로 bindingResult의 값은 false이이다
        if (item.getPrice() == null || item.getPrice() < 1_000 || item.getPrice() > 100_000) {
            bindingResult.addError(new FieldError("item", "price", item.getPrice(),
                    false, null, null, "가격은 1,000원 이상 100,000원 이하여야 합니다"));
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0 || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(),
                    false, null, null, "수량은 1개 이상 9999개 이하여야 합니다"));
        }
        if (item.getQuantity() != null && item.getPrice() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10_000) {
                bindingResult.addError(
                        new ObjectError("item", null, null,
                                "총 가격은 10000원 이상이어야 합니다. 현재값 : " + resultPrice));

                //ObjectError는 값이 넘어오거나 하는게 아니기 떄문에 바인딩 실패라는 건 없다. 다시 반환할 rejectedValue도 존재하지 않는다.
            }
        }
        /**
         * 사용자의 입력 데이터가 컨트롤러의 `@ModelAttribute` 에 바인딩되는 시점에 오류가 발생하면 모델 객체에 사용자
         * 입력 값을 유지하기 어렵다. 예를 들어서 가격에 숫자가 아닌 문자가 입력된다면 가격은 `Integer` 타입이므로 문자를
         * 보관할 수 있는 방법이 없다. 그래서 오류가 발생한 경우 사용자 입력 값을 보관하는 별도의 방법이 필요하다. 그리고 이
         * 렇게 보관한 사용자 입력 값을 검증 오류 발생시 화면에 다시 출력하면 된다.
         * `FieldError` 는 오류 발생시 사용자 입력 값을 저장하는 기능을 제공한다.
         * th:field="*{price}"`
         * 타임리프의 `th:field` 는 매우 똑똑하게 동작하는데, 정상 상황에는 모델 객체의 값을 사용하지만, 오류가 발생하면
         * `FieldError` 에서 보관한 값을 사용해서 값을 출력한다.
         */
    }

    private void addValidationV3(Item item, BindingResult bindingResult) {

        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),
                    false, new String[]{"required.item.itemName"}, null, null));
        }

        //바인딩은 잘 되어서 데이터가 잘 들어왔으므로 bindingResult의 값은 false이이다
        if (item.getPrice() == null || item.getPrice() < 1_000 || item.getPrice() > 100_000) {
            bindingResult.addError(new FieldError("item", "price", item.getPrice(),
                    false, new String[]{"range.item.price"}, new Object[]{"1,000", "1,000,000"}, null));
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0 || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(),
                    false, new String[]{"max.item.quantity"}, new Object[]{"9,999"}, null));
        }
        if (item.getQuantity() != null && item.getPrice() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10_000) {
                bindingResult.addError(
                        new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{"10,000", resultPrice},
                                null));

                //ObjectError는 값이 넘어오거나 하는게 아니기 떄문에 바인딩 실패라는 건 없다. 다시 반환할 rejectedValue도 존재하지 않는다.
            }
        }
        /**
         * 사용자의 입력 데이터가 컨트롤러의 `@ModelAttribute` 에 바인딩되는 시점에 오류가 발생하면 모델 객체에 사용자
         * 입력 값을 유지하기 어렵다. 예를 들어서 가격에 숫자가 아닌 문자가 입력된다면 가격은 `Integer` 타입이므로 문자를
         * 보관할 수 있는 방법이 없다. 그래서 오류가 발생한 경우 사용자 입력 값을 보관하는 별도의 방법이 필요하다. 그리고 이
         * 렇게 보관한 사용자 입력 값을 검증 오류 발생시 화면에 다시 출력하면 된다.
         * `FieldError` 는 오류 발생시 사용자 입력 값을 저장하는 기능을 제공한다.
         * th:field="*{price}"`
         * 타임리프의 `th:field` 는 매우 똑똑하게 동작하는데, 정상 상황에는 모델 객체의 값을 사용하지만, 오류가 발생하면
         * `FieldError` 에서 보관한 값을 사용해서 값을 출력한다.
         */
    }

    private void addValidationV4(Item item, BindingResult bindingResult) {

        log.info("bindingResult.getObjectName() = {}", bindingResult.getObjectName());
        /**
         * Item에 @Data어노테이션이 붙어있고 toSTring어노테이션도 붙어있어 출력시
         * bindingResult.getTarget() = Item(id=null, itemName=, price=null, quantity=null) 이게 나온다.
         */

        log.info("bindingResult.getTarget() = {}", bindingResult.getTarget());

        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.rejectValue("itemName", "required");
        }

        //바인딩은 잘 되어서 데이터가 잘 들어왔으므로 bindingResult의 값은 false이이다
        if (item.getPrice() == null || item.getPrice() < 1_000 || item.getPrice() > 100_000) {
            if (!StringUtils.hasText(item.getItemName())) {
                bindingResult.rejectValue("price", "range", new Object[]{"1000", "1000000"}, null);
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0 || item.getQuantity() > 9999) {
                bindingResult.rejectValue("quantity", "max", new Object[]{"9999"}, null);
            }
            if (item.getQuantity() != null && item.getPrice() != null) {
                int resultPrice = item.getPrice() * item.getQuantity();
                if (resultPrice < 10_000) {
                    bindingResult.reject("totalPriceMin", new Object[]{"10,000", resultPrice}, null);

                    //ObjectError는 값이 넘어오거나 하는게 아니기 떄문에 바인딩 실패라는 건 없다. 다시 반환할 rejectedValue도 존재하지 않는다.
                }
            }
        }
    }
}

