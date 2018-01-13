package com.example.SecurityDemo.validation;

import org.passay.*;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String>
{
    @Override
    public void initialize(final ValidPassword arg0) {

    }

    @Override
    public boolean isValid(final String password, final ConstraintValidatorContext context){
        final PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8,30),
                new CharacterRule(EnglishCharacterData.UpperCase,1),
                new CharacterRule(EnglishCharacterData.Digit,1),
                new CharacterRule(EnglishCharacterData.Special,1)
        ));

        final RuleResult result = validator.validate(new PasswordData(password));
        if(result.isValid()){
            return true;
        }

        context.disableDefaultConstraintViolation(); //prevent return of default constraint violation
        String template = String.join(",",validator.getMessages(result));
        String defaultMessage = "Password too weak (minimum 8 characters, 1 uppercase, 1 number and 1 special)";
        context.buildConstraintViolationWithTemplate(defaultMessage).addConstraintViolation();
        return false;
    }
}
