package org.helioviewer.jhv;

import java.io.File;

/**
 * An enum containing all the directories mapped in a system independent way. If
 * a new directory is required, just add it here and it will be created at
 * startup.
 */
public enum JHVDirectory {
    /** The home directory. */
    HOME {
        private final String path = System.getProperty("user.home");

        @Override
        public String getPath() {
            return path + File.separator + "JHelioviewer-SWHV" + File.separator;
        }

        @Override
        public File getFile() {
            return new File(getPath());
        }
    },
    CACHE {
        @Override
        public String getPath() {
            return HOME.getPath() + "Cache" + File.separator;
        }

        @Override
        public File getFile() {
            return new File(getPath());
        }
    },
    /** The JHV state directory. */
    STATES {
        @Override
        public String getPath() {
            return HOME.getPath() + "States" + File.separator;
        }

        @Override
        public File getFile() {
            return new File(getPath());
        }
    },
    /** The exports directory (movies, screenshots, meta data). */
    EXPORTS {
        @Override
        public String getPath() {
            return HOME.getPath() + "Exports" + File.separator;
        }

        @Override
        public File getFile() {
            return new File(getPath());
        }
    },
    /** The log directory. */
    LOGS {
        @Override
        public String getPath() {
            return HOME.getPath() + "Logs" + File.separator;
        }

        @Override
        public File getFile() {
            return new File(getPath());
        }
    },
    /** The log settings directory. */
    SETTINGS {
        @Override
        public String getPath() {
            return HOME.getPath() + "Settings" + File.separator;
        }

        @Override
        public File getFile() {
            return new File(getPath());
        }
    },
    /** The remote files directory. */
    REMOTEFILES {
        @Override
        public String getPath() {
            return HOME.getPath() + "Downloads" + File.separator;
        }

        @Override
        public File getFile() {
            return new File(getPath());
        }
    },
    /** The plugins directory. */
    PLUGINS {
        @Override
        public String getPath() {
            return HOME.getPath() + "Plugins" + File.separator;
        }

        @Override
        public File getFile() {
            return new File(getPath());
        }
    },
    /** Directory of automatically loaded GIMP gradient files. */
    COLOR_PLUGINS {
        @Override
        public String getPath() {
            return HOME.getPath() + "Colortables" + File.separator;
        }

        @Override
        public File getFile() {
            return new File(getPath());
        }
    },
    TEMP {
        @Override
        public String getPath() {
            return HOME.getPath() + "Temp" + File.separator;
        }

        @Override
        public File getFile() {
            return new File(getPath());
        }
    },
    EVENTS {
        @Override
        public String getPath() {
            return HOME.getPath() + "Events" + File.separator;
        }

        @Override
        public File getFile() {
            return new File(getPath());
        }
    };

    /** A String representation of the path of the directory. */
    public abstract String getPath();

    /** A File representation of the path of the directory. */
    public abstract File getFile();

}
