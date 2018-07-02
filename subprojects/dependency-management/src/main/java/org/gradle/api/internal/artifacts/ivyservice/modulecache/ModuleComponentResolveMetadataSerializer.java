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

package org.gradle.api.internal.artifacts.ivyservice.modulecache;

import org.gradle.api.internal.artifacts.ImmutableModuleIdentifierFactory;
import org.gradle.internal.component.external.model.AbstractLazyModuleComponentResolveMetadata;
import org.gradle.internal.component.external.model.AbstractRealisedModuleComponentResolveMetadata;
import org.gradle.internal.component.external.model.ivy.DefaultIvyModuleResolveMetadata;
import org.gradle.internal.component.external.model.ivy.RealisedIvyModuleResolveMetadataSerializationHelper;
import org.gradle.internal.component.external.model.maven.DefaultMavenModuleResolveMetadata;
import org.gradle.internal.component.external.model.ModuleComponentResolveMetadata;
import org.gradle.internal.component.external.model.ivy.RealisedIvyModuleResolveMetadata;
import org.gradle.internal.component.external.model.maven.RealisedMavenModuleResolveMetadata;
import org.gradle.internal.component.external.model.maven.RealisedMavenModuleResolveMetadataSerializationHelper;
import org.gradle.internal.resolve.caching.FullAttributeContainerSerializer;
import org.gradle.internal.serialize.AbstractSerializer;
import org.gradle.internal.serialize.Decoder;
import org.gradle.internal.serialize.Encoder;

import java.io.EOFException;

public class ModuleComponentResolveMetadataSerializer extends AbstractSerializer<ModuleComponentResolveMetadata> {

    private final RealisedIvyModuleResolveMetadataSerializationHelper ivySerializationHelper;
    private final RealisedMavenModuleResolveMetadataSerializationHelper mavenSerializationHelper;
    private final ModuleMetadataSerializer delegate;
    private final ImmutableModuleIdentifierFactory moduleIdentifierFactory;

    public ModuleComponentResolveMetadataSerializer(ModuleMetadataSerializer delegate, FullAttributeContainerSerializer attributeContainerSerializer, ImmutableModuleIdentifierFactory moduleIdentifierFactory) {
        this.delegate = delegate;
        this.moduleIdentifierFactory = moduleIdentifierFactory;
        ivySerializationHelper = new RealisedIvyModuleResolveMetadataSerializationHelper(attributeContainerSerializer, moduleIdentifierFactory);
        mavenSerializationHelper = new RealisedMavenModuleResolveMetadataSerializationHelper(attributeContainerSerializer, moduleIdentifierFactory);
    }

    @Override
    public ModuleComponentResolveMetadata read(Decoder decoder) throws EOFException, Exception {

        AbstractLazyModuleComponentResolveMetadata resolveMetadata = (AbstractLazyModuleComponentResolveMetadata) delegate.read(decoder, moduleIdentifierFactory).asImmutable();

        if (resolveMetadata instanceof DefaultIvyModuleResolveMetadata) {
            return ivySerializationHelper.readMetadata(decoder, (DefaultIvyModuleResolveMetadata) resolveMetadata);
        } else if (resolveMetadata instanceof DefaultMavenModuleResolveMetadata) {
            return mavenSerializationHelper.readMetadata(decoder, (DefaultMavenModuleResolveMetadata) resolveMetadata);
        } else {
            throw new IllegalStateException("Unknown resolved metadata type: " + resolveMetadata.getClass());
        }
    }

    @Override
    public void write(Encoder encoder, ModuleComponentResolveMetadata value) throws Exception {
        AbstractRealisedModuleComponentResolveMetadata transformed = transformToRealisedForSerialization(value);
        delegate.write(encoder, transformed);
        if (transformed instanceof RealisedIvyModuleResolveMetadata) {
            ivySerializationHelper.writeRealisedVariantsData(encoder, transformed);
            ivySerializationHelper.writeRealisedConfigurationsData(encoder, transformed);
        } else if (transformed instanceof RealisedMavenModuleResolveMetadata) {
            mavenSerializationHelper.writeRealisedVariantsData(encoder, transformed);
            mavenSerializationHelper.writeRealisedConfigurationsData(encoder, transformed);
        } else {
            throw new IllegalStateException("Unexpected realised module component resolve metadata type: " + transformed.getClass());
        }
    }

    private AbstractRealisedModuleComponentResolveMetadata transformToRealisedForSerialization(ModuleComponentResolveMetadata metadata) {
        if (metadata instanceof AbstractRealisedModuleComponentResolveMetadata) {
            return (AbstractRealisedModuleComponentResolveMetadata) metadata;
        } else if (metadata instanceof DefaultIvyModuleResolveMetadata) {
            return RealisedIvyModuleResolveMetadata.transform((DefaultIvyModuleResolveMetadata) metadata);
        } else if (metadata instanceof DefaultMavenModuleResolveMetadata) {
            return RealisedMavenModuleResolveMetadata.transform((DefaultMavenModuleResolveMetadata) metadata);
        }
        throw new IllegalStateException("The type of metadata received is not supported - " + metadata.getClass().getName());
    }
}
