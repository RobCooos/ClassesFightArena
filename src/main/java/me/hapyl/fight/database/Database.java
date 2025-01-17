package me.hapyl.fight.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.hapyl.fight.Main;
import me.hapyl.spigotutils.module.util.DependencyInjector;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * I really don't know how a database works or should work;
 * it's my first time working with mongodb.
 * <p>
 * Just using my knowledge of yml files, that's said,
 * I'm not sure if this is the best way to do it.
 * <p>
 * But to document:
 * {@link Database} is the main class that handles the connection to the database.
 * {@link PlayerDatabaseEntry} is an instance of database value, that handles specific fields.
 */
public class Database extends DependencyInjector<Main> {

    private final FileConfiguration config;
    private final NamedDatabase namedDatabase;

    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> players;
    private MongoCollection<Document> parkour;
    private MongoCollection<Document> heroStats;

    public Database(Main main) {
        super(main);
        this.config = main.getConfig();
        this.namedDatabase = NamedDatabase.byName(config.getString("database.type"));

        // Suppress logging
    }

    public NamedDatabase getNamedDatabase() {
        return namedDatabase;
    }

    public boolean isDevelopment() {
        return namedDatabase.isDevelopment();
    }

    public void stopConnection() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void createConnection() {
        try {
            final String connectionLink = config.getString("database.connection_link");

            if (connectionLink == null || connectionLink.equals("null")) {
                breakConnectionAndDisablePlugin("Provide a valid connection link in config.yml!");
                return;
            }

            try {
                client = MongoClients.create(connectionLink);
            } catch (Exception e) {
                getPlugin().getLogger().warning(connectionLink);
                breakConnectionAndDisablePlugin("Failed to connect to MongoDB! Invalid connection link?");
                return;
            }

            // load database
            database = client.getDatabase(namedDatabase.getName());

            getPlugin().getLogger().info(getDatabaseString());

            // load collections
            players = database.getCollection("players");
            parkour = database.getCollection("parkour");
            heroStats = database.getCollection("hero_stats");
        } catch (Exception e) {
            breakConnectionAndDisablePlugin("Failed to retrieve database collection!");
        }
    }

    public String getDatabaseString() {
        return "&a&lMONGO &fConnected Database: &6&l" + namedDatabase.name();
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoCollection<Document> getPlayers() {
        return players;
    }

    public MongoCollection<Document> getParkour() {
        return parkour;
    }

    public MongoCollection<Document> getHeroStats() {
        return heroStats;
    }

    private void breakConnectionAndDisablePlugin(String message) throws RuntimeException {
        getPlugin().getLogger().severe(message);
        Bukkit.getPluginManager().disablePlugin(getPlugin());

        throw new RuntimeException(message);
    }
}
