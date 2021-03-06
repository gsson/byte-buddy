package net.bytebuddy.dynamic.loading;

import net.bytebuddy.test.utility.MockitoRule;
import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import static net.bytebuddy.matcher.ElementMatchers.isBootstrapClassLoader;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class MultipleParentClassLoaderTest {

    private static final String FOO = "foo", BAR = "bar", QUX = "qux", BAZ = "baz", SCHEME = "http://";

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private ClassLoader first, second;

    private URL fooUrl, barFirstUrl, barSecondUrl, quxUrl;

    @Before
    public void setUp() throws Exception {
        doReturn(Foo.class).when(first).loadClass(FOO);
        doReturn(BarFirst.class).when(first).loadClass(BAR);
        when(first.loadClass(QUX)).thenThrow(new ClassNotFoundException());
        when(first.loadClass(BAZ)).thenThrow(new ClassNotFoundException());
        doReturn(BarSecond.class).when(second).loadClass(BAR);
        doReturn(Qux.class).when(second).loadClass(QUX);
        when(second.loadClass(BAZ)).thenThrow(new ClassNotFoundException());
        fooUrl = new URL(SCHEME + FOO);
        barFirstUrl = new URL(SCHEME + BAR);
        barSecondUrl = new URL(SCHEME + BAZ);
        quxUrl = new URL(SCHEME + QUX);
        when(first.getResource(FOO)).thenReturn(fooUrl);
        when(first.getResource(BAR)).thenReturn(barFirstUrl);
        when(second.getResource(BAR)).thenReturn(barSecondUrl);
        when(second.getResource(QUX)).thenReturn(quxUrl);
        when(first.getResources(FOO)).thenReturn(new SingleElementEnumeration(fooUrl));
        when(first.getResources(BAR)).thenReturn(new SingleElementEnumeration(barFirstUrl));
        when(second.getResources(BAR)).thenReturn(new SingleElementEnumeration(barSecondUrl));
        when(second.getResources(QUX)).thenReturn(new SingleElementEnumeration(quxUrl));
    }

    @Test
    public void testSingleParentReturnsOriginal() throws Exception {
        assertThat(new MultipleParentClassLoader.Builder()
                .append(getClass().getClassLoader(), getClass().getClassLoader())
                .build(), is(getClass().getClassLoader()));
    }

    @Test
    public void testClassLoaderFilter() throws Exception {
        assertThat(new MultipleParentClassLoader.Builder()
                .append(getClass().getClassLoader(), null)
                .filter(isBootstrapClassLoader())
                .build(), is(getClass().getClassLoader()));
    }

    @Test
    public void testMultipleParentClassLoading() throws Exception {
        ClassLoader classLoader = new MultipleParentClassLoader.Builder().append(first, second, null).build();
        assertEquals(Foo.class, classLoader.loadClass(FOO));
        assertEquals(BarFirst.class, classLoader.loadClass(BAR));
        assertEquals(Qux.class, classLoader.loadClass(QUX));
        verify(first).loadClass(FOO);
        verify(first).loadClass(BAR);
        verify(first).loadClass(QUX);
        verifyNoMoreInteractions(first);
        verify(second).loadClass(QUX);
        verifyNoMoreInteractions(second);
    }

    @Test(expected = ClassNotFoundException.class)
    public void testMultipleParentClassLoadingNotFound() throws Exception {
        new MultipleParentClassLoader.Builder().append(first, second, null).build().loadClass(BAZ);
    }

    @Test
    public void testMultipleParentURL() throws Exception {
        ClassLoader classLoader = new MultipleParentClassLoader.Builder().append(first, second, null).build();
        assertThat(classLoader.getResource(FOO), is(fooUrl));
        assertThat(classLoader.getResource(BAR), is(barFirstUrl));
        assertThat(classLoader.getResource(QUX), is(quxUrl));
        verify(first).getResource(FOO);
        verify(first).getResource(BAR);
        verify(first).getResource(QUX);
        verifyNoMoreInteractions(first);
        verify(second).getResource(QUX);
        verifyNoMoreInteractions(second);
    }

    @Test
    public void testMultipleParentURLNotFound() throws Exception {
        assertThat(new MultipleParentClassLoader.Builder().append(first, second, null).build().getResource(BAZ), nullValue(URL.class));
    }

    @Test
    public void testMultipleParentEnumerationURL() throws Exception {
        ClassLoader classLoader = new MultipleParentClassLoader.Builder().append(first, second, null).build();
        Enumeration<URL> foo = classLoader.getResources(FOO);
        assertThat(foo.hasMoreElements(), is(true));
        assertThat(foo.nextElement(), is(fooUrl));
        assertThat(foo.hasMoreElements(), is(false));
        Enumeration<URL> bar = classLoader.getResources(BAR);
        assertThat(bar.hasMoreElements(), is(true));
        assertThat(bar.nextElement(), is(barFirstUrl));
        assertThat(bar.hasMoreElements(), is(true));
        assertThat(bar.nextElement(), is(barSecondUrl));
        assertThat(bar.hasMoreElements(), is(false));
        Enumeration<URL> qux = classLoader.getResources(QUX);
        assertThat(qux.hasMoreElements(), is(true));
        assertThat(qux.nextElement(), is(quxUrl));
        assertThat(qux.hasMoreElements(), is(false));
    }

    @Test(expected = NoSuchElementException.class)
    public void testMultipleParentEnumerationNotFound() throws Exception {
        ClassLoader classLoader = new MultipleParentClassLoader.Builder().append(first, second, null).build();
        Enumeration<URL> enumeration = classLoader.getResources(BAZ);
        assertThat(enumeration.hasMoreElements(), is(false));
        enumeration.nextElement();
    }

    @Test
    public void testObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(MultipleParentClassLoader.class).applyBasic();
        ObjectPropertyAssertion.of(MultipleParentClassLoader.CompoundEnumeration.class).applyBasic();
        ObjectPropertyAssertion.of(MultipleParentClassLoader.Builder.class).apply();
    }

    public static class Foo {
        /* empty */
    }

    public static class BarFirst {
        /* empty */
    }

    public static class BarSecond {
        /* empty */
    }

    public static class Qux {
        /* empty */
    }

    private static class SingleElementEnumeration implements Enumeration<URL> {

        private URL element;

        public SingleElementEnumeration(URL element) {
            this.element = element;
        }

        @Override
        public boolean hasMoreElements() {
            return element != null;
        }

        @Override
        public URL nextElement() {
            if (!hasMoreElements()) {
                throw new AssertionError();
            }
            try {
                return element;
            } finally {
                element = null;
            }
        }
    }
}
