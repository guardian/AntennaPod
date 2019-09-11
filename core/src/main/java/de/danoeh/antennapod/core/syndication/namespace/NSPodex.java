package de.danoeh.antennapod.core.syndication.namespace;

import org.xml.sax.Attributes;

import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.syndication.handler.HandlerState;

/**
 * Process tags for experimental guardian podex feeds
 * Podex tags are similar to chapter markers but intended for rich media like images, web content
 * prompts for feedback, and donation buttons/links
 *
 * current attributes and tags are not finalised, mostly just set up here to mock up a front end
 */

public class NSPodex extends Namespace {

    private static final String TAG = "NSSimpleChapters";

    public static final String NSTAG = "podex";
    //todo point to an actual namespace documentation
    public static final String NSURI = "https://www.theguardian.com/info/2019/jun/12/why-we-want-to-make-podcasts-better";

    //default properties
    private static final String START = "start";
    private static final String END = "start";
    private static final String TITLE = "title";

    private static final String IMAGE = "image";
    private static final String HREF = "href";
    private static final String IMAGE_CAPTION = "caption";
    private static final String IMAGE_NOTIFICATION = "notification";

    @Override
    public SyndElement handleElementStart(String localName, HandlerState state, Attributes attributes) {
        FeedItem currentItem = state.getCurrentItem();
        if(currentItem != null) {
            if (IMAGE.equals(localName)) {

            }
        }

        return new SyndElement(localName, this);
    }

    @Override
    public void handleElementEnd(String localName, HandlerState state) {
        if (state.getContentBuf() == null) {
            return;
        }


    }
}
