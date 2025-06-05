package org.xyz.luckyjourney.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service(value = "redisCacheUtil")
public class RedisCacheUtil {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 设置过期时间
     *
     * @param key
     * @param time
     * @return
     */
    public Boolean expire(String key,Long time){
        try {
            if(time > 0){
                redisTemplate.expire(key,time, TimeUnit.SECONDS);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取过期时间
     * @param key
     * @return
     */
    public Long getExpire(String key){
        return redisTemplate.getExpire(key,TimeUnit.SECONDS);
    }

    /**
     * 判断是否存在key
     *
     * @param key
     * @return
     */
    public Boolean hasKey(String key){
        try {

            return redisTemplate.hasKey(key);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通设置缓存
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean set(String key,Object value){
        try {
            redisTemplate.opsForValue().set(key,value);
            return true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public Boolean set(String key,Object value,Long time){
        try {
            redisTemplate.opsForValue().set(key,value);

            if(time > 0){
                expire(key,time);
            }

            return true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取普通缓存
     *
     * @param key
     * @return
     */
    public Object get(String key){
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     *
     * @param keys 可以传一个或者多个
     */
    public void del(String... keys){
        if(keys != null && keys.length > 0){
            if(keys.length == 1){
                redisTemplate.delete(keys[0]);
            }else {
                redisTemplate.delete(Arrays.asList(keys));
            }
        }
    }

    /**
     * 普通缓存递增
     *
     * @param key
     * @param delta
     * @return
     */
    public Long incr(String key, Long delta){
        if(delta < 0){
            throw new RuntimeException("递增因子必须大于0");
        }

        return redisTemplate.opsForValue().increment(key,delta);
    }

    /**
     * 递减普通缓存
     *
     * @param key
     * @param delta
     * @return
     */
    public Long decr(String key,Long delta){
        if(delta < 0){
            throw new RuntimeException("递减因子必须大于0");
        }

        return redisTemplate.opsForValue().increment(key,-delta);
    }

    /*==========================================hash==========================================**/

    /**
     * HashGet
     *
     * @param key  不能为null不能为null
     * @param item 不能为null
     * @return
     */
    public Object hget(String key, String item){
        return redisTemplate.opsForHash().get(key,item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key
     * @return 键对应的所有值
     */
    public Map<Object, Object> hmget(String key){
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key
     * @param map
     * @return
     */
    public Boolean hmset(String key,Map<Object,Object> map){
        try {
            redisTemplate.opsForHash().putAll(key,map);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet并设置过期时间
     *
     * @param key
     * @param map
     * @param time
     * @return
     */
    public Boolean hmset(String key,Map<String,Object> map,Long time){
        try{
            redisTemplate.opsForHash().putAll(key,map);
            if(time > 0){
                expire(key,time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 单
     *
     * @param key
     * @param item
     * @param value
     * @return
     */
    public Boolean hset(String key,String item,Object value){
        try {
            redisTemplate.opsForHash().put(key,item,value);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 单 + 设置过期时间
     *
     * @param key
     * @param item
     * @param value
     * @param time
     * @return
     */
    public Boolean hset(String key,String item,Object value,Long time){
        try {
            redisTemplate.opsForHash().put(key,item,value);
            if(time > 0){
                expire(key,time);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key
     * @param item
     */
    public void hdel(String key,Object... item){
        redisTemplate.opsForHash().delete(key,item);
    }

    /**
     * 判断是否存在key
     *
     * @param key
     * @param item
     * @return
     */
    public Boolean hasHashKey(String key,String item){
        return redisTemplate.opsForHash().hasKey(key,item);
    }

    /**
     * Hash递增
     *
     * @param key
     * @param item
     * @param by
     * @return
     */
    public double hincr(String key,String item, double by){
        return redisTemplate.opsForHash().increment(key,item,by);
    }

    /**
     * Hash递减
     *
     * @param key
     * @param item
     * @param by
     * @return
     */
    public double hdecr(String key,String item, double by){
        return redisTemplate.opsForHash().increment(key,item,-by);
    }

    /*===================================set=====================================*/

    /**
     * 根据key获取Set的所有值
     *
     * @param key
     * @return
     */
    public Set<Object> sGet(String key){
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 将数据放入set缓存
     *
     * @param key
     * @param value
     * @return
     */
    public Long sSet(String key,Object... value){
       try{
            return redisTemplate.opsForSet().add(key, value);
       } catch (Exception e) {
           e.printStackTrace();
           return 0L;
       }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置Set缓存并且设置过期时间
     *
     * @param key
     * @param time
     * @param values
     * @return
     */
    public Long sSetAndTime(String key,Long time,Object... values){
        try {
            Long count = redisTemplate.opsForSet().add(key,values);
            if(time > 0){
                expire(key,time);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 获取Set缓存的大小
     *
     * @param key
     * @return
     */
    public Long sGetSetSize(String key){
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 从Set缓存移除值为values的
     *
     * @param key
     * @param values
     * @return
     */
    public Long sRemove(String key,Object... values){
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public Set<Object> sDiff(String key, String otherKey) {
        return redisTemplate.opsForSet().difference(key, otherKey);
    }

    public void zadd(String key,double score,Object value,long time){
        redisTemplate.opsForSet().add(key,value,score);

        expire(key,time);
    }

    public void zIncrementScore(String key, Object value, double delta) {
        redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    public Set<ZSetOperations.TypedTuple<Object>> getZSet(String key) {

        return redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
    }

    public Set zGet(String key){

        return redisTemplate.opsForZSet().reverseRange(key,0,-1);
    }

    public Set<ZSetOperations.TypedTuple<Object>> zSetGetByPage(String key, long pageNum, long pageSize) {
        try {
            if (redisTemplate.hasKey(key)) {
                long start = (pageNum - 1) * pageSize;
                long end = pageNum * pageSize - 1;
                Long size = redisTemplate.opsForZSet().size(key);
                if (end > size) {
                    end = -1;
                }

                return redisTemplate.opsForZSet().reverseRangeWithScores(key,start,end);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public Set<Object> getZSetObject(String key) {

        return redisTemplate.opsForZSet().range(key, 0, -1);
    }

    /**
     * 增量
     *
     * @param key
     * @param object
     * @param score
     */
    public void increment(String key, Object object, double score) {
        redisTemplate.opsForZSet().incrementScore(key, object, score);
    }


    public <T> List<T> lGet(String key,Long start,Long end){
        List<Object> list = redisTemplate.opsForList().range(key, start, end);
        List<T> objects = new ArrayList<>();

        for(Object o : list){
            objects.add((T) o);
        }

        try {
            return objects;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    public Object sRandom(String key){
        return redisTemplate.opsForSet().randomMember(key);
    }

    public List<Object> sRandom(List<String> keys){
        final List<Object> list = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for(String key:keys){
                    connection.sRandMember(key.getBytes());
                }
                return null;
            }
        });

        List<Object> res = new ArrayList<>();
        for(Object ele : list){
            if(ele != null){
                res.add(ele);
            }
        }
        return res;
    }

    /**
     * 随机key中随机拿数据
     * @param map
     * @return
     */
    public List<Object> lGetIndex(Map<String,Long> map){
        final List<Object> list = redisTemplate.executePipelined((RedisCallback<Long>) connection -> {
            map.forEach((k,v)->{
                connection.lIndex(k.getBytes(),v);
            });
            return null;
        });
        return list;
    }

    public List<Object> lSize(List<String> keys){

        final List<Object> list = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String key : keys) {
                connection.lLen(key.getBytes());
            }
            return null;
        });
        return list;
    }


    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * 获取队列头元素并删除
     *
     * @param key
     * @return
     */
    public Object lPopFirst(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }



    public void lPushRight(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    public void lPushLeft(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */

    public boolean lSet(String key, List<Object> value) {

        try {

            redisTemplate.opsForList().rightPushAll(key, value);

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    /**
     * 484
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 接受一个管道
    public List pipeline(RedisCallback redisCallback){
        return redisTemplate.executePipelined(redisCallback);
    }

}
