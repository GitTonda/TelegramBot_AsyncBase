package bot_base;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.*;

/**
 * TODO add logging via SLF4J
 * TODO implement graceful shutdown (executor.shutdown() on SIGINT)
 */

public class TelegramBot extends TelegramLongPollingBot
{
    /**
     * <h3>Constants</h3>
     * <p>- <strong>THREAD_CAP</strong>: defines how many concurrent threads can exist at the same time<br>
     * - <strong>QUEUE_CAP</strong>: defines how many Update objs can fit into the queue at the same time<br>
     * - <strong>USER_COOLDOWN_MS</strong>: defines how many ms have to pass between an update and the next</p>
     * <h3>Dynamic Variables</h3>
     * <p>- <strong>bot_username</strong>: the name given to the bot; default method of TelegramLongPollingBot<br>
     * - <strong>executor</strong>: the thread handler for async update processing<br>
     * - <strong>updates_queue</strong>: queue containing all updates received from the bot, waiting for processing<br>
     * - <strong>user_locks</strong>: hashmap to map users to updates, so that only one update per user is being
     * processed at the time<br>
     * - <strong>async_executor</strong>: helper structure to allow async processes to run into the |update handler|</p>
     */
    private final int USER_COOLDOWN_MS;
    String bot_username;
    ExecutorService executor;
    LinkedBlockingQueue<Update> updates_queue;
    ConcurrentHashMap<Long, Object> user_locks;
    ConcurrentHashMap<Long, Long> last_update_time;
    ExecutorService async_executor; // or fixed


    /**
     *
     * @param bot_username name of the bot
     * @param bot_token    telegram token of the bot
     * @implNote constructor for TelegramBot: initializes the ExecutorService and the LinkedBlockingQueue
     */
    public TelegramBot(String bot_username, String bot_token)
    {
        // super constructor
        super(bot_token);

        // class constants
        final int THREAD_CAP = Integer.parseInt(ConfigLoader.get("THREAD_CAP"));
        final int QUEUE_CAP = Integer.parseInt(ConfigLoader.get("QUEUE_CAP"));
        USER_COOLDOWN_MS = Integer.parseInt(ConfigLoader.get("USER_COOLDOWN_MS"));

        // class variables
        this.bot_username = bot_username;
        executor = Executors.newFixedThreadPool(THREAD_CAP);
        updates_queue = new LinkedBlockingQueue<>(QUEUE_CAP);
        user_locks = new ConcurrentHashMap<>();
        last_update_time = new ConcurrentHashMap<>();
        async_executor = Executors.newCachedThreadPool();

        // Preparing Threads
        for (int i = 0; i < THREAD_CAP; i++)
        {
            executor.submit(() ->
            {
                while (!Thread.currentThread().isInterrupted())
                {
                    try
                    {
                        update_handler(updates_queue.take());
                    }
                    catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
    }

    @Override
    public String getBotUsername()
    {
        return bot_username;
    }

    /**
     *
     * @param update Update obj received from the bot
     * @implNote add the update to the update_queue for the executor to process
     */
    @Override
    public void onUpdateReceived(Update update)
    {
        try
        {
            // if the queue is full, every update waits 100 ms, then is dropped in favor of a new one
            if (!updates_queue.offer(update, 100, TimeUnit.MILLISECONDS))
                System.out.println("Dropped update: " + update.getUpdateId());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    /**
     *
     * @param update Update received from the Bot
     */
    private void update_handler(Update update)
    {
        // Check if update comes from user (just in case)
        Long user_id = get_user_id(update);
        if (user_id == null)
        {
            System.out.println("Unknown user, skipping update.");
            return;
        }

        // Rate-limiting check
        // for every update per user, if less than 1000 ms have passed, it's dropped
        long now = System.currentTimeMillis();
        long last = last_update_time.getOrDefault(user_id, 0L);
        if (now - last < USER_COOLDOWN_MS)
        {
            System.out.println("User " + user_id + " is sending updates too fast. Dropping update: " + update.getUpdateId());
            send_toast(update, "Clicked too fast. Slow down...");
            return;
        }
        last_update_time.put(user_id, now);

        // User lock
        // doesn't allow an update to be processed unless the previous one is complete
        Object lock = new Object();
        Object existing = user_locks.putIfAbsent(user_id, lock);
        if (existing != null)
        {
            System.out.println("User " + user_id + " already has an update in progress. Dropping update: " + update.getUpdateId());
            send_toast(update, "Update is being processed. Slow down...");
            return;
        }

        // Finally, processing user update
        // This structure makes the current thread wait for the update to be done processing, even if
        // it contains async thread creation
        CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
        {
            // TODO new UpdateHandler(this, update).handle;
        }, async_executor);
        try
        {
            future.get();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            user_locks.remove(user_id);
        }
    }

    /**
     *
     * @param update Update obj
     * @return the user_id extracted from the update
     */
    private Long get_user_id(Update update)
    {
        if (update.hasMessage()) return update.getMessage().getFrom().getId();
        if (update.hasCallbackQuery()) return update.getCallbackQuery().getFrom().getId();
        return null;
    }

    /**
     *
     * @param update Update obj
     * @param text   Message of the toast
     */
    private void send_toast(Update update, String text)
    {
        // If update is a callback query, we can send a toast to warn the user about something
        try
        {
            String callbackId = update.hasCallbackQuery() ? update.getCallbackQuery().getId() : null;
            if (callbackId != null)
            {
                AnswerCallbackQuery toast = new AnswerCallbackQuery();
                toast.setCallbackQueryId(callbackId);
                toast.setShowAlert(false);
                toast.setText(text);
                execute(toast);
            }
        }
        catch (TelegramApiException e)
        {
            throw new RuntimeException();
        }
    }
}