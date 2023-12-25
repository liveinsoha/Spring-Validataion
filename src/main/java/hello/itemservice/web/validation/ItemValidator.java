package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Slf4j
@Component
public class ItemValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
        /**
         * 주어진 코드 Item.class.isAssignableFrom(clazz);는 Java에서 사용되는 표현으로,
         * clazz 변수가 Item 클래스와 동일하거나 해당 클래스의 하위 클래스 또는 구현 클래스인지를 확인합니다.
         */
    }

    @Override
    public void validate(Object target, Errors errors) {

        Item item = (Item) target;
        BindingResult bindingResult = (BindingResult) errors;

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

