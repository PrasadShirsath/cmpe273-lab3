package edu.sjsu.cmpe.cache.client;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A high performance thread safe implementation of Rendezvous (Highest Random Weight, HRW) hashing is an algorithm that allows clients to achieve distributed agreement on which node (or proxy) a given
 * key is to be placed in. This implementation has the following properties.
 */

public class RendezvousHash<K, N extends Comparable<? super N>>
{

    private final HashFunction hasher;
    private final Funnel<K> keyFunnel;
    private final Funnel<N> nodeFunnel;
    private final ConcurrentSkipListSet<N> ordered;

    public RendezvousHash(HashFunction hasher, Funnel<K> keyFunnel, Funnel<N> nodeFunnel, Collection<N> init)
    {
        if (hasher == null) throw new NullPointerException("hasher");
        if (keyFunnel == null) throw new NullPointerException("keyFunnel");
        if (nodeFunnel == null) throw new NullPointerException("nodeFunnel");
        if (init == null) throw new NullPointerException("init");
        this.hasher = hasher;
        this.keyFunnel = keyFunnel;
        this.nodeFunnel = nodeFunnel;
        this.ordered = new ConcurrentSkipListSet<N>(init);
    }

    public boolean remove(N node) {
        return ordered.remove(node);
    }

    public boolean add(N node) {
        return ordered.add(node);
    }

    public N get(K key) {
        long maxValue = Long.MIN_VALUE;
        N max = null;
        for (N node : ordered) {
            long nodesHash = hasher.newHasher()
                    .putObject(key, keyFunnel)
                    .putObject(node, nodeFunnel)
                    .hash().asLong();
            if (nodesHash > maxValue) {
                max = node;
                maxValue = nodesHash;
            }
        }
        return max;
    }
}