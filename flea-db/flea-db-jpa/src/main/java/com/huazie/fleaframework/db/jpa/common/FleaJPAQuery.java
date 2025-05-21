package com.huazie.fleaframework.db.jpa.common;

import com.huazie.fleaframework.common.exceptions.CommonException;
import com.huazie.fleaframework.common.slf4j.FleaLogger;
import com.huazie.fleaframework.common.slf4j.impl.FleaLoggerProxy;
import com.huazie.fleaframework.common.util.CollectionUtils;
import com.huazie.fleaframework.common.util.ExceptionUtils;
import com.huazie.fleaframework.common.util.MapUtils;
import com.huazie.fleaframework.common.util.NumberUtils;
import com.huazie.fleaframework.common.util.ObjectUtils;
import com.huazie.fleaframework.common.util.ReflectUtils;
import com.huazie.fleaframework.common.util.StringUtils;
import com.huazie.fleaframework.db.common.DBConstants.SQLConstants;
import com.huazie.fleaframework.db.common.exceptions.DaoException;
import com.huazie.fleaframework.db.jpa.FleaJPASplitHelper;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 自定义Flea JPA查询对象，封装了JPA标准化查询的常用API，
 * 通过自由组装各种常见的查询条件，使数据的查询更加灵活方便。
 *
 * <p> 下面介绍下Flea JPA查询对象的使用，大致分为如下四步：
 *
 * <p> 第一步，获取Flea JPA查询对象，请参考Flea JPA查询对象池；
 * <pre>举例如下：
 *   // 从Flea JPA查询对象池中获取
 *   FleaJPAQuery query = pool.getFleaObject();
 * </pre>
 *
 * <p> 第二步，调用 init 方法进行初始化；如果需要分库分表的场景，
 * 则需要再调用 initQueryEntity 方法，初始化查询实体对象；
 * <pre>举例如下：
 *   // 初始化Flea JPA查询对象
 *   query.init(entityManager, sourceClazz, resultClazz);
 *   // 初始化查询实体对象
 *   query.initQueryEntity(entity);
 * </pre>
 *
 * <p> 第三步，自由组装各种常见的查询条件；
 * <pre>举例如下：
 *   // 组装查询条件
 *   query.equal(attrName1, attrValue);
 *   query.like(attrName2, attrValue);
 *   query.in(attrName3, value);
 *
 *   // 当然上述查询条件可以写在一起
 *   query.equal(attrName1, attrValue)
 *        .like(attrName2, attrValue)
 *        .in(attrName3, value);
 *
 *   // 如果第二步已经初始化过查询实体对象，也可以这样组装查询条件
 *   // 其中各attrName对应的值，定义在查询实体entity对象示例里。
 *   query.equal(attrName1).like(attrName2).in(attrName3, value);
 * </pre>
 *
 * <p> 第四步，获取JPA的查询结果。
 * <pre>举例如下：
 *   // 获取查询的记录行结果集合
 *   query.getResultList();
 *
 *   // 设置查询范围，可用于分页
 *   query.getResultList(start, max);
 *
 *   // 以分页方式获取查询的记录行结果集合
 *   query.getResultList4Page(pageNum, pageCount);
 *
 *   // 获取查询的单个结果
 *   query.getSingleResult();
 *
 *   // 获取查询的单个属性列结果集合
 *   query.getSingleResultList();
 *
 *   // 设置查询范围
 *   query.getSingleResultList(start, max);
 * </pre>
 *
 * @author huazie
 * @version 2.0.0
 * @see FleaJPAQueryPool
 * @see FleaJPAQueryPoolConfig
 * @since 1.0.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class FleaJPAQuery implements Closeable {

    private static final FleaLogger LOGGER = FleaLoggerProxy.getProxyInstance(FleaJPAQuery.class);

    private FleaJPAQueryPool fleaObjectPool; // Flea JPA查询对象池

    private EntityManager entityManager; // JPA中用于增删改查的持久化接口

    private Class sourceClazz; // 实体类类对象

    private Class resultClazz; // 操作结果类类对象

    private Root root; // 根SQL表达式对象

    private CriteriaBuilder criteriaBuilder; // 标准化生成器

    private CriteriaQuery criteriaQuery; // 标准化查询对象

    private List<Predicate> predicates = new ArrayList<>(); // Where条件集合

    private List<Order> orders; // 排序集合

    private List<Expression> groups; // 分组集合

    private Object entity; // 查询的数据库实体类实例

    public FleaJPAQuery() {
    }

    @Override
    public void close() {
        if (ObjectUtils.isNotEmpty(fleaObjectPool)) {
            fleaObjectPool.returnFleaObject(this);
            fleaObjectPool = null;
        }
    }

    /**
     * Flea JPA查询对象池获取之后，一定要调用该方法进行初始化。
     *
     * @param entityManager JPA中用于增删改查的接口
     * @param sourceClazz   实体类类对象
     * @param resultClazz   操作结果类类对象
     * @since 1.0.0
     */
    public void init(EntityManager entityManager, Class sourceClazz, Class resultClazz) {

        this.entityManager = entityManager;
        this.sourceClazz = sourceClazz;
        this.resultClazz = resultClazz;
        // 从持久化接口中获取标准化生成器
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        // 通过标准化生成器 获取 标准化查询对象
        if (ObjectUtils.isEmpty(resultClazz)) {
            // 行记录查询结果
            this.criteriaQuery = criteriaBuilder.createQuery(sourceClazz);
        } else {
            // 单个查询结果
            this.criteriaQuery = criteriaBuilder.createQuery(resultClazz);
        }
        // 通过标准化查询对象，获取根SQL表达式对象
        this.root = criteriaQuery.from(sourceClazz);
    }

    /**
     * 初始化查询实体，主要用来构建查询条件值，以及分库分表
     *
     * @param entity 实体类实例
     * @return FleaJPAQuery对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery initQueryEntity(Object entity) throws CommonException {

        if (ObjectUtils.isNotEmpty(entity) && (ObjectUtils.isNotEmpty(sourceClazz) && sourceClazz.isInstance(entity))) {
            this.entity = entity;
            FleaJPASplitHelper.getHandler().handle(this, entity);
        } else {
            LOGGER.error1(new Object() {}, "初始化查询实体类【entity = {}】不是【sourceClazz = {}】的实例，请检查!", entity, sourceClazz);
        }
        return this;
    }

    /**
     * 等于条件；调用该方法前需要先初始化实体类，
     * 即调用 initQueryEntity(Object entity)。
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery equal(String attrName) throws CommonException {
        return equal(attrName, getAttrValue(attrName));
    }

    /**
     * 等于条件
     *
     * @param attrName 属性名
     * @param value    属性值
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery equal(String attrName, Object value) throws CommonException {
        return newEqualExpression(attrName, value, true);
    }

    /**
     * 等于条件；调用该方法前需要先初始化实体类，
     * 即调用 initQueryEntity(Object entity)。
     *
     * @param attrNames 实体属性名集合
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @see #initQueryEntity(Object)
     * @since 1.0.0
     */
    public FleaJPAQuery equal(Set<String> attrNames) throws CommonException {
        return newEqualExpression(attrNames, true);
    }

    /**
     * 等于条件
     *
     * @param paramMap 参数集合
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery equal(Map<String, Object> paramMap) throws CommonException {
        return newEqualExpression(paramMap, true);
    }

    /**
     * 不等于条件；调用该方法前需要先初始化实体类，
     * 即调用 initQueryEntity(Object entity)。
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @see #initQueryEntity(Object)
     * @since 1.0.0
     */
    public FleaJPAQuery notEqual(String attrName) throws CommonException {
        return notEqual(attrName, getAttrValue(attrName));
    }

    /**
     * 不等于条件
     *
     * @param attrName 属性名
     * @param value    属性值
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery notEqual(String attrName, Object value) throws CommonException {
        return newEqualExpression(attrName, value, false);
    }

    /**
     * 不等于条件；调用该方法前需要先初始化实体类，
     * 即调用 initQueryEntity(Object entity)。
     *
     * @param attrNames 实体属性名集合
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @see #initQueryEntity(Object)
     * @since 1.0.0
     */
    public FleaJPAQuery notEqual(Set<String> attrNames) throws CommonException {
        return newEqualExpression(attrNames, false);
    }

    /**
     * 不等于条件
     *
     * @param paramMap 条件集合
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery notEqual(Map<String, Object> paramMap) throws CommonException {
        return newEqualExpression(paramMap, false);
    }

    /**
     * 构建 equal 或 notEqual 表达式
     *
     * @param attrName 属性名
     * @param value    属性值
     * @param isEqual  true: 构建equal表达式; false: 构建notEqual表达式
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    private FleaJPAQuery newEqualExpression(String attrName, Object value, boolean isEqual) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (ObjectUtils.isEmpty(value)) {
            // 不做处理，直接返回即可
            return this;
        }
        Object obj = new Object() {};
        if (isEqual) {
            LOGGER.debug1(obj, "Equal, attrName = {}, value = {}", attrName, value);
            predicates.add(criteriaBuilder.equal(root.get(attrName), value));
        } else {
            LOGGER.debug1(obj, "Not Equal, attrName = {}, value = {}", attrName, value);
            predicates.add(criteriaBuilder.notEqual(root.get(attrName), value));
        }
        return this;
    }

    /**
     * 构建 equal 或 notEqual 表达式
     *
     * @param attrNames 属性名集合
     * @param isEqual   true: 构建equal表达式; false: 构建notEqual表达式
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    private FleaJPAQuery newEqualExpression(Set<String> attrNames, boolean isEqual) throws CommonException {

        // 【{0}】不能为空
        CollectionUtils.checkEmpty(attrNames, DaoException.class, "ERROR-DB-DAO0000000002", "attrNames");

        Object obj = new Object() {};
        if (isEqual) {
            LOGGER.debug1(obj, "Equal, attrNames = {}", attrNames);
        } else {
            LOGGER.debug1(obj, "Not Equal, attrNames = {}", attrNames);
        }
        for (String attrName : attrNames) {
            Object attrValue = getAttrValue(attrName);
            if (ObjectUtils.isEmpty(attrValue)) {
                // 属性值为空，跳过处理下一个
                continue;
            }
            if (isEqual) {
                predicates.add(criteriaBuilder.equal(root.get(attrName), attrValue));
            } else {
                predicates.add(criteriaBuilder.notEqual(root.get(attrName), attrValue));
            }
        }
        return this;
    }

    /**
     * 构建 equal 或 notEqual 表达式
     *
     * @param paramMap 条件集合
     * @param isEqual  true: 构建equal表达式; false: 构建notEqual表达式
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    private FleaJPAQuery newEqualExpression(Map<String, Object> paramMap, boolean isEqual) throws CommonException {

        // 【{0}】不能为空
        MapUtils.checkEmpty(paramMap, DaoException.class, "ERROR-DB-DAO0000000002", "paramMap");

        Object obj = new Object() {};
        if (isEqual) {
            LOGGER.debug1(obj, "Equal, paramMap = {}", paramMap);
        } else {
            LOGGER.debug1(obj, "Not Equal, paramMap = {}", paramMap);
        }
        Set<String> keySet = paramMap.keySet();
        for (String key : keySet) {
            Object value = paramMap.get(key);
            if (isEqual) {
                predicates.add(criteriaBuilder.equal(root.get(key), value));
            } else {
                predicates.add(criteriaBuilder.notEqual(root.get(key), value));
            }
        }
        return this;
    }

    /**
     * 某属性值为空的条件
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery isNull(String attrName) throws CommonException {
        return newIsNullExpression(attrName, true);
    }

    /**
     * 某属性值为非空的条件
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery isNotNull(String attrName) throws CommonException {
        return newIsNullExpression(attrName, false);
    }

    /**
     * 构建 isNull 或 isNotNull 表达式
     *
     * @param attrName 属性名
     * @param isNull   true: 构建isNull表达式; false: 构建isNotNull表达式
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    private FleaJPAQuery newIsNullExpression(String attrName, boolean isNull) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        Object obj = new Object() {};
        if (isNull) {
            LOGGER.debug1(obj, "Null, attrName = {}", attrName);
        } else {
            LOGGER.debug1(obj, "Not Null, attrName = {}", attrName);
        }
        if (isNull) {
            predicates.add(criteriaBuilder.isNull(root.get(attrName)));
        } else {
            predicates.add(criteriaBuilder.isNotNull(root.get(attrName)));
        }
        return this;
    }

    /**
     * in条件，attrName属性的值在value集合中
     *
     * @param attrName 属性名称
     * @param value    值集合
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery in(String attrName, Collection value) throws CommonException {
        return newInExpression(attrName, value, true);
    }

    /**
     * not in条件，attrName属性的值不在value集合中
     *
     * @param attrName 属性名称
     * @param value    值集合
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery notIn(String attrName, Collection value) throws CommonException {
        return newInExpression(attrName, value, false);
    }

    /**
     * 构建 In 或 notIn 表达式
     *
     * @param attrName 属性名称
     * @param value    值集合
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    private FleaJPAQuery newInExpression(String attrName, Collection value, boolean isIn) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (CollectionUtils.isEmpty(value)) {
            // 不做处理，直接返回即可
            return this;
        }
        Object obj = new Object() {};
        if (isIn) {
            LOGGER.debug1(obj, "In, attrName = {}, value = {}", attrName, value);
        } else {
            LOGGER.debug1(obj, "Not In, attrName = {}, value = {}", attrName, value);
        }
        Iterator iterator = value.iterator();
        In in = criteriaBuilder.in(root.get(attrName));
        while (iterator.hasNext()) {
            in.value(iterator.next());
        }
        if (isIn) {
            predicates.add(in);
        } else {
            predicates.add(criteriaBuilder.not(in));
        }
        return this;
    }

    /**
     * 模糊匹配条件；调用该方法前需要先初始化实体类，
     * 即调用 initQueryEntity(Object entity)。
     *
     * @param attrName 属性名称
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @see #initQueryEntity(Object)
     * @since 1.0.0
     */
    public FleaJPAQuery like(String attrName) throws CommonException {
        return like(attrName, StringUtils.valueOf(getAttrValue(attrName)));
    }

    /**
     * 模糊匹配条件
     *
     * @param attrName 属性名称
     * @param value    属性值
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery like(String attrName, String value) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (StringUtils.isBlank(value)) {
            // 不做处理，直接返回即可
            return this;
        }
        LOGGER.debug1(new Object() {}, "attrName = {}, value = {}", attrName, value);
        if (!value.contains(SQLConstants.SQL_PERCENT)) {
            value = SQLConstants.SQL_PERCENT + value + SQLConstants.SQL_PERCENT;
        }
        predicates.add(criteriaBuilder.like(root.get(attrName), value));
        return this;
    }

    /**
     * 小于等于条件；调用该方法前需要先初始化实体类，
     * 即调用 initQueryEntity(Object entity)。
     *
     * @param attrName 属性名称
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @see #initQueryEntity(Object)
     * @since 1.0.0
     */
    public FleaJPAQuery le(String attrName) throws CommonException {
        return le(attrName, getNumberAttrValue(attrName));
    }

    /**
     * 小于等于条件
     *
     * @param attrName 属性名称
     * @param value    属性值
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery le(String attrName, Number value) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (ObjectUtils.isEmpty(value)) {
            // 不做处理，直接返回即可
            return this;
        }
        LOGGER.debug1(new Object() {}, "le, attrName = {}, value = {}", attrName, value);
        predicates.add(criteriaBuilder.le(root.get(attrName), value));
        return this;
    }

    /**
     * 小于条件；调用该方法前需要先初始化实体类，
     * 即调用 initQueryEntity(Object entity)。
     *
     * @param attrName 属性名称
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @see #initQueryEntity(Object)
     * @since 1.0.0
     */
    public FleaJPAQuery lt(String attrName) throws CommonException {
        return lt(attrName, getNumberAttrValue(attrName));
    }

    /**
     * 小于条件
     *
     * @param attrName 属性名称
     * @param value    属性值
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery lt(String attrName, Number value) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (ObjectUtils.isEmpty(value)) {
            // 不做处理，直接返回即可
            return this;
        }
        LOGGER.debug1(new Object() {}, "lt, attrName = {}, value = {}", attrName, value);
        predicates.add(criteriaBuilder.lt(root.get(attrName), value));
        return this;
    }

    /**
     * 大于等于条件；调用该方法前需要先初始化实体类，
     * 即调用 initQueryEntity(Object entity)。
     *
     * @param attrName 属性名称
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @see #initQueryEntity(Object)
     * @since 1.0.0
     */
    public FleaJPAQuery ge(String attrName) throws CommonException {
        return ge(attrName, getNumberAttrValue(attrName));
    }

    /**
     * 大于等于条件
     *
     * @param attrName 属性名称
     * @param value    属性值
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery ge(String attrName, Number value) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (ObjectUtils.isEmpty(value)) {
            // 不做处理，直接返回即可
            return this;
        }
        LOGGER.debug1(new Object() {}, "ge, attrName = {}, value = {}", attrName, value);
        predicates.add(criteriaBuilder.ge(root.get(attrName), value));
        return this;
    }

    /**
     * 大于条件；调用该方法前需要先初始化实体类，
     * 即调用 initQueryEntity(Object entity)。
     *
     * @param attrName 属性名称
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @see #initQueryEntity(Object)
     * @since 1.0.0
     */
    public FleaJPAQuery gt(String attrName) throws CommonException {
        return gt(attrName, getNumberAttrValue(attrName));
    }

    /**
     * 大于条件
     *
     * @param attrName 属性名称
     * @param value    属性值
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery gt(String attrName, Number value) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (ObjectUtils.isEmpty(value)) {
            // 不做处理，直接返回即可
            return this;
        }
        LOGGER.debug1(new Object() {}, "gt, attrName = {}, value = {}", attrName, value);
        predicates.add(criteriaBuilder.gt(root.get(attrName), value));
        return this;
    }

    /**
     * 时间区间查询条件
     *
     * @param attrName  属性名
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery between(String attrName, Date startTime, Date endTime) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (ObjectUtils.isEmpty(startTime) || ObjectUtils.isEmpty(endTime)) {
            // 开始时间或结束时间为空
            ExceptionUtils.throwCommonException(DaoException.class, "ERROR-DB-DAO0000000003");
        }
        if (startTime.after(endTime)) {
            // 开始时间必须小于结束时间
            ExceptionUtils.throwCommonException(DaoException.class, "ERROR-DB-DAO0000000004");
        }
        LOGGER.debug1(new Object() {}, "attrName = {}, startTime = {}, endTime = {}", attrName, startTime, endTime);
        predicates.add(criteriaBuilder.between(root.get(attrName), startTime, endTime));
        return this;
    }

    /**
     * 大于某个日期值条件；调用该方法前需要先初始化实体类，
     * 即调用 initQueryEntity(Object entity)。
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @see #initQueryEntity(Object)
     * @since 1.0.0
     */
    public FleaJPAQuery greaterThan(String attrName) throws CommonException {
        return greaterThan(attrName, getDateAttrValue(attrName));
    }

    /**
     * 大于某个日期值条件
     *
     * @param attrName 属性名
     * @param value    指定日期值
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery greaterThan(String attrName, Date value) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (ObjectUtils.isEmpty(value)) {
            // 不做处理，直接返回即可
            return this;
        }
        LOGGER.debug1(new Object() {}, ">, attrName = {}, value = {}", attrName, value);
        predicates.add(criteriaBuilder.greaterThan(root.get(attrName), value));
        return this;
    }

    /**
     * 大于等于某个日期值；调用该方法前需要先初始化实体类，
     * 即调用 initQueryEntity(Object entity)。
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @see #initQueryEntity(Object)
     * @since 1.0.0
     */
    public FleaJPAQuery greaterThanOrEqualTo(String attrName) throws CommonException {
        return greaterThanOrEqualTo(attrName, getDateAttrValue(attrName));
    }

    /**
     * 大于等于某个日期值
     *
     * @param attrName 属性名
     * @param value    指定日期值
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery greaterThanOrEqualTo(String attrName, Date value) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (ObjectUtils.isEmpty(value)) {
            // 不做处理，直接返回即可
            return this;
        }
        LOGGER.debug1(new Object() {}, ">=, attrName = {}, value = {}", attrName, value);
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(attrName), value));
        return this;
    }

    /**
     * 小于某个日期值条件; 调用该方法需要先初始化实体类，
     * 即调用 initQueryEntity(Object entity)。
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @see #initQueryEntity(Object)
     * @since 1.0.0
     */
    public FleaJPAQuery lessThan(String attrName) throws CommonException {
        return lessThan(attrName, getDateAttrValue(attrName));
    }

    /**
     * 小于某个日期值条件
     *
     * @param attrName 属性名
     * @param value    指定日期值
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery lessThan(String attrName, Date value) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (ObjectUtils.isEmpty(value)) {
            // 不做处理，直接返回即可
            return this;
        }
        LOGGER.debug1(new Object() {}, "<, attrName = {}, value = {}", attrName, value);
        predicates.add(criteriaBuilder.lessThan(root.get(attrName), value));
        return this;
    }

    /**
     * 小于等于某个日期值条件；调用该方法需要先初始化实体类,
     * 即调用 initQueryEntity(Object entity) 。
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @see #initQueryEntity(Object)
     * @since 1.0.0
     */
    public FleaJPAQuery lessThanOrEqualTo(String attrName) throws CommonException {
        return lessThanOrEqualTo(attrName, getDateAttrValue(attrName));
    }

    /**
     * 小于等于某个日期值条件
     *
     * @param attrName 属性名
     * @param value    指定日期值
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery lessThanOrEqualTo(String attrName, Date value) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (ObjectUtils.isEmpty(value)) {
            // 不做处理，直接返回即可
            return this;
        }
        LOGGER.debug1(new Object() {}, "<=, attrName = {}, value = {}", attrName, value);
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(attrName), value));
        return this;
    }

    /**
     * 统计数目，在getSingleResult调用之前使用
     *
     * @return Flea JPA查询对象
     * @since 1.0.0
     */
    public FleaJPAQuery count() {
        criteriaQuery.select(criteriaBuilder.count(root));
        return this;
    }

    /**
     * 统计数目(带distinct参数)，在getSingleResult调用之前使用
     *
     * @return Flea JPA查询对象
     * @since 1.0.0
     */
    public FleaJPAQuery countDistinct(String attrName) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);
        LOGGER.debug1(new Object() {}, "attrName = {}", attrName);
        criteriaQuery.select(criteriaBuilder.countDistinct(root.get(attrName)));
        return this;
    }

    /**
     * 设置查询某属性的最大值，在getSingleResult调用之前使用
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery max(String attrName) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);
        LOGGER.debug1(new Object() {}, "attrName = {}", attrName);
        criteriaQuery.select(criteriaBuilder.max(root.get(attrName)));
        return this;
    }

    /**
     * 设置查询某属性的最小值，在getSingleResult调用之前使用
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery min(String attrName) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);
        LOGGER.debug1(new Object() {}, "attrName = {}", attrName);
        criteriaQuery.select(criteriaBuilder.min(root.get(attrName)));
        return this;
    }

    /**
     * 设置查询某属性的平均值，在getSingleResult调用之前使用
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery avg(String attrName) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);
        LOGGER.debug1(new Object() {}, "attrName = {}", attrName);
        criteriaQuery.select(criteriaBuilder.avg(root.get(attrName)));
        return this;
    }

    /**
     * 设置查询某属性的值的总和，在getSingleResult调用之前使用
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery sum(String attrName) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);
        LOGGER.debug1(new Object() {}, "attrName = {}", attrName);
        criteriaQuery.select(criteriaBuilder.sum(root.get(attrName)));
        return this;
    }

    /**
     * 设置查询某属性的值的总和，在getSingleResult调用之前使用
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery sumAsLong(String attrName) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);
        LOGGER.debug1(new Object() {}, "attrName = {}", attrName);
        criteriaQuery.select(criteriaBuilder.sumAsLong(root.get(attrName)));
        return this;
    }

    /**
     * 设置查询某属性的值的总和，在getSingleResult调用之前使用
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery sumAsDouble(String attrName) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);
        LOGGER.debug1(new Object() {}, "attrName = {}", attrName);
        criteriaQuery.select(criteriaBuilder.sumAsDouble(root.get(attrName)));
        return this;
    }

    /**
     * 去重某一列
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery distinct(String attrName) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);
        LOGGER.debug1(new Object() {}, "attrName = {}", attrName);
        criteriaQuery.select(root.get(attrName)).distinct(true);
        return this;
    }

    /**
     * 添加order by子句
     *
     * @param attrName 属性名
     * @param orderBy  排序顺序
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery addOrderBy(String attrName, String orderBy) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (CollectionUtils.isEmpty(orders)) {
            orders = new ArrayList<>();
        }
        if (orderBy.equalsIgnoreCase(SQLConstants.SQL_ORDER_ASC)) {
            orders.add(criteriaBuilder.asc(root.get(attrName)));
        } else if (orderBy.equalsIgnoreCase(SQLConstants.SQL_ORDER_DESC)) {
            orders.add(criteriaBuilder.desc(root.get(attrName)));
        } else {
            // 排序关键字【{0}】非法, 必须是【asc, ASC】 或【desc, DESC】
            ExceptionUtils.throwCommonException(DaoException.class, "ERROR-DB-DAO0000000005", orderBy);
        }
        LOGGER.debug1(new Object() {}, "attrName = {}, orderBy = {}", attrName, orderBy);
        return this;
    }

    /**
     * 添加 group by子句
     *
     * @param attrName 属性名
     * @return Flea JPA查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public FleaJPAQuery addGroupBy(String attrName) throws CommonException {
        // 属性列名非空校验
        checkAttrName(attrName);

        if (CollectionUtils.isEmpty(groups)) {
            groups = new ArrayList<>();
        }

        groups.add(root.get(attrName));

        LOGGER.debug1(new Object() {}, "attrName = {}", attrName);

        return this;
    }

    /**
     * 获取查询的记录行结果集合
     *
     * @param <T> 返回的实体类型
     * @return 记录行结果集合
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public <T> List<T> getResultList() throws CommonException {
        try {
            List<T> resultList = (List<T>) createQuery(false).getResultList();
            LOGGER.debug("ResultList = {}", resultList);
            LOGGER.debug("Size = {}", null != resultList ? resultList.size() : 0);
            return resultList;
        } finally {
            // 将Flea JPA查询对象重置，并归还给对象池
            close();
        }
    }

    /**
     * 获取查询的记录行结果集合（设置查询范围，可用于分页）
     *
     * @param start 开始查询记录行
     * @param max   最大查询数量
     * @param <T>   返回的实体类型
     * @return 记录行结果集合
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public <T> List<T> getResultList(int start, int max) throws CommonException {
        try {
            List<T> resultList = getResultList(start, max, false);
            Object obj = new Object() {};
            LOGGER.debug1(obj, "ResultList = {}", resultList);
            LOGGER.debug1(obj, "Size = {}", null != resultList ? resultList.size() : 0);
            return resultList;
        } finally {
            // 将Flea JPA查询对象重置，并归还给对象池
            close();
        }
    }

    private <T> List<T> getResultList(int start, int max, boolean isSingle) {
        TypedQuery query = createQuery(isSingle);
        // 设置开始查询记录行
        query.setFirstResult(start);
        // 设置一次最大查询数量
        query.setMaxResults(max);
        return (List<T>) query.getResultList();
    }

    /**
     * 以分页方式获取查询的记录行结果集合
     *
     * @param pageNum   指定页
     * @param pageCount 每页条数
     * @param <T>       返回的实体类型
     * @return 记录行结果集合
     * @throws CommonException 通用异常
     * @since 2.0.0
     */
    public <T> List<T> getResultList4Page(int pageNum, int pageCount) throws CommonException {
        LOGGER.debug1(new Object() {}, "pageNum = {}, pageCount = {}", pageNum, pageCount);
        if (pageNum > 0 && pageCount > 0)
            return getResultList((pageNum - 1) * pageCount, pageCount);
        else
            return getResultList();
    }

    /**
     * 获取查询的单个属性列结果集合
     *
     * @param <T> 单个属性列对应的类型
     * @return 单个属性列结果集合
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public <T> List<T> getSingleResultList() throws CommonException {
        try {
            List<T> singleResultList = (List<T>) createQuery(true).getResultList();
            LOGGER.debug("Single ResultList = {}", singleResultList);
            LOGGER.debug("Size = {}", null != singleResultList ? singleResultList.size() : 0);
            return singleResultList;
        } finally {
            // 将Flea JPA查询对象重置，并归还给对象池
            close();
        }
    }

    /**
     * 获取查询的单个属性列结果集合（设置查询范围）
     *
     * @param start 开始查询记录行
     * @param max   最大查询数量
     * @param <T>   单个属性列对应的类型
     * @return 单个属性列结果集合
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    public <T> List<T> getSingleResultList(int start, int max) throws CommonException {
        try {
            List<T> singleResultList = getResultList(start, max, true);
            Object obj = new Object() {};
            LOGGER.debug1(obj, "Single ResultList = {}", singleResultList);
            LOGGER.debug1(obj, "Size = {}", null != singleResultList ? singleResultList.size() : 0);
            return singleResultList;
        } finally {
            // 将Flea JPA查询对象重置，并归还给对象池
            close();
        }
    }

    /**
     * 获取查询的单个结果; 调用该方法需要先调用如下的单个结果的方法：
     * count, countDistinct, max, min, avg, sum, sumAsLong, sumAsDouble
     *
     * @return 查询的单个结果
     * @throws CommonException 通用异常
     * @see #count()
     * @see #countDistinct(String)
     * @see #max(String)
     * @see #min(String)
     * @see #avg(String)
     * @see #sum(String)
     * @see #sumAsLong(String)
     * @see #sumAsDouble(String)
     * @since 1.0.0
     */
    public <T> T getSingleResult() throws CommonException {
        try {
            T result = (T) createQuery(true).getSingleResult();
            LOGGER.debug("Single Result = {}", result);
            return result;
        } finally {
            // 将Flea JPA查询对象重置，并归还给对象池
            close();
        }
    }

    /**
     * 创建类型查询对象；如果存在分表，则重新设置类型查询对象的持久化信息。
     *
     * @return 查询对象
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    private TypedQuery createQuery(boolean isSingle) throws CommonException {

        // 查询非法，实体类类对象为空
        ObjectUtils.checkEmpty(sourceClazz, CommonException.class, "ERROR-DB-DAO0000000006");

        if (!isSingle) {
            criteriaQuery.select(root);
        }
        if (CollectionUtils.isNotEmpty(predicates)) {
            // 将所有条件用 and 联合起来
            criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        }
        if (CollectionUtils.isNotEmpty(orders)) {
            // 将order by 添加到查询语句中
            criteriaQuery.orderBy(orders);
        }
        if (CollectionUtils.isNotEmpty(groups)) {
            // 将group by 添加到查询语句中
            criteriaQuery.groupBy(groups);
        }
        TypedQuery typedQuery = entityManager.createQuery(criteriaQuery);
        // 如果存在分表，需要重新设置查询对象的持久化信息
        FleaJPASplitHelper.getHandler().handle(this, typedQuery);
        return typedQuery;
    }

    /**
     * 属性列名非空校验
     *
     * @param attrName 属性列名
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    private void checkAttrName(String attrName) throws CommonException {
        // 属性列名不能为空
        StringUtils.checkBlank(attrName, CommonException.class, "ERROR-DB-DAO0000000001");
    }

    /**
     * 根据属性名，从查询实体类实例中获取对应值。
     *
     * @param attrName 属性列名
     * @return 属性值
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    private Object getAttrValue(String attrName) throws CommonException {
        checkAttrName(attrName);

        if (ObjectUtils.isEmpty(entity) || (ObjectUtils.isNotEmpty(sourceClazz) && !sourceClazz.isInstance(entity))) {
            return null;
        }

        return ReflectUtils.getObjectAttrValue(entity, attrName);
    }

    /**
     * 根据属性名，从查询实体类中获取对应的数字值。
     *
     * @param attrName 属性列名
     * @return 属性值
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    private Number getNumberAttrValue(String attrName) throws CommonException {
        return NumberUtils.toNumber(getAttrValue(attrName));
    }

    /**
     * 根据属性名，从查询实体类中获取对应的日期值。
     *
     * @param attrName 属性列名
     * @return 属性值
     * @throws CommonException 通用异常
     * @since 1.0.0
     */
    private Date getDateAttrValue(String attrName) throws CommonException {
        Object attrValue = getAttrValue(attrName);
        Date value = null;
        if (ObjectUtils.isNotEmpty(attrValue) && attrValue instanceof Date) {
            value = (Date) attrValue;
        }
        return value;
    }

    /**
     * 设置Flea对象池
     *
     * @param fleaObjectPool Flea JPA查询对象池
     * @since 1.0.0
     */
    void setFleaObjectPool(FleaJPAQueryPool fleaObjectPool) {
        this.fleaObjectPool = fleaObjectPool;
    }

    /**
     * 重置Flea JPA查询对象
     *
     * @since 1.0.0
     */
    void reset() {
        LOGGER.debug("Start");
        LOGGER.debug("Before FleaJPAQuery = {}", toString());
        entityManager = null;
        sourceClazz = null;
        resultClazz = null;
        root = null;
        criteriaBuilder = null;
        criteriaQuery = null;
        if (CollectionUtils.isNotEmpty(predicates)) {
            predicates.clear();
        }
        orders = null;
        groups = null;
        entity = null;
        LOGGER.debug("After FleaJPAQuery = {}", toString());
        LOGGER.debug("End");
    }

    /**
     * 获取Flea JPA查询对象池的名称
     *
     * @return Flea JPA查询对象池的名称
     * @since 1.1.0
     */
    public String getPoolName() {
        return ObjectUtils.isEmpty(this.fleaObjectPool) ? null : this.fleaObjectPool.getPoolName();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public Class getSourceClazz() {
        return sourceClazz;
    }

    public Class getResultClazz() {
        return resultClazz;
    }

    public Root getRoot() {
        return root;
    }

    public List<Predicate> getPredicates() {
        return predicates;
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return criteriaBuilder;
    }

    public CriteriaQuery getCriteriaQuery() {
        return criteriaQuery;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public List<Expression> getGroups() {
        return groups;
    }

    public Object getEntity() {
        return entity;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
