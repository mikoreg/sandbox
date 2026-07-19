package com.acme.auctions.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class CoreArchitectureTest {

    @Test
    void coreMustNotDependOnFrameworksOrPersistenceAnnotations() {
        var importedClasses = new ClassFileImporter().importPackages("com.acme.auctions.core");

        noClasses().that().resideInAPackage("..core..")
                .should().accessClassesThat().resideInAnyPackage(
                        "..org.springframework..",
                        "..jakarta.persistence.."
                )
                .check(importedClasses);
    }
}
