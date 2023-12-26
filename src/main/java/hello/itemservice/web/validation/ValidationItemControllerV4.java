package hello.itemservice.web.validation;

import hello.itemservice.domain.item.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
@Slf4j
public class ValidationItemControllerV4 {

    /**
     * **ITEM 원복**
     * 이제 `Item` 의 검증은 사용하지 않으므로 검증 코드를 제거해도 된다.
     */

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator; //컴포넌트 스캔했고, 생성자 주입이 된다.


    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v4/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v4/addForm";
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

    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("info log = {addItemV2}");

        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setPrice(form.getPrice());
        item.setQuantity(form.getQuantity());

        //필드 에러가 아닌 ObjectError는 따로 검증 기능을 구현하자
        addObjectError(item, bindingResult);
        if (bindingResult.hasErrors()) { //부정의 부정은 리팩토링을 통해 이해하기 쉽게.
            log.info("bindingResult = {}", bindingResult);
            return "validation/v4/addForm";
        }
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
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
        return "validation/v4/editForm";
    }


    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult) {

        /**
         * itemId정보는 쿼리파리미터 경로에서 @PathVariable로 받아서 가지고 있고,
         * ItemUpdateForm폼에도 저장되어 컨트롤러로 가지고 들어온다.
         */
        Item itemParam = new Item();
        itemParam.setItemName(form.getItemName());
        itemParam.setPrice(form.getPrice());
        itemParam.setQuantity(form.getQuantity());

        addObjectError(itemParam, bindingResult);
        if (bindingResult.hasErrors()) {
            return "validation/v4/editForm";
        }
        itemRepository.update(itemId, itemParam);
        return "redirect:/validation/v4/items/{itemId}";
    }

}

