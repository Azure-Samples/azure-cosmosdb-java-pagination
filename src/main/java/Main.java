import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.util.CosmosPagedIterable;


public class Main {
    private CosmosClient client;

    private CosmosDatabase database;

    private final String databaseName = "test-kcdb";

    public void close() {
        client.close();
    }

    public static void main(String[] args) {
        Main p = new Main();

        try {
            p.getStartedDemo();
            System.out.println("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            System.err.printf("DocumentDB GetStarted failed with %s", e);
        } finally {
            System.out.println("close the client");
            p.close();
        }
        System.exit(0);
    }

    private void getStartedDemo() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);

        client = new CosmosClientBuilder()
            .endpoint(AccountSettings.HOST)
            .key(AccountSettings.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildClient();

        database=client.getDatabase(databaseName);

        //Demo CosmosDB Pagination
        queryDocumentsByPage();

        //Demo Querying a Document with a list saved into Cache which can be used sliced in UI code for pagination
        executeSimpleQueryWithList();
    }

    private void queryDocumentsByPage() throws JSONException {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

        String collectionName = "volcanoCollection";
        String contanierSql = String.format("SELECT * from c where c.id = '%s'", collectionName);
        System.out.println(contanierSql);
        String itemSql = String.format("SELECT * FROM %s", collectionName);
        System.out.println(itemSql);
        CosmosPagedIterable<CosmosContainerProperties> queryObservable = database.queryContainers(contanierSql, queryOptions);

        //Observable to Interator
        Iterator<FeedResponse<CosmosContainerProperties>> page = queryObservable.iterableByPage().iterator();

        List<CosmosContainerProperties> results = page.next().getResults();

        for (CosmosContainerProperties cosmosContainerProperties : results) {
            String id = cosmosContainerProperties.getId();
            CosmosContainer container = database.getContainer(id);
            container.queryItems(itemSql, queryOptions, InternalObjectNode.class).forEach(item->{
                System.out.println(item.toJson());
            });
            System.out.println(id);
        }
    }

    private void executeSimpleQueryWithList() throws JSONException {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

        String collectionName = "ListItemDetailsCollection";
        String contanierSql = String.format("SELECT * from c where c.id = '%s'", collectionName);
        System.out.println(contanierSql);
        String itemSql = String.format("SELECT * FROM %s", collectionName);
        System.out.println(itemSql);

        CosmosPagedIterable<CosmosContainerProperties> queryObservable = database.queryContainers(contanierSql, queryOptions);

        //1. Create a cache manager
        CacheManager cm = CacheManager.getInstance();

        //2. Get a cache called "listDocCache" defined in config
        Cache cache = cm.getCache("listDocCache");

        List<CosmosContainerProperties> results = queryObservable.iterableByPage().iterator().next().getResults();

        System.out.println("count " + results.size());

        for (CosmosContainerProperties containerProperties : results) {
            String id = containerProperties.getId();
            CosmosContainer container = database.getContainer(id);
            container.queryItems(itemSql, queryOptions, InternalObjectNode.class).forEach(item->{
                try {
                    JSONObject obj = new JSONObject(item.toJson());
                    JSONArray ja_data = obj.getJSONArray("listItemDetails");
                    System.out.println("JSONArray : " + ja_data);

                    int length = ja_data.length();
                for (int i = 0; i < length; i++) {
                    JSONObject jsonObj = ja_data.getJSONObject(i);
                    //4. Put  elements in cache
                    cache.put(new Element(i,jsonObj));

                    System.out.println("array item: " + jsonObj.toString());
                }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
            System.out.println(id);
        }

        System.out.println("Elements in the cache" + cache.getKeys().toString());
        Element ele = cache.get(0);

        //5. Print out the element
        String output = (ele == null ? null : ele.getObjectValue().toString());
        System.out.println("Element in the cache" + output);

        //6. Is key in cache?
        System.out.println(cache.isKeyInCache(0)); //true
        System.out.println(cache.isKeyInCache(7)); // false

        //This cache can be used in UI with slicing for pagination

        //7. shut down the cache manager
        cm.shutdown();
    }
}

