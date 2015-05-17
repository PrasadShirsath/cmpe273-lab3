package edu.sjsu.cmpe.cache.client;

import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;

import java.nio.charset.Charset;
import java.util.*;

public class Client {
    public static  List<CacheServiceInterface> caches;
    public static Map<Long, String> keysAndValues = new HashMap<Long, String>();

    public static void main(String[] args) throws Exception {


        System.out.println("\nStarting Cache Client...\n");

        caches = new ArrayList<CacheServiceInterface>();

        caches.add(new DistributedCacheService("http://localhost:3000"));
        caches.add(new DistributedCacheService("http://localhost:3001"));
        caches.add(new DistributedCacheService("http://localhost:3002"));


        keysAndValues.put(new Long(1), "a");
        keysAndValues.put(new Long(2), "b");
        keysAndValues.put(new Long(3), "c");
        keysAndValues.put(new Long(4), "d");
        keysAndValues.put(new Long(5), "e");
        keysAndValues.put(new Long(6), "f");
        keysAndValues.put(new Long(7), "g");
        keysAndValues.put(new Long(8), "h");
        keysAndValues.put(new Long(9), "i");
        keysAndValues.put(new Long(10), "j");


        /*********************************************Consistent Hash*********************************************************/
        consistentHash();
        /**********************************************Rendezvous Hash********************************************************/
        //rendezvousHash();


    }

    public static void consistentHash()
    {
        ConsistentHash<CacheServiceInterface> consistentHash;

        Integer replicationFactor = 10;


        Set set = keysAndValues.entrySet();
        Iterator iterator = set.iterator();

        consistentHash = new ConsistentHash<CacheServiceInterface>(Hashing.md5(), replicationFactor, caches);


        while (iterator.hasNext()) {


            Map.Entry mapentry = (Map.Entry) iterator.next();

            CacheServiceInterface bucket = consistentHash.get(mapentry.getKey());
            bucket.put((Long) mapentry.getKey(), (String) mapentry.getValue());
            System.out.println("put(" + mapentry.getKey() + " => " + mapentry.getValue() + ")");

        }
        for (int key = 1; key <= 10; key++) {
            CacheServiceInterface bucket = consistentHash.get(key);
            String value = bucket.get(key);
            System.out.println("get(" + key + ") => " + value);
        }
        System.out.println("\nDistribution of Values is: \n" +
                        "Server_A => http://localhost:3000/cache/  =>\n" + caches.get(0).getAllValues() +
                        "\nServer_B => http://localhost:3001/cache/  =>\n" + caches.get(1).getAllValues() +
                        "\nServer_C => http://localhost:3002/cache/  =>\n" + caches.get(2).getAllValues()
        );


    }

    public static void rendezvousHash() {

        Funnel<CharSequence> strFunnel = Funnels.stringFunnel(Charset.defaultCharset());
        Funnel<Long> longFunnel = Funnels.longFunnel();

        ArrayList<String> cacheUrls = new ArrayList<String>();
        for(CacheServiceInterface c : caches ) {
            DistributedCacheService d = (DistributedCacheService)c;
            cacheUrls.add(d.getUrl());
        }

        RendezvousHash rendezvousHash = new RendezvousHash(Hashing.md5(),longFunnel,strFunnel,cacheUrls);

        for(long key = 1 ; key <= 10 ; key++)
        {
            String value = Character.toString((char)('a' + key - 1));
            // returns url of cache
            String cacheUrl = (String)rendezvousHash.get(key);
            caches.get(cacheUrls.indexOf(cacheUrl)).put(key,value);
            //int bucket = ;
            System.out.println("put(" + key + " => " + value + ") ==> routed to :" + rendezvousHash.get(key));
        }

        for(long key = 1; key <= 10 ; key++)
        {
            String cacheUrl = (String)rendezvousHash.get(key);
            System.out.println("get(" + key + ") => " + caches.get(cacheUrls.indexOf(cacheUrl)).get(key));
        }

        System.out.println("\nDistribution of Values is: \n" +
                        "Server_A => http://localhost:3000/cache/  =>\n" + caches.get(0).getAllValues() +
                        "\nServer_B => http://localhost:3001/cache/  =>\n" + caches.get(1).getAllValues() +
                        "\nServer_C => http://localhost:3002/cache/  =>\n" + caches.get(2).getAllValues()
        );
    }
}