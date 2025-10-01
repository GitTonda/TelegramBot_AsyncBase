package bot_base;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.*;

public class TelegramBot extends TelegramLongPollingBot
{
    private static final int THREAD_CAP = 50, QUEUE_CAP = 1000;
    String bot_username;
    ExecutorService executor;
    LinkedBlockingQueue<Update> updates_queue;

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
                    handle_updates(updates_queue.take());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // set flag again
                }
            }
        });
    }

    @Override
    public String getBotUsername()
    {
        return bot_username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            updates_queue.put(update);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handle_updates (Update update)
    {
        System.out.println("Updates: " + update.getUpdateId());
        /*
        add your input handler code here
         */
    }
}
