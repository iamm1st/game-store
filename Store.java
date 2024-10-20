import java.util.*;
import java.io.*;

public class Store {
    private static Store instance;
    private List<Game> games;
    private List<User> users = new ArrayList<>();
    private Double maxPriceFilter = null;
    private Integer minYearFilter = null;

    /* СТРУКТУРА ПАТТЕРНА: Приватный конструктор — предотвращает создание объектов извне.
                           Статическое поле — для хранения единственного экземпляра класса.
                           Статический метод — для получения доступа к этому экземпляру. */
    private Store() { // конструктор
        games = new ArrayList<>();
        loadData();
    }
    public static synchronized Store getInstance() { //  метод для получения доступа к экземпляру. возвр. единственный экземпляр класса
        if (instance == null) { // поле instance хранит единств. экземпляр класса
            instance = new Store();
        }
        return instance;
    }

    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("store_data.ser"))) {
            oos.writeObject(games);
            oos.writeObject(users);
            System.out.println("Данные успешно сохранены.");
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        File file = new File("store_data.ser");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                games = (List<Game>) ois.readObject();
                users = (List<User>) ois.readObject();
                System.out.println("Данные успешно загружены.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Ошибка при загрузке данных: " + e.getMessage());
            }
        } else {
            games = new ArrayList<>(); // Создаем новый список, если файл не найден
        }
    }

    public void exit() {
        saveData();
    }
    public void addGame(Game game) {
        games.add(game);
    }

    public void displaySortedGames(List<Game> sortedGames) {
        for (Game game : sortedGames) {
            System.out.println("Название: " + game.getName() + ", Год выпуска: " + game.getYear() + ", Цена: " + game.getPrice());
        }
    }

    public boolean removeGame(String name) {
        return games.removeIf(game -> game.getName().equalsIgnoreCase(name));
    }

    public Game getGame(String name) {
        return games.stream()
                .filter(game -> game.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void displayGames() {
        if (games.isEmpty()) {
            System.out.println("Нет доступных игр.");
            return;
        }
        System.out.println("Список игр:");
        games.forEach(System.out::println);
    }
    public void addUser(User user) {
        users.add(user);
    }

    public User findUser(String name, int birthYear) {
        return users.stream()
                .filter(user -> user.getName().equalsIgnoreCase(name) && user.getBirthYear() == birthYear)
                .findFirst()
                .orElse(null);
    }

    public void blockUser(String name, int birthYear) {
        User user = findUser(name, birthYear);
        if (user != null && !user.isBlocked()) {
            user.setBlocked(true);
            System.out.println("Пользователь " + name + " успешно заблокирован.");
        } else {
            System.out.println("Пользователь не найден или уже заблокирован.");
        }
    }

    public void unblockUser(String name, int birthYear) {
        User user = findUser(name, birthYear);
        if (user != null && user.isBlocked()) {
            user.setBlocked(false);
            System.out.println("Пользователь " + name + " успешно разблокирован.");
        } else {
            System.out.println("Пользователь не найден или уже разблокирован.");
        }
    }
    public void displayUsers() {
        if (users.isEmpty()) {
            System.out.println("Нет зарегистрированных пользователей.");
            return;
        }
        System.out.println("Список пользователей:");
        users.forEach(user -> {
            String status = user.isBlocked() ? "Заблокирован" : "Разблокирован";
            System.out.println(user + " - Статус: " + status);
        });
    }
    public void displayFilteredGames(List<Game> filteredGames) {
        if (filteredGames.isEmpty()) {
            System.out.println("Нет игр, соответствующих заданным критериям.");
            return;
        }
        System.out.println("Отфильтрованные игры:");
        filteredGames.forEach(System.out::println);
    }

    public List<Game> getGames() {
        return games;
    }

    public void setMaxPriceFilter(double maxPrice) {
        this.maxPriceFilter = maxPrice;
    }

    public void setMinYearFilter(int minYear) {
        this.minYearFilter = minYear;
    }

    public void clearMaxPriceFilter() {
        maxPriceFilter = null;
    }

    public void clearMinYearFilter() {
        minYearFilter = null;
    }

    public void clearFilters() {
        clearMaxPriceFilter();
        clearMinYearFilter();
    }

    public Double getMaxPriceFilter() {
        return maxPriceFilter;
    }

    public Integer getMinYearFilter() {
        return minYearFilter;
    }
}