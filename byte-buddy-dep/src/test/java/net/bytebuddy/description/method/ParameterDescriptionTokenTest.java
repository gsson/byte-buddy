package net.bytebuddy.description.method;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.generic.GenericTypeDescription;
import net.bytebuddy.test.utility.MockitoRule;
import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class ParameterDescriptionTokenTest {

    private static final String FOO = "foo";

    private static final String BAR = "bar";

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private GenericTypeDescription first, second;

    @Test
    public void testTokenIdentity() throws Exception {
        ParameterDescription.Token token = new ParameterDescription.Token(mock(GenericTypeDescription.class),
                Collections.singletonList(mock(AnnotationDescription.class)),
                null,
                null);
        assertThat(token, is(token));
    }

    @Test
    public void testTokenInequality() throws Exception {
        GenericTypeDescription typeDescription = mock(GenericTypeDescription.class);
        AnnotationDescription annotationDescription = mock(AnnotationDescription.class);
        assertThat(new ParameterDescription.Token(typeDescription, Collections.singletonList(annotationDescription), null, null),
                not(new ParameterDescription.Token(typeDescription, Collections.singletonList(annotationDescription), null, null)));
    }

    @Test
    public void testNameEquality() throws Exception {
        assertThat(new ParameterDescription.Token(mock(GenericTypeDescription.class), Collections.singletonList(mock(AnnotationDescription.class)), FOO, null),
                is(new ParameterDescription.Token(mock(GenericTypeDescription.class), Collections.singletonList(mock(AnnotationDescription.class)), FOO, null)));
    }

    @Test
    public void testNameInequality() throws Exception {
        assertThat(new ParameterDescription.Token(mock(GenericTypeDescription.class), Collections.singletonList(mock(AnnotationDescription.class)), FOO, null),
                not(new ParameterDescription.Token(mock(GenericTypeDescription.class), Collections.singletonList(mock(AnnotationDescription.class)), BAR, null)));
    }

    @Test
    public void testObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(ParameterDescription.Token.class).create(new ObjectPropertyAssertion.Creator<Integer>() {
            @Override
            public Integer create() {
                return new Random().nextInt();
            }
        }).applyBasic();
    }
}
