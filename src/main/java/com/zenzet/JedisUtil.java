package com.zenzet;

import org.apache.log4j.Logger;
import org.springframework.util.ClassUtils;
import org.springframework.util.SerializationUtils;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.SafeEncoder;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Created by ristory on 16/5/29.
 */
public class JedisUtil {
    public static final boolean useKryoRegisterMode=true;
    public static AtomicBoolean mJedisCanUse = new AtomicBoolean(true);
    private static final Logger logger = Logger
            .getLogger(JedisUtil.class);
    private JedisPool jedisMasterPool;
    private JedisPool jedisSlavePool;
    private Random random = new Random();
    private int masterReadPercent = 20;
//    private ObjectPool<Kryo> kryoFactory;

    private void closeJedis(Jedis jedis){
        if( jedis != null) jedis.close();
    }

    public Jedis getResourceNotClosed() {

        Jedis jedis = null;
        try{
            jedis = jedisMasterPool.getResource();
            return jedis;
        }finally {
            //closeJedis(jedis);
        }

    }



    public Long push(String key, String value) {

        Jedis jedis = null;
        try{
            jedis = jedisMasterPool.getResource();
            return jedis.rpush(key, value);
        }finally {
            closeJedis(jedis);
        }

    }


    public String pop(String key) {


        Jedis jedis = null;
        try{
            jedis = jedisMasterPool.getResource();
            String result = jedis.lpop(key);
            return result;
        }finally {
            closeJedis(jedis);
        }

    }



    public void pipeline(Jedis jedis) {
        try{
            //Pipeline p = jedis.pipelined();
            jedis.pipelined().sync();
        }finally {
            closeJedis(jedis);
        }

    }


    public <T> void psadd(Jedis jedis,String key, T member) {

            //Pipeline p = jedis.pipelined();
        jedis.pipelined().sadd(SafeEncoder.encode(key), serialize(member));

    }

    public void phmset(Jedis jedis,String key, Map<String, String> hash) {

            //Pipeline p = jedis.pipelined();
        jedis.pipelined().hmset(key,hash);

    }

    public boolean canGetResource() {
        Jedis jedis = null;
        try {
            jedis = jedisMasterPool.getResource();
        } catch (JedisException e) {
            return false;
        } finally {
            closeJedis(jedis);
        }
        return jedis != null;
    }

    public boolean isJedisCanUse() {
        return mJedisCanUse.get();
    }


    public String hmset(String key, Map<String, String> hash) {

        Jedis jedis = null;
        try{
            jedis = jedisMasterPool.getResource();
            String result = jedis.hmset(key,hash);
            return result;
        }finally {
            closeJedis(jedis);
        }

    }

    public ScanResult<String> scan(String cursor) {


        Jedis jedis = null;
        try{
            jedis = jedisMasterPool.getResource();
            ScanResult<String> data = jedis.scan(cursor);
            return data;
        }finally {
            closeJedis(jedis);
        }

    }

//    public ScanResult<String> scan(String cursor) {
//        Jedis jedis = jedisMasterPool.getResource();
//        try {
//            ScanResult<String> data = jedis.scan(cursor);
//            return data;
//        } catch (Exception e) {
//            return null;
//        } finally {
//            jedisMasterPool.returnResource(jedis);
//        }
//    }

    public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            ScanResult<Entry<String, String>> data = jedis.hscan(key, cursor);
            return data;
        } catch (Exception e) {
            return null;
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }


    public Map<String,String> hgetAll(final String key) {

        Jedis jedis = null;
        try{
            jedis = jedisMasterPool.getResource();
            return jedis.hgetAll(key);
        }finally {
            closeJedis(jedis);
        }

    }


//    public Map<String,String> hgetAll(final String key) {
//        Jedis jedis = jedisMasterPool.getResource();
//        try {
//            return jedis.hgetAll(key);
//        } catch (Exception e) {
//            return null;
//        } finally {
//            jedisMasterPool.returnResource(jedis);
//        }
//    }

    public String get(final String key) {

        Jedis jedis = null;
        try{
            jedis = jedisMasterPool.getResource();
            return jedis.get(key);
        }finally {
            closeJedis(jedis);
        }

    }

