package net.bytebuddy.implementation.bind.annotation;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.constant.DefaultValue;
import net.bytebuddy.implementation.bytecode.constant.NullConstant;

import java.lang.annotation.*;

/**
 * A stub value represents the (boxed) default value of the intercepted method's return type. This value can
 * only be assigned to a {@link java.lang.Object} parameter. This annotation is useful to conditionally return a
 * default value from a method when using an {@link java.lang.Object} return type in combination with the
 * {@link net.bytebuddy.implementation.bind.annotation.RuntimeType} annotation. The value is either representing
 * {@code null} if a method returns a reference type or {@code void} or a boxed primitive of the return type
 * representing the numeric value {@code 0}.
 *
 * @see net.bytebuddy.implementation.MethodDelegation
 * @see net.bytebuddy.implementation.bind.annotation.TargetMethodAnnotationDrivenBinder
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface StubValue {

    /**
     * Binds the {@link net.bytebuddy.implementation.bind.annotation.StubValue} annotation.
     */
    static enum Binder implements TargetMethodAnnotationDrivenBinder.ParameterBinder<StubValue> {

        /**
         * The singleton instance.
         */
        INSTANCE;

        @Override
        public Class<StubValue> getHandledType() {
            return StubValue.class;
        }

        @Override
        public MethodDelegationBinder.ParameterBinding<?> bind(AnnotationDescription.Loadable<StubValue> annotation,
                                                               MethodDescription source,
                                                               ParameterDescription target,
                                                               Implementation.Target implementationTarget,
                                                               Assigner assigner) {
            if (!target.getType().asRawType().represents(Object.class)) {
                throw new IllegalStateException(target + " uses StubValue annotation on non-Object type");
            }
            StackManipulation stackManipulation = source.getReturnType().asRawType().represents(void.class)
                    ? NullConstant.INSTANCE
                    : new StackManipulation.Compound(DefaultValue.of(source.getReturnType().asRawType()),
                    assigner.assign(source.getReturnType().asRawType(), TypeDescription.OBJECT, Assigner.STATICALLY_TYPED));
            return stackManipulation.isValid()
                    ? new MethodDelegationBinder.ParameterBinding.Anonymous(stackManipulation)
                    : MethodDelegationBinder.ParameterBinding.Illegal.INSTANCE;
        }

        @Override
        public String toString() {
            return "StubValue.Binder." + name();
        }
    }
}
