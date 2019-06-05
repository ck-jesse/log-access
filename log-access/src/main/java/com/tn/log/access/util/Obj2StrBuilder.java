package com.tn.log.access.util;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * 将一个对象的成员变量和值映射成字串
 *
 * @author chenck
 * @date 2019/5/10 11:00
 */
public class Obj2StrBuilder {

    private static final Log logger = LogFactory.getLog(Obj2StrBuilder.class);

    /**
     * 是否映射集合中的每个元素
     */
    private boolean reflectCollection = false;
    /**
     * 是否映射map中的每个元素
     */
    private boolean reflectMap = true;
    /**
     * 是否映射map中的每个元素
     */
    private boolean reflectArray = false;

    public Obj2StrBuilder(boolean reflectCollection, boolean reflectMap, boolean reflectArray) {
        this.reflectCollection = reflectCollection;
        this.reflectMap = reflectMap;
        this.reflectArray = reflectArray;
    }

    public Obj2StrBuilder() {
    }

    public boolean isReflectCollection() {
        return reflectCollection;
    }

    public boolean isReflectMap() {
        return reflectMap;
    }

    public boolean isReflectArray() {
        return reflectArray;
    }

    public void setReflectArray(boolean reflectArray) {
        this.reflectArray = reflectArray;
    }

    public void setReflectCollection(boolean reflectCollection) {
        this.reflectCollection = reflectCollection;
    }

    public void setReflectMap(boolean reflectMap) {
        this.reflectMap = reflectMap;
    }

