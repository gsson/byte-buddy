package net.bytebuddy.description.type;

import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.annotation.AnnotatedCodeElement;
import net.bytebuddy.description.annotation.AnnotationList;
import org.objectweb.asm.Opcodes;

/**
 * A package description representedBy a Java package.
 */
public interface PackageDescription extends NamedElement.WithRuntimeName, AnnotatedCodeElement {

    /**
     * The name of a Java class representing a package description.
     */
    String PACKAGE_CLASS_NAME = "package-info";

    /**
     * The modifiers of a Java class representing a package description.
     */
    int PACKAGE_MODIFIERS = Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_SYNTHETIC;

    /**
     * Checks if this package description representedBy a sealed package. This information is only available
     * for descriptions that represented loaded packages as packages are sealed by a {@link ClassLoader}.
     *
     * @return {@code true} if this package is sealed.
     */
    boolean isSealed();

    /**
     * An abstract base implementation of a package description.
     */
    abstract class AbstractPackageDescription implements PackageDescription {

        @Override
        public String getInternalName() {
            return getName().replace('.', '/');
        }

        @Override
        public String getSourceCodeName() {
            return getName();
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof PackageDescription
                    && getName().equals(((PackageDescription) other).getName());
        }

        @Override
        public String toString() {
            return "package " + getName();
        }
    }

    /**
     * A simple implementation of a package without annotations.
     */
    class Simple extends AbstractPackageDescription {

        /**
         * The name of the package.
         */
        private final String name;

        /**
         * Creates a new simple package.
         *
         * @param name The name of the package.
         */
        public Simple(String name) {
            this.name = name;
        }

        @Override
        public AnnotationList getDeclaredAnnotations() {
            return new AnnotationList.Empty();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isSealed() {
            return false;
        }
    }

    /**
     * Represents a loaded {@link java.lang.Package} wrapped as a
     * {@link PackageDescription}.
     */
    class ForLoadedPackage extends AbstractPackageDescription {

        /**
         * The represented package.
         */
        private final Package aPackage;

        /**
         * Creates a new loaded package representation.
         *
         * @param aPackage The represented package.
         */
        public ForLoadedPackage(Package aPackage) {
            this.aPackage = aPackage;
        }

        @Override
        public AnnotationList getDeclaredAnnotations() {
            return new AnnotationList.ForLoadedAnnotation(aPackage.getDeclaredAnnotations());
        }

        @Override
        public String getName() {
            return aPackage.getName();
        }

        @Override
        public boolean isSealed() {
            return aPackage.isSealed();
        }
    }
}
