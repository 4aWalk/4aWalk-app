package fr.iutrodez.a4awalk.utils.validators;

public class PoiValidator {

    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        private final Double latitude;
        private final Double longitude;

        private ValidationResult(boolean isValid, String errorMessage, Double latitude, Double longitude) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public static ValidationResult success(double lat, double lon) {
            return new ValidationResult(true, null, lat, lon);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message, null, null);
        }

        public boolean isValid() { return isValid; }
        public String getErrorMessage() { return errorMessage; }
        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
    }

    /**
     * Valide les données d'entrée d'un POI.
     *
     * @param nom Le nom du POI (String)
     * @param latStr La latitude (String)
     * @param lonStr La longitude (String)
     * @return ValidationResult contenant l'état, le message d'erreur éventuel et les coordonnées parsées.
     */
    public static ValidationResult valider(String nom, String latStr, String lonStr) {
        // 1. Validation du nom
        if (nom == null || nom.trim().isEmpty()) {
            return ValidationResult.error("Le nom du POI ne peut pas être vide.");
        }

        double latitude;
        double longitude;

        // 2. Validation de la latitude
        try {
            latitude = Double.parseDouble(latStr.trim());
            if (latitude < -90.0 || latitude > 90.0) {
                return ValidationResult.error("La latitude doit être comprise entre -90 et 90.");
            }
        } catch (NumberFormatException e) {
            return ValidationResult.error("Le format de la latitude est incorrect.");
        }

        // 3. Validation de la longitude
        try {
            longitude = Double.parseDouble(lonStr.trim());
            if (longitude < -180.0 || longitude > 180.0) {
                return ValidationResult.error("La longitude doit être comprise entre -180 et 180.");
            }
        } catch (NumberFormatException e) {
            return ValidationResult.error("Le format de la longitude est incorrect.");
        }

        // Tout est valide
        return ValidationResult.success(latitude, longitude);
    }
}