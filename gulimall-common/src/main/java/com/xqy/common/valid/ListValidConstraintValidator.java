package com.xqy.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * 列出有效约束验证器
 *
 * @author xqy
 * @date 2022/12/17
 */
public class ListValidConstraintValidator implements ConstraintValidator<ListValue , Integer> {
    
    private Set<Integer> set = new HashSet<>();

    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] values = constraintAnnotation.values();
        for (int value:values) {
            set.add(value);
        }
    }


    /**
     * 是有效
     *
     * @param value                    需要校验的值
     * @param constraintValidatorContext 约束验证器上下文
     * @return boolean
     */
    @Override
    public boolean isValid(Integer value,ConstraintValidatorContext constraintValidatorContext) {

        return (set.contains(value));
    }
}
