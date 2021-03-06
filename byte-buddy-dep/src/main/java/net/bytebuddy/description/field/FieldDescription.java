package net.bytebuddy.description.field;

import net.bytebuddy.description.ByteCodeElement;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.generic.GenericTypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.objectweb.asm.signature.SignatureWriter;

import java.lang.reflect.Field;
import java.lang.reflect.GenericSignatureFormatError;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.none;

/**
 * Implementations of this interface describe a Java field. Implementations of this interface must provide meaningful
 * {@code equal(Object)} and {@code hashCode()} implementations.
 */
public interface FieldDescription extends ByteCodeElement, NamedElement.WithGenericName {

    /**
     * Returns the type of the described field.
     *
     * @return The type of the described field.
     */
    GenericTypeDescription getType();

    /**
     * Transforms this field description into a token. For the resulting token, all type variables within the scope
     * of this field's type are detached from their declaration context.
     *
     * @return A token representing this field.
     */
    Token asToken();

    /**
     * Transforms this field description into a token. For the resulting token, all type variables within the scope
     * of this field's type are detached from their declaration context.
     *
     * @param targetTypeMatcher A matcher that is applied to the field type for replacing any matched
     *                          type by {@link net.bytebuddy.dynamic.TargetType}.
     * @return A token representing this field.
     */
    Token asToken(ElementMatcher<? super TypeDescription> targetTypeMatcher);

    /**
     * An abstract base implementation of a field description.
     */
    abstract class AbstractFieldDescription extends AbstractModifierReviewable implements FieldDescription {

        @Override
        public String getInternalName() {
            return getName();
        }

        @Override
        public String getSourceCodeName() {
            return getName();
        }

        @Override
        public String getDescriptor() {
            return getType().asRawType().getDescriptor();
        }

        @Override
        public String getGenericSignature() {
            GenericTypeDescription fieldType = getType();
            try {
                return fieldType.getSort().isNonGeneric()
                        ? NON_GENERIC_SIGNATURE
                        : fieldType.accept(new GenericTypeDescription.Visitor.ForSignatureVisitor(new SignatureWriter())).toString();
            } catch (GenericSignatureFormatError ignored) {
                return NON_GENERIC_SIGNATURE;
            }
        }

        @Override
        public boolean isVisibleTo(TypeDescription typeDescription) {
            return getDeclaringType().asRawType().isVisibleTo(typeDescription)
                    && getType().asRawType().isVisibleTo(typeDescription)
                    && (isPublic()
                    || typeDescription.equals(getDeclaringType())
                    || (isProtected() && getDeclaringType().asRawType().isAssignableFrom(typeDescription))
                    || (!isPrivate() && typeDescription.isSamePackage(getDeclaringType().asRawType())));
        }

        @Override
        public FieldDescription.Token asToken() {
            return asToken(none());
        }

        @Override
        public FieldDescription.Token asToken(ElementMatcher<? super TypeDescription> targetTypeMatcher) {
            return new FieldDescription.Token(getName(),
                    getModifiers(),
                    getType().accept(new GenericTypeDescription.Visitor.Substitutor.ForDetachment(targetTypeMatcher)),
                    getDeclaredAnnotations());
        }

        @Override
        public boolean equals(Object other) {
            return other == this || other instanceof FieldDescription
                    && getName().equals(((FieldDescription) other).getName())
                    && getDeclaringType().asRawType().equals(((FieldDescription) other).getDeclaringType().asRawType());
        }

        @Override
        public int hashCode() {
            return getDeclaringType().asRawType().hashCode() + 31 * getName().hashCode();
        }

