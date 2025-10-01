package bot_base;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelegramBot extends TelegramLongPollingBot
{
    String bot_username;
    ExecutorService executor;

    public TelegramBot(String bot_username, String bot_token)
    {
        super(bot_token);
        this.bot_username = bot_username;
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public String getBotUsername()
    {
        return bot_username;
    }

    @Override
    public void onUpdateReceived(Update update)
    {
        executor.submit(() -> handle_updates(update));
    }

    private void handle_updates (Update update)
    {
        System.out.println("Updates: " + update.getUpdateId());
        /*
        add your input handler code here
         */
    }
}
