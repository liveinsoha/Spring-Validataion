package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/validation/v1/items")
@RequiredArgsConstructor
@Slf4j
public class ValidationItemControllerV1 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v1/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v1/addForm";
    }

    /**
     * Error가 발생할 경우에도 @ModelAttribute어노테이션이 item에 담긴 정보를 그대로 담아서 다시 뷰로 전달한다.
     * 사용자는 자신이 입력한 내용을 그대로 다시 볼 수 있다.
     */
    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes, Model model) {
        Map<String, String> errors = new HashMap<>();
        addValidation(item, errors);
        if (!errors.isEmpty()) { //부정의 부정은 리팩토링을 통해 이해하기 쉽게.
            log.info("errors = {}", errors);
            model.addAttribute("errors", errors);
            return "validation/v1/addForm";
        }
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v1/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v1/items/{itemId}";
    }

    private Map<String, String> addValidation(Item item, Map<String, String> errors) {

        if (!StringUtils.hasText(item.getItemName())) {
            errors.put("itemName", "이름은 필수입력입니다");
        }
        if (item.getPrice() == null || item.getPrice() < 1_000 || item.getPrice() > 100_000) {
            errors.put("price", "가격은 1,000원 이상 100,000원 이하여야 합니다");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0 || item.getQuantity() > 9999) {
            errors.put("quantity", "수량은 1개 이상 9999개 이하여야 합니다");
        }
        if (item.getQuantity() != null && item.getPrice() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10_000) {
                errors.put("global", "총 가격은 10000원 이상이어야 합니다. 현재값 : " + resultPrice);
            }
        }
        return errors;
    }

}