        @Override
        public String toGenericString() {
            StringBuilder stringBuilder = new StringBuilder();
            if (getModifiers() != EMPTY_MASK) {
                stringBuilder.append(Modifier.toString(getModifiers())).append(" ");
            }
            stringBuilder.append(getType().getSourceCodeName()).append(" ");
            stringBuilder.append(getDeclaringType().asRawType().getSourceCodeName()).append(".");
            return stringBuilder.append(getName()).toString();
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            if (getModifiers() != EMPTY_MASK) {
                stringBuilder.append(Modifier.toString(getModifiers())).append(" ");
            }
            stringBuilder.append(getType().asRawType().getSourceCodeName()).append(" ");
            stringBuilder.append(getDeclaringType().asRawType().getSourceCodeName()).append(".");
            return stringBuilder.append(getName()).toString();
        }
    }

    /**
     * An implementation of a field description for a loaded field.
     */
    class ForLoadedField extends AbstractFieldDescription {

        /**
         * The represented loaded field.
         */
        private final Field field;

        /**
         * Creates an immutable field description for a loaded field.
         *
         * @param field The represented field.
         */
        public ForLoadedField(Field field) {
            this.field = field;
        }

        @Override
        public GenericTypeDescription getType() {
            return new GenericTypeDescription.LazyProjection.OfLoadedFieldType(field);
        }

        @Override
        public AnnotationList getDeclaredAnnotations() {
            return new AnnotationList.ForLoadedAnnotation(field.getDeclaredAnnotations());
        }

        @Override
        public String getName() {
            return field.getName();
        }

        @Override
        public GenericTypeDescription getDeclaringType() {
            return new TypeDescription.ForLoadedType(field.getDeclaringClass());
        }

        @Override
        public int getModifiers() {
            return field.getModifiers();
        }

        @Override
        public boolean isSynthetic() {
            return field.isSynthetic();
        }
    }

    /**
     * A latent field description describes a field that is not attached to a declaring
     * {@link TypeDescription}.
     */
    class Latent extends AbstractFieldDescription {

        /**
         * The type for which this field is defined.
         */
        private final GenericTypeDescription declaringType;

        /**
         * The name of the field.
         */
        private final String fieldName;

        /**
         * The field's modifiers.
         */
        private final int modifiers;

        /**
         * The type of the field.
         */
        private final GenericTypeDescription fieldType;

        /**
         * The annotations of this field.
         */
        private final List<? extends AnnotationDescription> declaredAnnotations;

        /**
         * Creates a new latent field description. All provided types are attached to this instance before they are returned.
         *
         * @param declaringType The declaring type of the field.
         * @param token         A token representing the field's shape.
         */
        public Latent(GenericTypeDescription declaringType, FieldDescription.Token token) {
            this(declaringType,
                    token.getName(),
                    token.getModifiers(),
                    token.getType(),
                    token.getAnnotations());
        }

        /**
         * Creates a new latent field description. All provided types are attached to this instance before they are returned.
         *
         * @param declaringType       The declaring type of the field.
         * @param fieldName           The name of the field.
         * @param fieldType           The field's modifiers.
         * @param modifiers           The type of the field.
         * @param declaredAnnotations The annotations of this field.
         */
        public Latent(GenericTypeDescription declaringType,
                      String fieldName,
                      int modifiers,
                      GenericTypeDescription fieldType,
                      List<? extends AnnotationDescription> declaredAnnotations) {
            this.declaringType = declaringType;
            this.fieldName = fieldName;
            this.modifiers = modifiers;
            this.fieldType = fieldType;
            this.declaredAnnotations = declaredAnnotations;
        }

        @Override
        public GenericTypeDescription getType() {
            return fieldType.accept(GenericTypeDescription.Visitor.Substitutor.ForAttachment.of(this));
        }

        @Override
        public AnnotationList getDeclaredAnnotations() {
            return new AnnotationList.Explicit(declaredAnnotations);
        }

        @Override
        public String getName() {
            return fieldName;
        }

        @Override
        public GenericTypeDescription getDeclaringType() {
            return declaringType;
        }

        @Override
        public int getModifiers() {
            return modifiers;
        }
    }

