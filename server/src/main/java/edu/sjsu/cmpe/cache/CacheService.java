package edu.sjsu.cmpe.cache;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import edu.sjsu.cmpe.cache.api.resources.CacheResource;
import edu.sjsu.cmpe.cache.config.CacheServiceConfiguration;
import edu.sjsu.cmpe.cache.domain.Entry;
import edu.sjsu.cmpe.cache.repository.CacheInterface;
import edu.sjsu.cmpe.cache.repository.ChronicleMapCache;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class CacheService extends Service<CacheServiceConfiguration> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    static String serverName;

    public static void main(String[] args) throws Exception {
        serverName=args[1];
        serverName=serverName.replace(".yml","");
        serverName=serverName.replace("config/","");
        serverName=serverName.replace("_config","");

        new CacheService().run(args);
    }

    @Override
    public void initialize(Bootstrap<CacheServiceConfiguration> bootstrap) {
        bootstrap.setName("cache-server");
    }

    @Override
    public void run(CacheServiceConfiguration configuration,
            Environment environment) throws Exception {
        /** Cache APIs */
        Map<Long, Entry> map = createChronicleMap();
        CacheInterface cache = new ChronicleMapCache(map);
        environment.addResource(new CacheResource(cache));
        log.info("Loaded resources");
    }

    private Map<Long,Entry> createChronicleMap() {
        Map<Long, Entry> builder=null;
        File temp = new File("src/main/data"+serverName+".txt");


        if(builder!=null)
        {
            return builder;
        }
        try {
            builder = ChronicleMapBuilder.of(Long.class, Entry.class).createPersistedTo(temp);
            return builder;
        }catch (Exception e){}

        return null;
    }

}
