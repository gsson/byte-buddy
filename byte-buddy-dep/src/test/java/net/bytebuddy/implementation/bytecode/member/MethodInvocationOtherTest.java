package net.bytebuddy.implementation.bytecode.member;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.StackSize;
import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import org.junit.Test;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MethodInvocationOtherTest {

    private static final String FOO = "foo";

    @Test
    public void testObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(MethodInvocation.class).apply();
        ObjectPropertyAssertion.of(MethodInvocation.IllegalInvocation.class).apply();
        ObjectPropertyAssertion.of(MethodInvocation.Invocation.class).refine(new ObjectPropertyAssertion.Refinement<MethodDescription>() {
            @Override
            public void apply(MethodDescription mock) {
                TypeDescription declaringType = mock(TypeDescription.class);
                when(declaringType.asRawType()).thenReturn(declaringType);
                when(mock.getDeclaringType()).thenReturn(declaringType);
                TypeDescription returnType = mock(TypeDescription.class);
                when(returnType.getStackSize()).thenReturn(StackSize.ZERO);
                when(mock.getReturnType()).thenReturn(returnType);
                when(returnType.asRawType()).thenReturn(returnType);
                when(mock.getInternalName()).thenReturn(FOO);
                when(mock.getParameters()).thenReturn(new ParameterList.Empty());
            }
        }).apply();
        ObjectPropertyAssertion.of(MethodInvocation.DynamicInvocation.class).apply();
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegal() throws Exception {
        assertThat(MethodInvocation.IllegalInvocation.INSTANCE.isValid(), is(false));
        assertThat(MethodInvocation.IllegalInvocation.INSTANCE.special(mock(TypeDescription.class)),
                is((StackManipulation) StackManipulation.Illegal.INSTANCE));
        assertThat(MethodInvocation.IllegalInvocation.INSTANCE.virtual(mock(TypeDescription.class)),
                is((StackManipulation) StackManipulation.Illegal.INSTANCE));
        assertThat(MethodInvocation.IllegalInvocation.INSTANCE.dynamic(FOO, mock(TypeDescription.class), mock(TypeList.class), mock(List.class)),
                is((StackManipulation) StackManipulation.Illegal.INSTANCE));
        MethodInvocation.IllegalInvocation.INSTANCE.apply(mock(MethodVisitor.class), mock(Implementation.Context.class));
    }
}