//    public String get(final String key) {
//        Jedis jedis = jedisMasterPool.getResource();
//        try {
//            return jedis.get(key);
//        } catch (Exception e) {
//            return null;
//        } finally {
//            jedisMasterPool.returnResource(jedis);
//        }
//    }


    public Long zadd(final String key,final double score,final String member){
        Jedis jedis=jedisMasterPool.getResource();
        try{
            return jedis.zadd(key, score, member);
        }  finally{
            jedisMasterPool.returnResource(jedis);
        }
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final String key,
                                                 final double max, final double min, final int offset,
                                                 final int count){
        Jedis jedis=getReadJedis();
        try{
            return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
        } finally{
//            returnReadResource(jedis);
        }
    }

    public double zscore(final String key,final String member){
        Jedis jedis=getReadJedis();
        try{
            return jedis.zscore(key, member);
        }finally{
//            returnReadResource(jedis);
        }
    }

    public long zrevrank(final String key,final String member){
        Jedis jedis=getReadJedis();
        try{
            return jedis.zrevrank(key, member);
        }finally{
//            returnReadResource(jedis);
        }
    }

//    // 因为kryo是线程不安全的，所以这里用到对象池
//    private Kryo getKryo() {
//        if (this.kryoFactory == null) {
//            this.kryoFactory = new SoftReferenceObjectPool<Kryo>(
//                    new PoolableKryoFactory());
//        }
//        try {
//            return this.kryoFactory.borrowObject();
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
//    }
//
//    private void close(Kryo kryo) {
//        try {
//            this.kryoFactory.returnObject(kryo);
//        } catch (Exception ex) {
//            logger.error("returnObject", ex);
//        }
//    }

    // private static ThreadLocal<Kryo> localKroy = new ThreadLocal<Kryo>() {
    // @Override
    // protected Kryo initialValue() {
    // return new Kryo();
    // }
    // };
    //
    // private Kryo getKryo() {
    // return localKroy.get();
    // }

    private <T> byte[] serialize(T o) {
        if (o == null) {
            return new byte[0];
        }
        Class<? extends Object> c = o.getClass();
        if(o instanceof String){
            return SafeEncoder.encode((String)o);
        }
        if (ClassUtils.isPrimitiveOrWrapper(c)) {
            if (c.equals(Long.class) || c.equals(long.class)) {
                return SafeEncoder.encode(Long.toString((Long) o));
            }
            if (c.equals(Integer.class) || c.equals(int.class)) {
                return SafeEncoder.encode(Integer.toString((Integer) o));
            }
            if (c.equals(Short.class) || c.equals(short.class)) {
                return SafeEncoder.encode(Short.toString((Short) o));
            }
            if (c.equals(Boolean.class) || c.equals(boolean.class)) {
                Boolean b = (Boolean) o;
                if (b) {
                    return new byte[] { 1 };
                } else {
                    return new byte[] { 0 };
                }
            }
            if (c.equals(Byte.class) || c.equals(byte.class)) {
                byte[] b = new byte[1];
                b[0] = (Byte) o;
                return b;
            }
            if (c.equals(Character.class) || c.equals(char.class)) {
                Character cc = (Character) o;
                byte[] b = new byte[2];
                b[0] = (byte) ((cc & 0xFF00) >> 8);
                b[1] = (byte) (cc & 0x00FF);
                return b;
            }
        }
         return SerializationUtils.serialize((Serializable) o);
//        Kryo kryo = this.getKryo();
//        try{
//            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//            Output output = new Output(outStream, 256);
//            if(useKryoRegisterMode){
//                kryo.writeObject(output, o);
//            }else{
//                kryo.writeClassAndObject(output, o);
//            }
//            output.flush();
//            return outStream.toByteArray();
//        }finally{
//            close(kryo);
//        }
    }


    private <T> Object deserialize(byte[] bytes, Class c) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        if(c.equals(String.class)){
            return SafeEncoder.encode(bytes);
        }
        if (ClassUtils.isPrimitiveOrWrapper(c)) {
            if (c.equals(Long.class) || c.equals(long.class)) {
                return (Long) Long.parseLong(SafeEncoder.encode(bytes));
            }
            if (c.equals(Integer.class) || c.equals(int.class)) {
                return (Integer) Integer.parseInt(SafeEncoder.encode(bytes));
            }
            if (c.equals(Short.class) || c.equals(short.class)) {
                return (Short) Short.parseShort(SafeEncoder.encode(bytes));
            }
            if (c.equals(Boolean.class) || c.equals(boolean.class)) {
                if (bytes[0] == 1) {
                    return (Boolean) true;
                }
                return (Boolean) false;
            }
            if (c.equals(Byte.class) || c.equals(byte.class)) {
                return (Byte) bytes[0];
            }
            if (c.equals(Character.class) || c.equals(char.class)) {
                return ByteBuffer.wrap(bytes).getChar();
            }
        }
        return SerializationUtils.deserialize(bytes);

