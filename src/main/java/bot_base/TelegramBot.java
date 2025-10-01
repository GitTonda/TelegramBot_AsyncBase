package bot_base;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.concurrent.*;

public class TelegramBot extends TelegramLongPollingBot
{
    /**
     * <h3>Constants</h3>
     * <p>- <strong>THREAD_CAP</strong>: defines how many concurrent threads can exist at the same time<br>
     * - <strong>QUEUE_CAP</strong>: defines how many Update objs can fit into the queue at the same time</p>
     * <h3>Dynamic Variables</h3>
     * <p>- <strong>bot_username</strong>: the name given to the bot; default method of TelegramLongPollingBot<br>
     * - <strong>executor</strong>: the thread handler for async update processing<br>
     * - <strong>updates_queue</strong>: queue containing all updates received from the bot, waiting for processing</p>
     */
    private static final int THREAD_CAP = 50, QUEUE_CAP = 1000;
    String bot_username;
    ExecutorService executor;
    LinkedBlockingQueue<Update> updates_queue;

    /**
     *
     * @param bot_username name of the bot
     * @param bot_token telegram token of the bot
     *
     * @implNote constructor for TelegramBot: initializes the ExecutorService and the LinkedBlockingQueue
     */
    public TelegramBot(String bot_username, String bot_token)
    {
        super(bot_token);
        this.bot_username = bot_username;
        executor = Executors.newFixedThreadPool(THREAD_CAP);
        updates_queue = new LinkedBlockingQueue<>(QUEUE_CAP);

        // Preparing Threads
        executor.submit(() ->
        {
            while (!Thread.currentThread().isInterrupted())
            {
                try
                {
                    // if there is an update in the queue, it is forwarded to the update_handler;
                    // thread is blocked otherwise, until a new update is added to the queue
                    update_handler(updates_queue.take());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    @Override
    public String getBotUsername()
    {
        return bot_username;
    }

    /**
     *
     * @param update Update obj received from the bot
     *
     * @implNote add the update to the update_queue, for the executor to process
     */
    @Override
    public void onUpdateReceived(Update update) {
        try {
            updates_queue.put(update);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     *
     * @param update Update received from the Bot
     *
     * @implNote at this point in the code the update is ready to be handled
     */
    private void update_handler(Update update)
    {
        System.out.println("Updates: " + update.getUpdateId());
        // add your input handler code here
    }
}
