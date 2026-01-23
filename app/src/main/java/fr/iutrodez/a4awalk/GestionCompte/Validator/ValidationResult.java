package fr.iutrodez.a4awalk.GestionCompte.Validator;

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
}
