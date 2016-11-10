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

import com.github.tkurz.sparqlmm.Constants;
import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import org.apache.marmotta.ldpath.parser.Configuration;
import org.apache.maven.plugin.logging.Log;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.codehaus.plexus.util.FileUtils;
import org.openrdf.query.algebra.evaluation.function.Function;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 22.10.15.
 */
public class FunctionBuilder {

    private static final String BASE_URI = Constants.NAMESPACE;

    private Set<Function> functions;

    private Log log;

    private VelocityEngine engine;

    public FunctionBuilder(Set<String> packages, Log log) {

        engine = new VelocityEngine();
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "file");
        properties.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        engine.init(properties);

        this.log = log;

        this.functions = new HashSet<Function>();

        ServiceLoader<Function> loader = ServiceLoader.load(Function.class);
        //select functions
        for(Function function : loader) {
            if(function.getURI().startsWith(BASE_URI)) {
                if(packages.contains(function.getClass().getPackage().getName())) {
                    this.functions.add(function);
                }
            }
        }
    }

    public void writeClasses(Path path) throws IOException {

        Path basicTarget = path.resolve("com/github/anno4j/extension/sparqlmm");
        Files.createDirectories(basicTarget);

        Path functionTarget = path.resolve("com/github/anno4j/extension/sparqlmm/functions");
        Files.createDirectories(functionTarget);

        Path expressionTarget = path.resolve("com/github/anno4j/extension/sparqlmm/expression");
        Files.createDirectories(expressionTarget);

        ArrayList<String> function_classes = new ArrayList<>();
        ArrayList<String> import_classes = new ArrayList<>();

        for(Function function : this.functions) {
            log.info(String.format("Write class files for sparql function %s", function.getURI()));

            Context context = new VelocityContext();

            context.put("name",function.getURI().substring(Constants.NAMESPACE.length()));
            context.put("uri",function.getURI());
            context.put("class_name", function.getClass().getSimpleName() + "Test");
            context.put("expression_name", "E_" + function.getClass().getSimpleName());

            writeFunctionClass(functionTarget, context);
            writeExpressionClass(expressionTarget, context);

            function_classes.add("\t"+function.getClass().getSimpleName()+"Test.class");
            import_classes.add("import com.github.anno4j.extension.sparqlmm.functions."+function.getClass().getSimpleName() + "Test;");
        }

        Context context = new VelocityContext();

        context.put("function_classes", Joiner.on(",\n").join(function_classes));
        context.put("import_classes", Joiner.on("\n").join(import_classes));

        final Path vFile = basicTarget.resolve("SparqlMMFunctionTest.java");

        try(PrintWriter writer = new PrintWriter(vFile.toFile())) {
            log.info("Write anno4j test class file");
            Template template = engine.getTemplate("SparqlMMFunctionTest.vm");
            template.merge(context, writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        final Path iFile = basicTarget.resolve("SparqlMMTestFunction.java");

        try(PrintWriter writer = new PrintWriter(iFile.toFile())) {
            log.info("Write anno4j test class interface");
            Template template = engine.getTemplate("SparqlMMTestFunction.vm");
            template.merge(context, writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeFunctionClass(Path path, Context context) {
        final Path vFile = path.resolve(context.get("class_name").toString()+".java");
        try(PrintWriter writer = new PrintWriter(vFile.toFile())) {
            Template template = engine.getTemplate("TestClassTemplate.vm");
            template.merge(context, writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeExpressionClass(Path path, Context context) {
        final Path vFile = path.resolve(context.get("expression_name").toString()+".java");
        try(PrintWriter writer = new PrintWriter(vFile.toFile())) {
            Template template = engine.getTemplate("ExpressionClassTemplate.vm");
            template.merge(context, writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void writeServices(Path path) throws IOException {
        Files.createDirectories(path);
        final File file = path.resolve("org.apache.marmotta.ldpath.api.functions.TestFunction").toFile();
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for(Function function : this.functions) {
                writer.write("com.github.anno4j.extension.sparqlmm.functions." + function.getClass().getSimpleName() + "Test");
                writer.newLine();
            }
        }
    }
}
