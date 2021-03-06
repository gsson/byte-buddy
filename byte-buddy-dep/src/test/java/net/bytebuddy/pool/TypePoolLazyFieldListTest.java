package net.bytebuddy.pool;

import net.bytebuddy.description.field.AbstractFieldListTest;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.anyOf;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TypePoolLazyFieldListTest extends AbstractFieldListTest<Field> {

    private TypePool typePool;

    @Before
    public void setUp() throws Exception {
        typePool = TypePool.Default.ofClassPath();
    }

    @After
    public void tearDown() throws Exception {
        typePool.clear();
    }

    @Override
    protected Field getFirst() throws Exception {
        return Foo.class.getDeclaredField("foo");
    }

    @Override
    protected Field getSecond() throws Exception {
        return Foo.class.getDeclaredField("bar");
    }

    @Override
    protected FieldList asList(List<Field> elements) {
        return typePool.describe(Foo.class.getName()).resolve().getDeclaredFields().filter(anyOf(elements.toArray(new Field[elements.size()])));
    }

    @Override
    protected FieldDescription asElement(Field element) {
        return new FieldDescription.ForLoadedField(element);
    }
}
