package org.justice.modelapicorereloaded.service;

import org.justice.modelapicorereloaded.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class ModelService {
    private final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final String conString;

    public Connection getConn()  {
        try {

            return DriverManager.getConnection(conString);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public ModelService(String string) {
        this.conString = string;
    }

    public void registerModel(Class<?> clazz) throws SQLException {
        if (!clazz.isAnnotationPresent(Model.class)) return;
        Model modelAnn = clazz.getAnnotation(Model.class);
        String tableName = modelAnn.tableName();

        StringBuilder create = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        List<String> columns = new ArrayList<>();
        List<String> foreignKeys = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ModelField.class)) {
                ModelField mf = field.getAnnotation(ModelField.class);
                String colName = field.getName();
                String colType = mf.columnType();
                String col = colName + " " + colType;
                if (field.isAnnotationPresent(Id.class)) col += " PRIMARY KEY";
                columns.add(col);
            } else if (field.isAnnotationPresent(ManyToOne.class)) {
                ManyToOne fk = field.getAnnotation(ManyToOne.class);
                String colName = field.getName() + "_id";
                columns.add(colName + " VARCHAR(36)");
                foreignKeys.add("FOREIGN KEY(" + colName + ") REFERENCES " +
                        getTableName(fk.targetEntity()) + "(" + fk.referencedColumn() + ")");
            }
        }

        columns.addAll(foreignKeys);
        create.append(String.join(", ", columns)).append(")");
        try (PreparedStatement ps = getConn().prepareStatement(create.toString())) {
            ps.execute();
        }

        // ManyToMany join table creation
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ManyToMany.class)) {
                ManyToMany mm = field.getAnnotation(ManyToMany.class);
                String joinSql = "CREATE TABLE IF NOT EXISTS " + mm.joinTable() + " ("
                        + mm.joinColumn() + " VARCHAR(36), "
                        + mm.inverseJoinColumn() + " VARCHAR(36), "
                        + "PRIMARY KEY(" + mm.joinColumn() + ", " + mm.inverseJoinColumn() + ")"
                        + ")";
                try (PreparedStatement ps = getConn().prepareStatement(joinSql)) {
                    ps.execute();
                }
            }
        }
    }

    private String getTableName(Class<?> clazz) {
        return clazz.isAnnotationPresent(Model.class) ? clazz.getAnnotation(Model.class).tableName() : clazz.getSimpleName().toLowerCase();
    }

    public <T> void save(T entity) throws Exception {
        Class<?> clazz = entity.getClass();
        if (!clazz.isAnnotationPresent(Model.class)) return;
        Model modelAnn = clazz.getAnnotation(Model.class);
        String tableName = modelAnn.tableName();

        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(ModelField.class)) {
                columns.add(field.getName());
                values.add("?");
                params.add(field.get(entity));
            } else if (field.isAnnotationPresent(ManyToOne.class)) {
                Object related = field.get(entity);
                if (related != null) {
                    Field relId = getIdField(related.getClass());
                    relId.setAccessible(true);
                    columns.add(field.getName() + "_id");
                    values.add("?");
                    params.add(relId.get(related));
                }
            }
        }

               // Építjük az UPDATE szakaszt a meglévő oszlopok alapján
