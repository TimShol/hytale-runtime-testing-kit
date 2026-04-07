package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a performance benchmark to be measured by the HRTK runner.
 *
 * <p>Benchmark methods are executed in two phases: a warmup phase (not measured) followed
 * by a measurement phase. Results including average time, throughput, and percentiles are
 * captured via the injected {@code BenchmarkContext}. Use {@link #batchSize()} to indicate
 * how many logical operations each iteration performs for accurate throughput calculation.</p>
 *
 * <pre>{@code
 * @Benchmark(warmup = 100, iterations = 10000)
 * void benchSerialize(BenchmarkContext ctx) {
 *     MyCodec.encode(data);
 * }
 *
 * @Benchmark(warmup = 50, iterations = 5000, batchSize = 10)
 * void benchBatchInsert(BenchmarkContext ctx) {
 *     for (int i = 0; i < 10; i++) {
 *         store.insert(entities[i]);
 *     }
 * }
 * }</pre>
 *
 * @see HytaleTest
 * @see Timeout
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Benchmark {

    /**
     * Number of warmup iterations executed before measurement begins. Warmup
     * iterations allow the JVM to optimize hot paths and are not included in results.
     *
     * @return the warmup iteration count (defaults to 5)
     */
    int warmup() default 5;

    /**
     * Number of measured iterations. Each iteration is timed and included in the
     * final benchmark report.
     *
     * @return the measured iteration count (defaults to 100)
     */
    int iterations() default 100;

    /**
     * Number of logical operations performed per iteration. Used to calculate
     * per-operation throughput when an iteration processes a batch of items.
     *
     * @return the batch size (defaults to 1)
     */
    int batchSize() default 1;
}
