package ar.com.padel.model.entity;

public enum Category {

    LIBRE(0, "Libre"),
    PRIMERA(1, "Primera"),
    SEGUNDA(2, "Segunda"),
    TERCERA(3, "Tercera"),
    CUARTA(4, "Cuarta"),
    QUINTA(5, "Quinta"),
    SEXTA(6, "Sexta"),
    SEPTIMA(7, "Séptima"),
    OCTAVA(8, "Octava"),
    SUMA_9(9, "Suma 9"),
    SUMA_10(10, "Suma 10"),
    SUMA_11(11, "Suma 11"),
    SUMA_12(12, "Suma 12"),
    SUMA_13(13, "Suma 13"),
    SUMA_14(14, "Suma 14"),
    SUMA_15(15, "Suma 15");

    private final int value;
    private final String description;

    Category(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    // Obtiene la categoría correspondiente a un valor entero.
    public static Category fromValue(int value) {
        for (Category category : Category.values()) {
            if (category.value == value) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid category value: " + value);
    }

    @Override
    public String toString() {
        return description;
    }
}