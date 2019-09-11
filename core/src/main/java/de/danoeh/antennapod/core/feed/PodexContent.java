package de.danoeh.antennapod.core.feed;

import static java.lang.Long.MAX_VALUE;

/**
 * Podex content, shown between the times specified by start and end
 */
public abstract class PodexContent extends FeedComponent {
    //start and end in ms
    long start;
    long end;

    String title;

    PodexContent() {
        start = 0;
        end = MAX_VALUE;
        title = "";
    }

    public PodexContent(long start, long end, String title) {
        this.start = start;
        this.end = end;
        this.title = title;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
