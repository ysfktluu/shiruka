/*
 * MIT License
 *
 * Copyright (c) 2020 Shiru ka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.github.shiruka.shiruka.world.generators;

import io.github.shiruka.api.base.FeatureGenerator;
import io.github.shiruka.api.world.World;
import io.github.shiruka.api.world.generators.GeneratorContainer;
import io.github.shiruka.api.world.generators.GeneratorProvider;
import io.github.shiruka.api.world.generators.PropGenerator;
import io.github.shiruka.api.world.generators.TerrainGenerator;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * a generator provider that provides the proper generators for a flat world level type.
 */
public final class FlatGeneratorProvider implements GeneratorProvider {

  /**
   * the singleton instance of this provider.
   */
  public static final FlatGeneratorProvider INSTANCE = new FlatGeneratorProvider();

  /**
   * ctor.
   */
  private FlatGeneratorProvider() {
  }

  @NotNull
  @Override
  public Set<FeatureGenerator> getFeatureGenerators(@NotNull final World world) {
    return Collections.emptySet();
  }

  @NotNull
  @Override
  public GeneratorContainer getGenerationContainer() {
    return GeneratorContainer.ARBITRARY;
  }

  @NotNull
  @Override
  public Set<PropGenerator> getPropGenerators(@NotNull final World world) {
    return Collections.emptySet();
  }

  @NotNull
  @Override
  public TerrainGenerator getTerrainGenerator(@NotNull final World world) {
    return FlatTerrainGenerator.INSTANCE;
  }
}
