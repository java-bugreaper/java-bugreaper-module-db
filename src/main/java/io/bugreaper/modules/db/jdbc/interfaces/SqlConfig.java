package io.bugreaper.modules.db.jdbc.interfaces;

/**
 * Defines configuration accessors for SQL execution components.
 *
 * <p>
 * This interface contains only configuration methods.
 * </p>
 */
public interface SqlConfig<T extends SqlConfig<T>> {

    /**
     * Configure await in asserts with await
     *
     * @param awaitMs ms await
     * @return this instance for method chaining
     * @throws IllegalArgumentException on invalid setup
     */
    T withAwaitMs(int awaitMs);

    /**
     * Configure directory in resources for tables templates
     *
     * @param templatePath path in resources "my_dir/sub_dir/"
     * @return this instance for method chaining
     * @throws IllegalArgumentException on invalid setup
     */
    T setTemplatesDirectory(String templatePath);

    /**
     * Returns a human-readable summary of the current SQL configuration.
     * <p>
     * The returned string typically includes all configuration values such as
     * query text, parameters, execution options, timeouts, or any other settings
     * managed by the implementing class.
     * </p>
     *
     * <p>
     * As a side effect, this method logs the generated summary at the INFO level.
     * This can be useful for debugging or tracing configuration usage during
     * test execution.
     * </p>
     *
     * @return a string containing a formatted summary of all configuration values
     */
    String getConfigSummary();
}