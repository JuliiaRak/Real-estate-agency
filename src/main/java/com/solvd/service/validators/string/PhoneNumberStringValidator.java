package com.solvd.service.validators.string;

import com.solvd.service.validators.Validator;

public class PhoneNumberStringValidator implements Validator<String> {
    private final Validator<String> validator;

    public PhoneNumberStringValidator() {
        this.validator = new BasicStringValidator();
    }

    public PhoneNumberStringValidator(Validator<String> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(String fieldName, String entity) {
        validator.validate(fieldName, entity);
        if (!entity.matches("^\\+?[0-9]{12}$")) {
            throw new IllegalArgumentException("The phone number does not match the pattern '+380980307445'");
        }
    }
}
