package com.ebsco.platform.shared.cmsimport.rs.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.extern.log4j.Log4j2;

public class PojoUtil {
	
	@SuppressWarnings("unchecked")
	public static <T> T get(Object obj, String prop) {
		try {
			return (T) PropertyUtils.getProperty(obj, prop);
		} catch (Exception e) {
			return null;
		}
	}
	
    public static <T> T getJsonField(Object obj, String jsonFieldName) {
	    String pojoFieldName = fieldName(obj, jsonFieldName);
	    return get(obj, pojoFieldName);
    }
	
	public static void set(Object obj, String prop, Object value) {
		try {
			PropertyUtils.setProperty(obj, prop, value);
		} catch (Exception ignore) {
			return;
		}
	}
	
	public static void setJsonField(Object obj, String jsonFieldName, Object value) {
	    String pojoFieldName = fieldName(obj, jsonFieldName);
	    set(obj, pojoFieldName, value);
    }
	
	public static String fieldName(Object obj, String jsonFieldName) {
		Field[] fields = obj.getClass().getDeclaredFields();
	    for (Field field : fields) {
	        if (field.isAnnotationPresent(JsonProperty.class)) {
	            String annotationValue = field.getAnnotation(JsonProperty.class).value();
	            if (annotationValue.equals(jsonFieldName)) {
	            	return field.getName();
	            }
	        }
	    }
	    return jsonFieldName;
	}
	
	public static Map<String, Object> describe(Object pojo) {
		try {
			return PropertyUtils.describe(pojo);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
	
	public static Set<String> fieldNamesOfType(Class<?> ofType, Object pojo) {
        Set<String> result = new HashSet<>();
	    try {
            PropertyDescriptor[] descrs = PropertyUtils.getPropertyDescriptors(pojo);
            for (PropertyDescriptor desc : descrs) {
                Method readMethod = desc.getReadMethod();
                if (readMethod.getReturnType().equals(ofType)) {
                    result.add(desc.getName());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	    return result;
    }
	
	public static Set<String> fieldNamesOfTypeOrParametrizedType(Class type, Object pojo) {
        Set<String> result = new HashSet<>();
        try {
            PropertyDescriptor[] descrs = PropertyUtils.getPropertyDescriptors(pojo);
            for (PropertyDescriptor desc : descrs) {
                Method readMethod = desc.getReadMethod();
                Class returnType = readMethod.getReturnType();
                if (returnType.equals(type)) {
                    result.add(desc.getName());
                } else {
                    Field f = findDeclaredFieldRecurcively(pojo.getClass(), desc.getName());
                    if (Objects.isNull(f)) {
                        continue;
                    }
                    Type t = f.getGenericType();
                    if (t instanceof ParameterizedType genericType) {
                        Type[] actualTypeArguments = genericType.getActualTypeArguments();
                        for (Type genType : actualTypeArguments) {
                            if (genType instanceof ParameterizedType genericTypeArg) {
                                if (genericTypeArg.getRawType().equals(type)) {
                                    result.add(desc.getName());
                                    continue;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
	
	private static Field findDeclaredFieldRecurcively(Class cls, String fieldName) {
	    if (Objects.isNull(cls)) {
	        return null;
	    }
	    Field[] declaredFields = cls.getDeclaredFields();
	    for (Field field : declaredFields) {
            if (fieldName.equals(field.getName())) {
                return field;
            }
        }
	    return findDeclaredFieldRecurcively(cls.getSuperclass(), fieldName);
    }
	
	public static <T> T merge(List<T> pojos) {
	    return new Merger<T>(pojos).merge();
    }
	
	public static <T> T merge(List<T> pojos, boolean logger) {
        return new Merger<T>(pojos).logger(logger).merge();
    }
    
	@Log4j2
	public static class Merger<T> {
        
        private List<T> pojos;
        private boolean overrite;
        private boolean logger;
        
        private Merger(List<T> pojos) {
            this.pojos = pojos;
        }

        public Merger<T> overrite(boolean overrite) {
            this.overrite = overrite;
            return this;
        }
        
        public Merger<T> logger(boolean logger) {
            this.logger = logger;
            return this;
        }
        
        public T merge() {
            return copyNonNullPropertiesIntoFirstInstance (sortByNumberOfNonNullProps(pojos));
        }
        
        private static <T> List<T> sortByNumberOfNonNullProps(List<T> items) {
            items.sort(Comparator.comparing(pojo -> {
                Map<String, Object> props = PojoUtil.describe(pojo);
                int nonNullPropsCout = 0;
                for (Map.Entry<String, Object> entries : props.entrySet()) {
                    Object value = entries.getValue();
                    if (Objects.nonNull(value)) {
                        nonNullPropsCout++;
                    }
                }
                return nonNullPropsCout;
            }).reversed());
            return items;
        }
        
        private T copyNonNullPropertiesIntoFirstInstance (List<T> items) {
            T first = items.iterator().next();
            for (int i = 1; i < items.size(); i++) {
                T item = items.get(i);
                copyProperties(item, first);
            }
            return first;
        }
        
        private void copyProperties(Object from, Object to) {
            try {
                Map<String, Object> prop2Value = PropertyUtils.describe(from);
                for (Map.Entry<String, Object> entry: prop2Value.entrySet()) {
                    String propName = entry.getKey();
                    Object previousValue = get(to, propName);
                    Object value = entry.getValue();
                    if (Objects.nonNull(previousValue) && Objects.nonNull(value) && !Objects.equals(previousValue, value)
                            && logger) {
                        log.warn("overrite={}, both non null and not equal:\n previous: {}.{}={}\n current:  {}.{}={}\n", 
                                overrite, to.getClass().getSimpleName(), propName, previousValue, from.getClass().getSimpleName(), propName, value);
                    }
                    
                    if (overrite || Objects.isNull(previousValue)) {
                        set(to, propName, value);
                    }
                }
                
            } catch (Exception ignore) {}
        }
    }
}
