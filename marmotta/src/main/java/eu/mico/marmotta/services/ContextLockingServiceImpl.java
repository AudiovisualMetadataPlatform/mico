package eu.mico.marmotta.services;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import eu.mico.marmotta.api.ContextLockingService;
import org.apache.marmotta.kiwi.hazelcast.caching.HazelcastCacheManager;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.openrdf.model.URI;
import org.openrdf.sail.Sail;
import org.openrdf.sail.helpers.SailWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * An implementation of a read-write locking service based on the Hazelcast instance managed by the KiWi cache.
 * Instead of providing a full readers-writer locking scheme, this service only does a write lock; this is sufficient
 * for Marmotta because the database anyways ensures transaction isolation.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class ContextLockingServiceImpl implements ContextLockingService {

    private static Logger log = LoggerFactory.getLogger(ContextLockingServiceImpl.class);

    private HazelcastInstance hazelcast;

    @Inject
    private SesameService sesameService;

    @PostConstruct
    public void initialise() {
        KiWiStore s = findKiWiStore(sesameService.getRepository().getSail());

        if(s != null && s.getPersistence().getCacheManager() instanceof HazelcastCacheManager) {
            hazelcast = ((HazelcastCacheManager) (s.getPersistence().getCacheManager())).getBackend();
        } else {
            // testing, local machine only
            Config cfg = new Config();
            cfg.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            hazelcast = Hazelcast.newHazelcastInstance(cfg);
        }
    }


    private KiWiStore findKiWiStore(Sail sail) {
        if(sail instanceof KiWiStore) {
            return (KiWiStore) sail;
        } else if(sail instanceof SailWrapper) {
            return findKiWiStore(((SailWrapper) sail).getBaseSail());
        } else {
            log.warn("the base sail of this stack is not a KiWi store: {}", sail.getClass());
            return null;
        }

    }

    /**
     * Lock the context with the given URI for reading using a cluster-wide semaphore and lock. If the current
     * process holds a read-lock, there can be any number of additional read-locks by other processes, but
     * no write locks.
     *
     * @param contexts
     */
    @Override
    public void lockContextsRead(URI... contexts) {
        // implement write locking only
    }

    /**
     * Lock the context with the given URI for writing using a cluster-wide semaphore and lock. If the current
     * process holds a write lock, there can be no other read-locks or write-locks.
     *
     * @param context
     */
    @Override
    public void lockContextWrite(URI context) {
        log.debug("WRITE LOCK: {}", context.stringValue());
        ILock wlock = hazelcast.getLock("W::"+context.stringValue());
        wlock.lock();
    }

    /**
     * Release the read lock held by the current process, and signal waiting writers in case there are no
     * more read locks.
     *
     * @param contexts
     */
    @Override
    public void unlockContextsRead(URI... contexts) {
        // implement write locking only
    }

    /**
     * Release the write lock held by the current process and signal waiting readers and writers.
     *
     * @param context
     */
    @Override
    public void unlockContextWrite(URI context) {
        log.debug("WRITE UNLOCK: {}", context.stringValue());
        ILock wlock = hazelcast.getLock("W::"+context.stringValue());
        wlock.unlock();

    }
}
