package fr.iutrodez.a4awalk.GestionCompte;

public class User {
    private String nom;
    private String prenom;
    private int age;
    private String adresse;
    private String email;
    private String password;
    private String niveau;
    private String morphologie;

    // Constructeur
    public User(String nom, String prenom, int age, String adresse,
                String email, String password, String niveau, String morphologie) {
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.adresse = adresse;
        this.email = email;
        this.password = password;
        this.niveau = niveau;
        this.morphologie = morphologie;
    }

    // Getters
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public int getAge() { return age; }
    public String getAdresse() { return adresse; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getNiveau() { return niveau; }
    public String getMorphologie() { return morphologie; }
}
