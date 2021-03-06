package net.bytebuddy.description.method;

import net.bytebuddy.description.ByteCodeElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.generic.GenericTypeDescription;
import net.bytebuddy.description.type.generic.GenericTypeList;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.FilterableList;
import net.bytebuddy.utility.JavaMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.none;

/**
 * Represents a list of parameters of a method or a constructor.
 */
public interface ParameterList extends FilterableList<ParameterDescription, ParameterList> {

    /**
     * Transforms this list of parameters into a list of the types of the represented parameters.
     *
     * @return A list of types representing the parameters of this list.
     */
    GenericTypeList asTypeList();

    /**
     * Transforms the list of parameter descriptions into a list of detached tokens.
     *
     * @return The transformed token list.
     */
    ByteCodeElement.Token.TokenList<ParameterDescription.Token> asTokenList();

    /**
     * Transforms the list of parameter descriptions into a list of detached tokens. All types that are matched by the provided
     * target type matcher are substituted by {@link net.bytebuddy.dynamic.TargetType}.
     *
     * @param targetTypeMatcher A matcher that indicates type substitution.
     * @return The transformed token list.
     */
    ByteCodeElement.Token.TokenList<ParameterDescription.Token> asTokenList(ElementMatcher<? super TypeDescription> targetTypeMatcher);

    /**
     * Checks if all parameters in this list define both an explicit name and an explicit modifier.
     *
     * @return {@code true} if all parameters in this list define both an explicit name and an explicit modifier.
     */
    boolean hasExplicitMetaData();

    /**
     * An base implementation for a {@link ParameterList}.
     */
    abstract class AbstractBase extends FilterableList.AbstractBase<ParameterDescription, ParameterList> implements ParameterList {

