package com.huazie.frame.db.jpa.dao.impl;

import com.huazie.frame.common.pool.FleaObjectPoolFactory;
import com.huazie.frame.common.util.CollectionUtils;
import com.huazie.frame.common.util.ObjectUtils;
import com.huazie.frame.db.common.exception.DaoException;
import com.huazie.frame.db.common.sql.pojo.SqlParam;
import com.huazie.frame.db.common.sql.template.ITemplate;
import com.huazie.frame.db.common.sql.template.SqlTemplateFactory;
import com.huazie.frame.db.common.sql.template.TemplateTypeEnum;
import com.huazie.frame.db.common.table.split.TableSplitHelper;
import com.huazie.frame.db.jpa.common.FleaJPAQuery;
import com.huazie.frame.db.jpa.common.FleaJPAQueryPool;
import com.huazie.frame.db.jpa.dao.interfaces.IAbstractFleaJPADAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p> 抽象Flea JPA DAO层实现类，该类实现了基本的增删改查功能，可以自行拓展 </p>
 *
 * @author huazie
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractFleaJPADAOImpl<T> implements IAbstractFleaJPADAO<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFleaJPADAOImpl.class);

    private Class<T> clazz;

    /**
     * <p> 获取T类型的Class对象 </p>
     *
     * @since 1.0.0
     */
    public AbstractFleaJPADAOImpl() {
        // 获取泛型类的子类对象的Class对象
        Class<?> clz = getClass();
        // 获取子类对象的泛型父类类型（也就是AbstractDaoImpl<T>）
        ParameterizedType type = (ParameterizedType) clz.getGenericSuperclass();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Type={}", type);
        }
        Type[] types = type.getActualTypeArguments();
        clazz = (Class<T>) types[0];
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ClassName={}", clazz.getName());
        }
    }

    @Override
    public T query(long entityId) throws Exception {
        return queryById(entityId, null);
    }

    @Override
    public T query(String entityId) throws Exception {
        return queryById(entityId, null);
    }

    @Override
    public T queryNew(long entityId, T entity) throws Exception {
        return queryById(entityId, entity);
    }

    @Override
    public T queryNew(String entityId, T entity) throws Exception {
        return queryById(entityId, entity);
    }

    /**
     * <p> 根据主键编号查询数据行对应的实体类信息 </p>
     *
     * @param entityId 主键编号
     * @return 数据行对应的实体类信息
     * @throws Exception
     * @since 1.0.0
     */
    protected T queryById(Object entityId, T entity) throws Exception {
        checkPrimaryKey(entityId);
        T t = getEntityManager(entity, true).find(clazz, entityId);
        return t;
    }

    @Override
    public List<T> query(Map<String, Object> paramMap) throws Exception {
        return getQuery(null).equal(paramMap).getResultList();
    }

    @Override
    public List<T> query(Map<String, Object> paramMap, String attrName, String orderBy) throws Exception {
        return getQuery(null).equal(paramMap).addOrderby(attrName, orderBy).getResultList();
    }

    @Override
    public List<T> query(Map<String, Object> paramMap, int start, int max) throws Exception {
        return getQuery(null).equal(paramMap).getResultList(start, max);
    }

    @Override
    public List<T> query(Map<String, Object> paramMap, String attrName, String orderBy, int start, int max)
            throws Exception {
        return getQuery(null).equal(paramMap).addOrderby(attrName, orderBy).getResultList(start, max);
    }

    @Override
    public List<T> queryAll() throws Exception {
        return getQuery(null).getResultList();
    }

    @Override
    public List<T> queryAll(String attrName, String orderBy) throws Exception {
        return getQuery(null).addOrderby(attrName, orderBy).getResultList();
    }

    @Override
    public List<T> queryAll(int start, int max) throws Exception {
        return getQuery(null).getResultList(start, max);
    }

    @Override
    public List<T> queryAll(String attrName, String orderBy, int start, int max) throws Exception {
        return getQuery(null).addOrderby(attrName, orderBy).getResultList(start, max);
    }

    @Override
    public long queryCount() throws Exception {
        // 调用SQL的COUNT函数统计数目 count()
        return ((Long) getQuery(Long.class).count().getSingleResult());
    }

    @Override
    public long queryCount(Map<String, Object> paramMap) throws Exception {
        // 添加Where子句查询条件 equal(Map)
        // 调用SQL的COUNT函数统计数目 count()
        return ((Long) getQuery(Long.class).equal(paramMap).count().getSingleResult());
    }

    @Override
    public List<T> query(Set<String> attrNames, T entity) throws Exception {
        return getQuery(null).initQueryEntity(entity).equal(attrNames).getResultList();
    }

    @Override
    public List<T> query(Set<String> attrNames, String attrName, String orderBy, T entity) throws Exception {
        return getQuery(null).initQueryEntity(entity).equal(attrNames).addOrderby(attrName, orderBy).getResultList();
    }

    @Override
    public List<T> query(Set<String> attrNames, int start, int max, T entity) throws Exception {
        return getQuery(null).initQueryEntity(entity).equal(attrNames).getResultList(start, max);
    }

    @Override
    public List<T> query(Set<String> attrNames, String attrName, String orderBy, int start, int max, T entity) throws Exception {
        return getQuery(null).initQueryEntity(entity).equal(attrNames).addOrderby(attrName, orderBy).getResultList(start, max);
    }

    @Override
    public List<T> queryAll(T entity) throws Exception {
        return getQuery(null).initQueryEntity(entity).getResultList();
    }

    @Override
    public List<T> queryAll(String attrName, String orderBy, T entity) throws Exception {
        return getQuery(null).initQueryEntity(entity).addOrderby(attrName, orderBy).getResultList();
    }

    @Override
    public List<T> queryAll(int start, int max, T entity) throws Exception {
        return getQuery(null).initQueryEntity(entity).getResultList(start, max);
    }

    @Override
    public List<T> queryAll(String attrName, String orderBy, int start, int max, T entity) throws Exception {
        return getQuery(null).initQueryEntity(entity).addOrderby(attrName, orderBy).getResultList(start, max);
    }

    @Override
    public long queryCount(T entity) throws Exception {
        // 调用SQL的COUNT函数统计数目 count()
        return ((Long) getQuery(Long.class).initQueryEntity(entity).count().getSingleResult());
    }

    @Override
    public long queryCount(Set<String> attrNames, T entity) throws Exception {
        // 添加Where子句查询条件 equal(Set)
        // 调用SQL的COUNT函数统计数目 count()
        return ((Long) getQuery(Long.class).initQueryEntity(entity).equal(attrNames).count().getSingleResult());
    }

    @Override
    public boolean remove(long entityId) throws Exception {
        return removeById(entityId, null);
    }

    @Override
    public boolean remove(String entityId) throws Exception {
        return removeById(entityId, null);
    }

    @Override
    public boolean removeNew(long entityId, T entity) throws Exception {
        return removeById(entityId, entity);
    }

    @Override
    public boolean removeNew(String entityId, T entity) throws Exception {
        return removeById(entityId, entity);
    }

    /**
     * <p> 根据主键编号删除指定行数据 </p>
     *
     * @param entityId 主键数据
     * @return true : 删除成功; false : 删除失败
     * @throws Exception
     * @since 1.0.0
     */
    protected boolean removeById(Object entityId, T entity) throws Exception {
        checkPrimaryKey(entityId);
        final T old = queryById(entityId, entity);
        if (ObjectUtils.isNotEmpty(old)) {
            getEntityManager(entity, false).remove(old);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public T update(T entity) throws Exception {
        if (ObjectUtils.isEmpty(entity)) {
            throw new DaoException("ERROR-DB-DAO0000000012");
        }
        return getEntityManager(entity, false).merge(entity);
    }

    @Override
    public List<T> batchUpdate(List<T> entities) throws Exception {
        if (CollectionUtils.isEmpty(entities)) {
            throw new DaoException("ERROR-DB-DAO0000000013");
        }
        for (T t : entities) {
            getEntityManager(t, false).merge(t);
        }
        return entities;
    }

    @Override
    public void save(T entity) throws Exception {
        if (ObjectUtils.isEmpty(entity)) {
            throw new DaoException("ERROR-DB-DAO0000000012");
        }
        getEntityManager(entity, false).persist(entity);
    }

    @Override
    public void batchSave(List<T> entities) throws Exception {
        if (CollectionUtils.isEmpty(entities)) {
            throw new DaoException("ERROR-DB-DAO0000000013");
        }
        for (T t : entities) {
            getEntityManager(t, false).persist(t);
        }
    }

    @Override
    public List<T> query(String relationId, T entity) throws Exception {
        return createNativeQuery(relationId, entity, null).getResultList();
    }

    @Override
    public Object querySingle(String relationId, T entity) throws Exception {
        return createNativeQuery(relationId, entity, Object.class).getSingleResult();
    }

    /**
     * <p> 构建原生查询对象 </p>
     *
     * @param relationId  关系编号
     * @param entity      实体类
     * @param resultClazz 查询结果Class
     * @return 实体类数据集合
     * @throws Exception
     * @since 1.0.0
     */
    private Query createNativeQuery(String relationId, T entity, Class resultClazz) throws Exception {
        // 构建并执行 SELECT SQL模板
        ITemplate<T> selectSqlTemplate = SqlTemplateFactory.newSqlTemplate(relationId, entity, TemplateTypeEnum.SELECT);
        selectSqlTemplate.initialize();
        String nativeSql = selectSqlTemplate.toNativeSql();
        List<SqlParam> nativeParam = selectSqlTemplate.toNativeParams();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("AbstractFleaJPADAOImpl##createNativeQuery(String, T, Class) SQL = {}", nativeSql);
        }

        Query query;
        if (ObjectUtils.isNotEmpty(resultClazz)) {
            query = getEntityManager().createNativeQuery(nativeSql, resultClazz);
        } else {
            query = getEntityManager().createNativeQuery(nativeSql, entity.getClass());
        }

        setParameter(query, nativeParam, TemplateTypeEnum.SELECT.getKey());

        return query;
    }

    @Override
    public int insert(String relationId, T entity) throws Exception {
        // 构建并执行INSERT SQL模板
        return save(SqlTemplateFactory.newSqlTemplate(relationId, entity, TemplateTypeEnum.INSERT));
    }

    @Override
    public int update(String relationId, T entity) throws Exception {
        // 构建并执行UPDATE SQL模板
        return save(SqlTemplateFactory.newSqlTemplate(relationId, entity, TemplateTypeEnum.UPDATE));
    }

    @Override
    public int delete(String relationId, T entity) throws Exception {
        // 构建并执行DELETE SQL模板
        return save(SqlTemplateFactory.newSqlTemplate(relationId, entity, TemplateTypeEnum.DELETE));
    }

    /**
     * <p> 处理INSERT,UPDATE,DELETE SQL模板 </p>
     *
     * @param sqlTemplate SQL模板（包含 INSERT,UPDATE,DELETE）
     * @throws Exception
     * @since 1.0.0
     */
    private int save(ITemplate<T> sqlTemplate) throws Exception {
        sqlTemplate.initialize();
        String nativeSql = sqlTemplate.toNativeSql();
        List<SqlParam> nativeParam = sqlTemplate.toNativeParams();

        if (LOGGER.isDebugEnabled()) {
            if (TemplateTypeEnum.INSERT.getKey().equals(sqlTemplate.getTemplateType().getKey())) {
                LOGGER.debug("AbstractFleaJPADAOImpl##insert(String, T) SQL = {}", nativeSql);
            } else if (TemplateTypeEnum.UPDATE.getKey().equals(sqlTemplate.getTemplateType().getKey())) {
                LOGGER.debug("AbstractFleaJPADAOImpl##update(String, T) SQL = {}", nativeSql);
            } else if (TemplateTypeEnum.DELETE.getKey().equals(sqlTemplate.getTemplateType().getKey())) {
                LOGGER.debug("AbstractFleaJPADAOImpl##delete(String, T) SQL = {}", nativeSql);
            }
        }

        Query query = getEntityManager().createNativeQuery(nativeSql);
        setParameter(query, nativeParam, sqlTemplate.getTemplateType().getKey());
        // 执行原生SQL语句（可能包含 INSERT, UPDATE, DELETE）
        return query.executeUpdate();
    }

    /**
     * <p> 校验主键合法性 </p>
     *
     * @param entityId 实体类对应的主键编号
     * @throws DaoException 数据操作层异常
     * @since 1.0.0
     */
    private void checkPrimaryKey(Object entityId) throws DaoException {
        if (entityId.getClass() == long.class || entityId.getClass() == Long.class) {
            if (Long.valueOf(entityId.toString()) <= 0) {
                // 主键字段必须是正整数
                throw new DaoException("ERROR-DB-DAO0000000009");
            }
        } else if (entityId.getClass() == String.class) {
            if (ObjectUtils.isEmpty(entityId)) {
                // 主键字段不能为空
                throw new DaoException("ERROR-DB-DAO0000000010");
            }
        } else {
            // 主键必须是long(Long) 或 String
            throw new DaoException("ERROR-DB-DAO0000000011");
        }
    }

    /**
     * <p> 为<code>Query</code>对象设置参数 </p>
     *
     * @param query     <code>Query</code>对象
     * @param sqlParams 原生Sql参数
     * @since 1.0.0
     */
    private void setParameter(Query query, List<SqlParam> sqlParams, String templateType) {
        if (CollectionUtils.isNotEmpty(sqlParams)) {
            for (SqlParam sqlParam : sqlParams) {
                if (TemplateTypeEnum.INSERT.getKey().equals(templateType)) {
                    LOGGER.debug("AbstractFleaJPADAOImpl##insert(String, T) COL{} = {}, PARAM{} = {}", sqlParam.getIndex(), sqlParam.getTabColName(), sqlParam.getIndex(), sqlParam.getAttrValue());
                } else if (TemplateTypeEnum.UPDATE.getKey().equals(templateType)) {
                    LOGGER.debug("AbstractFleaJPADAOImpl##update(String, T) COL{} = {}, PARAM{} = {}", sqlParam.getIndex(), sqlParam.getTabColName(), sqlParam.getIndex(), sqlParam.getAttrValue());
                } else if (TemplateTypeEnum.DELETE.getKey().equals(templateType)) {
                    LOGGER.debug("AbstractFleaJPADAOImpl##delete(String, T) COL{} = {}, PARAM{} = {}", sqlParam.getIndex(), sqlParam.getTabColName(), sqlParam.getIndex(), sqlParam.getAttrValue());
                } else if (TemplateTypeEnum.SELECT.getKey().equals(templateType)) {
                    LOGGER.debug("AbstractFleaJPADAOImpl##query(String, T) COL{} = {}, PARAM{} = {}", sqlParam.getIndex(), sqlParam.getTabColName(), sqlParam.getIndex(), sqlParam.getAttrValue());
                }
                query.setParameter(sqlParam.getIndex(), sqlParam.getAttrValue());
            }
        }
    }

    /**
     * <p> 获取指定的查询对象 </p>
     *
     * @return 自定义Flea JPA查询对象
     * @since 1.0.0
     */
    protected FleaJPAQuery getQuery(Class result) {
        // 获取Flea JPA查询对象池 （使用默认连接池名"default"即可）
        FleaJPAQueryPool pool = FleaObjectPoolFactory.getFleaObjectPool(FleaJPAQuery.class, FleaJPAQueryPool.class);
        if (ObjectUtils.isEmpty(pool)) {
            throw new RuntimeException("Can not get a object pool instance");
        }
        // 获取Flea JPA查询对象实例
        FleaJPAQuery query = pool.getFleaObject();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("AbstractFleaJPADAOImpl##getQuery(Class) FleaJPAQueryPool = {}", pool);
            LOGGER.debug("AbstractFleaJPADAOImpl##getQuery(Class) FleaJPAQuery = {}", query);
        }
        // 获取实例后必须调用该方法,对Flea JPA查询对象进行初始化
        query.init(getEntityManager(), clazz, result);
        return query;
    }

    /**
     * <p> 获取JPA持久化对象 </p>
     *
     * @return JPA持久化对象
     * @since 1.0.0
     */
    protected abstract EntityManager getEntityManager();

    /**
     * <p> 获取JPA持久化对象 </p>
     *
     * @param entity 实体类对象实例
     * @return JPA持久化对象
     * @since 1.0.0
     */
    protected EntityManager getEntityManager(T entity, boolean isRead) throws Exception {
        EntityManager entityManager = getEntityManager();
        // 处理并添加分表信息，如果不存在分表则不处理
        TableSplitHelper.findTableSplitHandle().handle(entityManager, entity, isRead);
        return entityManager;
    }

}