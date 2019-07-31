import com.microsoft.azure.cosmosdb.*;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    private final ExecutorService executorService;
    private final Scheduler scheduler;

    private AsyncDocumentClient client;

    private final String databaseName = "test-kcdb";

    public Main() {
        executorService = Executors.newFixedThreadPool(100);
        scheduler = Schedulers.from(executorService);
    }

    public void close() {
        executorService.shutdown();
        client.close();
    }


    public static void main(String[] args) {
        Main p = new Main();

        try {
            p.getStartedDemo();
            System.out.println(String.format("Demo complete, please hold while resources are released"));
        } catch (Exception e) {
            System.err.println(String.format("DocumentDB GetStarted failed with %s", e));
        } finally {
            System.out.println("close the client");
            p.close();
        }
        System.exit(0);
    }



    private void getStartedDemo() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);

        client = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(AccountSettings.HOST)
                .withMasterKeyOrResourceToken(AccountSettings.MASTER_KEY)
                .withConnectionPolicy(ConnectionPolicy.GetDefault())
                .withConsistencyLevel(ConsistencyLevel.Eventual)
                .build();

        //Demo CosmosDB Pagination with ContinuationToken
        //This method returns a map with nextContinuationtoken and prevContinuationToken with resultsets
        //Which can be used in UI
        QueryPageByPage();

        //Demo Querying a Document with a list saved into Cache which can be used sliced in UI code for pagination
       executeSimpleQueryWithList();

    }




    /*
     Method demonstrated CosmosDB Pagination using ContinuationToken
     This method returns represents client calling with page size
     */
    private void QueryPageByPage() throws JSONException {
        int pageSize = 500; //No of docs per page
        int currentPageNumber = 1;
        int documentNumber = 1;
        String continuationToken = null;

        do {
            System.out.println("Page " + currentPageNumber);
            String nextContinuationKey = "";

            // Loads ALL documents for the current page
            HashMap<String, List<Document>> docsForCurrPage = QueryDocumentsByPage(currentPageNumber, pageSize, continuationToken);

            for (Map.Entry<String, List<Document>> entry : docsForCurrPage.entrySet()) {
                System.out.println("Continuationkey" + entry.getKey() + " = " + entry.getValue());
                nextContinuationKey = entry.getKey();
                documentNumber++;
            }

            // Ensure the continuation token is kept for the next page query execution
            continuationToken = nextContinuationKey;
            System.out.println("continuationToken3 " + continuationToken);
            currentPageNumber++;
        } while (continuationToken != null);
    }


    /* This method returns continuation token with list of records for pagination on UI or REST API call*/
    private HashMap<String, List<Document>> QueryDocumentsByPage(int currentPageNumber, int pageSize, String continuationToken) throws JSONException {

        HashMap<String, List<Document>> map = new HashMap<>();

        FeedOptions queryOptions = new FeedOptions();

        // note that setMaxItemCount sets the number of items to return in a single page result
        queryOptions.setMaxItemCount(pageSize);
        queryOptions.setEnableCrossPartitionQuery(true);
        queryOptions.setRequestContinuation(continuationToken);

        String collectionName = "volcanoCollection";
        String sql = "SELECT * FROM volcanoCollection";

        String collectionLink = String.format("/dbs/%s/colls/%s", databaseName, collectionName);
        Observable<FeedResponse<Document>> queryObservable =
                client.queryDocuments(collectionLink,
                        sql, queryOptions);

        //Observable to Interator
        Iterator<FeedResponse<Document>> it = queryObservable.toBlocking().getIterator();

        FeedResponse<Document> page = it.next();
        List<Document> results = page.getResults();
        for (Document doc : results) {
            JSONObject obj = new JSONObject(doc.toJson());
            String id = obj.getString("id");

        }
        continuationToken = page.getResponseContinuation();
        System.out.println("continuationToken2: " + continuationToken);
        map.put(continuationToken, results);
        return map;
    }



    private void executeSimpleQueryWithList() throws JSONException {

        FeedOptions queryOptions = new FeedOptions();
        // note that setMaxItemCount sets the number of items to return in a single page result
        queryOptions.setMaxItemCount(500);
        queryOptions.setEnableCrossPartitionQuery(true);

        HashMap<Integer, List<JSONObject>> docsPerPage = new HashMap<>();

        String collectionName = "ListItemDetailsCollection";

        String collectionLink = String.format("/dbs/%s/colls/%s", databaseName, collectionName);
        Observable<FeedResponse<Document>> queryObservable =
                client.queryDocuments(collectionLink,
                        "SELECT * FROM ListItemDetailsCollection", queryOptions);

        Iterator<FeedResponse<Document>> it = queryObservable.toBlocking().getIterator();

        //1. Create a cache manager
        CacheManager cm = CacheManager.getInstance();

        //2. Get a cache called "listDocCache" defined in config
        Cache cache = cm.getCache("listDocCache");


        FeedResponse<Document> page = it.next();
        List<Document> results = page.getResults();
        System.out.println("count " + results.size());
        for (Document doc : results) {
            JSONObject obj = new JSONObject(doc.toJson());

            JSONArray ja_data = obj.getJSONArray("listItemDetails");
            System.out.println("JSONArray : " + ja_data);

            int length = ja_data.length();

            for (int i = 0; i < length; i++) {
                JSONObject jsonObj = ja_data.getJSONObject(i);
                //4. Put  elements in cache
                cache.put(new Element(i,jsonObj));

                System.out.println("array item: " + jsonObj.toString());

            }
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

