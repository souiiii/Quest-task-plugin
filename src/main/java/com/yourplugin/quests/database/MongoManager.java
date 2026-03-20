package com.yourplugin.quests.database;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.yourplugin.quests.QuestsPlugin;
import org.bson.Document;

public class MongoManager {

    private final QuestsPlugin plugin;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoManager(QuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        String uri = this.plugin.getConfig().getString("mongodb.uri");
        String databaseName = this.plugin.getConfig().getString("mongodb.database");
        this.mongoClient = MongoClients.create(uri);
        this.database = this.mongoClient.getDatabase(databaseName);
    }

    public void disconnect() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
            this.mongoClient = null;
        }
    }

    public MongoCollection<Document> getCollection(String name) {
        return this.database.getCollection(name);
    }
}
