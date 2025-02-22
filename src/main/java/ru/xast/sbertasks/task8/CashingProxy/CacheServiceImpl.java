package ru.xast.sbertasks.task8.CashingProxy;

import java.util.*;

/**
 * implements interface CacheService
 * @see CacheService
 * @author Khasrovyan Artyom
 */
public class CacheServiceImpl implements CacheService {
    @Override
    public List<String> run(String item, double value, Date date) {
        List<String> result = new ArrayList<>();
        result.add("Result from run: " + item + " with value " + value + " at " + date);
        return result;
    }

    @Override
    public List<String> work(String item) {
       List<String> result = new ArrayList<>();
       result.add("Result from work: " + item);
       return result;
    }
}
