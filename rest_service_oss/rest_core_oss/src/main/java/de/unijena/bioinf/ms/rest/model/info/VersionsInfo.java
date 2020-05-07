package de.unijena.bioinf.ms.rest.model.info;

import de.unijena.bioinf.ms.properties.PropertyManager;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.sql.Timestamp;
import java.util.*;

public class VersionsInfo {

    /**
     * this version number increments if custom databases change and are not longer valid. This is for example the case when the fingerprint computation changes.
     */
    public static final int CUSTOM_DATABASE_SCHEMA = PropertyManager.getInteger("de.unijena.bioinf.fingerid.customdb.version",0);
    public String databaseDate;
    public final DefaultArtifactVersion siriusGuiVersion;
    //Expiry Dates
    public final boolean expired;
    public final Timestamp acceptJobs;
    public final Timestamp finishJobs;

    //News
    protected List<News> newsList;

    public VersionsInfo(String siriusGuiVersion, String databaseDate, boolean expired) {
        this(siriusGuiVersion, databaseDate, expired, null, null, Collections.<News>emptyList());
    }

    public VersionsInfo(String siriusGuiVersion, String databaseDate, boolean expired, Timestamp acceptJobs, Timestamp finishJobs) {
        this(siriusGuiVersion, databaseDate, expired, acceptJobs, finishJobs, Collections.<News>emptyList());
    }

    public VersionsInfo(String siriusGuiVersion, String databaseDate, boolean expired, Timestamp acceptJobs, Timestamp finishJobs, List<News> newsList) {
        this.siriusGuiVersion = new DefaultArtifactVersion(siriusGuiVersion);
        this.databaseDate = databaseDate;
        this.expired = expired;
        this.acceptJobs = acceptJobs;
        this.finishJobs = finishJobs;
        this.newsList = filterNews(newsList);
    }

    /**
     * filter already seen news
     *
     * @param newsList
     * @return
     */
    private List<News> filterNews(List<News> newsList) {
        List<News> filteredNews = new ArrayList<>();
        final String property = News.PROPERTY_KEY;
        final String propertyValue = PropertyManager.getProperty(property, null, "");
        Set<String> knownNews = new HashSet<>(Arrays.asList(propertyValue.split(",")));
        for (News news : newsList) {
            if (!knownNews.contains(news.getId())) filteredNews.add(news);
        }
        return filteredNews;
    }

    public boolean outdated() {
        return expired() && !finishJobs();
    }

    public boolean expired() {
        return expired;
    }

    public boolean acceptJobs() {
        if (acceptJobs == null) return false;
        return acceptJobs.getTime() >= System.currentTimeMillis();
    }

    public boolean finishJobs() {
        if (finishJobs == null) return false;
        return finishJobs.getTime() >= System.currentTimeMillis();
    }


    public boolean hasNews() {
        return (newsList != null && newsList.size() > 0);
    }

    public List<News> getNews() {
        return newsList;
    }

    public boolean databaseOutdated(String s) {
        return databaseDate.compareTo(s) > 0;
    }

    @Override
    public String toString() {
        return "Sirius-gui-version: " + siriusGuiVersion +
                ", Database-date: " + databaseDate +
                ", isExpired: " + expired +
                ", acceptJobs: " + (acceptJobs != null ? acceptJobs.toString() : "NULL") +
                ", finishJobs: " + (finishJobs != null ? finishJobs.toString() : "NULL");
    }

    public static boolean areMajorEqual(DefaultArtifactVersion v1, DefaultArtifactVersion v2) {
        return v1.getMajorVersion() == v2.getMajorVersion();
    }

    public static boolean areMinorEqual(DefaultArtifactVersion v1, DefaultArtifactVersion v2) {
        return areMajorEqual(v1, v2) && v1.getMinorVersion() == v2.getMinorVersion();
    }

    public static boolean areIncrementalEqual(DefaultArtifactVersion v1, DefaultArtifactVersion v2) {
        return areMinorEqual(v1, v2) && v1.getIncrementalVersion() == v2.getIncrementalVersion();
    }


}
