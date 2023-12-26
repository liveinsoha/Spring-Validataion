package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.saveCheck;
import hello.itemservice.domain.item.updateCheck;
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

import java.util.List;

@Controller
@RequestMapping("/validation/v3/items")
@RequiredArgsConstructor
@Slf4j
public class ValidationItemControllerV3 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator; //컴포넌트 스캔했고, 생성자 주입이 된다.


    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v3/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v3/addForm";
    }
    /**
     * Error가 발생할 경우에도 @ModelAttribute어노테이션이 item에 담긴 정보를 그대로 담아서 다시 뷰로 전달한다.
     * 사용자는 자신이 입력한 내용을 그대로 다시 볼 수 있다.
     */


    /**
     * `@Validated` 는 검증기를 실행하라는 애노테이션이다.
     * 이 애노테이션이 붙으면 앞서 `WebDataBinder` 에 등록한 검증기를 찾아서 실행한다. 그런데 여러 검증기를 등록한다
     * 면 그 중에 어떤 검증기가 실행되어야 할지 구분이 필요하다. 이때 `supports()` 가 사용된다. 여기서는
     * `supports(Item.class)` 호출되고, 결과가 `true` 이므로 `ItemValidator` 의 `validate()` 가 호출된다.
     */
    //@PostMapping("/add")
    public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("info log = {addItem}");

        //필드 에러가 아닌 ObjectError는 따로 검증 기능을 구현하자
        addObjectError(item, bindingResult);
        if (bindingResult.hasErrors()) { //부정의 부정은 리팩토링을 통해 이해하기 쉽게.
            log.info("bindingResult = {}", bindingResult);
            return "validation/v3/addForm";
        }
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }

    @PostMapping("/add")
    public String addItemV2(@Validated(saveCheck.class) @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("info log = {addItemV2}");

        //필드 에러가 아닌 ObjectError는 따로 검증 기능을 구현하자
        addObjectError(item, bindingResult);
        if (bindingResult.hasErrors()) { //부정의 부정은 리팩토링을 통해 이해하기 쉽게.
            log.info("bindingResult = {}", bindingResult);
            return "validation/v3/addForm";
        }
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }

    private static void addObjectError(Item item, BindingResult bindingResult) {
        if (item.getQuantity() != null && item.getPrice() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10_000) {
                bindingResult.reject("totalPriceMin", new Object[]{"10,000", resultPrice}, null);

                //ObjectError는 값이 넘어오거나 하는게 아니기 떄문에 바인딩 실패라는 건 없다. 다시 반환할 rejectedValue도 존재하지 않는다.
            }
        }
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/editForm";
    }

//    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute Item item, BindingResult bindingResult) {
        addObjectError(item, bindingResult);
        if (bindingResult.hasErrors()) {
            return "validation/v3/editForm";
        }
        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }

    @PostMapping("/{itemId}/edit")
    public String editV2(@PathVariable Long itemId, @Validated(updateCheck.class) @ModelAttribute Item item, BindingResult bindingResult) {
        addObjectError(item, bindingResult);
        if (bindingResult.hasErrors()) {
            return "validation/v3/editForm";
        }
        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }

}

