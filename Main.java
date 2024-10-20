import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.*;
import java.util.stream.Collectors;

//Фабричный - User, Singleton - Store
// Сохр. данных в Store
//Фильтры, сорт - сохраняться должны
//у каждого пользователя своя корзина, которая тоже должна сохраняться

/* в моем коде джава нужно будет сделать так, чтобы у каждого пользователя была своя корзина.
 (т.е. пользователь входит в меню пользователя, выбирает пункт 3 меню "Добавить игру в корзину" и добавляет игру. Тут без изменений.
 После чего, если выбрать пункт 4 меню "Выход" и снова зайти в меню пользователя, но уже с аккаунта другого пользователя, то у него должна быть своя корзина,
  изначально пустая, в которую он также будет добавлять игры). После этого мне необходимо, чтобы после завершения консоли ( выбор пункта 3 В главном меню)
и повторном запуске сохранялись еще и действия каждого пользователя (у каждого будут разные, очевидно). то есть примененные ими фильтры, сортировки и корзина соответственно. */

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Store store = Store.getInstance(); // SINGLETON (Store)
        Cart cart = new Cart();
        final String ADMIN_PASSWORD = "123";

        AtomicBoolean isAscendingSortApplied = new AtomicBoolean(false);
        AtomicBoolean isDescendingSortApplied = new AtomicBoolean(false);
        AtomicBoolean isSortCancelled = new AtomicBoolean(false);

        Map<Integer, Runnable> adminMenuActions = new HashMap<>();
        Map<Integer, Runnable> userMenuActions = new HashMap<>();
        Map<Integer, Runnable> mainMenuActions = new HashMap<>();

        // Администраторские действия
        adminMenuActions.put(1, () -> {
            System.out.print("Введите название игры: ");
            String name = scanner.nextLine();
            int year = getValidIntInput(scanner, "Введите год выпуска: ");
            double price = getValidDoubleInput(scanner, "Введите цену: ");
            scanner.nextLine();
            store.addGame(new Game(name, year, price));
            System.out.println("Игра успешно добавлена.");
        });

        adminMenuActions.put(2, () -> {
            System.out.print("Введите название игры для удаления: ");
            String name = scanner.nextLine();
            if (store.removeGame(name)) {
                System.out.println("Игра успешно удалена.");
            } else {
                System.out.println("Игра не найдена.");
            }
        });

        adminMenuActions.put(3, () -> {
            System.out.print("Введите название игры для редактирования: ");
            String name = scanner.nextLine();
            Game game = store.getGame(name);
            if (game != null) {
                int year = getValidIntInput(scanner, "Введите новый год выпуска (текущий: " + game.getYear() + "): ");
                double price = getValidDoubleInput(scanner, "Введите новую цену (текущая: " + game.getPrice() + "): ");
                game.setYear(year);
                game.setPrice(price);
                System.out.println("Игра успешно отредактирована.");
            } else {
                System.out.println("Игра не найдена.");
            }
        });

        adminMenuActions.put(4, store::displayGames);
        adminMenuActions.put(5, store::displayUsers); // Новый пункт для просмотра зарегистрированных пользователей
        adminMenuActions.put(6, () -> System.out.println("Выход из системы администратора."));
        adminMenuActions.put(7, () -> {
            System.out.print("Введите имя пользователя для блокировки: ");
            String userName = scanner.nextLine();
            int birthYear = getValidIntInput(scanner, "Введите год рождения пользователя: ");
            store.blockUser(userName, birthYear);
        });

        adminMenuActions.put(8, () -> {
            System.out.print("Введите имя пользователя для разблокировки: ");
            String userName = scanner.nextLine();
            int birthYear = getValidIntInput(scanner, "Введите год рождения пользователя: ");
            store.unblockUser(userName, birthYear);
        });
        // Действия пользователя
        userMenuActions.put(1, () -> {
            // Применение фильтров
            List<Game> filteredGames = store.getGames().stream()
                    .filter(game -> (store.getMaxPriceFilter() == null || game.getPrice() <= store.getMaxPriceFilter()) &&
                            (store.getMinYearFilter() == null || game.getYear() >= store.getMinYearFilter()))
                    .collect(Collectors.toList());

            // Применение сортировки
            if (isAscendingSortApplied.get()) {
                SortAscendingTask sortAscendingTask = new SortAscendingTask(store, filteredGames, isSortCancelled);
                Thread thread = new Thread(sortAscendingTask);
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else if (isDescendingSortApplied.get()) {
                SortDescendingTask sortDescendingTask = new SortDescendingTask(store, filteredGames, isSortCancelled);
                sortDescendingTask.start();
                try {
                    sortDescendingTask.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                store.displayFilteredGames(filteredGames);
            }
        });

        userMenuActions.put(2, () -> {
            System.out.print("Введите название игры для добавления в корзину: ");
            String name = scanner.nextLine();
            Game game = store.getGame(name);
            if (game != null) {
                cart.addGame(game);
                System.out.println("Игра добавлена в корзину.");
            } else {
                System.out.println("Игра не найдена.");
            }
        });

        userMenuActions.put(3, cart::displayCart);
        userMenuActions.put(4, () -> System.out.println("Выход из системы пользователя."));

        // Фильтры
        userMenuActions.put(5, () -> {
            while (true) {
                System.out.println("Меню фильтров:");
                System.out.println("1. Добавить фильтр по максимальной цене");
                System.out.println("2. Добавить фильтр по минимальному году выпуска");
                System.out.println("3. Удалить фильтр по максимальной цене");
                System.out.println("4. Удалить фильтр по минимальному году выпуска");
                System.out.println("5. Сбросить все фильтры");
                System.out.println("6. Вернуться в меню пользователя");

                int filterChoice = getValidIntInput(scanner, "Выберите опцию: ");
                switch (filterChoice) {
                    case 1 -> {
                        double maxPrice = getValidDoubleInput(scanner, "Введите максимальную цену: ");
                        store.setMaxPriceFilter(maxPrice);
                    }
                    case 2 -> {
                        int minYear = getValidIntInput(scanner, "Введите минимальный год выпуска: ");
                        store.setMinYearFilter(minYear);
                    }
                    case 3 -> store.clearMaxPriceFilter();
                    case 4 -> store.clearMinYearFilter();
                    case 5 -> store.clearFilters();
                    case 6 -> {
                        return;
                    }
                    default -> System.out.println("Некорректный выбор. Попробуйте снова.");
                }
            }
        });

        // Сортировка
        userMenuActions.put(6, () -> {
            while (true) {
                System.out.println("Меню сортировки игр:");
                System.out.println("1. Добавить сортировку по возрастанию");
                System.out.println("2. Добавить сортировку по убыванию");
                System.out.println("3. Отмена сортировки по возрастанию");
                System.out.println("4. Отмена сортировки по убыванию");
                System.out.println("5. Вернуться в меню пользователя");

                int sortChoice = getValidIntInput(scanner, "Выберите опцию: ");
                switch (sortChoice) {
                    case 1 -> {
                        isAscendingSortApplied.set(true);
                        isDescendingSortApplied.set(false);
                        isSortCancelled.set(false);
                        System.out.println("Сортировка по возрастанию добавлена.");
                    }
                    case 2 -> {
                        isDescendingSortApplied.set(true);
                        isAscendingSortApplied.set(false);
                        isSortCancelled.set(false);
                        System.out.println("Сортировка по убыванию добавлена.");
                    }
                    case 3 -> {
                        isAscendingSortApplied.set(false);
                        isSortCancelled.set(true);
                        System.out.println("Сортировка по возрастанию отменена.");
                    }
                    case 4 -> {
                        isDescendingSortApplied.set(false);
                        isSortCancelled.set(true);
                        System.out.println("Сортировка по убыванию отменена.");
                    }
                    case 5 -> {
                        return;
                    }
                    default -> System.out.println("Некорректный выбор. Попробуйте снова.");
                }
            }
        });

        // Действия главного меню
        mainMenuActions.put(1, () -> {
            System.out.print("Введите пароль: ");
            String password = scanner.nextLine();
            if (ADMIN_PASSWORD.equals(password)) {
                while (true) {
                    System.out.println("Меню администратора:");
                    System.out.println("1. Добавить игру");
                    System.out.println("2. Удалить игру");
                    System.out.println("3. Редактировать игру");
                    System.out.println("4. Показать игры");
                    System.out.println("5. Зарегистрированные пользователи");
                    System.out.println("6. Выход");
                    System.out.println("7. Заблокировать пользователя");
                    System.out.println("8. Разблокировать пользователя");

                    int adminChoice = getValidIntInput(scanner, "Выберите опцию: ");
                    scanner.nextLine();

                    Runnable adminAction = adminMenuActions.get(adminChoice);
                    if (adminAction != null) {
                        adminAction.run();
                    } else {
                        System.out.println("Некорректный выбор. Попробуйте снова.");
                    }
                    if (adminChoice == 6) {
                        break;
                    }
                }
            } else {
                System.out.println("Неверный пароль. Возвращение в главное меню.");
            }
        });

        mainMenuActions.put(2, () -> {
            System.out.print("Вы уже зарегистрированы? (да/нет): ");
            String isRegistered = scanner.nextLine();

            if (isRegistered.equalsIgnoreCase("да")) {
                System.out.print("Введите ваше имя: ");
                String name = scanner.nextLine();
                int birthYear = getValidIntInput(scanner, "Введите ваш год рождения: ");
                scanner.nextLine(); // очистка

                User user = store.findUser(name, birthYear);
                if (user != null) {
                    if (user.isBlocked()) {
                        System.out.println("Упс! Кажется, вы заблокированы и не можете войти.");
                        return; // возвращаемся в главное меню
                    } else {
                        System.out.println("Добро пожаловать, " + name + "!");
                        userMenu(scanner, store, userMenuActions, cart, isAscendingSortApplied, isDescendingSortApplied, isSortCancelled);
                    }
                } else {
                    System.out.println("Неправильное имя или год рождения.");
                }
            } else if (isRegistered.equalsIgnoreCase("нет")) {
                System.out.print("Введите ваше имя: ");
                String name = scanner.nextLine();
                int birthYear = getValidIntInput(scanner, "Введите ваш год рождения: ");
                scanner.nextLine(); // очистка

                // ФАБРИЧНЫЙ МЕТОД - порождающий, для организации удобного безопасного создания объектов
                User newUser = User.UserFactory.createUser(name, birthYear);
                store.addUser(newUser);
                System.out.println("Регистрация успешна! Добро пожаловать, " + name + "!");
                userMenu(scanner, store, userMenuActions, cart, isAscendingSortApplied, isDescendingSortApplied, isSortCancelled);
            } else {
                System.out.println("Некорректный ответ. Возвращение в главное меню.");
            }
        });


        mainMenuActions.put(3, () -> System.out.println("Выход из программы."));

        // Главный цикл
        while (true) {
            System.out.println("Главное меню:");
            System.out.println("1. Войти как администратор");
            System.out.println("2. Войти как пользователь");
            System.out.println("3. Выход");

            int mainChoice = getValidIntInput(scanner, "Выберите опцию: ");
            scanner.nextLine();

            Runnable mainAction = mainMenuActions.get(mainChoice);
            if (mainAction != null) {
                mainAction.run();
            } else {
                System.out.println("Некорректный выбор. Попробуйте снова.");
            }
            if (mainChoice == 3) {
                store.exit();
                break;
            }
        }
    }

    private static void userMenu(Scanner scanner, Store store, Map<Integer, Runnable> userMenuActions, Cart cart,
                                 AtomicBoolean isAscendingSortApplied, AtomicBoolean isDescendingSortApplied, AtomicBoolean isSortCancelled) {
        while (true) {
            System.out.println("Меню пользователя:");
            System.out.println("1. Просмотреть игры");
            System.out.println("2. Добавить игру в корзину");
            System.out.println("3. Просмотреть корзину");
            System.out.println("4. Выход");
            System.out.println("5. Фильтр игр");
            System.out.println("6. Сортировка игр");


            int userChoice = getValidIntInput(scanner, "Выберите опцию: ");
            scanner.nextLine();

            Runnable userAction = userMenuActions.get(userChoice);
            if (userAction != null) {
                userAction.run();
            } else {
                System.out.println("Некорректный выбор. Попробуйте снова.");
            }
            if (userChoice == 4) {
                break;
            }
        }
    }

    private static int getValidIntInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                return value;
            } else {
                System.out.println("Некорректный ввод. Пожалуйста, введите целое число.");
                scanner.next(); // очистка неверного ввода
            }
        }
    }

    private static double getValidDoubleInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextDouble()) {
                double value = scanner.nextDouble();
                return value;
            } else {
                System.out.println("Некорректный ввод. Пожалуйста, введите число.");
                scanner.next(); // очистка неверного ввода
            }
        }
    }
}