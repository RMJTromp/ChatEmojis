package com.rmjtromp.chatemojis.utils;

import com.rmjtromp.chatemojis.TestBase;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionUtilsTest extends TestBase {

    // Test target classes
    @SuppressWarnings("unused")
    static class Parent {
        private String parentField = "parent";
        protected int parentInt = 42;

        public String parentMethod() { return "parent"; }
    }

    @SuppressWarnings("unused")
    static class Child extends Parent {
        private String childField = "child";
        private String anotherField = "another";
        private double childDouble = 3.14;

        public String childMethod() { return "child"; }
        public int compute(int a, int b) { return a + b; }
    }

    @Test
    void searchField_byName_findsField() throws NoSuchFieldException {
        Field field = ReflectionUtils.searchField(Child.class, query -> query
            .name("childField")
            .build()
        );
        assertNotNull(field);
        assertEquals("childField", field.getName());
    }

    @Test
    void searchField_byType_findsField() throws NoSuchFieldException {
        Field field = ReflectionUtils.searchField(Child.class, query -> query
            .type(double.class)
            .build()
        );
        assertNotNull(field);
        assertEquals("childDouble", field.getName());
    }

    @Test
    void searchField_notFound_throwsNoSuchFieldException() {
        assertThrows(NoSuchFieldException.class, () ->
            ReflectionUtils.searchField(Child.class, query -> query
                .name("nonExistent")
                .build()
            )
        );
    }

    @Test
    void searchField_multipleMatches_throwsRuntimeException() {
        // Child has both childField (String) and childDouble (double) which are both non-int
        // But searching by type String in Child: childField is String
        // Actually searching by name with two matching fields - let's use a filter
        assertThrows(RuntimeException.class, () ->
            ReflectionUtils.searchField(Child.class, query -> query
                .filter(f -> !f.getType().isPrimitive())
                .build()
            )
        );
    }

    @Test
    void searchField_recursive_searchesSuperclass() throws NoSuchFieldException {
        Field field = ReflectionUtils.searchField(Child.class, query -> query
            .name("parentField")
            .recursive(true)
            .build()
        );
        assertNotNull(field);
        assertEquals("parentField", field.getName());
    }

    @Test
    void searchField_notRecursive_doesNotFindParentField() {
        assertThrows(NoSuchFieldException.class, () ->
            ReflectionUtils.searchField(Child.class, query -> query
                .name("parentField")
                .build()
            )
        );
    }

    @Test
    void searchMethod_byName_findsMethod() throws NoSuchMethodException {
        Method method = ReflectionUtils.searchMethod(Child.class, query -> query
            .name("childMethod")
            .build()
        );
        assertNotNull(method);
        assertEquals("childMethod", method.getName());
    }

    @Test
    void searchMethod_byReturnTypeAndParams_findsMethod() throws NoSuchMethodException {
        Method method = ReflectionUtils.searchMethod(Child.class, query -> query
            .returnType(int.class)
            .paramTypes(new Class[]{int.class, int.class})
            .build()
        );
        assertNotNull(method);
        assertEquals("compute", method.getName());
    }

    @Test
    void searchMethod_notFound_throwsNoSuchMethodException() {
        assertThrows(NoSuchMethodException.class, () ->
            ReflectionUtils.searchMethod(Child.class, query -> query
                .name("nonExistentMethod")
                .build()
            )
        );
    }

    @Test
    void searchMethod_recursive_searchesSuperclass() throws NoSuchMethodException {
        Method method = ReflectionUtils.searchMethod(Child.class, query -> query
            .name("parentMethod")
            .recursive(true)
            .build()
        );
        assertNotNull(method);
        assertEquals("parentMethod", method.getName());
    }
}
