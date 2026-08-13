package net.bugreaper.modules.db.jdbc.interfaces;

/**
 * Defines configuration accessors for SQL execution components.
 *
 * <p>
 * This interface contains only configuration methods.
 * </p>
 */
public interface SqlConfig<T extends SqlConfig<T>> {

    /**
     * Configures the global await timeout for assertions that use await.
     *
     * @param awaitMs await timeout in milliseconds
     * @return this instance for method chaining
     * @throws IllegalArgumentException if the provided timeout is invalid or less than 200 milliseconds
     */
    T setAwaitMs(int awaitMs);

    /**
     * Sets a custom await timeout for the next assertion.
     *
     * <p>After the operation is completed, the timeout is reset to the global value
     * configured by {@link #setAwaitMs(int)}.</p>
     *
     * @param awaitMs await timeout in milliseconds
     * @return this instance for method chaining
     * @throws IllegalArgumentException if the timeout is invalid
     */
    T withAwaitMs(int awaitMs);


    /**
     * Configures the directory in resources containing tables templates.
     *
     * @param templatePath path to the templates directory in resources (example: {@code "my_dir/sub_dir/"})
     * @return this instance for method chaining
     * @throws IllegalArgumentException if the provided path is invalid
     */
    T setTemplatesDirectory(String templatePath);

    /**
     * Enables or disables enhanced reporting.
     *
     * <p>When enabled, full table data is attached to Allure,
     * and actual column values are included in data assertion exceptions.</p>
     *
     * @param enableEnchantedReport {@code true} to enable, {@code false} to disable
     * @return this instance for method chaining
     */
    T setEnchantedReport(boolean enableEnchantedReport);

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