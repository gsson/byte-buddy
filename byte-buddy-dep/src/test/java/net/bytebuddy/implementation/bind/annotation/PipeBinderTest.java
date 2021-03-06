package net.bytebuddy.implementation.bind.annotation;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.MethodLookupEngine;
import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PipeBinderTest extends AbstractAnnotationBinderTest<Pipe> {

    private TargetMethodAnnotationDrivenBinder.ParameterBinder<Pipe> binder;

    @Mock
    private MethodDescription targetMethod;

    @Mock
    private TypeDescription targetMethodType;

    public PipeBinderTest() {
        super(Pipe.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(targetMethod.getDeclaringType()).thenReturn(targetMethodType);
        binder = new Pipe.Binder(targetMethod);
        when(targetMethodType.asRawType()).thenReturn(targetMethodType);
    }

    @Override
    protected TargetMethodAnnotationDrivenBinder.ParameterBinder<Pipe> getSimpleBinder() {
        return binder;
    }

    @Test
    public void testParameterBinding() throws Exception {
        when(target.getType()).thenReturn(targetMethodType);
        MethodDelegationBinder.ParameterBinding<?> parameterBinding = binder.bind(annotationDescription,
                source,
                target,
                implementationTarget,
                assigner);
        assertThat(parameterBinding.isValid(), is(true));
    }

    @Test
    public void testCannotPipeStaticMethod() throws Exception {
        when(target.getType()).thenReturn(targetMethodType);
        when(source.isStatic()).thenReturn(true);
        MethodDelegationBinder.ParameterBinding<?> parameterBinding = binder.bind(annotationDescription,
                source,
                target,
                implementationTarget,
                assigner);
        assertThat(parameterBinding.isValid(), is(false));
    }

    @Test(expected = IllegalStateException.class)
    public void testParameterBindingOnIllegalTargetTypeThrowsException() throws Exception {
        TypeDescription targetType = mock(TypeDescription.class);
        when(targetType.asRawType()).thenReturn(targetType);
        when(target.getType()).thenReturn(targetType);
        binder.bind(annotationDescription,
                source,
                target,
                implementationTarget,
                assigner);
    }

    @Test
    public void testObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(Pipe.Binder.class).apply();
        ObjectPropertyAssertion.of(Pipe.Binder.PrecomputedFinding.class).apply();
        ObjectPropertyAssertion.of(Pipe.Binder.Redirection.class).apply();
        ObjectPropertyAssertion.of(Pipe.Binder.Redirection.MethodCall.class).skipSynthetic().apply();
        ObjectPropertyAssertion.of(Pipe.Binder.Redirection.ConstructorCall.class).apply();
    }

    @Test
    public void testRedirectionHashCodeEquals() throws Exception {
        MethodDescription sourceMethod = mock(MethodDescription.class);
        Assigner assigner = mock(Assigner.class);
        MethodLookupEngine.Factory factory = mock(MethodLookupEngine.Factory.class);
        Pipe.Binder.Redirection redirection = new Pipe.Binder.Redirection(targetMethodType,
                sourceMethod,
                assigner,
                false,
                factory);
        assertThat(redirection.hashCode(), is(new Pipe.Binder.Redirection(targetMethodType,
                sourceMethod,
                assigner,
                false,
                factory).hashCode()));
        assertThat(redirection, is(new Pipe.Binder.Redirection(targetMethodType,
                sourceMethod,
                assigner,
                false,
                factory)));
        assertThat(redirection.hashCode(), not(is(new Pipe.Binder.Redirection(targetMethodType,
                sourceMethod,
                assigner,
                true,
                factory).hashCode())));
        assertThat(redirection, not(is(new Pipe.Binder.Redirection(targetMethodType,
                sourceMethod,
                assigner,
                true,
                factory))));
    }
}
