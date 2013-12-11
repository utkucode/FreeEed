/*
 *
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.services;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.freeeed.main.ParameterProcessing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton for the desktop application, passing parameters through the properties file. Note that for non-present keys
 * we return an empty string rather than a null. This agrees with "Avoid nulls, use Null Objects" design pattern:
 * https://code.google.com/p/guava-libraries/wiki/UsingAndAvoidingNullExplained.
 *
 * @author mark
 */
public class Settings extends Properties {
    private static Logger logger = LoggerFactory.getLogger(Settings.class);
    
    private static Settings settings = new Settings();
    private final static int MAX_RECENT_PROJECTS = 8;
    private static String settingsFile;

    static public Settings getSettings() {
        return settings;
    }

    /**
     * Return empty string instead of null, see above.
     *
     * @param key key to extract.
     * @return value corresponding to the key.
     */
    @Override
    public String getProperty(String key) {
        if (this.containsKey(key)) {
            return super.getProperty(key);
        } else {
            return "";
        }
    }

    static public void setSettings(Settings aSettings) {
        settings = aSettings;
    }

    public String getLastProjectCode() {
        return getProperty(ParameterProcessing.LAST_PROJECT_CODE);
    }

    public void setLastProjectCode(String projectCode) {
        setProperty(ParameterProcessing.LAST_PROJECT_CODE, projectCode);
    }

