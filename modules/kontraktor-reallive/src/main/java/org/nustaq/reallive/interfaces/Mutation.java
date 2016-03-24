package org.nustaq.reallive.interfaces;

import org.nustaq.kontraktor.IPromise;

import java.util.function.Consumer;

/**
 * Created by moelrue on 05.08.2015.
 */
public interface Mutation<K> {

    /**
     * FIXME: does not trigger a change notification !
     * @param casCondition
     * @param key
     * @param keyVals
     * @return
     */
    IPromise<Boolean> putCAS( RLPredicate<Record<K>> casCondition, K key, Object... keyVals);
    void atomic(K key, RLConsumer action);

    // FIXME: collides with put key, record
    void put(K key, Object... keyVals);
    default void put(K key, Record rec) {
        rec.key(key);
    }
    void addOrUpdate(K key, Object... keyVals);
    void add( K key, Object ... keyVals );
    void add( Record<K> rec );
    void addOrUpdateRec(Record<K> rec);
    void put(Record<K> rec);
    void update( K key, Object ... keyVals );
    void remove(K key);

}