        @Override
        public boolean hasExplicitMetaData() {
            for (ParameterDescription parameterDescription : this) {
                if (!parameterDescription.isNamed() || !parameterDescription.hasModifiers()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ByteCodeElement.Token.TokenList<ParameterDescription.Token> asTokenList() {
            return asTokenList(none());
        }

        @Override
        public ByteCodeElement.Token.TokenList<ParameterDescription.Token> asTokenList(ElementMatcher<? super TypeDescription> targetTypeMatcher) {
            List<ParameterDescription.Token> tokens = new ArrayList<ParameterDescription.Token>(size());
            for (ParameterDescription parameterDescription : this) {
                tokens.add(parameterDescription.asToken(targetTypeMatcher));
            }
            return new ByteCodeElement.Token.TokenList<ParameterDescription.Token>(tokens);
        }

        @Override
        public GenericTypeList asTypeList() {
            List<GenericTypeDescription> types = new ArrayList<GenericTypeDescription>(size());
            for (ParameterDescription parameterDescription : this) {
                types.add(parameterDescription.getType());
            }
            return new GenericTypeList.Explicit(types);
        }

        @Override
        protected ParameterList wrap(List<ParameterDescription> values) {
            return new Explicit(values);
        }
    }

    /**
     * Represents a list of parameters for an executable, i.e. a {@link java.lang.reflect.Method} or
     * {@link java.lang.reflect.Constructor}.
     */
    class ForLoadedExecutable extends AbstractBase {

        /**
         * Represents the {@code java.lang.reflect.Executable}'s {@code getParameters} method.
         */
        private static final JavaMethod GET_PARAMETERS;

        /*
         * Initializes the {@link net.bytebuddy.utility.JavaMethod} instances of this class dependant on
         * whether they are available.
         */
        static {
            JavaMethod getParameters;
            try {
                Class<?> executableType = Class.forName("java.lang.reflect.Executable");
                getParameters = new JavaMethod.ForLoadedMethod(executableType.getDeclaredMethod("getParameters"));
            } catch (Exception ignored) {
                getParameters = JavaMethod.ForUnavailableMethod.INSTANCE;
            }
            GET_PARAMETERS = getParameters;
        }

        /**
         * An array of the represented {@code java.lang.reflect.Parameter} instances.
         */
        private final Object[] parameter;

        /**
         * Creates a list representing a method's or a constructor's parameters.
         *
         * @param parameter The {@code java.lang.reflect.Parameter}-typed parameters to represent.
         */
        protected ForLoadedExecutable(Object[] parameter) {
            this.parameter = parameter;
        }

        /**
         * Creates a parameter list for a loaded method.
         *
         * @param method The method to represent.
         * @return A list of parameters for this method.
         */
        public static ParameterList of(Method method) {
            return GET_PARAMETERS.isInvokable()
                    ? new ForLoadedExecutable((Object[]) GET_PARAMETERS.invoke(method))
                    : new OfLegacyVmMethod(method);
        }

        /**
         * Creates a parameter list for a loaded constructor.
         *
         * @param constructor The constructor to represent.
         * @return A list of parameters for this constructor.
         */
        public static ParameterList of(Constructor<?> constructor) {
            return GET_PARAMETERS.isInvokable()
                    ? new ForLoadedExecutable((Object[]) GET_PARAMETERS.invoke(constructor))
                    : new OfLegacyVmConstructor(constructor);
        }

        @Override
        public ParameterDescription get(int index) {
            return new ParameterDescription.ForLoadedParameter(parameter[index], index);
        }

        @Override
        public int size() {
            return parameter.length;
        }

        @Override
        public GenericTypeList asTypeList() {
            List<GenericTypeDescription> types = new ArrayList<GenericTypeDescription>(parameter.length);
            for (Object aParameter : parameter) {
                types.add(new GenericTypeDescription.LazyProjection.OfLoadedParameter(aParameter));
            }
            return new GenericTypeList.Explicit(types);
        }

        /**
         * Represents a list of method parameters on virtual machines where the {@code java.lang.reflect.Parameter}
         * type is not available.
         */
        protected static class OfLegacyVmMethod extends ParameterList.AbstractBase {

            /**
             * The represented method.
             */
            private final Method method;

            /**
             * An array of this method's parameter types.
             */
            private final Class<?>[] parameterType;

            /**
             * An array of all parameter annotations of the represented method.
             */
            private final Annotation[][] parameterAnnotation;

            /**
             * Creates a legacy representation of a method's parameters.
             *
             * @param method The method to represent.
             */
            protected OfLegacyVmMethod(Method method) {
                this.method = method;
                this.parameterType = method.getParameterTypes();
                this.parameterAnnotation = method.getParameterAnnotations();
            }

            @Override
            public ParameterDescription get(int index) {
                return new ParameterDescription.ForLoadedParameter.OfLegacyVmMethod(method, index, parameterType[index], parameterAnnotation[index]);
            }

            @Override
            public int size() {
                return parameterType.length;
            }

            @Override
            public GenericTypeList asTypeList() {
                List<GenericTypeDescription> types = new ArrayList<GenericTypeDescription>(parameterType.length);
                for (int index = 0; index < parameterType.length; index++) {
                    types.add(new GenericTypeDescription.LazyProjection.OfLoadedParameter.OfLegacyVmMethod(method, index, parameterType[index]));
                }
                return new GenericTypeList.Explicit(types);
            }
        }

        /**
         * Represents a list of constructor parameters on virtual machines where the {@code java.lang.reflect.Parameter}
         * type is not available.
         */
        protected static class OfLegacyVmConstructor extends ParameterList.AbstractBase {

            /**
             * The represented constructor.
             */
            private final Constructor<?> constructor;

            /**
             * An array of this method's parameter types.
             */
            private final Class<?>[] parameterType;

            /**
             * An array of all parameter annotations of the represented method.
             */
            private final Annotation[][] parameterAnnotation;

            /**
             * Creates a legacy representation of a constructor's parameters.
             *
             * @param constructor The constructor to represent.
             */
            public OfLegacyVmConstructor(Constructor<?> constructor) {
                this.constructor = constructor;
                this.parameterType = constructor.getParameterTypes();
                this.parameterAnnotation = constructor.getParameterAnnotations();
            }

            @Override
            public ParameterDescription get(int index) {
                return new ParameterDescription.ForLoadedParameter.OfLegacyVmConstructor(constructor, index, parameterType[index], parameterAnnotation[index]);
            }

            @Override
            public int size() {
                return parameterType.length;
            }

            @Override
            public GenericTypeList asTypeList() {
                List<GenericTypeDescription> types = new ArrayList<GenericTypeDescription>(parameterType.length);
                for (int index = 0; index < parameterType.length; index++) {
                    types.add(new GenericTypeDescription.LazyProjection.OfLoadedParameter.OfLegacyVmConstructor(constructor, index, parameterType[index]));
                }
                return new GenericTypeList.Explicit(types);
            }
        }
    }

    /**
     * A list of explicitly provided parameter descriptions.
     */
    class Explicit extends AbstractBase {

        /**
         * The list of parameter descriptions that are represented by this list.
         */
        private final List<? extends ParameterDescription> parameterDescriptions;

        /**
         * Creates a new list of explicit parameter descriptions.
         *
         * @param parameterDescriptions The list of parameter descriptions that are represented by this list.
         */
        public Explicit(List<? extends ParameterDescription> parameterDescriptions) {
            this.parameterDescriptions = Collections.unmodifiableList(parameterDescriptions);
        }

        @Override
        public ParameterDescription get(int index) {
            return parameterDescriptions.get(index);
        }

        @Override
        public int size() {
            return parameterDescriptions.size();
        }

        /**
         * A parameter list representing parameters without meta data or annotations.
         */
        public static class ForTypes extends ParameterList.AbstractBase {

            /**
             * The method description that declares the parameters.
             */
            private final MethodDescription methodDescription;

            /**
             * A list of detached types representing the parameters.
             */
            private final List<? extends GenericTypeDescription> typeDescriptions;

            /**
             * Creates a new parameter type list.
             *
             * @param methodDescription The method description that declares the parameters.
             * @param typeDescriptions  A list of detached types representing the parameters.
             */
            public ForTypes(MethodDescription methodDescription, List<? extends GenericTypeDescription> typeDescriptions) {
                this.methodDescription = methodDescription;
                this.typeDescriptions = typeDescriptions;
            }

            @Override
            public ParameterDescription get(int index) {
                int offset = methodDescription.isStatic() ? 0 : 1;
                for (GenericTypeDescription typeDescription : typeDescriptions.subList(0, index)) {
                    offset += typeDescription.getStackSize().getSize();
                }
                return new ParameterDescription.Latent(methodDescription, typeDescriptions.get(index), index, offset);
            }

            @Override
            public int size() {
                return typeDescriptions.size();
            }
        }
    }

    /**
     * A list of parameter descriptions for a list of detached tokens. For the returned parameter, each token is attached to its parameter representation.
     */
    class ForTokens extends AbstractBase {

        /**
         * The method that is declaring the represented token.
         */
        private final MethodDescription declaringMethod;

        /**
         * The list of tokens to represent.
         */
        private final List<? extends ParameterDescription.Token> tokens;

        /**
         * Creates a new parameter list for the provided tokens.
         *
         * @param declaringMethod The method that is declaring the represented token.
         * @param tokens          The list of tokens to represent.
         */
        public ForTokens(MethodDescription declaringMethod, List<? extends ParameterDescription.Token> tokens) {
            this.declaringMethod = declaringMethod;
            this.tokens = tokens;
        }

        @Override
        public ParameterDescription get(int index) {
            int offset = declaringMethod.isStatic() ? 0 : 1;
            for (ParameterDescription.Token token : tokens.subList(0, index)) {
                offset += token.getType().getStackSize().getSize();
            }
            return new ParameterDescription.Latent(declaringMethod, tokens.get(index), index, offset);
        }

        @Override
        public int size() {
            return tokens.size();
        }
    }

    /**
     * A list of parameter descriptions that yields {@link net.bytebuddy.description.method.ParameterDescription.TypeSubstituting}.
     */
    class TypeSubstituting extends AbstractBase {

        /**
         * The method that is declaring the transformed parameters.
         */
        private final MethodDescription declaringMethod;

        /**
         * The untransformed parameters that are represented by this list.
         */
        private final List<? extends ParameterDescription> parameterDescriptions;

        /**
         * The visitor to apply to the parameter types before returning them.
         */
        private final GenericTypeDescription.Visitor<? extends GenericTypeDescription> visitor;

        /**
         * Creates a new type substituting parameter list.
         *
         * @param declaringMethod       The method that is declaring the transformed parameters.
         * @param parameterDescriptions The untransformed parameters that are represented by this list.
         * @param visitor               The visitor to apply to the parameter types before returning them.
         */
        public TypeSubstituting(MethodDescription declaringMethod,
                                List<? extends ParameterDescription> parameterDescriptions,
                                GenericTypeDescription.Visitor<? extends GenericTypeDescription> visitor) {
            this.declaringMethod = declaringMethod;
            this.parameterDescriptions = parameterDescriptions;
            this.visitor = visitor;
        }

        @Override
        public ParameterDescription get(int index) {
            return new ParameterDescription.TypeSubstituting(declaringMethod, parameterDescriptions.get(index), visitor);
        }

        @Override
        public int size() {
            return parameterDescriptions.size();
        }
    }

    /**
     * An empty list of parameters.
     */
    class Empty extends FilterableList.Empty<ParameterDescription, ParameterList> implements ParameterList {

        @Override
        public boolean hasExplicitMetaData() {
            return true;
        }

        @Override
        public GenericTypeList asTypeList() {
            return new GenericTypeList.Empty();
        }

        @Override
        public ByteCodeElement.Token.TokenList<ParameterDescription.Token> asTokenList() {
            return new ByteCodeElement.Token.TokenList<ParameterDescription.Token>(Collections.<ParameterDescription.Token>emptyList());
        }

        @Override
        public ByteCodeElement.Token.TokenList<ParameterDescription.Token> asTokenList(ElementMatcher<? super TypeDescription> targetTypeMatcher) {
            return new ByteCodeElement.Token.TokenList<ParameterDescription.Token>(Collections.<ParameterDescription.Token>emptyList());
        }
    }
}