/**            List<String> updateParts = new ArrayList<>();
            for (String column : columns) {
                updateParts.add(column + " = VALUES(" + column + ")");
            }

            String sql = "INSERT INTO " + tableName + " (" +
                    String.join(", ", columns) + ") VALUES (" +
                    String.join(", ", values) + ") ON DUPLICATE KEY UPDATE " +
                    String.join(", ", updateParts);

            try (PreparedStatement ps = getConn().prepareStatement(sql)) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                ps.execute();
            }
**/
// Építjük az INSERT OR REPLACE INTO szakaszt a meglévő oszlopok alapján
        String sql = "INSERT OR REPLACE INTO " + tableName + " (" +
                String.join(", ", columns) + ") VALUES (" +
                String.join(", ", values) + ")";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ps.execute();
        }

        cache.put(clazz, entity);

        // OneToMany kapcsolatok mentése
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToMany.class)) {
                field.setAccessible(true);
                Collection<?> relatedEntities = (Collection<?>) field.get(entity);
                if (relatedEntities != null) {
                    OneToMany otm = field.getAnnotation(OneToMany.class);
                    String mappedByField = otm.mappedBy();
                    for (Object relatedEntity : relatedEntities) {
                        Field mappedBy = relatedEntity.getClass().getDeclaredField(mappedByField);
                        mappedBy.setAccessible(true);
                        mappedBy.set(relatedEntity, entity);
                        save(relatedEntity);
                    }
                }
            }
        }

        // ManyToMany kapcsolatok mentése
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ManyToMany.class)) {
                field.setAccessible(true);
                Collection<?> relatedEntities = (Collection<?>) field.get(entity);
                if (relatedEntities != null) {
                    ManyToMany mtm = field.getAnnotation(ManyToMany.class);
                    String joinTable = mtm.joinTable();
                    String joinColumn = mtm.joinColumn();
                    String inverseJoinColumn = mtm.inverseJoinColumn();

                    Field idField = getIdField(clazz);
                    idField.setAccessible(true);
                    Object thisId = idField.get(entity);

                    String deleteSql = "DELETE FROM " + joinTable + " WHERE " + joinColumn + " = ?";
                    try (PreparedStatement ps = getConn().prepareStatement(deleteSql)) {
                        ps.setObject(1, thisId);
                        ps.execute();
                    }

                    String insertSql = "INSERT INTO " + joinTable + " (" + joinColumn + ", " + inverseJoinColumn + ") VALUES (?, ?)";
                    try (PreparedStatement ps = getConn().prepareStatement(insertSql)) {
                        for (Object related : relatedEntities) {
                            Field relatedId = getIdField(related.getClass());
                            relatedId.setAccessible(true);
                            ps.setObject(1, thisId);
                            ps.setObject(2, relatedId.get(related));
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
            }
        }
    }

    private Field getIdField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) return field;
        }
        throw new IllegalStateException("No @Id field found in " + clazz);
    }

    public <T> T findByColumn(Class<T> clazz, String columnName, Object value) throws Exception {
        if (!clazz.isAnnotationPresent(Model.class)) return null;
        Model modelAnn = clazz.getAnnotation(Model.class);
        String tableName = modelAnn.tableName();

        // Cache ellenőrzés: ez most már nem csak ID alapján működik, ezért cache-t ide nem használunk egyszerűen (vagy át kell írni bonyolultabbá)
        // Cache kikapcsolva ebben az esetben, vagy implementálható más módon

        String sql = "SELECT * FROM " + tableName + " WHERE " + columnName + " = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                T instance = clazz.getDeclaredConstructor().newInstance();

                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(ModelField.class)) {
                        field.set(instance, rs.getObject(field.getName()));
                    } else if (field.isAnnotationPresent(ManyToOne.class)) {
                        Class<?> relatedClass = field.getType();
                        Object relId = rs.getObject(field.getName() + "_id");
                        if (relId != null) {
                            Object related = findById(relatedClass, relId);
                            field.set(instance, related);
                        }
                    }
                }

                // OneToMany betöltés
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(OneToMany.class)) {
                        OneToMany otm = field.getAnnotation(OneToMany.class);
                        String mappedBy = otm.mappedBy();
                        ParameterizedType listType = (ParameterizedType) field.getGenericType();
                        Class<?> relatedType = (Class<?>) listType.getActualTypeArguments()[0];
                        String relatedTable = getTableName(relatedType);

                        // Azonosító lekérése az instance-ból
                        Field idField = getIdField(clazz);
                        idField.setAccessible(true);
                        Object idValue = idField.get(instance);

                        String sqlChild = "SELECT * FROM " + relatedTable + " WHERE " + mappedBy + "_id = ?";
                        try (PreparedStatement psChild = getConn().prepareStatement(sqlChild)) {
                            psChild.setObject(1, idValue);
                            try (ResultSet rsChild = psChild.executeQuery()) {
                                List<Object> children = new ArrayList<>();
                                while (rsChild.next()) {
                                    Object child = relatedType.getDeclaredConstructor().newInstance();
                                    for (Field childField : relatedType.getDeclaredFields()) {
                                        childField.setAccessible(true);
                                        if (childField.isAnnotationPresent(ModelField.class)) {
                                            childField.set(child, rsChild.getObject(childField.getName()));
                                        } else if (childField.isAnnotationPresent(ManyToOne.class)) {
                                            if (childField.getName().equals(mappedBy)) {
                                                childField.set(child, instance); // beállítjuk a kapcsolatot
                                            } else {
                                                Object relId = rsChild.getObject(childField.getName() + "_id");
                                                if (relId != null) {
                                                    Object rel = findById(childField.getType(), relId);
                                                    childField.set(child, rel);
                                                }
                                            }
                                        }
                                    }
                                    children.add(child);
                                }
                                field.set(instance, children);
                            }
                        }
                    }
                }

                // ManyToMany betöltés
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(ManyToMany.class)) {
                        ManyToMany mtm = field.getAnnotation(ManyToMany.class);
                        Class<?> target = mtm.targetEntity();
                        String joinTable = mtm.joinTable();
                        String joinColumn = mtm.joinColumn();
                        String inverseColumn = mtm.inverseJoinColumn();

                        Field idField = getIdField(clazz);
                        idField.setAccessible(true);
                        Object idValue = idField.get(instance);

                        String sqlJoin = "SELECT " + inverseColumn + " FROM " + joinTable + " WHERE " + joinColumn + " = ?";
                        try (PreparedStatement psJoin = getConn().prepareStatement(sqlJoin)) {
                            psJoin.setObject(1, idValue);
                            try (ResultSet rsJoin = psJoin.executeQuery()) {
                                List<Object> relatedList = new ArrayList<>();
                                while (rsJoin.next()) {
                                    Object relId = rsJoin.getObject(1);
                                    Object related = findById(target, relId);
                                    if (related != null) {
                                        relatedList.add(related);
                                    }
                                }
                                field.setAccessible(true);
                                field.set(instance, relatedList);
                            }
                        }
                    }
                }

                // Cache feltöltés, opcionálisan, csak ha a keresés az ID oszlopon volt:
                Field idField = getIdField(clazz);
                idField.setAccessible(true);
                Object idVal = idField.get(instance);
                if (columnName.equals(idField.getName()) && idVal != null) {
                    cache.put(clazz, instance);
                }

                return instance;
            }
        }
    }

    public <T> T findById(Class<T> clazz, Object id) throws Exception {
        if (!clazz.isAnnotationPresent(Model.class)) return null;
        Model modelAnn = clazz.getAnnotation(Model.class);
        String tableName = modelAnn.tableName();

        // Cache ellenőrzés
        if (cache.containsKey(clazz)) {
            T cached = (T) cache.get(clazz);
            Field idField = getIdField(clazz);
            idField.setAccessible(true);
            if (Objects.equals(idField.get(cached), id)) {
                return cached;
            }
        }

        Field idField = getIdField(clazz);

        String sql = "SELECT * FROM " + tableName + " WHERE " + idField.getName() + " = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                T instance = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(ModelField.class)) {
                        field.set(instance, rs.getObject(field.getName()));
                    } else if (field.isAnnotationPresent(ManyToOne.class)) {
                        Class<?> relatedClass = field.getType();
                        Object relId = rs.getObject(field.getName() + "_id");
                        if (relId != null) {
                            Object related = findById(relatedClass, relId);
                            field.set(instance, related);
                        }
                    }
                }

                // OneToMany betöltés
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(OneToMany.class)) {
                        OneToMany otm = field.getAnnotation(OneToMany.class);
                        String mappedBy = otm.mappedBy();
                        ParameterizedType listType = (ParameterizedType) field.getGenericType();
                        Class<?> relatedType = (Class<?>) listType.getActualTypeArguments()[0];
                        String relatedTable = getTableName(relatedType);
                        String sqlChild = "SELECT * FROM " + relatedTable + " WHERE " + mappedBy + "_id = ?";
                        try (PreparedStatement psChild = getConn().prepareStatement(sqlChild)) {
                            psChild.setObject(1, id);
                            try (ResultSet rsChild = psChild.executeQuery()) {
                                List<Object> children = new ArrayList<>();
                                while (rsChild.next()) {
                                    Object child = relatedType.getDeclaredConstructor().newInstance();
                                    for (Field childField : relatedType.getDeclaredFields()) {
                                        childField.setAccessible(true);
                                        if (childField.isAnnotationPresent(ModelField.class)) {
                                            childField.set(child, rsChild.getObject(childField.getName()));
                                        } else if (childField.isAnnotationPresent(ManyToOne.class)) {
                                            if (childField.getName().equals(mappedBy)) {
                                                childField.set(child, instance); // beállítjuk a kapcsolatot
                                            } else {
                                                Object relId = rsChild.getObject(childField.getName() + "_id");
                                                if (relId != null) {
                                                    Object rel = findById(childField.getType(), relId);
                                                    childField.set(child, rel);
                                                }
                                            }
                                        }
                                    }
                                    children.add(child);
                                }
                                field.set(instance, children);
                            }
                        }
                    }
                }

                // ManyToMany betöltés
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(ManyToMany.class)) {
                        ManyToMany mtm = field.getAnnotation(ManyToMany.class);
                        Class<?> target = mtm.targetEntity();
                        String joinTable = mtm.joinTable();
                        String joinColumn = mtm.joinColumn();
                        String inverseColumn = mtm.inverseJoinColumn();

                        String sqlJoin = "SELECT " + inverseColumn + " FROM " + joinTable + " WHERE " + joinColumn + " = ?";
                        try (PreparedStatement psJoin = getConn().prepareStatement(sqlJoin)) {
                            psJoin.setObject(1, id);
                            try (ResultSet rsJoin = psJoin.executeQuery()) {
                                List<Object> relatedList = new ArrayList<>();
                                while (rsJoin.next()) {
                                    Object relId = rsJoin.getObject(1);
                                    Object related = findById(target, relId);
                                    if (related != null) {
                                        relatedList.add(related);
                                    }
                                }
                                field.setAccessible(true);
                                field.set(instance, relatedList);
                            }
                        }
                    }
                }

                cache.put(clazz, instance);
                return instance;
            }
        }
    }

    public <T> void delete(T entity) throws Exception {
        Class<?> clazz = entity.getClass();
        if (!clazz.isAnnotationPresent(Model.class)) return;

        Field idField = getIdField(clazz);
        idField.setAccessible(true);
        Object id = idField.get(entity);

        String tableName = clazz.getAnnotation(Model.class).tableName();
        String sql = "DELETE FROM " + tableName + " WHERE " + idField.getName() + " = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.execute();
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ManyToMany.class)) {
                ManyToMany mm = field.getAnnotation(ManyToMany.class);
                String joinTable = mm.joinTable();
                String joinColumn = mm.joinColumn();

                String joinSql = "DELETE FROM " + joinTable + " WHERE " + joinColumn + " = ?";
                try (PreparedStatement ps = getConn().prepareStatement(joinSql)) {
                    ps.setObject(1, id);
                    ps.execute();
                }
            }
        }

        cache.remove(clazz);
    }
    public <T> void deleteAsync(T entity) {
        executor.submit(() -> {
            try {
                delete(entity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    public <T> List<T> findAll(Class<T> clazz) throws Exception {
        if (!clazz.isAnnotationPresent(Model.class)) return Collections.emptyList();

        String tableName = clazz.getAnnotation(Model.class).tableName();
        String sql = "SELECT * FROM " + tableName;

        List<T> results = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                T instance = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(ModelField.class)) {
                        field.set(instance, rs.getObject(field.getName()));
                    } else if (field.isAnnotationPresent(ManyToOne.class)) {
                        Class<?> relatedClass = field.getType();
                        Object relId = rs.getObject(field.getName() + "_id");
                        if (relId != null) {
                            Object related = findById(relatedClass, relId);
                            field.set(instance, related);
                        }
                    }
                }
                results.add(instance);
                cache.put(clazz, instance);
            }
        }
        return results;
    }
    public <T> List<T> findAll(Class<T> clazz, String column, Object value) throws Exception {
        if (!clazz.isAnnotationPresent(Model.class)) return Collections.emptyList();

        String tableName = clazz.getAnnotation(Model.class).tableName();
        String sql = "SELECT * FROM " + tableName + " WHERE " + column + " = " + value;

        List<T> results = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                T instance = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(ModelField.class)) {
                        field.set(instance, rs.getObject(field.getName()));
                    } else if (field.isAnnotationPresent(ManyToOne.class)) {
                        Class<?> relatedClass = field.getType();
                        Object relId = rs.getObject(field.getName() + "_id");
                        if (relId != null) {
                            Object related = findById(relatedClass, relId);
                            field.set(instance, related);
                        }
                    }
                }
                results.add(instance);
                cache.put(clazz, instance);
            }
        }
        return results;
    }

    public <T> void saveAsync(T entity) {
        executor.submit(() -> {
            try {
                save(entity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
