package com.yourplugin.quests.database;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.yourplugin.quests.model.PlayerQuestData;
import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerDataRepository {

    private final MongoManager mongoManager;
    private final String collectionName = "player_data";

    public PlayerDataRepository(MongoManager mongoManager) {
        this.mongoManager = mongoManager;
    }

    public CompletableFuture<PlayerQuestData> loadData(UUID uuid) {
        CompletableFuture<PlayerQuestData> future = new CompletableFuture<>();
        MongoCollection<Document> collection = mongoManager.getCollection(collectionName);

        collection.find(Filters.eq("uuid", uuid.toString())).first().subscribe(new Subscriber<Document>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(Document document) {
                future.complete(deserialize(document));
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                if (!future.isDone()) {
                    future.complete(new PlayerQuestData(uuid));
                }
            }
        });

        return future;
    }

    public CompletableFuture<Void> saveData(PlayerQuestData data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        MongoCollection<Document> collection = mongoManager.getCollection(collectionName);

        Document document = serialize(data);
        ReplaceOptions options = new ReplaceOptions().upsert(true);

        collection.replaceOne(Filters.eq("uuid", data.getUuid().toString()), document, options).subscribe(new Subscriber<UpdateResult>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(UpdateResult updateResult) {
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                future.complete(null);
            }
        });

        return future;
    }

    private Document serialize(PlayerQuestData data) {
        Document document = new Document();
        document.put("uuid", data.getUuid().toString());

        Document progressDoc = new Document();
        for (Map.Entry<String, Integer> entry : data.getQuestProgress().entrySet()) {
            progressDoc.put(entry.getKey(), entry.getValue());
        }
        document.put("questProgress", progressDoc);
        document.put("completedQuests", data.getCompletedQuests());

        return document;
    }

    private PlayerQuestData deserialize(Document document) {
        UUID uuid = UUID.fromString(document.getString("uuid"));

        Map<String, Integer> questProgress = new HashMap<>();
        Document progressDoc = document.get("questProgress", Document.class);
        if (progressDoc != null) {
            for (String key : progressDoc.keySet()) {
                questProgress.put(key, progressDoc.getInteger(key));
            }
        }

        Set<String> completedQuests = new HashSet<>();
        List<String> completedList = document.getList("completedQuests", String.class);
        if (completedList != null) {
            completedQuests.addAll(completedList);
        }

        return new PlayerQuestData(uuid, questProgress, completedQuests);
    }
}
