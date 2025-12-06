package io.bugreaper.modules.db.jdbc.interfaces;


public interface Setters<T extends Setters<T>> {

    /**
     * Configure await in asserts with await
     *
     * @param awaitMs ms await
     * @return self()
     * @throws IllegalArgumentException on invalid setup
     */
    T withAwaitMs(int awaitMs);

    /**
     * Configure directory in resources for tables templates
     *
     * @param templatePath path in resources "my_dir/sub_dir/"
     * @return self()
     * @throws IllegalArgumentException on invalid setup
     */
    T setTemplatesDirectory(String templatePath);

}