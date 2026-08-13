package org.labs.genesis.config.docker;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DockerConf {

    private List<Volume> volumes;
    private List<Environment> environments;

    @Getter
    @Setter
    public static class Volume {
        private String name;
        private String dir;

        @Override
        public String toString() {
            return "Volume{" +
                    "name='" + name + '\'' +
                    ", dir='" + dir + '\'' +
                    '}';
        }
    }

    @Getter
    @Setter
    public static class Environment {
        private String name;
        private String separator;
        private String value;

        @Override
        public String toString() {
            return "Environment{" +
                    "name='" + name + '\'' +
                    ", separator='" + separator + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "DockerConfiguration{" +
                "volumes=" + volumes +
                ", environments=" + environments +
                '}';
    }
}