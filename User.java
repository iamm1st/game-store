import java.io.Serializable;
// ФАБРИЧНЫЙ МЕТОД - порождающий, для организации удобного безопасного создания объектов
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private int birthYear;
    private boolean isBlocked; // Флаг блокировки
    private User(String name, int birthYear) {
        this.name = name;
        this.birthYear = birthYear;
        this.isBlocked = false;}
    public String getName() {
        return name;}
    public int getBirthYear() {
        return birthYear;}
    public boolean isBlocked() {
        return isBlocked;}
    public void setBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
    @Override
    public String toString() {
        return "Имя: " + name + ", Год рождения: " + birthYear;}
    // Static вложенный класс фабрики
    public static class UserFactory {
        public static User createUser(String name, int birthYear) {

            return new User(name, birthYear);}}}
