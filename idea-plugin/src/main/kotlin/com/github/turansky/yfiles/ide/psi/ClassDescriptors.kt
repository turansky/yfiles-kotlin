package com.github.turansky.yfiles.ide.psi

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

internal val KtClassOrObject.descriptor: ClassDescriptor?
    get() = resolveToDescriptorIfAny(BodyResolveMode.FULL)
