/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.anno4j.extension.sparqlmm.functions;

import com.google.common.collect.ImmutableSet;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 22.10.15.
 */
public class FunctionBuilderTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Rule
    public TemporaryFolder serviceFolder= new TemporaryFolder();

    private Set<String> packages = ImmutableSet.of(
            "com.github.tkurz.sparqlmm.function.spatial.relation.directional"
    );

    private Log log = new SystemStreamLog();

    @Test
    public void testSimpleFunctionBuild() throws IOException {
        FunctionBuilder builder = new FunctionBuilder(packages, log);
        builder.writeClasses(folder.getRoot().toPath());
        builder.writeServices(serviceFolder.getRoot().toPath());

        File main_dir = Paths.get(folder.getRoot().getAbsolutePath(),"com/github/anno4j/extension/sparqlmm").toFile();

        Assert.assertTrue(main_dir.exists());
        Assert.assertTrue(main_dir.isDirectory());

        File main_file = Paths.get(folder.getRoot().getAbsolutePath(),"com/github/anno4j/extension/sparqlmm/SparqlMMFunctionTest.java").toFile();

        Assert.assertTrue(main_file.exists());
        Assert.assertFalse(main_file.isDirectory());

        File interface_file = Paths.get(folder.getRoot().getAbsolutePath(),"com/github/anno4j/extension/sparqlmm/SparqlMMTestFunction.java").toFile();

        Assert.assertTrue(interface_file.exists());
        Assert.assertFalse(interface_file.isDirectory());

        Assert.assertTrue(main_file.length() >= 4090);

        File function_dir = Paths.get(folder.getRoot().getAbsolutePath(),"com/github/anno4j/extension/sparqlmm/functions").toFile();

        Assert.assertTrue(function_dir.exists());
        Assert.assertTrue(function_dir.isDirectory());
        Assert.assertEquals(8, function_dir.listFiles().length);

        File expression_dir = Paths.get(folder.getRoot().getAbsolutePath(),"com/github/anno4j/extension/sparqlmm/expression").toFile();

        Assert.assertTrue(expression_dir.exists());
        Assert.assertTrue(expression_dir.isDirectory());
        Assert.assertEquals(8, expression_dir.listFiles().length);
    }

}
