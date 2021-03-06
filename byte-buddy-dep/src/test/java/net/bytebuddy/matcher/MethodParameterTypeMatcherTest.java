package net.bytebuddy.matcher;

import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.generic.GenericTypeDescription;
import net.bytebuddy.description.type.generic.GenericTypeList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class MethodParameterTypeMatcherTest extends AbstractElementMatcherTest<MethodParameterTypeMatcher<?>> {

    @Mock
    private ElementMatcher<? super List<? extends GenericTypeDescription>> parameterMatcher;

    @Mock
    private GenericTypeList typeList;

    @Mock
    private ParameterList parameterList;

    @SuppressWarnings("unchecked")
    public MethodParameterTypeMatcherTest() {
        super((Class<MethodParameterTypeMatcher<?>>) (Object) MethodParameterTypeMatcher.class, "types");
    }

    @Before
    public void setUp() throws Exception {
        when(parameterList.asTypeList()).thenReturn(typeList);
    }

    @Test
    public void testMatch() throws Exception {
        when(parameterMatcher.matches(typeList)).thenReturn(true);
        assertThat(new MethodParameterTypeMatcher<ParameterList>(parameterMatcher).matches(parameterList), is(true));
        verify(parameterMatcher).matches(typeList);
        verifyNoMoreInteractions(parameterMatcher);
    }

    @Test
    public void testNoMatch() throws Exception {
        when(parameterMatcher.matches(typeList)).thenReturn(false);
        assertThat(new MethodParameterTypeMatcher<ParameterList>(parameterMatcher).matches(parameterList), is(false));
        verify(parameterMatcher).matches(typeList);
        verifyNoMoreInteractions(parameterMatcher);
    }
}
