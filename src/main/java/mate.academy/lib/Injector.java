package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {

        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] fields = clazz.getDeclaredFields();
        Object fieldInstance;

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Error: Class " + clazz.getName()
                    + " doesn't have annotation @Component");
        }

        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstantClazz(clazz);

                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class:" + clazz.getName() + ". Field: " + field.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstantClazz(clazz);
        }

        return clazzImplementationInstance;
    }

    private Object createNewInstantClazz(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementation = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductService.class, ProductServiceImpl.class,
                ProductParser.class, ProductParserImpl.class
        );
        if (interfaceClazz.isInterface()) {
            return interfaceImplementation.get(interfaceClazz);

        }
        return interfaceClazz;
    }
}
