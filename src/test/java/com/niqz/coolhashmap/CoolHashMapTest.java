package com.niqz.coolhashmap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для CoolHashMap.
 *
 * @see CoolHashMap
 */
class CoolHashMapTest {

    /**
     * Размер входных данных при случайном тестировании.
     */
    private static final int RANDOM_TEST_SIZE = 5000;

    private static Random random;

    @BeforeAll
    static void setup() {
        random = new Random(408);
    }

    /**
     * Проверка валидации емкости.
     *
     * @see CoolHashMap#CoolHashMap(int, float)
     */
    @Test
    void whenCreateMapWithInvalidCapacity_ThrowException() {
        assertThrows(CoolHashMap.IllegalArgumentException.class, () -> new CoolHashMap(-1));
    }

    /**
     * Проверка валидации границы загрузки.
     *
     * @see CoolHashMap#CoolHashMap(int, float)
     */
    @Test
    void whenCreateMapWithInvalidLoadBorder_ThrowException() {
        assertThrows(CoolHashMap.IllegalArgumentException.class, () -> new CoolHashMap(1, 0));
        assertThrows(CoolHashMap.IllegalArgumentException.class, () -> new CoolHashMap(1, 0.39f));
        assertThrows(CoolHashMap.IllegalArgumentException.class, () -> new CoolHashMap(1, 1.01f));
        assertThrows(CoolHashMap.IllegalArgumentException.class, () -> new CoolHashMap(1, 2f));
    }

    /**
     * Проверка размера карты.
     */
    @Test
    void whenGetSize_ReturnSize() {
        CoolHashMap map = new CoolHashMap();
        map.put(0, 0);
        map.put(1, 1);
        map.put(-1, 2);
        assertEquals(3, map.getSize());
        // Так как ключ "1" уже есть в карте, то размер не изменится
        map.put(1, 3);
        assertEquals(3, map.getSize());
    }

    /**
     * Проверка получения значений по существующим ключам.
     */
    @Test
    void whenGetExistingKey_ReturnValue() {
        CoolHashMap map = new CoolHashMap();
        map.put(0, 0);
        map.put(1, 1);
        map.put(-1, 2);
        map.put(Integer.MAX_VALUE, 3);
        map.put(Integer.MIN_VALUE, 4);
        assertEquals(0, map.get(0));
        assertEquals(1, map.get(1));
        assertEquals(2, map.get(-1));
        assertEquals(3, map.get(Integer.MAX_VALUE));
        assertEquals(4, map.get(Integer.MIN_VALUE));
    }

    /**
     * Проверка возвращаемого значения при перезаписи.
     */
    @Test
    void whenPutExistingKey_ReturnPreviousValue() {
        CoolHashMap map = new CoolHashMap();
        map.put(0, 0);
        assertEquals(0, map.put(0, 1));
    }

    /**
     * Проверка доступа к несуществующим ключам.
     */
    @Test
    void whenGetNonexistentKey_ReturnNull() {
        CoolHashMap map = new CoolHashMap(4);
        map.put(1, 0);
        map.put(13, 1);
        assertNull(map.get(-1));
        assertNull(map.get(25));
        assertNull(map.get(Integer.MAX_VALUE));
        assertNull(map.get(Integer.MIN_VALUE));
    }

    /**
     * Проверка записи и получения данных при принудительном увеличении емкости карты.
     */
    @Test
    void whenPutAndGetWithForceResize_ReturnValue() {
        CoolHashMap map = new CoolHashMap(1, 0.4f);
        map.put(0, 0);
        assertEquals(0, map.get(0));
    }

    /**
     * Тест со случайными начальными параметрами карты и парами (ключ, значение)
     * для проверки записи, перезаписи и получения значений по ключу.
     */
    @RepeatedTest(50)
    void randomPutAndGetTest() {
        CoolHashMap map = new CoolHashMap(
                (int) Math.pow(2, random.nextInt(10)),
                0.5f + random.nextFloat() / 2);
        HashMap<Integer, Long> testMap = new HashMap<>();

        // Генерация и запись/перезапись данных

        for (int i = 0; i < RANDOM_TEST_SIZE; i++) {
            int key = random.nextInt(RANDOM_TEST_SIZE) - (RANDOM_TEST_SIZE / 2);
            long value = random.nextLong();

            testMap.put(key, value);
            map.put(key, value);
        }

        // Проверка значений

        for (int key : testMap.keySet()) {
            assertEquals(testMap.get(key), map.get(key));
        }
    }

}