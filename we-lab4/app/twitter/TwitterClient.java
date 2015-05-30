package twitter;

import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterClient implements ITwitterClient {


    final String CONSUMER_KEY = "GZ6tiy1XyB9W0P4xEJudQ";
    final String CONSUMER_SECRET = "gaJDlW0vf7en46JwHAOkZsTHvtAiZ3QUd2mD1x26J9w";
    final String ACCESSTOKEN = "1366513208-MutXEbBMAVOwrbFmZtj1r4Ih2vcoHGHE2207002";
    final String ACCESSTOKEN_SECRET = "RMPWOePlus3xtURWRVnv1TgrjTyK7Zk33evp4KKyA";

    @Override
    public void publishUuid(TwitterStatusMessage message) throws Exception {
        AccessToken token = new AccessToken(ACCESSTOKEN, ACCESSTOKEN_SECRET);
        Status status = null;

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(CONSUMER_KEY);
        builder.setOAuthConsumerSecret(CONSUMER_SECRET);
        builder.setOAuthAccessToken(ACCESSTOKEN);
        builder.setOAuthAccessTokenSecret(ACCESSTOKEN_SECRET);
        Configuration configuration = builder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        Twitter twitter = factory.getInstance();

        try {
            status = twitter.updateStatus(message.getTwitterPublicationString());
        }catch(TwitterException e){
            System.out.println(e.getMessage());
        }

        System.out.println(status.getText());

    }
}
