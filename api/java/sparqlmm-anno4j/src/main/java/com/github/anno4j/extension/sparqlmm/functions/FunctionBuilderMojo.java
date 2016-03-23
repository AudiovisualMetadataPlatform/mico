package com.github.anno4j.extension.sparqlmm.functions;

import com.google.common.collect.ImmutableSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.openrdf.query.algebra.evaluation.function.Function;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 22.10.15.
 */
@Mojo(name = "generate",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true)
public class FunctionBuilderMojo extends AbstractMojo {

    @Parameter(property = "output", defaultValue = "${project.build.directory}/generated-sources/sparqlmm")
    private File outputDirectory;

    @Parameter(property = "services", defaultValue = "${project.build.directory}/classes/META-INF/services")
    private File services;

    @Parameter
    private Set<String> packages = ImmutableSet.of(
            "com.github.tkurz.sparqlmm.function.spatial.relation.directional"
    );

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        FunctionBuilder builder = new FunctionBuilder(packages,getLog());

        //add sources
        Path path = outputDirectory.toPath();

        try {
            builder.writeClasses(path);

            if (project != null) {
                project.addCompileSourceRoot(path.toString());
            }

        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(),e);
        }

        //add services
        Path servicePath = services.toPath();

        try {
            builder.writeServices(servicePath);
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(),e);
        }
    }
}
