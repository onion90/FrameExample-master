package com.otcyan.jlog.print;

import com.otcyan.jlog.core.LogLevel;
import com.otcyan.jlog.core.LogTool;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * .
 */

public class ObjPrinter implements Printer {
    // 基本数据类型
    private final static String[] TYPES = {"int", "java.lang.String", "boolean", "char",
            "float", "double", "long", "short", "byte"};

    @Override
    public void printConsole(@LogLevel String level, String tag, Object object, StackTraceElement element) {
        LogTool.log(level , tag , LogTool.parseMessage(parseObj(object) , element));
    }

    public static String parseObj(Object object) {
        if (object == null) {
            return "null";
        }

        final String simpleName = object.getClass().getSimpleName();
        if (object instanceof Collection) {
            Collection collection = (Collection) object;
            String msg = "%s size = %d [\n";
            msg = String.format(Locale.ENGLISH, msg, simpleName, collection.size());
            if (!collection.isEmpty()) {
                Iterator iterator = collection.iterator();
                int flag = 0;
                while (iterator.hasNext()) {
                    String itemString = "[%d]:%s%s";
                    Object item = iterator.next();
                    msg += String.format(Locale.ENGLISH, itemString,
                            flag,
                            objectToString(item),
                            flag++ < collection.size() - 1 ? ",\n" : "\n");
                }
            }
            return msg + "\n]";
        } else if (object instanceof Map) {
            String msg = simpleName + " {\n";
            Map map = (Map) object;
            Set keys = map.keySet();
            for (Object key : keys) {
                String itemString = "[%s -> %s]\n";
                Object value = map.get(key);
                msg += String.format(itemString, objectToString(key), objectToString(value));
            }
            return msg + "}";
        } else {
            return objectToString(object);
        }
    }

    /**
     * 将对象转化为String
     */
    protected static <T> String objectToString(T object) {
        if (object == null) {
            return "Object{object is null}";
        }
        if (object.toString().startsWith(object.getClass().getName() + "@")) {
            StringBuilder builder = new StringBuilder(object.getClass().getSimpleName() + "{");
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                boolean flag = false;
                for (String type : TYPES) {
                    if (field.getType().getName().equalsIgnoreCase(type)) {
                        flag = true;
                        Object value = null;
                        try {
                            value = field.get(object);
                        } catch (IllegalAccessException e) {
                            value = e;
                        } finally {
                            builder.append(String.format("%s=%s, ", field.getName(),
                                    value == null ? "null" : value.toString()));
                        }
                    }
                }
                if (!flag) {
                    builder.append(String.format("%s=%s, ", field.getName(), "Object"));
                }
            }
            return builder.replace(builder.length() - 2, builder.length() - 1, "}").toString();
        } else {
            return object.toString();
        }
    }
}
