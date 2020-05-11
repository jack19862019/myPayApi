package com.pay.data.supper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.pay.common.annotation.Query;
import com.pay.common.enums.RoleType;
import com.pay.common.security.SecurityUtils;
import com.pay.common.utils.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
public class QueryHelp {

    public static <R, Q> Predicate getPredicate(Root<R> root, Q query, CriteriaBuilder cb) {
        List<Predicate> list = doBusiness(root, query, cb);
        return cb.and(list.toArray(new Predicate[0]));
    }

    public static <R, Q> Predicate getPredicate(Root<R> root, Q query, CriteriaBuilder cb, boolean permission) {
        if (permission) {
            RoleType roleType = SecurityUtils.getRoleType();
            Object[] args = new Object[]{SecurityUtils.getUsername()};
            if (!roleType.equals(RoleType.MANAGER)) {
                ReflectUtils.invokeMethodByName(query, "setCreateUser", args);
            }
        }
        List<Predicate> list = doBusiness(root, query, cb);
        return cb.and(list.toArray(new Predicate[0]));
    }

    private static <R, Q> List<Predicate> doBusiness(Root<R> root, Q query, CriteriaBuilder cb) {
        List<Predicate> list = new ArrayList<>();
        if (ObjectUtils.isEmpty(query)) {
            return new ArrayList<>();
        }
        try {
            List<Field> fields = getAllFields(query.getClass(), new ArrayList<>());
            for (Field field : fields) {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                Query q = field.getAnnotation(Query.class);
                if (q != null) {
                    String propName = q.propName();
                    String joinName = q.joinName();
                    String blurry = q.blurry();
                    String attributeName = isBlank(propName) ? field.getName() : propName;
                    Class<?> fieldType = field.getType();
                    Object val = field.get(query);
                    if (ObjectUtil.isEmpty(val)) {
                        continue;
                    }
                    Join join = null;
                    // 模糊多字段
                    if (ObjectUtil.isNotEmpty(blurry)) {
                        String[] blurbs = blurry.split(",");
                        List<Predicate> orPredicate = new ArrayList<>();
                        for (String s : blurbs) {
                            orPredicate.add(cb.like(root.get(s).as(String.class), "%" + val.toString() + "%"));
                        }
                        Predicate[] p = new Predicate[orPredicate.size()];
                        list.add(cb.or(orPredicate.toArray(p)));
                        continue;
                    }
                    if (ObjectUtil.isNotEmpty(joinName)) {
                        switch (q.join()) {
                            case LEFT:
                                join = root.join(joinName, JoinType.LEFT);
                                break;
                            case RIGHT:
                                join = root.join(joinName, JoinType.RIGHT);
                                break;
                            case INNER:
                                join = root.join(joinName, JoinType.INNER);
                                break;
                        }
                    }
                    switch (q.type()) {
                        case EQUAL:
                            list.add(cb.equal(getExpression(attributeName, join, root).as(fieldType), val));
                            break;
                        case NOT_EQUAL:
                            list.add(cb.notEqual(getExpression(attributeName, join, root).as(fieldType), val));
                            break;
                        case GREATER_THAN:
                            list.add(cb.greaterThanOrEqualTo(getExpression(attributeName, join, root).as(fieldType), (Comparable) val));
                            break;
                        case LESS_THAN:
                            list.add(cb.lessThanOrEqualTo(getExpression(attributeName, join, root).as(fieldType), (Comparable) val));
                            break;
                        case LESS_THAN_NQ:
                            list.add(cb.lessThan(getExpression(attributeName, join, root).as(fieldType), (Comparable) val));
                            break;
                        case INNER_LIKE:
                            list.add(cb.like(getExpression(attributeName, join, root).as(String.class), "%" + val.toString() + "%"));
                            break;
                        case LEFT_LIKE:
                            list.add(cb.like(getExpression(attributeName, join, root).as(String.class), "%" + val.toString()));
                            break;
                        case RIGHT_LIKE:
                            list.add(cb.like(getExpression(attributeName, join, root).as(String.class), val.toString() + "%"));
                            break;
                        case IN:
                            if (CollUtil.isNotEmpty((Collection<Long>) val)) {
                                list.add(getExpression(attributeName, join, root).in((Collection<Long>) val));
                            }
                            break;
                        case NOT_NULL:
                            list.add(cb.isNotNull(getExpression(attributeName, join, root)));
                            break;
                        case BETWEEN:
                            List<Object> between = new ArrayList<>(Arrays.asList(val.toString().split(",")));
                            list.add(cb.between(getExpression(attributeName, join, root).as(between.get(0).getClass()),
                                    (Comparable) between.get(0), (Comparable) between.get(1)));
                            break;
                    }
                }
                field.setAccessible(accessible);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return list;
    }


    private static Expression getExpression(String attributeName, Join join, Root root) {
        if (ObjectUtil.isNotEmpty(join)) {
            return join.get(attributeName);
        } else {
            String[] split = new String[]{attributeName};
            if (attributeName.contains(".")) {
                split = attributeName.split("\\.");
            }
            String attr = split[0];
            Path path = root.get(attr);
            for (int i = 1; i < split.length; i++) {
                path = path.get(split[i]);
            }
            return path;
        }
    }

    private static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static List<Field> getAllFields(Class clazz, List<Field> fields) {
        if (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            getAllFields(clazz.getSuperclass(), fields);
        }
        return fields;
    }
}
