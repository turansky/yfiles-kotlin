package com.github.turansky.yfiles.compiler.backend.js

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.name.Name

class YClassPropertyDescriptor(
    owner: ClassDescriptor
) : PropertyDescriptorImpl(
    owner,
    null,
    Annotations.EMPTY,
    Modality.FINAL,
    Visibilities.PUBLIC,
    false,
    Name.identifier("\$class"),
    CallableMemberDescriptor.Kind.DECLARATION,
    owner.source,
    false,
    false,
    false,
    false,
    false,
    false
)
