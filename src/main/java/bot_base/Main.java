package bot_base;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main
{
    /// <h2>Add your bot's data here</h2>
    private static final String
            bot_username = "",
            bot_token = "";

    public static void main(String[] args) throws TelegramApiException
    {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new TelegramBot(bot_username, bot_token));
    }
}