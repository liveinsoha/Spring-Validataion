package hello.itemservice.domain.item;

import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.ScriptAssert;
//hibernate가 붙어 있으면 hibernate구현체에서 동작한다.
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
//jakarta는 Bean Validation이 표준적으로 제공하는 것이다. 어떤 구현체에서도 동작한다.

/**
 * javax.validation` 으로 시작하면 특정 구현에 관계없이 제공되는 표준 인터페이스이고,
 * `org.hibernate.validator` 로 시작하면 하이버네이트 validator 구현체를 사용할 때만 제공되는 검증 기
 * 능이다. 실무에서 대부분 하이버네이트 validator를 사용하므로 자유롭게 사용해도 된다.
 */

@Data
//@ScriptAssert(lang = "javascript", script = "_this.price * _this.quantity >= 10000")
public class Item {

    @NotNull(groups = updateCheck.class)
    private Long id;

    @NotBlank(groups = {updateCheck.class, saveCheck.class})
    private String itemName;

    @NotNull(groups = {updateCheck.class, saveCheck.class})
    @Range(min = 1000, max = 1000000, groups = {updateCheck.class, saveCheck.class})
    private Integer price;

    @NotNull(groups = {updateCheck.class, saveCheck.class})
    @Max(value = 9999, groups = saveCheck.class)
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }

    /**
     * 스프링 부트가 `spring-boot-starter-validation` 라이브러리를 넣으면 자동으로 Bean Validator를 인지하고 스프링에 통합한다.
     * **스프링 부트는 자동으로 글로벌 Validator로 등록한다.**
     * `LocalValidatorFactoryBean` 을 글로벌 Validator로 등록한다. 이 Validator는 `@NotNull` 같은 애노테이션을
     * 보고 검증을 수행한다. 이렇게 글로벌 Validator가 적용되어 있기 때문에, `@Valid` , `@Validated` 만 적용하면 된다.
     * 검증 오류가 발생하면, `FieldError` , `ObjectError` 를 생성해서 `BindingResult` 에 담아준다.
     */

    /**
     * `NotBlank` 라는 오류 코드를 기반으로 `MessageCodesResolver` 를 통해 다양한 메시지 코드가 순서대로 생성된다.
     * **@NotBlank**
     * NotBlank.item.itemName
     * NotBlank.itemName
     * NotBlank.java.lang.String
     * NotBlank
     */
}
