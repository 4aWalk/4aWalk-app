package fr.iutrodez.a4awalk.modeles.entites;

public class ValidationResult {
    public final boolean valid;
    public final String field;
    public final String message;
    public int age;

    public ValidationResult(boolean valid, String field, String message, int age) {
        this.valid = valid;
        this.field = field;
        this.message = message;
        this.age = age;
    }

    public boolean isValid() {
        return valid;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public int getAge() {
        return age;
    }
}