    /**
     * A field description that representedBy a given field but with a substituted field type.
     */
    class TypeSubstituting extends AbstractFieldDescription {

        /**
         * The declaring type of the field.
         */
        private final GenericTypeDescription declaringType;

        /**
         * The represented field.
         */
        private final FieldDescription fieldDescription;

        /**
         * A visitor that is applied to the field type.
         */
        private final GenericTypeDescription.Visitor<? extends GenericTypeDescription> visitor;

        /**
         * Creates a field description with a substituted field type.
         *
         * @param declaringType    The declaring type of the field.
         * @param fieldDescription The represented field.
         * @param visitor          A visitor that is applied to the field type.
         */
        public TypeSubstituting(GenericTypeDescription declaringType,
                                FieldDescription fieldDescription,
                                GenericTypeDescription.Visitor<? extends GenericTypeDescription> visitor) {
            this.declaringType = declaringType;
            this.fieldDescription = fieldDescription;
            this.visitor = visitor;
        }

        @Override
        public GenericTypeDescription getType() {
            return fieldDescription.getType().accept(visitor);
        }

        @Override
        public AnnotationList getDeclaredAnnotations() {
            return fieldDescription.getDeclaredAnnotations();
        }

        @Override
        public GenericTypeDescription getDeclaringType() {
            return declaringType;
        }

        @Override
        public int getModifiers() {
            return fieldDescription.getModifiers();
        }

        @Override
        public String getName() {
            return fieldDescription.getName();
        }
    }

    /**
     * A token that representedBy a field's shape. A field token is equal to another token when the other field
     * tokens's name is equal to this token.
     */
    class Token implements ByteCodeElement.Token<Token> {

        /**
         * The name of the represented field.
         */
        private final String name;

        /**
         * The modifiers of the represented field.
         */
        private final int modifiers;

        /**
         * The type of the represented field.
         */
        private final GenericTypeDescription type;

        /**
         * The annotations of the represented field.
         */
        private final List<? extends AnnotationDescription> annotations;

        /**
         * Creates a new field token without annotations.
         *
         * @param name      The name of the represented field.
         * @param modifiers The modifiers of the represented field.
         * @param type      The type of the represented field.
         */
        public Token(String name, int modifiers, GenericTypeDescription type) {
            this(name, modifiers, type, Collections.<AnnotationDescription>emptyList());
        }

        /**
         * Creates a new field token.
         *
         * @param name        The name of the represented field.
         * @param modifiers   The modifiers of the represented field.
         * @param type        The type of the represented field.
         * @param annotations The annotations of the represented field.
         */
        public Token(String name, int modifiers, GenericTypeDescription type, List<? extends AnnotationDescription> annotations) {
            this.name = name;
            this.modifiers = modifiers;
            this.type = type;
            this.annotations = annotations;
        }

        /**
         * Returns the name of the represented field.
         *
         * @return The name of the represented field.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the type of the represented field.
         *
         * @return The type of the represented field.
         */
        public GenericTypeDescription getType() {
            return type;
        }

        /**
         * Returns the modifiers of the represented field.
         *
         * @return The modifiers of the represented field.
         */
        public int getModifiers() {
            return modifiers;
        }

        /**
         * Returns the annotations of the represented field.
         *
         * @return The annotations of the represented field.
         */
        public AnnotationList getAnnotations() {
            return new AnnotationList.Explicit(annotations);
        }

        @Override
        public Token accept(GenericTypeDescription.Visitor<? extends GenericTypeDescription> visitor) {
            return new Token(getName(),
                    getModifiers(),
                    getType().accept(visitor),
                    getAnnotations());
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Token)) return false;
            Token token = (Token) other;
            return getName().equals(token.getName());
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

        @Override
        public String toString() {
            return "FieldDescription.Token{" +
                    "name='" + name + '\'' +
                    ", modifiers=" + modifiers +
                    ", type=" + type +
                    ", annotations=" + annotations +
                    '}';
        }
    }
}
