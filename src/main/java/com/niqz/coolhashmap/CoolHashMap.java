package com.niqz.coolhashmap;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Реализация структуры данных "Хеш-таблица".
 * Используется открытая адресация с двойным хешированием для разрешения коллизий.
 *
 * @see <a href="https://neerc.ifmo.ru/wiki/index.php?title=%D0%A0%D0%B0%D0%B7%D1%80%D0%B5%D1%88%D0%B5%D0%BD%D0%B8%D0%B5_%D0%BA%D0%BE%D0%BB%D0%BB%D0%B8%D0%B7%D0%B8%D0%B9#.D0.94.D0.B2.D0.BE.D0.B9.D0.BD.D0.BE.D0.B5_.D1.85.D0.B5.D1.88.D0.B8.D1.80.D0.BE.D0.B2.D0.B0.D0.BD.D0.B8.D0.B5">Алгоритм двойного хеширования</a>
 */
public class CoolHashMap {
    /**
     * Емкость карты по умолчанию.
     */
    private static final int DEFAULT_INIT_CAPACITY = 16;

    /**
     * Граница загрузки по умолчанию.
     */
    private static final float DEFAULT_LOAD_BORDER = 0.75f;

    /**
     * Множитель увеличения емкости.
     * Определяет во сколько раз увеличится емкость карты при вызове метода resize.
     *
     * @see CoolHashMap#resize(int)
     */
    private static final float GROW_MULTIPLIER = 2f;

    /**
     * Размер карты.
     */
    private int size;

    /**
     * Граница загрузки.
     * Значение должно принадлежать интеравлу [0.4, 1].
     */
    private final float loadBorder;

    /**
     * Содержимое карты.
     */
    private Entity[] content;

    /**
     * Возникает при попытке создать карту с невалидными параметрами.
     */
    @SuppressWarnings("InnerClassMayBeStatic")
    class IllegalArgumentException extends RuntimeException {
        public IllegalArgumentException(String message) {
            super(message);
        }
    }

    /**
     * Используется для хранения одной записи в карте.
     */
    @AllArgsConstructor
    class Entity {
        /**
         * Основной хеш.
         */
        @Getter
        private int hashMain;

        /**
         * Второстепенный хеш.
         */
        @Getter
        private int hashSecondary;

        /**
         * Ключ.
         */
        private final int key;

        /**
         * Значение.
         */
        private long value;

        public Entity(int key, long value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Расчитывет хеши.
         * Второстепенный хеш зависит от текущей емкости карты.
         */
        private void countHash() {
            this.hashMain = getMainHash(key);
            this.hashSecondary = getSecondaryHash(key);
        }

        /**
         * Служит для создания копий обьекта Entity.
         *
         * @return Копия этого обьекта.
         */
        public Entity copy() {
            return new Entity(hashMain, hashSecondary, key, value);
        }
    }

    /**
     * Конструктор по умолчанию.
     *
     * @see CoolHashMap#DEFAULT_INIT_CAPACITY
     * @see CoolHashMap#DEFAULT_LOAD_BORDER
     */
    public CoolHashMap() {
        this(DEFAULT_INIT_CAPACITY, DEFAULT_LOAD_BORDER);
    }

    /**
     * Конструктор с возможностью задать начальную емкость.
     *
     * @param initCapacity Начальная емкость.
     * @see CoolHashMap#CoolHashMap(int, float)
     */
    public CoolHashMap(int initCapacity) {
        this(initCapacity, DEFAULT_LOAD_BORDER);
    }

    /**
     * Конструктор с возможностью задать начальную емкость и границу загрузки.
     * Значение начальной емкости должно быть больше 1. Требования к значению границы загрузки
     * указаны в описаниии к этому полю.
     *
     * @param initCapacity Начальная емкость.
     * @param loadBorder   Граница загрузки.
     * @see CoolHashMap#loadBorder
     */
    public CoolHashMap(int initCapacity, float loadBorder) {
        // Валидация входных данных
        if (initCapacity <= 0)
            throw new IllegalArgumentException("Illegal initial capacity : " + initCapacity);
        if ((loadBorder < 0.4) || (loadBorder > 1))
            throw new IllegalArgumentException("Illegal load border : " + loadBorder);

        this.loadBorder = loadBorder;
        this.content = new Entity[initCapacity];
        this.size = 0;
    }

    /**
     * Метод для вставки записи новых значений в карту или перезаписи уже существующих.
     *
     * @param key   Ключ для записи.
     * @param value Значение для записи.
     * @return Предыдущее значение по заданному ключу, если его не существуют то null.
     */
    public Long put(int key, long value) {
        return putVal(new Entity(key, value), false);
    }

    private Long putVal(Entity entity, boolean forceResize) {
        // Проверка емкости для записи нового значения
        checkCapacity(size + 1, forceResize);

        // Подсчет хешей
        entity.countHash();

        int capacity = content.length;
        int currentIdx = entity.getHashMain() % capacity;
        int step = entity.getHashSecondary();

        // Запись
        for (int i = 0; i < capacity; i++) {
            if (content[currentIdx] == null) {
                size++;
                content[currentIdx] = entity;
                return null;
            }
            if (content[currentIdx].key == entity.key) {
                long val = content[currentIdx].value;
                content[currentIdx].value = entity.value;
                return val;
            }
            currentIdx = (currentIdx + step) % capacity;
        }

        // Принудительное увеличение размера карты при невозможности записи
        return putVal(entity, true);
    }

    /**
     * Метод для получения значения по ключу.
     *
     * @param key Ключ для поиска значения.
     * @return Найденное значение или null, если его не существует.
     */
    public Long get(int key) {
        int capacity = content.length;
        int currentIdx = getMainHash(key) % capacity;
        int step = getSecondaryHash(key);

        for (int i = 0; i < capacity; i++) {
            if (content[currentIdx] == null)
                return null;
            if (content[currentIdx].key == key)
                return content[currentIdx].value;
            currentIdx = (currentIdx + step) % capacity;
        }
        return null;
    }

    /**
     * @return Размер карты.
     */
    public int getSize() {
        return this.size;
    }

    private void checkCapacity(int neededSize, boolean force) {
        int capacity = content.length;
        if (force) {
            resize(Math.max(neededSize, capacity));
        } else {
            if (neededSize > capacity * loadBorder)
                if (neededSize > capacity * GROW_MULTIPLIER * loadBorder)
                    resize((int) (neededSize / loadBorder));
                else
                    resize();
        }
    }

    private void resize() {
        resize(this.content.length);
    }

    private void resize(int currentCapacity) {
        // Увеличение емкости карты
        Entity[] oldContent = content;
        content = new Entity[(int) (currentCapacity * GROW_MULTIPLIER)];
        size = 0;

        // Запись значений из предидущей карты в новую
        for (Entity e : oldContent) {
            if (e != null) {
                putVal(e.copy(), false);
            }
        }
    }

    private int getMainHash(int key) {
        return Math.abs(key);
    }

    private int getSecondaryHash(int key) {
        return Math.abs(key != Integer.MIN_VALUE ? key : key + 1) % (this.content.length - 1) + 1;
    }
}
