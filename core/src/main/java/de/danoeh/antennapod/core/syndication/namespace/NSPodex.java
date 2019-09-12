package de.danoeh.antennapod.core.syndication.namespace;

import android.util.Log;

import org.xml.sax.Attributes;

import java.util.ArrayList;

import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.PodexContent;
import de.danoeh.antennapod.core.feed.podex.PodexImage;
import de.danoeh.antennapod.core.syndication.handler.HandlerState;
import de.danoeh.antennapod.core.util.DateUtils;

/**
 * Process tags for experimental guardian podex feeds
 * Podex tags are similar to chapter markers but intended for rich media like images, web content
 * prompts for feedback, and donation buttons/links
 *
 * current attributes and tags are not finalised, mostly just set up here to mock up a front end
 */

public class NSPodex extends Namespace {
    private static final String TAG = "NSPodex";

    public static final String NSTAG = "podex";
    //todo point to actual namespace documentation
    public static final String NSURI = "https://www.theguardian.com/info/2019/jun/12/why-we-want-to-make-podcasts-better";

    //default properties
    private static final String START = "start";
    private static final String END = "end";
    private static final String TITLE = "title";

    private static final String IMAGE = "image";
    private static final String HREF = "href";
    private static final String IMAGE_CAPTION = "caption";
    private static final String IMAGE_NOTIFICATION = "notification";

    @Override
    public SyndElement handleElementStart(String localName, HandlerState state, Attributes attributes) {
        if(state.getCurrentItem() != null) {
            //we expect a href, otherwise there is nothing to display
            String imageUrl = attributes.getValue(HREF);

            if (IMAGE.equals(localName) && imageUrl != null) {
                state.getPodexContentStack().push(new PodexImage(imageUrl));
            }
        } else {
            //clear invalid podex tags
            state.getPodexContentStack().clear();
        }

        return new SyndElement(localName, this);
    }

    @Override
    public void handleElementEnd(String localName, HandlerState state) {
        FeedItem currentItem = state.getCurrentItem();
        if (currentItem != null &&
                !state.getPodexContentStack().empty()) {
            PodexContent currentPodexContent = state.getPodexContentStack().pop();

            //handle tags that contain data
            if (state.getContentBuf() != null) {
                if (START.equals(localName)) {
                    long start = parseTime(state.getContentBuf());
                    currentPodexContent.setStart(start);
                } else if (END.equals(localName)) {
                    long end = parseTime(state.getContentBuf());
                    currentPodexContent.setEnd(end);
                } else if (TITLE.equals(localName)) {
                    String title = parseTitle(state.getContentBuf());
                    currentPodexContent.setTitle(title);
                }

                //image specific fields
                if (currentPodexContent instanceof PodexImage) {
                    PodexImage currentImage = (PodexImage) currentPodexContent;

                    if (IMAGE_CAPTION.equals(localName)) {
                        String caption = parseCaption(state.getContentBuf());
                        currentImage.setCaption(caption);
                    } else if (IMAGE_NOTIFICATION.equals(localName) &&
                            state.getPodexContentStack().peek() instanceof PodexImage) {
                        String notification = parseCaption(state.getContentBuf());
                        currentImage.setNotification(notification);
                    }
                }

                //add podex data on closing tags
                if (IMAGE.equals(localName)) {
                    if (currentItem.getPodexContentList() == null) {
                        currentItem.setPodexContentList(new ArrayList<>());
                    }
                    currentItem.getPodexContentList().add(currentPodexContent);
                } else {
                    state.getPodexContentStack().push(currentPodexContent);
                }
            }
        } else {
            //clear invalid podex tags
            state.getPodexContentStack().clear();
        }


    }

    private String parseCaption(StringBuilder contentBuf) {
        return contentBuf.toString();
    }

    private String parseTitle(StringBuilder contentBuf) {
        return contentBuf.toString();
    }

    private long parseTime(StringBuilder contentBuf) {
        long time;
        try {
            time = DateUtils.parseTimeString(contentBuf.toString());
        } catch (NumberFormatException e) {
            time = 0;
            Log.e(TAG, "parseTime: invalid time string: " + e.getMessage());
        }
        return time;
    }
}
