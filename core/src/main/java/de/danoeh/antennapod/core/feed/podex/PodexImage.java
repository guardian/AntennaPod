package de.danoeh.antennapod.core.feed.podex;

import de.danoeh.antennapod.core.feed.PodexContent;

/**
 * Image to be displayed along with text alongside playback
 */
public class PodexImage extends PodexContent {
    String href;
    String caption;
    String notification;

    public PodexImage(long start, long end, String title, String href, String caption, String notification) {
        super(start, end, title);
        this.href = href;
        this.caption = caption;
        this.notification = notification;
    }

    @Override
    public String getHumanReadableIdentifier() {
        return href != null ? href : String.valueOf(getStart());
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }
}
