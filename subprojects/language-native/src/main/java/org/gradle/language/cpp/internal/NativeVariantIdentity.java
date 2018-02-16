/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.language.cpp.internal;

import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Usage;
import org.gradle.api.component.ComponentWithCoordinates;
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier;
import org.gradle.api.internal.component.SoftwareComponentInternal;
import org.gradle.api.internal.component.UsageContext;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.nativeplatform.OperatingSystemFamily;
import org.gradle.util.CollectionUtils;
import org.gradle.util.GUtil;

import java.util.Set;

public class NativeVariantIdentity implements SoftwareComponentInternal, ComponentWithCoordinates {
    private final String name;
    private final Provider<String> baseName;
    private final Provider<String> group;
    private final Provider<String> version;
    private final boolean debuggable;
    private final boolean optimized;
    private final OperatingSystemFamily operatingSystemFamily;
    private final Set<? extends UsageContext> usageContexts;

    public NativeVariantIdentity(String name, Provider<String> baseName, Provider<String> group, Provider<String> version, boolean debuggable, boolean optimized, OperatingSystemFamily operatingSystemFamily, Set<? extends UsageContext> usageContexts) {
        this.name = name;
        this.baseName = baseName;
        this.group = group;
        this.version = version;
        this.debuggable = debuggable;
        this.optimized = optimized;
        this.operatingSystemFamily = operatingSystemFamily;
        this.usageContexts = usageContexts;
    }

    public boolean isDebuggable() {
        return debuggable;
    }

    public boolean isOptimized() {
        return optimized;
    }

    public OperatingSystemFamily getOperatingSystemFamily() {
        return operatingSystemFamily;
    }

    @Override
    public ModuleVersionIdentifier getCoordinates() {
        return new DefaultModuleVersionIdentifier(group.get(), baseName.get() + "_" + GUtil.toWords(name, '_'), version.get());
    }

    public AttributeContainer getRuntimeAttributes() {
        return CollectionUtils.findFirst(usageContexts, new Spec<UsageContext>() {
            @Override
            public boolean isSatisfiedBy(UsageContext element) {
                return element.getUsage().getName().equals(Usage.NATIVE_RUNTIME);
            }
        }).getAttributes();
    }

    public AttributeContainer getLinkAttributes() {
        return CollectionUtils.findFirst(usageContexts, new Spec<UsageContext>() {
            @Override
            public boolean isSatisfiedBy(UsageContext element) {
                return element.getUsage().getName().equals(Usage.NATIVE_LINK);
            }
        }).getAttributes();
    }

    @Override
    public Set<? extends UsageContext> getUsages() {
        return usageContexts;
    }

    @Override
    public String getName() {
        return name;
    }
}
