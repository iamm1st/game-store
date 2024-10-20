import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Cart implements Serializable {
    private static final long serialVersionUID = 1L; // уник. идентификатор для сериализации
    private List<Game> games; // Список игр в корзине

    public Cart() {
        this.games = new ArrayList<>();
    }

    public void addGame(Game game) {
        games.add(game);
    }

    public void displayCart() {
        if (games.isEmpty()) {
            System.out.println("Корзина пуста.");
        } else {
            System.out.println("Игры в корзине:");
            for (Game game : games) {
                System.out.println(game);
            }
        }
    }
}