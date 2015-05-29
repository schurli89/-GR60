package twitter;

import twitter4j.*;
import twitter4j.auth.AccessToken;

public class TwitterClient implements ITwitterClient {


    final String consumerKey = "GZ6tiy1XyB9W0P4xEJudQ";
    final String consumerSecret = "gaJDlW0vf7en46JwHAOkZsTHvtAiZ3QUd2mD1x26J9w";
    final String accessToken = "1366513208-MutXEbBMAVOwrbFmZtj1r4Ih2vcoHGHE2207002";
    final String accessTokenSecret = "RMPWOePlus3xtURWRVnv1TgrjTyK7Zk33evp4KKyA";

    public void publishUuid(TwitterStatusMessage message) throws Exception {
        Twitter twitter = TwitterFactory.getSingleton();
        AccessToken token = new AccessToken(accessToken,accessTokenSecret);

        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        twitter.setOAuthAccessToken(token);
        twitter.getAuthorization();

        Status status = null;
        try {
            status = twitter.updateStatus(message.getTwitterPublicationString());
        }catch(TwitterException e){
            System.out.println(e.getMessage());
        }

        System.out.println(status.getText());

    }
}
