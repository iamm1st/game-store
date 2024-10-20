import java.util.concurrent.atomic.AtomicBoolean;
import java.util.*;

class SortDescendingTask extends Thread {
    private final Store store;
    private final List<Game> games;
    private final AtomicBoolean isCancelled;

    public SortDescendingTask(Store store, List<Game> games, AtomicBoolean isCancelled) {
        this.store = store;
        this.games = games;
        this.isCancelled = isCancelled;
    }

    @Override
    public void run() {
        if (!isCancelled.get()) {
            Collections.sort(games, Comparator.comparingDouble(Game::getPrice).reversed());
            System.out.println("Игры отсортированы по убыванию цены:");
            store.displayFilteredGames(games);
        } else {
            System.out.println("Сортировка по убыванию была отменена.");
        }
    }
}