package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.backend.common.implementsYObjectDirectly
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

internal val ClassDescriptor.shouldHaveGeneratedCompanion: Boolean
    get() = kind == ClassKind.CLASS && implementsYObjectDirectly

internal class ResolveExtension : SyntheticResolveExtension {
    override fun getSyntheticCompanionObjectNameIfNeeded(thisDescriptor: ClassDescriptor): Name? =
        if (thisDescriptor.shouldHaveGeneratedCompanion) {
            DEFAULT_NAME_FOR_COMPANION_OBJECT
        } else {
            null
        }
}
