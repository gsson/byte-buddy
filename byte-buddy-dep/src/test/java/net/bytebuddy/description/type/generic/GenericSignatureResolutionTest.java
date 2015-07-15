package net.bytebuddy.description.type.generic;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class GenericSignatureResolutionTest {

    private static final String FOO = "foo";

    @Test
    public void testGenericType() throws Exception {
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(GenericType.class)
                .make();
        Class<?> type = unloaded.load(null, ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        TypeDescription createdType = new TypeDescription.ForLoadedType(type);
        TypeDescription originalType = new TypeDescription.ForLoadedType(GenericType.class);
        assertThat(createdType.getTypeVariables(), is(originalType.getTypeVariables()));
        assertThat(createdType.getSuperType(), is(originalType.getSuperType()));
        assertThat(createdType.getInterfaces(), is(originalType.getInterfaces()));
    }

    @Test
    public void testGenericField() throws Exception {
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(GenericField.class)
                .make();
        Class<?> type = unloaded.load(null, ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        FieldDescription createdField = new FieldDescription.ForLoadedField(type.getDeclaredField(FOO));
        FieldDescription originalField = new FieldDescription.ForLoadedField(GenericField.class.getDeclaredField(FOO));
        assertThat(createdField.getType(), is(originalField.getType()));
    }

    @Test
    public void testGenericMethod() throws Exception {
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(GenericMethod.class)
                .method(named("foo"))
                .intercept(FixedValue.nullValue())
                .make();
        Class<?> type = unloaded.load(null, ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        MethodDescription createdMethod = new MethodDescription.ForLoadedMethod(type.getDeclaredMethod(FOO, Exception.class));
        MethodDescription originalMethod = new MethodDescription.ForLoadedMethod(GenericMethod.class.getDeclaredMethod(FOO, Exception.class));
        assertThat(createdMethod.getTypeVariables(), is(originalMethod.getTypeVariables()));
        assertThat(createdMethod.getReturnType(), is(originalMethod.getReturnType()));
        assertThat(createdMethod.getParameters().getOnly().getType(), is(originalMethod.getParameters().getOnly().getType()));
        assertThat(createdMethod.getExceptionTypes().getOnly(), is(originalMethod.getExceptionTypes().getOnly()));
    }

    @Test
    public void testNoSuperType() throws Exception {
        assertThat(new ByteBuddy().redefine(Object.class).make(), notNullValue(DynamicType.class));
    }

    @Test
    public void testTypeVariableClassBound() throws Exception {
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(TypeVariableClassBound.class)
                .make();
        Class<?> type = unloaded.load(null, ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        TypeDescription createdType = new TypeDescription.ForLoadedType(type);
        TypeDescription originalType = new TypeDescription.ForLoadedType(TypeVariableClassBound.class);
        assertThat(createdType.getTypeVariables(), is(originalType.getTypeVariables()));
        assertThat(createdType.getSuperType(), is(originalType.getSuperType()));
        assertThat(createdType.getInterfaces(), is(originalType.getInterfaces()));
    }

    @Test
    public void testTypeVariableInterfaceBound() throws Exception {
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(TypeVariableInterfaceBound.class)
                .make();
        Class<?> type = unloaded.load(null, ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        TypeDescription createdType = new TypeDescription.ForLoadedType(type);
        TypeDescription originalType = new TypeDescription.ForLoadedType(TypeVariableInterfaceBound.class);
        assertThat(createdType.getTypeVariables(), is(originalType.getTypeVariables()));
        assertThat(createdType.getSuperType(), is(originalType.getSuperType()));
        assertThat(createdType.getInterfaces(), is(originalType.getInterfaces()));
    }

    @Test
    public void testTypeVariableClassAndInterfaceBound() throws Exception {
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(TypeVariableClassAndInterfaceBound.class)
                .make();
        Class<?> type = unloaded.load(null, ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        TypeDescription createdType = new TypeDescription.ForLoadedType(type);
        TypeDescription originalType = new TypeDescription.ForLoadedType(TypeVariableClassAndInterfaceBound.class);
        assertThat(createdType.getTypeVariables(), is(originalType.getTypeVariables()));
        assertThat(createdType.getSuperType(), is(originalType.getSuperType()));
        assertThat(createdType.getInterfaces(), is(originalType.getInterfaces()));
    }

    @Test
    public void testTypeVariableWildcardNoBound() throws Exception {
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(TypeVariableWildcardNoBound.class)
                .make();
        Class<?> type = unloaded.load(null, ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        TypeDescription createdType = new TypeDescription.ForLoadedType(type);
        TypeDescription originalType = new TypeDescription.ForLoadedType(TypeVariableWildcardNoBound.class);
        assertThat(createdType.getTypeVariables(), is(originalType.getTypeVariables()));
        assertThat(createdType.getSuperType(), is(originalType.getSuperType()));
        assertThat(createdType.getInterfaces(), is(originalType.getInterfaces()));
    }

    @Test
    public void testTypeVariableWildcardUpperClassBound() throws Exception {
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(TypeVariableWildcardUpperClassBound.class)
                .make();
        Class<?> type = unloaded.load(null, ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        TypeDescription createdType = new TypeDescription.ForLoadedType(type);
        TypeDescription originalType = new TypeDescription.ForLoadedType(TypeVariableWildcardUpperClassBound.class);
        assertThat(createdType.getTypeVariables(), is(originalType.getTypeVariables()));
        assertThat(createdType.getSuperType(), is(originalType.getSuperType()));
        assertThat(createdType.getInterfaces(), is(originalType.getInterfaces()));
    }

    @Test
    public void testTypeVariableWildcardUpperInterfaceBound() throws Exception {
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(TypeVariableWildcardUpperInterfaceBound.class)
                .make();
        Class<?> type = unloaded.load(null, ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        TypeDescription createdType = new TypeDescription.ForLoadedType(type);
        TypeDescription originalType = new TypeDescription.ForLoadedType(TypeVariableWildcardUpperInterfaceBound.class);
        assertThat(createdType.getTypeVariables(), is(originalType.getTypeVariables()));
        assertThat(createdType.getSuperType(), is(originalType.getSuperType()));
        assertThat(createdType.getInterfaces(), is(originalType.getInterfaces()));
    }

    @Test
    public void testTypeVariableWildcardLowerClassBound() throws Exception {
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(TypeVariableWildcardLowerClassBound.class)
                .make();
        Class<?> type = unloaded.load(null, ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        TypeDescription createdType = new TypeDescription.ForLoadedType(type);
        TypeDescription originalType = new TypeDescription.ForLoadedType(TypeVariableWildcardLowerClassBound.class);
        assertThat(createdType.getTypeVariables(), is(originalType.getTypeVariables()));
        assertThat(createdType.getSuperType(), is(originalType.getSuperType()));
        assertThat(createdType.getInterfaces(), is(originalType.getInterfaces()));
    }

    @Test
    public void testTypeVariableWildcardLowerInterfaceBound() throws Exception {
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(TypeVariableWildcardLowerInterfaceBound.class)
                .make();
        Class<?> type = unloaded.load(null, ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        TypeDescription createdType = new TypeDescription.ForLoadedType(type);
        TypeDescription originalType = new TypeDescription.ForLoadedType(TypeVariableWildcardLowerInterfaceBound.class);
        assertThat(createdType.getTypeVariables(), is(originalType.getTypeVariables()));
        assertThat(createdType.getSuperType(), is(originalType.getSuperType()));
        assertThat(createdType.getInterfaces(), is(originalType.getInterfaces()));
    }

    @SuppressWarnings("unused")
    public static abstract class GenericType<T extends ArrayList<T> & Callable<T>,
            S extends Callable<?>,
            U extends Callable<? extends Callable<U>>,
            V extends ArrayList<? super ArrayList<V>>,
            W extends Callable<W[]>> extends ArrayList<T> implements Callable<T> {

    }

    @SuppressWarnings("unused")
    public static class GenericMethod {

        <T extends Exception & Callable<T>> T foo(T arg) throws T {
            return null;
        }
    }

    @SuppressWarnings("unused")
    public static class GenericField<T> {

        T foo;
    }

    @SuppressWarnings("unused")
    public static class TypeVariableClassBound<T extends ArrayList<T>> {
        /* empty */
    }

    @SuppressWarnings("unused")
    public static abstract class TypeVariableInterfaceBound<T extends Callable<T>> {
        /* empty */
    }

    @SuppressWarnings("unused")
    public static abstract class TypeVariableClassAndInterfaceBound<T extends ArrayList<T> & Callable<T>> {
        /* empty */
    }

    @SuppressWarnings("unused")
    public static class TypeVariableWildcardNoBound<T extends ArrayList<?>> {
        /* empty */
    }

    @SuppressWarnings("unused")
    public static class TypeVariableWildcardUpperClassBound<T extends ArrayList<? extends ArrayList<T>>> {
        /* empty */
    }

    @SuppressWarnings("unused")
    public static class TypeVariableWildcardUpperInterfaceBound<T extends ArrayList<? extends Callable<T>>> {
        /* empty */
    }

    @SuppressWarnings("unused")
    public static class TypeVariableWildcardLowerClassBound<T extends ArrayList<? super ArrayList<T>>> {
        /* empty */
    }

    @SuppressWarnings("unused")
    public static class TypeVariableWildcardLowerInterfaceBound<T extends ArrayList<? super Callable<T>>> {
        /* empty */
    }
}
