import java.io.Serializable;
class Game implements Serializable {
    private static final long serialVersionUID = 1L; //  уник.  идентификатор для сериализации
    private String name;
    private int year;
    private double price;

    public Game(String name, int year, double price) {
        this.name = name;
        this.year = year;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Игра: " + name + ", Год: " + year + ", Цена: " + price;
    }
}