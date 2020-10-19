import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.util.List;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;

public class Main {
    private CosmosClient client;

    private CosmosDatabase database;

    private final String databaseName = "test-kcdb";

    public void close() {
        client.close();
    }

    public static void main(String[] args) {
        final String accountHost = System.getProperty("ACCOUNT_HOST", "").trim();
        final String accountKey = System.getProperty("ACCOUNT_KEY", "").trim();

        if (accountKey.isEmpty()) {
            System.err.println("ACCOUNT_KEY is not set");
            return;
        } else if (accountHost.isEmpty()) {
            System.err.println("ACCOUNT_HOST is not set");
            return;
        }

        Main p = new Main();

        try {
            p.getStartedDemo(accountHost, accountKey);
            System.out.println("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            System.err.printf("DocumentDB GetStarted failed with %s", e);
        } finally {
            System.out.println("close the client");
            p.close();
        }
    }

    private void getStartedDemo(String accountHost, String accountKey) throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + accountHost);

        client = new CosmosClientBuilder()
            .endpoint(accountHost)
            .key(accountKey)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildClient();

        database = client.getDatabase(databaseName);

        // Demo CosmosDB Pagination
        queryDocumentsByPage();

        // Demo Querying a Document with a list saved into Cache which can be used sliced in UI code for pagination
        executeSimpleQueryWithList();
    }

    private void queryDocumentsByPage() {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

        String collectionName = "volcanoCollection";
        String containerSql = String.format("SELECT * from c where c.id = '%s'", collectionName);
        System.out.println(containerSql);
        String itemSql = String.format("SELECT * FROM %s", collectionName);
        System.out.println(itemSql);

        CosmosPagedIterable<CosmosContainerProperties> results = database.queryContainers(containerSql, queryOptions);

        // Iterating through all the results.
        // If there are more than a single page of results, it will auto-magically fetch the next page.
        for (CosmosContainerProperties cosmosContainerProperties : results) {
            String id = cosmosContainerProperties.getId();
            CosmosContainer container = database.getContainer(id);
            container.queryItems(itemSql, queryOptions, TestObject.class).forEach(item -> {
                System.out.println(item.toString());
            });
            System.out.println(id);
        }
    }

    private void executeSimpleQueryWithList() {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

        String collectionName = "ListItemDetailsCollection";
        String containerSql = String.format("SELECT * from c where c.id = '%s'", collectionName);
        System.out.println(containerSql);
        String itemSql = String.format("SELECT * FROM %s", collectionName);
        System.out.println(itemSql);

        CosmosPagedIterable<CosmosContainerProperties> results = database.queryContainers(containerSql, queryOptions);

        // 1. Create a cache manager
        CacheManager cm = CacheManager.getInstance();

        // 2. Get a cache called "listDocCache" defined in config
        Cache cache = cm.getCache("listDocCache");

        // Iterating through all the results.
        // If there are more than a single page of results, it will auto-magically fetch the next page.
        for (CosmosContainerProperties containerProperties : results) {
            String id = containerProperties.getId();
            CosmosContainer container = database.getContainer(id);
            container.queryItems(itemSql, queryOptions, TestObject.class).forEach(item -> {
                List<ItemDetail> listItemDetails = item.getListItemDetails();
                System.out.println(listItemDetails);
                for (int i = 0; i < listItemDetails.size(); i++) {
                    ItemDetail itemDetailObject = listItemDetails.get(i);
                    // 4. Put elements in cache
                    cache.put(new Element(i, itemDetailObject));
                    System.out.println("item detail: " + itemDetailObject.toString());
                }
            });
            System.out.println(id);
        }

        System.out.println("Elements in the cache " + cache.getKeys().toString());
        Element ele = cache.get(0);

        // 5. Print out the element
        String output = (ele == null ? null : ele.getObjectValue().toString());
        System.out.println("Element in the cache " + output);

        // 6. Is key in cache?
        System.out.println(cache.isKeyInCache(0)); // true
        System.out.println(cache.isKeyInCache(7)); // false

        // This cache can be used in UI with slicing for pagination

        // 7. shut down the cache manager
        cm.shutdown();
    }
}
