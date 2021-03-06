package net.bytebuddy.description.method;

import net.bytebuddy.description.ByteCodeElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.generic.GenericTypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.FilterableList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.none;

/**
 * Implementations represent a list of method descriptions.
 */
public interface MethodList extends FilterableList<MethodDescription, MethodList> {

    /**
     * Transforms the list of method descriptions into a list of detached tokens.
     *
     * @return The transformed token list.
     */
    ByteCodeElement.Token.TokenList<MethodDescription.Token> asTokenList();

    /**
     * Transforms the list of method descriptions into a list of detached tokens. All types that are matched by the provided
     * target type matcher are substituted by {@link net.bytebuddy.dynamic.TargetType}.
     *
     * @param targetTypeMatcher A matcher that indicates type substitution.
     * @return The transformed token list.
     */
    ByteCodeElement.Token.TokenList<MethodDescription.Token> asTokenList(ElementMatcher<? super TypeDescription> targetTypeMatcher);

    /**
     * A base implementation of a {@link MethodList}.
     */
    abstract class AbstractBase extends FilterableList.AbstractBase<MethodDescription, MethodList> implements MethodList {

        @Override
        protected MethodList wrap(List<MethodDescription> values) {
            return new Explicit(values);
        }

        @Override
        public ByteCodeElement.Token.TokenList<MethodDescription.Token> asTokenList() {
            return asTokenList(none());
        }

        @Override
        public ByteCodeElement.Token.TokenList<MethodDescription.Token> asTokenList(ElementMatcher<? super TypeDescription> targetTypeMatcher) {
            List<MethodDescription.Token> tokens = new ArrayList<MethodDescription.Token>(size());
            for (MethodDescription fieldDescription : this) {
                tokens.add(fieldDescription.asToken(targetTypeMatcher));
            }
            return new ByteCodeElement.Token.TokenList<MethodDescription.Token>(tokens);
        }
    }

    /**
     * A method list implementation that returns all loaded byte code methods (methods and constructors) that
     * are declared for a given type.
     */
    class ForLoadedType extends AbstractBase {

        /**
         * The loaded methods that are represented by this method list.
         */
        private final List<? extends Method> methods;

        /**
         * The loaded constructors that are represented by this method list.
         */
        private final List<? extends Constructor<?>> constructors;

        /**
         * Creates a new list for a loaded type. Method descriptions are created on demand.
         *
         * @param type The type to be represented by this method list.
         */
        public ForLoadedType(Class<?> type) {
            this(type.getDeclaredConstructors(), type.getDeclaredMethods());
        }

        /**
         * Creates a method list that representedBy the given constructors and methods in their given order. The
         * constructors are assigned the indices before the methods.
         *
         * @param constructor The constructors to be represented by the method list.
         * @param method      The methods to be represented by the method list.
         */
        public ForLoadedType(Constructor<?>[] constructor, Method[] method) {
            this(Arrays.asList(constructor), Arrays.asList(method));
        }

        /**
         * Creates a method list that representedBy the given constructors and methods in their given order. The
         * constructors are assigned the indices before the methods.
         *
         * @param constructors The constructors to be represented by the method list.
         * @param methods      The methods to be represented by the method list.
         */
        public ForLoadedType(List<? extends Constructor<?>> constructors, List<? extends Method> methods) {
            this.constructors = constructors;
            this.methods = methods;
        }

        @Override
        public MethodDescription get(int index) {
            return index < constructors.size()
                    ? new MethodDescription.ForLoadedConstructor(constructors.get(index))
                    : new MethodDescription.ForLoadedMethod(methods.get(index - constructors.size()));

        }

        @Override
        public int size() {
            return constructors.size() + methods.size();
        }
    }

    /**
     * A method list that is a wrapper for a given list of method descriptions.
     */
    class Explicit extends AbstractBase {

        /**
         * The list of methods that is represented by this method list.
         */
        private final List<? extends MethodDescription> methodDescriptions;

        /**
         * Creates a new wrapper for a given list of methods.
         *
         * @param methodDescriptions The underlying list of methods used for this method list.
         */
        public Explicit(List<? extends MethodDescription> methodDescriptions) {
            this.methodDescriptions = Collections.unmodifiableList(methodDescriptions);
        }

        @Override
        public MethodDescription get(int index) {
            return methodDescriptions.get(index);
        }

        @Override
        public int size() {
            return methodDescriptions.size();
        }
    }

    /**
     * A list of method descriptions for a list of detached tokens. For the returned method, each token is attached to its method representation.
     */
    class ForTokens extends AbstractBase {

        /**
         * The method's declaring type.
         */
        private final TypeDescription declaringType;

        /**
         * The list of method tokens to represent.
         */
        private final List<? extends MethodDescription.Token> tokens;

        /**
         * Creates a new list of method descriptions for a list of detached tokens.
         *
         * @param declaringType The method's declaring type.
         * @param tokens        The list of method tokens to represent.
         */
        public ForTokens(TypeDescription declaringType, List<? extends MethodDescription.Token> tokens) {
            this.declaringType = declaringType;
            this.tokens = tokens;
        }

        @Override
        public MethodDescription get(int index) {
            return new MethodDescription.Latent(declaringType, tokens.get(index));
        }

        @Override
        public int size() {
            return tokens.size();
        }
    }

    /**
     * A list of method descriptions that yields {@link net.bytebuddy.description.method.MethodDescription.TypeSubstituting}.
     */
    class TypeSubstituting extends AbstractBase {

        /**
         * The methods' declaring type.
         */
        private final GenericTypeDescription declaringType;

        /**
         * The list of method descriptions to represent.
         */
        private final List<? extends MethodDescription> methodDescriptions;

        /**
         * The visitor to apply to each method description before returning it.
         */
        private final GenericTypeDescription.Visitor<? extends GenericTypeDescription> visitor;

        /**
         * Creates a new type substituting method list.
         *
         * @param declaringType      The methods' declaring type.
         * @param methodDescriptions The list of method descriptions to represent.
         * @param visitor            The visitor to apply to each method description before returning it.
         */
        public TypeSubstituting(GenericTypeDescription declaringType,
                                List<? extends MethodDescription> methodDescriptions,
                                GenericTypeDescription.Visitor<? extends GenericTypeDescription> visitor) {
            this.declaringType = declaringType;
            this.methodDescriptions = methodDescriptions;
            this.visitor = visitor;
        }

        @Override
        public MethodDescription get(int index) {
            return new MethodDescription.TypeSubstituting(declaringType, methodDescriptions.get(index), visitor);
        }

        @Override
        public int size() {
            return methodDescriptions.size();
        }
    }

    /**
     * An implementation of an empty method list.
     */
    class Empty extends FilterableList.Empty<MethodDescription, MethodList> implements MethodList {

        @Override
        public ByteCodeElement.Token.TokenList<MethodDescription.Token> asTokenList() {
            return new ByteCodeElement.Token.TokenList<MethodDescription.Token>(Collections.<MethodDescription.Token>emptyList());
        }

        @Override
        public ByteCodeElement.Token.TokenList<MethodDescription.Token> asTokenList(ElementMatcher<? super TypeDescription> targetTypeMatcher) {
            return new ByteCodeElement.Token.TokenList<MethodDescription.Token>(Collections.<MethodDescription.Token>emptyList());
        }
    }
}
