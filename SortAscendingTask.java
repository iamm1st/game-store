import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

//возраст.
class SortAscendingTask implements Runnable {
    private List<Game> games;
    private AtomicBoolean isCancelled;
    private Store store;

    public SortAscendingTask(Store store, List<Game> games, AtomicBoolean isCancelled) {
        this.store = store;
        this.games = games;
        this.isCancelled = isCancelled;
    }

    @Override
    public void run() {
        if (isCancelled.get()) {
            System.out.println("Сортировка по возрастанию отменена.");
            return;
        }

        Collections.sort(games, Comparator.comparingDouble(Game::getPrice));

        System.out.println("Сортировка по возрастанию завершена.");
        store.displaySortedGames(games);
    }
}
