package com.tn.log.access.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tn.log.access.jackson.SimpleCollectionTypeSerializers;
import com.tn.log.access.jackson.SpecStringSerializer;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author chenck
 * @date 2019/5/13 12:02
 */
public class ObjUtils {

    private static final Logger logger = LoggerFactory.getLogger(ObjUtils.class);

    /**
     * 获取对象的字段值
     *
     * @param obj
     * @param clazz     对象类型，可以是对象的类自身或父类
     * @param fieldName
     * @return
     */
    public static Object getFieldValue(Object obj, Class<?> clazz, String fieldName) {
        if (obj == null || fieldName == null || clazz == null)
            return null;

        try {
            // 获取类字段信息
            Field field = clazz.getDeclaredField(fieldName);
            if (field == null)
                return null;
            // 设置字段可访问
            field.setAccessible(true);
            return field.get(obj);
        } catch (Throwable t) {
            logger.error("get object field value fail." + clazz.getName() + "#" + fieldName, t);
        }

        return null;
    }

    /**
     * 获取对象的字段值,搜索对象自身定义的字段和父类定义的字段
     *
     * @param obj
     * @param fieldName
     * @return
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || fieldName == null) {
            return null;
        }

        // 按类自身到父类逐层搜索
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            Object value = getFieldValue(obj, clazz, fieldName);
            if (value != null)
                return value;

            clazz = clazz.getSuperclass();
        }

        return null;
    }

    public static Map<String, Object> obj2Map(Object obj, boolean expandMapField) {
        Map<String, Object> map = new HashMap<String, Object>();

        return obj2Map(obj, expandMapField, map);
    }

    public static Map<String, Object> obj2Map(Object obj, boolean expandMapField, Map<String, Object> map) {
        if (obj == null || map == null) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            final Field[] fields = clazz.getDeclaredFields();
            objField2Map(obj, fields, expandMapField, map);
            clazz = clazz.getSuperclass();
        }

        return map;
    }

    @SuppressWarnings("rawtypes")
    public static void objField2Map(Object obj, Field[] fields, boolean expandMapField,
                                    Map<String, Object> map) {
        if (obj == null || fields == null || map == null) {
            return;
        }

        AccessibleObject.setAccessible(fields, true);
        for (final Field field : fields) {
            try {
                //忽略表静态字段
                if (!isCanReflect(field))
                    continue;

                final String fieldName = field.getName();
                Object value = field.get(obj);
                if (value != null) {
                    if (value instanceof Map && expandMapField) {
                        Map mpObj = (Map) value;
                        Iterator it = mpObj.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry entry = (Map.Entry) it.next();
                            if (entry.getKey() != null) {
                                map.put(entry.getKey().toString(), entry.getValue());
                            }
                        }
                    } else {
                        map.put(fieldName, value);
                    }
                }
            } catch (Throwable e) {
                logger.debug("object fields reflect to  map fail", e);
            }
        }
    }

    public static String obj2String(Object obj) {
        return new Obj2StrBuilder().reflectionToString(obj);
    }

    public static String obj2String(Object obj, boolean reflectCollection, boolean reflectMap,
                                    boolean reflectArray) {
        return new Obj2StrBuilder(reflectCollection, reflectArray, reflectArray)
                .reflectionToString(obj);
    }

    /**
     * @param obj               转json的对象,如果转换出来则返回null，用于日打印，不可用于接口参数返回
     * @param sensitiveDataSafe 是否处理敏感数据
     * @param maxCollLen        数组是否缩略输出
     * @param limitTextLen      字串最大长度限制，超过长度则用...+长度表示
     * @return
     */
    public static String obj2Json(Object obj, boolean sensitiveDataSafe, int maxCollLen, int limitTextLen) {
        ObjectMapper mapper = getObjectMapper(sensitiveDataSafe, maxCollLen, limitTextLen);

        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            //吞噬异常，打印错误日志，因此不可用于接口或需要确切内容的转换
            logger.error("obj2Json failed", e);
        }

        return null;
    }

    /**
     * 根据当前参数初始json的序列化类，根据参数要求将对应的序列化实例注册到ObjectMapper中
     *
     * @param sensitiveDataSafe 是否进行敏感数据脱敏处理
     * @param maxCollLen        输出集合最大长度
     * @param limitTextLen
     * @return
     */
    protected static ObjectMapper getObjectMapper(boolean sensitiveDataSafe, int maxCollLen, int limitTextLen) {
        ObjectMapper objMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        //如果设定了数组输出长度，则需要添加输入输出的序列化类
        if (maxCollLen >= 0) {
            module.setSerializers(new SimpleCollectionTypeSerializers());
        }

        // 如果进行敏感数据处理，和输出文本长度限定，则需要注册SpecStringSerializer来处理
        if (sensitiveDataSafe || limitTextLen > 0) {
            SpecStringSerializer strSer = new SpecStringSerializer();

            if (limitTextLen > 0) {
                strSer.setOutTextLen(limitTextLen);
            }
            module.addSerializer(strSer);
        }

        objMapper.registerModule(module);

        return objMapper;
    }

    public static String obj2QueryString(Object obj) {
        return new Obj2StrBuilder().reflectionToKV(obj);
    }

    /**
     * 判断字段是否参与映射，对transient和static的字段不参与映射
     *
     * @param field
     * @return
     */
    protected static boolean isCanReflect(final Field field) {
        if (field.getName().indexOf(ClassUtils.INNER_CLASS_SEPARATOR_CHAR) != -1) {
            // Reject field from inner class.
            return false;
        }

        if (Modifier.isTransient(field.getModifiers())) {
            // Reject transient fields.
            return false;
        }

        if (Modifier.isStatic(field.getModifiers())) {
            // Reject static fields.
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        class MyInput implements Serializable {
            /**
             *
             */
            private static final long serialVersionUID = 1L;
            private String a;
            private String b;

            public String getA() {
                return a;
            }

            public void setA(String a) {
                this.a = a;
            }

            public String getB() {
                return b;
            }

            public void setB(String b) {
                this.b = b;
            }

            public MyInput() {
                // TODO Auto-generated constructor stub
            }

        }

        class Test {
            private String listid;
            private String spid;
            private MyInput ad;

            public Test(String a1, String a2) {
                listid = a1;
                spid = a2;
                ad = new MyInput();
                ad.setA("a1");
                ad.setB("a2");
            }
        }

        Test t1 = new Test("1111", "44rr");
        System.out.println(ObjUtils.obj2String(t1));
        Test[] ad = new Test[]{new Test("a1", "dds"), null};
        System.out.println(ObjUtils.obj2String(ad, true, true, true));
//		Map<String, String> mp = new HashMap<String, String>();
//		mp.put("ts12", "d3we");
//		mp.put("we12", "rrrd3we");
//		System.out.println(ObjUtils.obj2String(mp));
//
//		MyInput input = new MyInput();
//		input.setA("b");
//		input.setB("1");
//
//		System.out.println(ObjUtils.obj2String(input));
    }
}