    /**
     * 将参数的值拼接为字串 ，格式为：对象类型(对象成员值)
     * []表示数组,()表示对象,{}表示map
     *
     * @param obj
     * @return
     */
    public String reflectionToString(Object obj) {
        if (obj == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        //如果是原始对象，直接将对象以string方式输出值
        if (isNativeObject(obj)) {
            sb.append(obj.getClass().getSimpleName());
            sb.append("(");
            objectReflect(obj, sb);
            sb.append(")");

            // 对集合做处理
            if ((obj instanceof Collection)) {
                Collection collection = (Collection) obj;
                Iterator it = collection.iterator();
                for (; it.hasNext(); ) {
                    reflectionToString(it.next());
                }
            }

            return sb.toString();
        }

        boolean bFirst = true;
        sb.append(obj.getClass().getSimpleName());
        sb.append("(");

        //通过反射获取所有字段，并根据字段类型进行处理
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            //获取类定义的所有字段
            final Field[] fields = clazz.getDeclaredFields();

            AccessibleObject.setAccessible(fields, true);
            for (final Field field : fields) {
                try {
                    //判断字段是否参与映射
                    if (!isCanReflect(field)) {
                        continue;
                    }

                    final String fieldName = field.getName();
                    Object value = field.get(obj);
                    //字段以字段名=value的形式组装，多个字段以逗号分隔
                    if (bFirst) {
                        bFirst = false;
                    } else {
                        sb.append(",");
                    }
                    sb.append(fieldName);
                    sb.append("=");
                    //如果字段为null，则格式为字段名=<null>
                    if (value != null) {
                        objectReflect(value, sb);
                    } else {
                        sb.append("<null>");
                    }
                } catch (Throwable e) {
                    logger.debug("object fields reflect to  map fail", e);
                }
            }
            //获取父类的信息
            clazz = clazz.getSuperclass();
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * 判断是否为java原始数据类型，如果为原始数据类型直接用toString获取值
     *
     * @param obj
     * @return
     */
    private boolean isNativeObject(Object obj) {
        if ((obj instanceof CharSequence) || (obj instanceof Number) || (obj instanceof Character)
                || (obj instanceof Boolean) || (obj instanceof Date) || (obj instanceof Collection)
                || (obj instanceof Map) || obj.getClass().isArray()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断字段是否参与映射，对transient和static的字段不参与映射
     *
     * @param field
     * @return
     */
    private boolean isCanReflect(final Field field) {
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

    /**
     * 判断对象是否为文件上传对象
     * 注：仅支持基于spring mvc的文件上传
     *
     * @param obj
     * @return
     */
    private boolean isMultipartFile(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.getClass().isAssignableFrom(MultipartFile.class);
    }


    /**
     * 将一个对象映射为字串
     *
     * @param obj
     * @param sb
     */
    public void objectReflect(Object obj, StringBuilder sb) {
        // 集合类型的对象
        if ((obj instanceof Collection)) {
            collectionReflectToStr((Collection) obj, sb);
        } else if (obj instanceof Map) {
            // map类型的对象
            mapReflectToStr((Map) obj, sb);
        } else if (obj.getClass().isArray()) {
            // 数组类型的对象
            arrayReflectToStr(obj, sb);
        } else {
            // 原始的数据类型
            objectReflectToStr(obj, sb);
        }
    }

    /**
     * 将对象的属性值转为字串列表
     *
     * @param obj
     * @return
     */
    public void objectReflectToStr(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
            return;
        }

        // 如果基本数据类型则直接toString转换
        if ((obj instanceof CharSequence) || (obj instanceof Number) || (obj instanceof Character)
                || (obj instanceof Boolean) || (obj instanceof Date)) {
            sb.append(obj.toString());
        } else if (isMultipartFile(obj)) {
            try {
                // 对文件对象转换为只显示相应的属性
                //MultipartFile mfile = (MultipartFile)obj;
                sb.append(MethodUtils.invokeMethod(obj, "getSimpleName"));
                //sb.append(obj.getClass().getSimpleName());
                sb.append("(_filename=");
                sb.append(MethodUtils.invokeMethod(obj, "getOriginalFilename"));
                //sb.append(mfile.getOriginalFilename());
                sb.append(",_size=");
                sb.append(MethodUtils.invokeMethod(obj, "getSize"));
                //sb.append(mfile.getSize());
                sb.append(")");
            } catch (Throwable t) {
                logger.error("reflect MultipartFile object properties fail", t);
            }
        } else {
            // 对对象使用apache的反映转换，前后加上(),表示对象
            sb.append(obj.getClass().getSimpleName());
            sb.append("(");
            sb.append(ToStringBuilder.reflectionToString(obj, Obj2StrStyle.SIMPLE_STYLE));
            sb.append(")");
        }
    }

    /**
     * 将集合对象映射为字串值
     * 集合以{}表示
     *
     * @param collection 集合对象
     * @param sb
     */
    @SuppressWarnings("rawtypes")
    public void collectionReflectToStr(Collection collection, StringBuilder sb) {
        if (collection == null || sb == null) {
            return;
        }

        // 如果不映射集合类型，则只映射集合类型和数据量
        if (!reflectCollection) {
            sb.append("{");
            sb.append(collection.getClass().getSimpleName());
            sb.append("(size=");
            sb.append(collection.size());
            sb.append(")}");
            return;
        }

        sb.append("{");
        boolean collFirst = true;
        for (Object item : collection) {
            if (!collFirst) {
                sb.append(",");
            } else {
                collFirst = false;
            }
            //将集合中每个对象直接映射转换
            objectReflectToStr(item, sb);
        }

        sb.append("}");
    }

    /**
     * 将Map对象映射转换为字串
     * map以{}表示
     *
     * @param map
     * @param sb
     */
    @SuppressWarnings("rawtypes")
    public void mapReflectToStr(Map map, StringBuilder sb) {
        if (map == null || sb == null) {
            return;
        }

        // 如果不映射集合类型，则只映射集合类型和数据量
        if (!reflectMap) {
            sb.append("{");
            sb.append(map.getClass().getSimpleName());
            sb.append("(size=");
            sb.append(map.size());
            sb.append(")}");
            return;
        }

        // 是map类型，遍历map，用key=value的形式表示map的内容
        Map mpObj = (Map) map;
        Iterator it = mpObj.entrySet().iterator();
        sb.append("{");
        boolean mapFirst = true;
        while (it.hasNext()) {
            if (!mapFirst) {
                sb.append(",");
            } else {
                mapFirst = false;
            }

            Map.Entry entry = (Map.Entry) it.next();
            //将key对象映射为字串
            objectReflectToStr(entry.getKey(), sb);
            sb.append("=");
            //将value对象映射为字串
            objectReflectToStr(entry.getValue(), sb);
        }

        sb.append("}");
    }

    /**
     * 数组对象映射为字串
     * 数组以[]表示
     *
     * @param arrayObj
     * @param sb
     */
    public void arrayReflectToStr(Object arrayObj, StringBuilder sb) {
        if (arrayObj == null) {
            return;
        }

        int len = Array.getLength(arrayObj);
        // 如果不映射数组，则只填写数据类型和大小
        if (!reflectArray) {
            sb.append("[");
            sb.append(arrayObj.getClass().getSimpleName());
            sb.append("(length=");
            sb.append(len);
            sb.append(")]");
            return;
        }

        // 如果是数组类型，则遍历数组
        sb.append("[");
        boolean arryFirst = true;
        for (int i = 0; i < len; ++i) {
            if (!arryFirst) {
                sb.append(",");
            } else {
                arryFirst = false;
            }

            //将数据的元素对象转换为字串
            objectReflectToStr(Array.get(arrayObj, i), sb);
        }
        sb.append("]");
    }

    public String reflectionToKV(Object obj) {
        if (obj == null) {
            return null;
        }

        //如果是原始对象，直接将对象以string方式输出值
        if (isNativeObject(obj)) {
            return obj.toString();
        }

        boolean bFirst = true;
        StringBuilder sb = new StringBuilder();

        //通过反射获取所有字段，并根据字段类型进行处理
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            //获取类定义的所有字段
            final Field[] fields = clazz.getDeclaredFields();

            AccessibleObject.setAccessible(fields, true);
            for (final Field field : fields) {
                try {
                    //判断字段是否参与映射
                    if (!isCanReflect(field)) {
                        continue;
                    }

                    final String fieldName = field.getName();
                    Object value = field.get(obj);
                    // 字段以字段名=value的形式组装，多个字段以逗号分隔
                    if (bFirst) {
                        bFirst = false;
                    } else {
                        sb.append("&");
                    }

                    sb.append(fieldName);
                    sb.append("=");
                    //如果字段为null，则格式为字段名=<null>
                    if (value != null) {
                        objectReflectToStr(value, sb);
                    } else {
                        sb.append("<null>");
                    }
                } catch (Throwable e) {
                    logger.debug("object fields reflect to key-value string fail", e);
                }
            }
            //获取父类的信息
            clazz = clazz.getSuperclass();
        }
        return sb.toString();
    }
}