//        Kryo kryo = this.getKryo();
//        try{
//            Input input = new Input(bytes);
//            Object o =null;
//            if(useKryoRegisterMode){
//                o = kryo.readObject(input, c);
//            }else{
//                o = kryo.readClassAndObject(input);
//            }
//            return o;
//        }finally{
//            close(kryo);
//        }
    }


    public <T> List<T> lrange(String key, final int offset, final int limit,Class<T> c) {
        Jedis jedis = jedisMasterPool.getResource();
        try{
            final byte[] keyByte = SafeEncoder.encode(key);
            List<byte[]> list = jedis.lrange(keyByte, offset, offset + limit - 1);
            List<T> results = new ArrayList<T>(list.size());
            for (byte[] bs : list) {
                results.add((T)deserialize(bs, c));
            }
            return results;
        }finally{
            jedisMasterPool.returnResource(jedis);
        }
    }

    public <T> Long lpushAndTrim(String key, int trimSize, T... values) {
        if (trimSize <= 0) {
            throw new IllegalArgumentException("not valid size");
        }
        if (values.length > trimSize) {
            values = Arrays.copyOfRange(values, 0, trimSize);
        }
        Jedis jedis = jedisMasterPool.getResource();
        try {
            byte[] k = SafeEncoder.encode(key);
            byte[][] bss = new byte[values.length][];
            for (int i = 0; i < bss.length; i++) {
                bss[i] = serialize(values[i]);
            }
            Long num = jedis.lpush(k, bss);
            jedis.ltrim(key, 0, trimSize - 1);
            return num;
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    public <T> Long lpushAndTrimAndExpire(String key, T value, int trimSize,int expireSeconds) {
        if (trimSize <= 0) {
            throw new IllegalArgumentException("not valid size");
        }
        Jedis jedis = jedisMasterPool.getResource();
        try {
            byte[] k = SafeEncoder.encode(key);
            byte[] bs = serialize(value);
            Pipeline p = jedis.pipelined();
            Response<Long> resp = p.lpush(k, bs);
            p.ltrim(k, 0, trimSize - 1);
            p.expire(k, expireSeconds);
            p.sync();
            return resp.get();
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    public Long hSetObject(final String key, final String field, Object value)
            throws IOException {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            byte[] bytes = serialize(value);
            return jedis.hset(SafeEncoder.encode(key),
                    SafeEncoder.encode(field), bytes);
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    public <T> T hGetObject(final String key, final String field,Class<T> c)
            throws IOException, ClassNotFoundException {
        Jedis jedis = getReadJedis();
        try {
            byte[] bt = jedis.hget(SafeEncoder.encode(key),
                    SafeEncoder.encode(field));
            if (bt != null && bt.length > 0) {
                return (T)deserialize(bt,c);
            } else {
                return null;
            }
        } finally {
//            returnReadResource(jedis);
        }
    }


    public <T> Set<T> smembers(String key,Class<T> c) {


        Jedis jedis = null;
        byte[] keyByte=SafeEncoder.encode(key);
        try{
            jedis = jedisMasterPool.getResource();
            Set<byte[]> byteSets = jedis.smembers(keyByte);
            Set<T> result = new HashSet<T>(byteSets.size());
            for(byte[] bs:byteSets){
                result.add((T)deserialize(bs, c));
            }
            return result;

        }finally {
            closeJedis(jedis);
        }

    }


//    public <T> Set<T> smembers(String key,Class<T> c) {
//        Jedis jedis = getReadJedis();
//        byte[] keyByte=SafeEncoder.encode(key);
//        try {
//            Set<byte[]> byteSets = jedis.smembers(keyByte);
//            Set<T> result = new HashSet<T>(byteSets.size());
//            for(byte[] bs:byteSets){
//                result.add((T)deserialize(bs, c));
//            }
//            return result;
//        } finally {
////            returnReadResource(jedis);
//        }
//    }


    public <T> long sadd(String key, T member) {

        Jedis jedis = null;
        try {
            jedis = jedisMasterPool.getResource();

            return jedis.sadd(SafeEncoder.encode(key), serialize(member));
        } finally {
            closeJedis(jedis);
        }
    }

//	public <T> Set<String> sinter(final String... keys) {
//		Jedis jedis = getReadJedis();
//		try {
//			return jedis.sinter(keys);
//		} finally {
//			returnReadResource(jedis);
//		}
//	}

    /**
     * Remove the specified member from the set value stored at key. If member
     * was not a member of the set no operation is performed. If key does not
     * hold a set value an error is returned.
     * <p/>
     * Time complexity O(1)
     *
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the new element was removed 0
     *         if the new element was not a member of the set
     */
    public <T> Long srem(final String key, final T member) {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            return jedis.srem(SafeEncoder.encode(key), serialize(member));
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    /**
     * Set the string value as value of the key. The string can't be longer than
     * 1073741824 bytes (1 GB).
     * <p/>
     * Time complexity: O(1)
     *
     * @param key
     * @param value
     * @return Status code reply
     * @throws IOException
     */
    public String set(final String key, String value) {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            return jedis.set(key, value);
        } catch (Exception e) {
            logger.error("set value error", e);
            return null;
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    /**
     * The command is exactly equivalent to the following group of commands:
     * {@link #set(String, String) SET} + {@link #expire(String, int) EXPIRE}.
     * The operation is atomic.
     * <p/>
     * Time complexity: O(1)
     *
     * @param key
     * @param seconds
     * @param value
     * @return Status code reply
     */
    public String setex(final String key, final int seconds, final String value) {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            return jedis.setex(key, seconds, value);
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }
    public <T> String setex(final String key, final int seconds, final T value) {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            return jedis.setex(SafeEncoder.encode(key), seconds, serialize(value));
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    /**
     * Remove the specified keys. If a given key does not exist no operation is
     * performed for this key. The command returns the number of keys removed.
     * <p/>
     * Time complexity: O(1)
     *
     * @param keys
     * @return Integer reply, specifically: an integer greater than 0 if one or
     *         more keys were removed 0 if none of the specified key existed
     */
    public Long del(final String... keys) {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            return jedis.del(keys);
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    public String getSet(final String key, final String value) {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            return jedis.getSet(key, value);
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    public <T> T getSet(final String key, final T value) {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            byte[] bs = jedis.getSet(SafeEncoder.encode(key), serialize(value));
            return (T)deserialize(bs,value.getClass());
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    public Set<String> keys(final String pattern) {
        Jedis jedis = getReadJedis();
        try {
            return jedis.keys(pattern);
        } finally {
//            returnReadResource(jedis);
        }
    }


    public Long expire(final String key, final int seconds) {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            return jedis.expire(key, seconds);
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    public Long expireAt(final String key, final long seconds) {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            return jedis.expireAt(key, seconds);
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    public Long incr(final String key) {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            return jedis.incr(key);
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    /**
     * Get the values of all the specified keys. If one or more keys dont exist
     * or is not of type String, a 'nil' value is returned instead of the value
     * of the specified key, but the operation never fails.
     * <p/>
     * Time complexity: O(1) for every key
     *
     * @param keys
     * @return Multi bulk reply
     */
    public List<String> mget(final String... keys) {
        Jedis jedis = getReadJedis();
        try {
            return jedis.mget(keys);
        } finally {
//            returnReadResource(jedis);
        }
    }

    public <T> List<T> mgetObject(final String[] keys,Class<T> c) {
        Jedis jedis = getReadJedis();
        try {
            List<byte[]> bss = jedis.mget(SafeEncoder.encodeMany(keys));
            List<T> result = new ArrayList<T>(bss.size());
            for (byte[] bs : bss) {
                result.add((T) deserialize(bs,c));
            }
            return result;
        } finally {
//            returnReadResource(jedis);
        }
    }

    /**
     * set Object to redis
     *
     * @param key
     * @param value
     * @return Status code reply
     * @throws IOException
     */
    public String setObject(final String key, int seconds, Object value)
            throws IOException {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            return jedis.setex(SafeEncoder.encode(key), seconds, serialize(value));
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    public String setObject(final String key, Object value) throws IOException {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            return jedis.set(SafeEncoder.encode(key), serialize(value));
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    /**
     * get Object from redis
     *
     * @param key
     * @return Object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public <T> T getObject(final String key,Class<T> c) throws IOException,
            ClassNotFoundException {
        Jedis jedis = getReadJedis();
        try {
            byte[] bt = jedis.get(SafeEncoder.encode(key));
            if (bt != null && bt.length > 0) {
                return (T) deserialize(bt,c);
            } else {
                return null;
            }
        } finally {
//            returnReadResource(jedis);
        }
    }

    public Long llen(final String key) throws IOException,
            ClassNotFoundException {
        Jedis jedis = getReadJedis();
        try {
            return jedis.llen(SafeEncoder.encode(key));
        } finally {
//            returnReadResource(jedis);
        }
    }

    public <T> long lpush(final String key, int seconds, List<T> list)
            throws IOException {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            byte[][] bss = new byte[list.size()][];
            for (int i = 0; i < bss.length; i++) {
                bss[i] = serialize(list.get(i));
            }
            long len = jedis.lpush(SafeEncoder.encode(key), bss);
            jedis.expire(SafeEncoder.encode(key), seconds);
            return len;
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }

    public <T> long lpush(final String key, List<T> list) throws IOException {
        Jedis jedis = jedisMasterPool.getResource();
        try {
            byte[][] bss = new byte[list.size()][];
            for (int i = 0; i < bss.length; i++) {
                bss[i] = serialize(list.get(i));
            }
            long len = jedis.lpush(SafeEncoder.encode(key), bss);
            return len;
        } finally {
            jedisMasterPool.returnResource(jedis);
        }
    }



    public Jedis getReadJedis() {
        if (random.nextInt(100) < masterReadPercent || jedisSlavePool == null) {
            try {
                return jedisMasterPool.getResource();
            } catch (JedisConnectionException e) {


                logger.error("Redis Connection Lost : ",e);

                if (jedisSlavePool != null) {
                    logger.error(
                            "get read jedis from jedisMasterPool error,try get from slave",
                            e);
                    return jedisSlavePool.getResource();
                } else {
                    throw e;
                }
            }
        } else {
            try {
                return jedisSlavePool.getResource();
            } catch (JedisConnectionException e) {
                logger.error(
                        "get read jedis from jedisSlavePool error {},try get from master",
                        e);
                return jedisMasterPool.getResource();
            }
        }
    }

    public BinaryJedis getBinaryJedis() {
        if (random.nextInt(100) < masterReadPercent || jedisSlavePool == null) {
            try {
                return jedisMasterPool.getResource();
            } catch (JedisConnectionException e) {
                if (jedisSlavePool != null) {
                    logger.error(
                            "get read jedis from jedisMasterPool error,try get from slave",
                            e);
                    return jedisSlavePool.getResource();
                } else {
                    throw e;
                }
            }
        } else {
            try {
                return jedisSlavePool.getResource();
            } catch (JedisConnectionException e) {
                logger.error(
                        "get read jedis from jedisSlavePool error {},try get from master",
                        e);
                return jedisMasterPool.getResource();
            }
        }
    }

//    private void returnReadResource(Jedis jedis) {
//        String key = jedis.getClient().getHost() + ":"
//                + jedis.getClient().getPort();
//        if (jedisSlavePool != null
//                && jedisSlavePool.getHostString().equalsIgnoreCase(key)) {
//            jedisSlavePool.returnResource(jedis);
//        } else if (jedisMasterPool.getHostString().equalsIgnoreCase(key)) {
//            jedisMasterPool.returnResource(jedis);
//        } else {
//            logger.error("can't return jedis {} to pool!", key);
//        }
//    }

    public void setJedisMasterPool(JedisPool jedisMasterPool) {
        this.jedisMasterPool = jedisMasterPool;
    }

    public void setJedisSlavePool(JedisPool jedisSlavePool) {
        this.jedisSlavePool = jedisSlavePool;
    }

    public void setMasterReadPercent(int masterReadPercent) {
        this.masterReadPercent = masterReadPercent;
    }
}