    /**
     * Load settings for the program to operate.
     *
     * @throws IllegalStateException in case of any problem with the settings file: not present, mis-configured, etc.
     */
    public static void load() throws IllegalStateException {
        String settingsToUse = settingsFile != null ? settingsFile : ParameterProcessing.DEFAULT_SETTINGS;
        if (!new File(settingsToUse).exists()) {
            throw new IllegalStateException("Missing settings : " + settingsToUse);
        }
        try {
            settings.load(new FileReader(settingsToUse));
            if (settings.keySet().isEmpty()) {
                throw new IllegalStateException("Invalid settings : " + settingsToUse);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Problem with settins ", e);
        }
    }

    public void save() {
        try {
            String settingsToUse = settingsFile != null ? settingsFile : ParameterProcessing.DEFAULT_SETTINGS;
            settings.store(new FileWriter(settingsToUse), ParameterProcessing.APP_NAME + " Settings");
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public String getCurrentDir() {
        return getProperty(ParameterProcessing.CURRENT_DIR);
    }

    public void setCurrentDir(String filePath) {
        if (filePath == null) {
            filePath = ".";
        }
        setProperty(ParameterProcessing.CURRENT_DIR, filePath);
    }

    /**
     * Get recent projects. Note that this methods updates 'settings.properties'.
     *
     * @return
     */
    // TODO recent projects require redesign. Saving them in settings.properties may be  a bad idea
    public List<Project> getRecentProjects() {
        ArrayList<Project> recentProjects = new ArrayList<>();
        String recentProjectsStr = getProperty(ParameterProcessing.RECENT_PROJECTS);
        if (recentProjectsStr == null) {
            return recentProjects;
        }
        String[] projects = recentProjectsStr.split(",");
        for (String projectPath : projects) {
            projectPath = projectPath.trim();
            if (new File(projectPath).exists()) {
                try {
                    Project project = new Project().loadFromFile(new File(projectPath));
                    if (!recentProjects.contains(project)) {
                        recentProjects.add(project);
                    }
                } catch (Exception e) {
                    History.appendToHistory("Warning: project " + projectPath
                            + " was not found");
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        for (Project project : recentProjects) {
            builder.append(project.getProjectFilePath()).append(",");
        }
        if (recentProjects.size() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        setProperty(ParameterProcessing.RECENT_PROJECTS, builder.toString());
        save();
        return recentProjects;
    }

    /**
     * Add recent project to the store.
     *
     * @param recentProjectPath
     */
    // TODO recent projects require redesign. Saving them in settings.properties may be  a bad idea
    public void addRecentProject(String recentProjectPath) {
        List<Project> projects = getRecentProjects();
        for (Properties project : projects) {
            if (recentProjectPath.equalsIgnoreCase(
                    project.getProperty(ParameterProcessing.PROJECT_FILE_PATH))) {
                return;
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append(recentProjectPath).append(",");
        int nProj = 1;
        for (Properties project : projects) {
            ++nProj;
            if (nProj > MAX_RECENT_PROJECTS) {
                break;
            }
            builder.append(project.getProperty(ParameterProcessing.PROJECT_FILE_PATH)).append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        setProperty(ParameterProcessing.RECENT_PROJECTS, builder.toString());
        save();
    }

    public boolean isUseJpst() {
        // TODO switch to jreadpst (?)
        // and when you do - distribute it with -libjars
        return getProperty(ParameterProcessing.USE_JPST) != null;
    }

    public boolean isLoadBalance() {
        return getProperty(ParameterProcessing.LOAD_BALANCE) != null;
    }

    public String getEnv() {
        String env = getProperty(ParameterProcessing.PROCESS_WHERE);
        if (env == null) {
            env = "local";
        }
        return env;
    }

    public void setEnv(String env) {
        setProperty(ParameterProcessing.PROCESS_WHERE, env);
    }

    public String getAccessKeyId() {
        return getProperty(ParameterProcessing.ACCESS_KEY_ID);
    }

    public void setAccessKeyId(String accessKeyId) {
        setProperty(ParameterProcessing.ACCESS_KEY_ID, accessKeyId);
    }

    public String getSecretAccessKey() {
        return getProperty(ParameterProcessing.SECRET_ACCESS_KEY);
    }

    public void setSecretAccessKey(String secretAccessKey) {
        setProperty(ParameterProcessing.SECRET_ACCESS_KEY, secretAccessKey);
    }

    public String getProjectBucket() {
        return getProperty(ParameterProcessing.PROJECT_BUCKET);
    }

    public void setProjectBucket(String projectBucket) {
        setProperty(ParameterProcessing.PROJECT_BUCKET, projectBucket);
    }

    public String getSecurityGroup() {
        return getProperty(ParameterProcessing.SECURITY_GROUP);
    }

    public void setSecurityGroup(String securityGroup) {
        setProperty(ParameterProcessing.SECURITY_GROUP, securityGroup);
    }

    public String getKeyPair() {
        return getProperty(ParameterProcessing.KEY_PAIR);
    }

    public void setKeyPair(String keyPair) {
        setProperty(ParameterProcessing.KEY_PAIR, keyPair);
    }

    public int getClusterSize() {
        try {
            return Integer.parseInt(getProperty(ParameterProcessing.CLUSTER_SIZE));
        } catch (Exception e) {
            logger.warn("Cluster size not found, setting to {}, reason: {}", 1, e.getMessage());
            return 1;
        }
    }

    public void setClusterSize(int clusterSize) {
        setProperty(ParameterProcessing.CLUSTER_SIZE, Integer.toString(clusterSize));
    }

    public int getNumReduce() {
        try {
            return Integer.parseInt(getProperty(ParameterProcessing.NUM_REDUCE));
        } catch (Exception e) {
            return 1;
        }
    }

    public void setNumReduce(int numReduce) {
        setProperty(ParameterProcessing.NUM_REDUCE, Integer.toString(numReduce));
    }

    public String getClusterAmi() {
        return getProperty(ParameterProcessing.CLUSTER_AMI);
    }

    public String getInstanceType() {
        String instanceType = getProperty(ParameterProcessing.INSTANCE_TYPE);
        if (instanceType == null) {
            instanceType = "c1.medium";
            setProperty(ParameterProcessing.INSTANCE_TYPE, instanceType);
        }
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        setProperty(ParameterProcessing.INSTANCE_TYPE, instanceType);
    }

    public String getAvailabilityZone() {
        String zone = getProperty(ParameterProcessing.AVAILABILITY_ZONE);
        if (zone == null) {
            zone = "us-east-1a";
            setProperty(ParameterProcessing.AVAILABILITY_ZONE, zone);
        }
        return zone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        setProperty(ParameterProcessing.AVAILABILITY_ZONE, availabilityZone);
    }

    public int getClusterTimeoutMin() {
        try {
            return Integer.parseInt(getProperty(ParameterProcessing.CLUSTER_TIMEOUT));
        } catch (Exception e) {
            History.appendToHistory("WARN: time out incorrect, setting to 5 min");
            return 5;
        }
    }

    public void setClusterTimeoutMin(int clusterTimeoutMin) {
        setProperty(ParameterProcessing.CLUSTER_TIMEOUT, Integer.toString(clusterTimeoutMin));
    }

    public String getManualPage() {
        System.out.println("Manual key: " + ParameterProcessing.MANUAL_PAGE);
        return getProperty(ParameterProcessing.MANUAL_PAGE);
    }

    @Override
    /**
     * Custom string serialization is chosen for this object, because of its limited applicability.
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Object[] keys = keySet().toArray();
        Arrays.sort(keys);
        for (Object key : keys) {
            builder.append(key.toString()).append("=").
                    append(get(key).toString()).append("\n");
        }
        return builder.toString();
    }

    /**
     * Loads the settings from a string. Used for passing the settings in the config object. Custom string serialization
     * is chosen for this object, because of its limited applicability.
     *
     * @param str Settings to be loaded.
     * @return validated Settings object.
     * @throws IllegalStateException on any malformed element.
     */
    public static Settings loadFromString(String str) throws IllegalStateException {
        Settings s = new Settings();
        if (str == null) {
            return s;
        }
        String[] lines = str.split("\n");
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }
            int equal = line.indexOf("=");
            if (equal < 0 || equal == line.length() - 1) {
                throw new IllegalStateException("Error parsing line " + line);
            }
            String key = line.substring(0, equal);
            String value = line.substring(equal + 1);
            s.put(key.trim(), value.trim());
        }
        return s;
    }

    @Deprecated
    public String getDownloadLink() {
        return getProperty(ParameterProcessing.DOWNLOAD_LINK);
    }

    public int getItemsPerMapper() {
        try {
            return Integer.parseInt(getProperty(ParameterProcessing.ITEMS_PER_MAPPER));
        } catch (Exception e) {
            History.appendToHistory("WARN: " + e.getMessage());
            return 5000;
        }
    }

    public long getBytesPerMapper() {
        try {
            return Long.parseLong(getProperty(ParameterProcessing.BYTES_PER_MAPPER));
        } catch (Exception e) {
            History.appendToHistory("WARN: " + e.getMessage());
            return 250000000;
        }
    }

    public boolean isHadoopDebug() {
        return getProperty(ParameterProcessing.HADOOP_DEBUG) != null;
    }

    /**
     *
     * Set the solr endpoint.
     *
     * @param endpoint
     */
    public void setSolrEndpoint(String endpoint) {
        setProperty(ParameterProcessing.SOLR_ENDPOINT, endpoint);
    }

    /**
     *
     * Return the configured Solr endpoint.
     *
     * @return
     */
    public String getSolrEndpoint() {
        String solrEndpoint = getProperty(ParameterProcessing.SOLR_ENDPOINT);
        return (solrEndpoint != null && solrEndpoint.trim().length() > 0) ? solrEndpoint : "http://localhost:8983";
    }

    /**
     * Check whether the application should skip amazon instance creation.
     *
     * @return
     */
    public boolean skipInstanceCreation() {
        String value = getProperty(ParameterProcessing.SKIP_INSTANCE_CREATION);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return false;
    }

    public void setSkipInstanceCreation(boolean b) {
        setProperty(ParameterProcessing.SKIP_INSTANCE_CREATION, "" + b);
    }

    /**
     * @return the settingsFile
     */
    public String getSettingsFile() {
        return settingsFile;
    }

    /**
     * @param settingsFile the settingsFile to set
     */
    public static void setSettingsFile(String aSettingsFile) {
        settingsFile = aSettingsFile;
    }

    /**
     * @return the trainingClusters
     */
    @Deprecated
    public boolean isTrainingClusters() {
        String value = getProperty(ParameterProcessing.MULTIPLE_CLUSTERS);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return false;
    }

    /**
     * @param trainingClusters the trainingClusters to set
     */
    public void setTrainingClusters(boolean b) {
        setProperty(ParameterProcessing.MULTIPLE_CLUSTERS, "" + b);
    }

    /**
     * @return the numberTrainingClusters
     */
    @Deprecated
    public int getNumberTrainingClusters() {
        try {
            return Integer.parseInt(getProperty(ParameterProcessing.NUMBER_CLUSTERS));
        } catch (Exception e) {
            History.appendToHistory("WARN: " + e.getMessage());
            return 1;
        }
    }

    /**
     * @param numberTrainingClusters the numberTrainingClusters to set
     */
    public void setNumberClusters(int numberClusters) {
        setProperty(ParameterProcessing.NUMBER_CLUSTERS, Integer.toString(numberClusters));
    }

    /**
     *
     * Set external processing machine endpoint.
     *
     * @param endpoint
     */
    public void setExternalProssingEndpoint(String endpoint) {
        setProperty(ParameterProcessing.EXTERNAL_PROCESSING_MACHINE_ENDPOINT, endpoint);
    }

    /**
     *
     * Returns the external processing machine endpoint.
     *
     * @return
     */
    public String getExternalProssingEndpoint() {
        return getProperty(ParameterProcessing.EXTERNAL_PROCESSING_MACHINE_ENDPOINT);
    }

    public int getSolrCloudReplicaCount() {
        int replicaCount = 0;
        try {
            replicaCount = Integer.parseInt(getProperty(ParameterProcessing.SOLRCLOUD_REPLICA_COUNT));
        } catch (Exception e) {
            History.appendToHistory("getSolrCloudReplicaCount WARN: " + e.getMessage());
        }
        if (replicaCount < 1) {
            replicaCount = 1;
            setSolrCloudReplicaCount(1);
        }
        return replicaCount;
    }

    public void setSolrCloudReplicaCount(int replicaCount) {
        setProperty(ParameterProcessing.SOLRCLOUD_REPLICA_COUNT, Integer.toString(replicaCount));
    }

    /**
     * TODO - discuss the purpose of this with Austin
     *
     * @return
     */
    public int getSolrCloudShardCount() {
        int shardCount = 0;
        try {
            shardCount = Integer.parseInt(getProperty(ParameterProcessing.SOLRCLOUD_SHARD_COUNT));
        } catch (Exception e) {
            History.appendToHistory("getSolrCloudShardCount WARN: " + e.getMessage());

        }
        if (shardCount < 1) {
            shardCount = 1;
            setSolrCloudShardCount(1);
        }
        return shardCount;

    }

    public void setSolrCloudShardCount(int shardCount) {
        setProperty(ParameterProcessing.SOLRCLOUD_SHARD_COUNT, Integer.toString(shardCount));
    }
}