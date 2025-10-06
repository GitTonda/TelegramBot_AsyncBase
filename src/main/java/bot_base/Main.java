package bot_base;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main
{
    public static void main(String[] args) throws TelegramApiException
    {
        try
        {
            DatabaseManager.get_instance().init();
            System.out.println("Database initialized successfully.");
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage());
        }

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new TelegramBot(ConfigLoader.get("BOT_USERNAME"), ConfigLoader.get("BOT_TOKEN")));
    }
}