package testcontainers;

import java.util.Objects;

public class DockerSetup {

    private static final String CI = System.getenv("CI");
    public static final String CI_HOST = getHost();

    private static String getHost() {
        if (Objects.equals(CI, "true")) {
            return "docker";
        } else {
            return "localhost";
        }
    }


    static void printVersion(String image){
        System.out.printf("""
                \u001B[32m
                ============================================
                >>> TESTS RUNNING ON ON DOCKER IMAGE: %s <<<
                ============================================
                \u001B[0m
                %n""", image);

    }

   static String resolveDockerImage(String stableVersion, String latestVersion) {
        String dockerVersion = System.getProperty("dockerTestVersion");

        if ("latest".equalsIgnoreCase(dockerVersion)) {
            return latestVersion;
        }

        return stableVersion;
    }
}
